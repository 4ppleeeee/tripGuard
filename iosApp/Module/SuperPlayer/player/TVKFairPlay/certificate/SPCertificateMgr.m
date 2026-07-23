/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPCertificateMgr.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 2018/1/13
 Description :
 History     : 2018/1/13 初始版本
 ***********************************************************/

#import "SPCertificateMgr.h"
#import "SPNetWorkManager.h"

@interface TVKCertificate : NSObject

@property (nonatomic, copy) NSString *url;

@property (nonatomic, copy) NSData *cerData;

@end

@implementation TVKCertificate
@end

@interface SPCertificateMgr ()
@property (nonatomic, strong) NSMutableDictionary *dict;
@property (nonatomic, strong) NSString *cachedDir;
@property (nonatomic, strong) NSString *certificateCachedPath;
@property (atomic, copy) NSString *requestingUrl;
@end

static NSString *const TVKCerUrlKey = @"cerUrl";
static NSString *const TVKCerDataKey = @"cerData";

@implementation SPCertificateMgr

+ (SPCertificateMgr *)sharedInstance {
    static SPCertificateMgr *s_cerMgr = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_cerMgr = [[SPCertificateMgr alloc] init];
    });

    return s_cerMgr;
}

// 这里我们并没有考虑两个相同的请求同时到来的情况，就目前来说，这个情况发生的概率比较小，目前先简单处理，如果要考虑这种负责的情况，必须临时保存url和对应的completion方可,
// 须以url为key，以completion组成的array为value
- (void)getCertificateWithUrl:(NSString *)url completion:(TVKCertificateCompletionBlock)completion {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSData *localCerData = [self readFromLocalWithUrl:url];
        if (localCerData) {
            completion(localCerData, nil);
        } else {
            [self getFromServerWithUrl:url completion:completion];
        }
    });
}

- (NSString *)cachedDir {
    @synchronized(self) {
        if (_cachedDir == nil) {
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
            _cachedDir = [paths[0] stringByAppendingPathComponent:@"TVKCertificate"];
        }
    }

    return _cachedDir;
}

- (NSString *)certificateCachedPath {
    @synchronized(self) {
        if (_certificateCachedPath == nil) {
            _certificateCachedPath = [self.cachedDir stringByAppendingString:@"/certificate"];
        }
    }

    return _certificateCachedPath;
}

- (BOOL)hasLocalCertification {
    return [[NSFileManager defaultManager] fileExistsAtPath:self.certificateCachedPath];
}

- (NSData *)readFromLocalWithUrl:(NSString *)url {
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.certificateCachedPath]) {
        SPLOGS(@"TVKPlayFlow-cer", @"has no local certificate");
        return nil;
    }

    /*The dictionary representation in the file identified by path must contain only property list objects (NSString, NSData, NSDate, NSNumber,
     * NSArray, or NSDictionary objects). For more details, see Property List Programming Guide. The objects contained by this dictionary are
     * immutable, even if the dictionary is mutable.*/
    NSDictionary *dict = [NSDictionary dictionaryWithContentsOfFile:self.certificateCachedPath];
    NSString *saveUrl = [dict objectForKey:TVKCerUrlKey];
    if (![saveUrl isEqualToString:url]) {
        // url已发生变化
        SPLOGS(@"TVKPlayFlow-cer", @"certificate url changed");
        return nil;
    }

    NSData *data = [dict objectForKey:TVKCerDataKey];
    return data;
}

- (void)writeCertificateToLocalWithUrl:(NSString *)url cerData:(NSData *)cerData {
    BOOL isDir = YES;
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.cachedDir isDirectory:&isDir]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:self.cachedDir withIntermediateDirectories:YES attributes:nil error:nil];
    }

    /*The dictionary representation in the file identified by path must contain only property list objects (NSString, NSData, NSDate, NSNumber,
     * NSArray, or NSDictionary objects). For more details, see Property List Programming Guide. The objects contained by this dictionary are
     * immutable, even if the dictionary is mutable.*/
    NSDictionary *dict = @{TVKCerUrlKey : url, TVKCerDataKey : cerData};
    [dict writeToFile:self.certificateCachedPath atomically:YES];
}

- (void)getFromServerWithUrl:(NSString *)url completion:(TVKCertificateCompletionBlock)completion {
    [[SPNetWorkManager shareInstance] getRequest:url
                                   requestHeaders:nil
                                completionHandler:^(NSData *__nullable responseData, NSError *__nullable error) {
                                    if (error) {
                                        SPLOGS(@"TVKPlayFlow-cer", @"get certificate fail:%@", error);
                                        completion(nil, error);
                                    } else {
                                        completion(responseData, nil);
                                        [self writeCertificateToLocalWithUrl:url cerData:responseData];
                                    }
                                }];
}
@end
