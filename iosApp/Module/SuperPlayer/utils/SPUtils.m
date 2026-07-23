/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPUtils.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 12-12-18
 Description :
 History     : 12-12-18 初始版本
 ***********************************************************/

#import "SPUtils.h"

#include <sys/socket.h>  // Per msqr
#include <sys/sysctl.h>
#include <net/if.h>
#include <net/if_dl.h>
#import <AdSupport/AdSupport.h>

#import "SPLog.h"
#import "SPVcSystemInfo.h"
#import "NSURL+SPUtils.h"
#import <AVFoundation/AVFoundation.h>
#import <CommonCrypto/CommonDigest.h>
#import <objc/runtime.h>


static NSString *const kKeychainUsername = @"GUID";

@implementation SPUtils

#pragma mark - 类方法

// 1.7.0，原来的计算方式有bug
+ (NSString *)getAppVersion {
    NSString *appVersion      = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
    NSMutableArray *component = [NSMutableArray arrayWithArray:[appVersion componentsSeparatedByString:@"."]];
    while (component.count < 3) {
        [component addObject:@"0"];
    }
    while (component.count > 3) {
        [component removeLastObject];
    }
    appVersion = [component componentsJoinedByString:@"."];
    return appVersion;
}

// 1.7.0.1000，原来的计算方式有bug
+ (NSString *)getAppBuildVersion {
    NSString *buildVersion = [SPUtils getAppVersion];
    buildVersion           = [buildVersion stringByAppendingFormat:@".%@", [SPUtils getAppBuildNumber]];
    return buildVersion;
}

+ (NSString *)getAppBuildNumber {
    return [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
}

// com.tencent.live4iphone
+ (NSString *)bundleID {
    return [[NSBundle mainBundle] bundleIdentifier];
}

// 腾讯视频HD2.0.1 的形式
+ (NSString *)fullBundleDisplayName {
    NSDictionary *infoDict = [[NSBundle mainBundle] infoDictionary];
    NSString *versionstr   = [NSString stringWithFormat:@"%@%@",
                                                      [infoDict objectForKey:@"CFBundleDisplayName"],
                                                      [infoDict objectForKey:@"CFBundleShortVersionString"]];
    return versionstr;
}

+ (BOOL)isRetinaDisplay {
    return ([UIScreen instancesRespondToSelector:@selector(scale)] && ([UIScreen mainScreen].scale == 2.0));
}

#pragma mark - 获取guid

+ (NSString *)getKeyChainUserName {
    return kKeychainUsername;
}

+ (NSString *)getKeyChainServiceName {
    return [SPUtils bundleID];
}

#pragma mark - NSdate相关，NSString NSDate互转，时间比较
+ (NSDate *)dateFromString:(NSString *)dateString {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSDate *destDate = [dateFormatter dateFromString:dateString];
    return destDate;
}
+ (NSString *)stringFromDate:(NSDate *)date {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSString *destDate = [dateFormatter stringFromDate:date];
    return destDate;
}
+ (NSInteger)getDateToDateDays:(NSDate *)date withSaveDate:(NSDate *)saveDate {
    NSCalendar *chineseClendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSCalendarIdentifierGregorian];
    NSUInteger unitFlags       = NSCalendarUnitHour | NSCalendarUnitMinute |
                           NSCalendarUnitSecond | NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear;
    NSDateComponents *cps = [chineseClendar components:unitFlags fromDate:date toDate:saveDate options:0];
    NSInteger diffDay     = [cps day];
    return diffDay;
}
+ (NSString *)safeEncodeURLString:(NSString *)urlstr {
    //urlstr = @" http://cache.tv.qq.com/images/540-320海洋天堂190303.jpg";
    if (urlstr.length && [NSURL URLWithString:urlstr] == nil) {
        NSString *encoded = [urlstr copy];

        encoded = [encoded stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];

        if ([encoded hasPrefix:@"http://"]) {
            encoded = [encoded substringFromIndex:7];
        }

        NSRange range = [encoded rangeOfString:@"/"];
        if (range.location != NSNotFound) {
            NSString *domain = [encoded substringToIndex:range.location];

            NSString *path = [encoded substringFromIndex:range.location + 1];
            path           = [path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];

            encoded = [NSString stringWithFormat:@"http://%@/%@", domain, path];

            return encoded;
        }
    }
    return urlstr;
}

+ (NSString *)parameterWithURL:(NSString *)url forKey:(NSString *)key {
    if (url == nil || key == nil) {
        return nil;
    }

    NSScanner *scanner = [NSScanner scannerWithString:url];
    [scanner setCharactersToBeSkipped:[NSCharacterSet characterSetWithCharactersInString:@"&?"]];
    [scanner scanUpToString:@"?" intoString:nil];

    NSString *tmpValue;
    while ([scanner scanUpToString:@"&" intoString:&tmpValue]) {
        NSArray *components = [tmpValue componentsSeparatedByString:@"="];

        if (components.count >= 2) {
            if ([[components[0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding] isEqualToString:key]) {
                NSString *value = [components[1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
                return value;
            }
        }
    }

    return nil;
}

+ (BOOL)isAllowsArbitraryLoads {
    static BOOL checked              = NO;
    static BOOL allowsArbitraryLoads = YES;

    if (checked) {
        return allowsArbitraryLoads;
    }

    NSDictionary *infoDic   = [[NSBundle mainBundle] infoDictionary];
    NSDictionary *tSecurity = [infoDic spDictionaryForKey:@"NSAppTransportSecurity"];

    if (!tSecurity) {
        // 没有设置安全选项
        allowsArbitraryLoads = YES;
    } else {
        allowsArbitraryLoads = [tSecurity spBoolForKeySafeModel:@"NSAllowsArbitraryLoads"];
    }

    checked = YES;
    return allowsArbitraryLoads;
}

+ (NSString *)replaceUlr:(NSString *)url key:(NSString *)key value:(NSString *)value {
    if (url.length <= 0) {
        return nil;
    }

    if (key.length <= 0 || value.length <= 0) {
        return url;
    }

    NSString *newUlr = nil;
    if ([[SPVcSystemInfo sharedInstance] isIOS8OrLatter]) {
        NSURLComponents *component            = [[NSURLComponents alloc] initWithString:url];
        NSArray<NSURLQueryItem *> *queryItems = [component queryItems];
        if (!queryItems) {
            return url;
        }

        NSMutableArray *mutableArray = [[NSMutableArray alloc] initWithArray:queryItems];
        NSInteger resultIndex        = -1;
        for (NSURLQueryItem *item in mutableArray) {
            if ([item.name isEqualToString:key]) {
                resultIndex = [mutableArray indexOfObject:item];
                break;
            }
        }

        if (resultIndex != -1) {
            [mutableArray removeObjectAtIndex:resultIndex];
            NSURLQueryItem *item = [[NSURLQueryItem alloc] initWithName:key value:value];
            if (resultIndex >= 0 && resultIndex < mutableArray.count) {
                [mutableArray insertObject:item atIndex:resultIndex];
            } else {
                [mutableArray addObject:item];
            }

            component.queryItems = mutableArray;
        }

        newUlr = component.string;
    } else {
        NSURLComponents *component    = [[NSURLComponents alloc] initWithString:url];
        NSString *urlQuery            = [component query];
        NSDictionary *dic             = [NSURL spParseQueryComponentsFromQueryString:urlQuery];
        NSMutableDictionary *queryDic = [[NSMutableDictionary alloc] initWithDictionary:dic];

        [queryDic setObject:value forKey:key];

        NSString *newQuery = [SPUtils dictionToUrl:queryDic];
        component.query    = newQuery;
        newUlr             = component.URL.absoluteString;
    }
    return newUlr;
}

#if !TARGET_OS_MACCATALYST
+ (NSString *)currentRouteDevice {
    UInt32 propertySize = sizeof(CFStringRef);
    CFStringRef state   = nil;
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    OSStatus status = AudioSessionGetProperty(kAudioSessionProperty_AudioRoute, &propertySize, &state);
    if (status == kAudioSessionNoError) {
        NSString *nsState = (__bridge NSString *)state;
        if (nsState) {
            SPLOGI(@"routeDevice", @"getCurrentRouteDevice nsState is:%@", nsState);
            return nsState;
        }
    }
#pragma clang diagnostic pop
    return nil;
}
#endif

+ (NSString *)dictionToUrl:(NSDictionary *)dic {
    if (dic.count <= 0) {
        return nil;
    }
    NSMutableArray *parts = [NSMutableArray array];
    for (NSString *key in dic.allKeys) {
        NSString *value = [dic objectForKey:key];
        NSString *part  = [NSString stringWithFormat:@"%@=%@", key ? key : @"", value ? value : @""];
        [parts addObject:part];
    }
    return [parts componentsJoinedByString:@"&"];
}

+ (NSString *)generateUUID {
    CFUUIDRef uuidObj    = CFUUIDCreate(nil);  // create a new UUID
    NSString *uuidString =
        (__bridge_transfer NSString *)CFUUIDCreateString(nil, uuidObj);
    CFRelease(uuidObj);
    return uuidString;
}

+ (NSString *)keyValueStringFromDictionary:(NSDictionary<NSString *, NSString *> *)dictionary {
    NSMutableString *str = [[NSMutableString alloc] initWithString:@""];
    for (NSString *key in dictionary.allKeys) {
        NSString *value = [dictionary valueForKey:key];
        [str appendFormat:@"&%@=%@", key, value];
    }

    return str;
}

+ (NSString *)keyValueStringWithUrlEncodeFromDictionary:(NSDictionary<NSString *, NSString *> *)dictionary {
    if (dictionary.count == 0) {
        return nil;
    }
    NSMutableCharacterSet *characterSet = [NSCharacterSet.URLQueryAllowedCharacterSet mutableCopy];
    [characterSet removeCharactersInString:@"&="];
    NSMutableString *str = [[NSMutableString alloc] initWithString:@""];
    for (NSString *key in dictionary.allKeys) {
        NSString *value = [dictionary valueForKey:key];
        if (![value isKindOfClass:[NSString class]]) {
            continue;
        }
        NSString *encodedValue = [value stringByAddingPercentEncodingWithAllowedCharacters:characterSet];
        [str appendFormat:@"&%@=%@", key, encodedValue];
    }

    return str;
}

+ (BOOL)isDomainUrl:(NSURL *)url {
    if (![url isKindOfClass:[NSURL class]]) {
        return NO;
    }

    if (url.absoluteString.length <= 0) {
        return NO;
    }

    NSString *host     = [url host];
    NSString *urlRegEx = @"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\" \
    @".([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    NSPredicate *urlTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", urlRegEx];
    BOOL isIp            = [urlTest evaluateWithObject:host];
    return !isIp;
}

+ (BOOL)isDomainUrlString:(NSString *)urlString {
    NSURL *url = [NSURL URLWithString:urlString];
    return [SPUtils isDomainUrl:url];
}

+ (BOOL)urlsContainDomain:(NSArray<NSURL *> *)urlArray {
    for (NSURL *url in urlArray) {
        //如果是ios10 ip的播放地址可以使用原生播放
        if ([SPUtils isDomainUrl:url]) {
            return YES;
        }
    }
    return NO;
}

+ (NSString *)jsonStringFromDict:(NSDictionary *)dict {
    if (![NSJSONSerialization isValidJSONObject:dict]) {
        SPLOGI(@"SPUtils", @"%s invalid dic[%@]", __FUNCTION__, dict);
        return nil;
    }
    NSError *error   = nil;
    NSData *jsonData = [NSJSONSerialization
        dataWithJSONObject:dict
                   options:kNilOptions
                     error:&error];
    if ([jsonData length] > 0 && error == nil) {
        NSLog(@"Successfully serialized the dictionary into data.");
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        return jsonString;
    } else if ([jsonData length] == 0 && error == nil) {
        NSLog(@"No data was returned after serialization.");
        return nil;
    } else if (error != nil) {
        NSLog(@"An error happened = %@", error);
        return nil;
    }

    return nil;
}

+ (NSString *)md5ForLowerCase:(NSString *)input {
    if (input.length == 0) {
        return @"";
    }
    const char *str = [input UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(str, (CC_LONG)strlen(str), result);
    NSMutableString *ret = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for (int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [ret appendFormat:@"%02x", result[i]];
    }

    return ret;
}

+ (NSArray *)getAllProperties:(id)obj {
    if (!obj) {
        return nil;
    }
    u_int count;
    objc_property_t *properties     = class_copyPropertyList([obj class], &count);
    NSMutableArray *propertiesArray = [NSMutableArray arrayWithCapacity:count];
    for (int i = 0; i < count; i++) {
        const char *propertyName = property_getName(properties[i]);
        [propertiesArray addObject:[NSString stringWithUTF8String:propertyName]];
    }
    free(properties);
    return propertiesArray;
}
@end

@implementation NSObject (SPUtils)
- (NSString *)safeDescription {
    if ([self isKindOfClass:[NSString class]]) {
        return (NSString *)self;
    }
    return [self description];
}

- (NSString *)safeString {
    if ([self isKindOfClass:[NSString class]]) {
        return (NSString *)self;
    }
    return @"";
}
@end

@implementation NSString (SPUtils)
+ (NSString *)stringWithFormatSafely:(NSString *)format, ... {
    va_list args;
    va_start(args, format);
    NSString *targetStr = [[NSString alloc] initWithFormat:format arguments:args];
    va_end(args);

    targetStr = [targetStr stringByReplacingOccurrencesOfString:@"NULL" withString:@""];
    targetStr = [targetStr stringByReplacingOccurrencesOfString:@"(null)" withString:@""];

    return targetStr;
}
@end
