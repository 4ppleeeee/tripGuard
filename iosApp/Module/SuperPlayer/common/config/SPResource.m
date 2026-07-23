/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPResource.m
 Author      : Odie
 Version     : 1.0
 Date        : 14-4-28
 Description :
 History     : 14-4-28 初始版本
 ***********************************************************/

#import "SPResource.h"
#import <Security/Security.h>
#import "SPVcSystemInfo.h"
//私有变量和方法
@interface SPResource () {
    NSMutableDictionary *_URLDict;
    NSMutableDictionary *_URLDictTest;
    NSMutableDictionary *_URLDictPreRelease;
}

// 是否测试环境, 默认 no
@property (nonatomic, assign) SPResouceEnvMode vEnvMode;

- (BOOL)setURL:(NSString *)key value:(NSString *)url;
@end

@implementation SPResource

#pragma mark-- 实例构造与释放

+ (instancetype)instance {
    static id sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (id)init {
    self = [super init];
    if (self) {
        [self setupUrlDict];
    }

    return self;
}

- (void)setEnvMode:(SPResouceEnvMode)envMode {
}

- (NSString *)getResourceFilePath:(SPResouceEnvMode)envMode {
    NSBundle *bundle = [NSBundle bundleWithPath:[[NSBundle mainBundle] pathForResource:@"SPPlayerSDK" ofType:@"bundle"]];

    // ats 开关打开的时候使用 ats 资源文件
    NSString *namePrefix = [SPVcSystemInfo sharedInstance].isAllowsArbitraryLoads ? @"urlInfo" : @"urlInfo_ats";
    NSString *nameSufix = nil;

    switch (envMode) {
        case SPResouceEnvModeTest:
            nameSufix = @"test";
            break;
        case SPResouceEnvModePreRelease:
            nameSufix = @"preRelease";
            break;
        default:
            break;
    }

    NSString *resName = namePrefix;
    if (nameSufix && nameSufix.length) {
        resName = [resName stringByAppendingFormat:@"_%@", nameSufix];
    }

    NSString *resUrlPath = [bundle pathForResource:resName ofType:@"res"];
    return resUrlPath;
}

- (void)setupUrlDict {
    @synchronized(self) {
        if (!_URLDict) {
            _URLDict = [NSMutableDictionary dictionaryWithDictionary:SPSDKCONF_OBJECT(SPSDKCONFKEY_URLS_LIST)];
            if (_URLDict == nil) {
                _URLDict = [NSMutableDictionary dictionary];
            }
        }
    }
}

#pragma mark-- 外部方法
- (NSString *)URLWithKey:(NSString *)key {
    @synchronized(self) {
        NSString *url = [_URLDict valueForKey:key];
        NSString *confUrl = SP_SDK_CONF_STRING(key);
        // 判断后台是否有配置
        if (confUrl && [confUrl length]) {
            // 判断是否合法的url地址
            if ([NSURL URLWithString:confUrl]) {
                url = confUrl;
            }
        }

#if TARGET_IPHONE_SIMULATOR
        if (url == nil) {
            //便于定位问题是哪个URL
            SPLOGE(SP_CGI_LOG_FILTER, @"url is nil with key:%@", key);
        }
        assert(url);
#else
        if (url == nil) {
            //便于定位问题是哪个URL
            SPLOGE(SP_CGI_LOG_FILTER, @"url is nil with key:%@", key);
            return key;
        }
#endif
        return url;
    }
    return nil;
}

- (BOOL)setURL:(NSString *)key value:(NSString *)url {
    @synchronized(self) {
        if (_URLDict == nil) {
            SPLOGE(@"SPResource", @"url字典还没准备好，设不进去");
            return NO;
        }
        [_URLDict setObject:url forKey:key];
    }
    return YES;
}

@end
