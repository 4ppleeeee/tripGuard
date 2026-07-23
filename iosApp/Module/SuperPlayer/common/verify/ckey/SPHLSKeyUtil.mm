/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPHLSKeyUtil.m
 Author      : 周辰
 Version     : 1.0
 Date        : 13-5-10
 Description :
 History     : 13-5-10 初始版本
 ***********************************************************/

#import "SPHLSKeyUtil.h"
#import "SPLog.h"
#import "SPVcSystemInfo.h"
#import "SPSDKParamsMgr.h"
#import "SPNetWorkManager.h"
#import "SPResource.h"
#import "SPATSHTTPRequest.h"
#import "SPJSONResponse.h"
#import <vsCKey/ckey_lib.h>

//#define IPHONE_APP_PRIVATE_KEY @"0975356290D03FCE63B3EDABAC74BB16EA7A50E77869F07B1D2ECFB5BBBAA72D0973A32532CEECE0A82A5478B317EEA45D39679F90B7A022BC9BC6513B021C4E"
//#define IPAD_APP_PRIVATE_KEY @"F97D2BF7FA3291D22CF6FADFF398E54CB298B57953A46A783A2097B29F56387E3C4C539231EE4460FC5FE0D8300D7AFE36C2B69AAA8813A2F6C14922C2FE3C66"

NSString *const SPHLSKeyUtil_UserDefault_timeSeverSystem = @"kSPHLSKeyUtil_UserDefault_timeSeverSystem";
NSString *const SPHLSKeyUtil_UserDefault_timeSendRequest = @"kSPHLSKeyUtil_UserDefault_timeSendRequest";
NSString *SPHLSKeyUtil_DidUpdateToken_Notification       = @"SPHLSKeyUtil_DidUpdateToken_Notification";

#define kAppTokenForCKey @"APP_TOKEN_FOR_CEKY"

#define LOG_TAG @"SPKeyUtil"

@implementation SPCKeyParam
@end

@interface SPHLSKeyUtil () <reportProtocol>

@property (atomic, assign) NSTimeInterval timeSeverSystem;
@property (atomic, assign) NSTimeInterval timeSendRequest;
@property (atomic, assign) NSInteger retryCount;
@property (atomic, assign) BOOL shouldMatchWhenNetworkAvailable;
@property (atomic, copy) NSString *validTokenForCKey;
@property (nonatomic, strong) dispatch_queue_t tokenForCKeyQueue;

@property (nonatomic, strong) SPATSHTTPRequest *httpRequest;
@property (atomic, retain) NSString *randFlag;
@property (nonatomic, strong) ckey_lib *ckeyLib;
@end

@implementation SPHLSKeyUtil

+ (SPHLSKeyUtil *)sharedInstance {
    static SPHLSKeyUtil *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
      instance = [[SPHLSKeyUtil alloc] init];
    });
    return instance;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[SPNetworkChecker sharedInstance] stopCheckingNetwork:self type:SPNetworkCheckerTypeAll];
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    [_httpRequest cancel];
    _httpRequest = nil;
}

- (id)init {
    if (self = [super init]) {
        // modify (2014/1/8)
        // SPNetworkCheckerTypeEnterForegroundAndReachabilityChanged = SPNetworkCheckerTypeEnterForeground | SPNetworkCheckerTypeReachabilityChanged
        [[SPNetworkChecker sharedInstance] startCheckingNetwork:self type:SPNetworkCheckerTypeEnterForegroundAndReachabilityChanged];

        self.timeSeverSystem = [[NSUserDefaults standardUserDefaults] doubleForKey:SPHLSKeyUtil_UserDefault_timeSeverSystem];
        self.timeSendRequest = [[NSUserDefaults standardUserDefaults] doubleForKey:SPHLSKeyUtil_UserDefault_timeSendRequest];
        [self matchingSystemTimeAfterDelay:1.0f];
        _ckeyLib = [ckey_lib sharedInstance];
    }
    return self;
}

- (void)reachabilityChanged {
    if ([SPNetworkChecker networkAvailable] && self.shouldMatchWhenNetworkAvailable) {
        [self matchingSystemTimeAfterDelay:0.5f];
    }
}

- (void)appEnterForeground {
    [self matchingSystemTimeAfterDelay:1.0f];
}

#pragma mark - 系统状态
- (NSString *)getMagicNum {
    return @"12345";
}

- (NSString *)getPlatform {
    return [SPSDKParamsMgr sharedInstance].sdkGetVInfoModels.firstObject.platform;
}

- (NSString *)getAppVersion {
#if SP_TARGET_PLAYER_SDK
    return SPSDKManager.sharedInstance.version;
#else
    return [SPVcSystemInfo sharedInstance].appver;
#endif
}

- (NSString *)getPrivateKey {
    return @"0";
}

- (NSString *)getEncryptVersion {
    return SPSDKCONF_CKEY_VERSION;
}

- (NSString *)getCurrentSystemTime {
    NSTimeInterval currentSystemTime = [[NSDate date] timeIntervalSince1970] - self.timeSendRequest + self.timeSeverSystem;
    return [NSString stringWithFormat:@"%.0f", currentSystemTime];
}

- (NSTimeInterval)getCurrentSystemTimeInterval {
    NSTimeInterval currentSystemTime = [[NSDate date] timeIntervalSince1970] - self.timeSendRequest + self.timeSeverSystem;
    return currentSystemTime;
}

- (BOOL)initCkeyWithGuid:(NSString *)guid vsAppKey:(NSString *)vsAppKey {
    return [self.ckeyLib initCkeyLib:self
                                guid:guid
                            vsAppkey:vsAppKey];
}

- (NSString *)createCKeyUrlWithParam:(SPCKeyParam *)ckeyParam {
    NSString *ckey = [self createCKeyWithParam:ckeyParam];
    return [NSString stringWithFormat:@"&platform=%@&newplatform=%@&sdtfrom=%@&appVer=%@&encryptVer=%@&cKey=%@", ckeyParam.platform, ckeyParam.platform, ckeyParam.sdtFrom, [self getAppVersion], [self getEncryptVersion], ckey];
}

- (NSString *)createCKeyWithParam:(SPCKeyParam *)ckeyParam {
    NSString *appVersion = [self getAppVersion];
    
    NSString *vid = nil;
    if (ckeyParam.videoIDForCKey) {
        vid = ckeyParam.videoIDForCKey;
    } else if (ckeyParam.previd) {
        vid = [self previdMD5:ckeyParam.previd];
    }
    
    NSString *platform = ckeyParam.platform;
    NSString *sdtFrom = ckeyParam.sdtFrom;

    NSString *randFlag = self.randFlag;

    NSString *bundleId = [SPVcSystemInfo sharedInstance].bundlId;
    //使用后台下发的guid
    NSString *guid = SPSDKPARAMS_GUID;

    //ckey 5.0 v3
    int extParams[1];
    extParams[0] = ckeyParam.isRender ? 1 : 0;
    extParams[0] += (ckeyParam.isDownload ? 1 : 0) << 2;
    
    NSString *strCkey = [self.ckeyLib getCKeyAll:[platform intValue]
                                        unEncVer:0x5203
                                       strappVer:appVersion
                                          strvid:vid
                                           uTime:[[self getCurrentSystemTime] intValue]
                                         sdtFrom:sdtFrom
                                     strRandflag:randFlag
                                     strBundleID:bundleId
                                         strGuid:guid
                                            intA:extParams
                                          ArrLen:sizeof(extParams) / sizeof(int)];
    
    if (strCkey.length <= 0) {
        SPLOGE(LOG_TAG, @"createCKey error, getckey empty");
    }

    return strCkey;
}

- (void)onGetCurrentServerTime:(NSTimeInterval)serverTime {
    self.timeSendRequest = [[NSDate date] timeIntervalSince1970];
    self.timeSeverSystem = serverTime;
}

- (void)onGetRandFlag:(NSString *)randFlag {
    self.randFlag = randFlag;
}

#pragma mark - reportProtocol
- (void)report:(NSString*)appkey
         event:(NSString*)event
         param:(NSDictionary*)reportInfo {
    
}

/*
 *  @param appkey 灯塔appkey
 *  @return 返回灯塔Qimei36
*/
- (NSString*)getQimei36: (NSString*)appkey {
    return SPSDKPARAMS_QIMEI;
}

#pragma mark - 获取和更新ckey5.0的token
//- (NSString *)getTokenForCKey {
//    if (self.validTokenForCKey.length > 0) {
//        return self.validTokenForCKey;
//    }
//
//    //获取临时token
//    NSString *guid = SPSDKPARAMS_GUID;
//    if (guid.length > 0) {
//        std::string strGuid([guid UTF8String]);
//        std::string strLocalToken = LocalToken(strGuid);
//        if (strLocalToken.length() > 0) {
//            return [NSString stringWithUTF8String:strLocalToken.data()];
//        }
//    }
//
//    SPLOGE(LOG_TAG, @"getTokenForCKey return empty");
//    return @"";
//}

//更新计算ckey所需要的token
//若已经存在本地，则直接返回
//否则检查后台的localGUID是否存在，若存在则启动线程获取token
//- (void)updateTokenForCKey {
//    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
//    NSString *validToken         = [userDefaults objectForKey:kAppTokenForCKey];
//    if (validToken) {
//        self.validTokenForCKey = validToken;
//        [self updateTokenNotify];
//        return;
//    }
//
//    SPLOGI(LOG_TAG, @"updateTokenForCKey begin");
//
//    NSString *localGUID = SPSDKPARAMS_GUID;
//    if (localGUID.length > 0) {
//        //后台guid存在，取换取token
//        if (self.tokenForCKeyQueue == nil) {
//            self.tokenForCKeyQueue = dispatch_queue_create("tokenForCKeyQueue", NULL);
//
//            unsigned int platform = [[self getPlatform] intValue];
//            NSString *appVersion  = [self getAppVersion];
//
//            if (appVersion == nil) {
//                //释放queue
//                self.tokenForCKeyQueue = nil;
//
//                SPLOGI(LOG_TAG, @"get app version was failrure");
//                return;
//            }
//
//            dispatch_async(self.tokenForCKeyQueue, ^{
//
//              NSString *token = nil;
//              int tokenRet;
//
//              @try {
//                  //计算token
//                  std::string strLocalGUID([localGUID UTF8String]);
//                  std::string strAppVersion([appVersion UTF8String]);
//                  std::string strToken;
//                  tokenRet = GetToken(strLocalGUID, platform, strAppVersion, strToken);
//
//                  if (strToken.length() > 0) {
//                      token = [NSString stringWithUTF8String:strToken.data()];
//                  }
//              }
//              @catch (NSException *exception) {
//                  SPLOGI(LOG_TAG, @"get app version was failrure");
//              }
//              @catch (...) {
//                  SPLOGI(LOG_TAG, @"get app version was failrure");
//              }
//
//              dispatch_async(dispatch_get_main_queue(), ^{
//
//                if (token) {
//                    if (tokenRet == 0) {
//                        //存入本地
//                        NSUserDefaults *userDefaults2 = [NSUserDefaults standardUserDefaults];
//                        [userDefaults2 setObject:token forKey:kAppTokenForCKey];
//                        [userDefaults2 synchronize];
//
//                        SPLOGI(LOG_TAG, @"updateTokenForCKey success");
//                    } else {
//                        SPLOGE(LOG_TAG, @"updateTokenForCKey failed, ret:%d", tokenRet);
//                    }
//
//                    self.validTokenForCKey = token;
//                    [self updateTokenNotify];
//                } else {
//                    SPLOGE(LOG_TAG, @"updateTokenForCKey error");
//                }
//
//                //释放queue
//                self.tokenForCKeyQueue = nil;
//              });
//            });
//        }
//    } else {
//        SPLOGI(LOG_TAG, @"updateTokenForCKey failed coz no local GUID");
//    }
//}

#pragma mark - 获取系统时间
- (void)matchingSystemTimeAfterDelay:(NSTimeInterval)delay {
    self.retryCount      = 0;
    dispatch_time_t time = dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC);
    dispatch_after(time, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      [self performSelector:@selector(sendMatchingRequest) withObject:nil];
    });
}

- (void)sendMatchingRequest {
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(sendMatchingRequest) object:nil];

    if (![SPNetworkChecker networkAvailable]) {
        self.shouldMatchWhenNetworkAvailable = YES;
        return;
    }
    self.shouldMatchWhenNetworkAvailable = NO;

    unsigned int random  = arc4random();
    NSString *requestStr = [NSString stringWithFormat:@"%@?otype=json&localTime=%.0f&n=%u", SP_RESOURCE_URL(checkTimeURL), [[NSDate date] timeIntervalSince1970], random];

    if (requestStr == nil) {
        return;
    }
    if (!_httpRequest) {
        [_httpRequest cancel];
        _httpRequest = nil;
    }

    //    QLASIHTTPRequest* request = [QLASIHTTPRequest requestWithURL:requestStr userdelegate:self];
    //    [request setCachePolicy:ASIDoNotReadFromCacheCachePolicy];
    //    request.modelID = enumSPModuleReportCheckTime;
    //    [request setTimeOutSeconds:60];
    //    request.response = [[[QLJSONPResponse alloc] init] autorelease];
    //    [request setUserInfo:[NSMutableDictionary dictionaryWithObjectsAndKeys:
    //                      [NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970]], @"TimeSendRequest",
    //                       nil]];
    //    self.request = request;
    //    [self.request send];
    self.timeSendRequest = [[NSDate date] timeIntervalSince1970];

    SPATSHTTPRequest *request = [[SPNetWorkManager shareInstance] getRequest:requestStr
                                                                requestHeaders:nil
                                                             completionHandler:^(NSData *_Nullable responseData, NSError *_Nullable error) {

                                                               if (!error) {
                                                                   NSError *resultErr        = error;
                                                                   SPJSONResponse *response = nil;
                                                                   if (resultErr == nil) {
                                                                       response  = [[SPJSONResponse alloc] init];
                                                                       resultErr = [response processResponseData:responseData];
                                                                   }

                                                                   if (resultErr) {
                                                                       SPLOGW(LOG_TAG, @"request error:%@", resultErr);
                                                                       response = nil;

                                                                       [self retryMatching];
                                                                   } else {
                                                                       NSDictionary *root   = response.rootObject;
                                                                       NSNumber *systemTime = [root objectForKey:@"t"];
                                                                       if (![systemTime isKindOfClass:[NSNumber class]]) {
                                                                           //同步失败
                                                                           response = nil;

                                                                           [self retryMatching];
                                                                           return;
                                                                       }

                                                                       self.timeSeverSystem = [systemTime doubleValue];

                                                                       NSString *randFlag = [root objectForKey:@"rand"];
                                                                       if (randFlag && [randFlag isKindOfClass:[NSString class]] && randFlag.length > 0) {
                                                                           self.randFlag = randFlag;
                                                                       } else {
                                                                           self.randFlag = nil;
                                                                           SPLOGE(LOG_TAG, @"checktime no rand flag:%@", randFlag);
                                                                       }

                                                                       SPLOGI(LOG_TAG, @"checktime timeSendRequest:%.0f, response %@", self.timeSendRequest, root);

                                                                       [[NSUserDefaults standardUserDefaults] setDouble:self.timeSendRequest forKey:SPHLSKeyUtil_UserDefault_timeSendRequest];
                                                                       [[NSUserDefaults standardUserDefaults] setDouble:self.timeSeverSystem forKey:SPHLSKeyUtil_UserDefault_timeSeverSystem];

                                                                       if (fabs(self.timeSendRequest - [[NSDate date] timeIntervalSince1970]) > 5.0f) {
                                                                           [self retryMatching];
                                                                       }

                                                                       response = nil;
                                                                   }

                                                               } else {
                                                               }

                                                             }];

    self.httpRequest = request;
    return;
}

- (void)retryMatching {
    self.retryCount++;
    if (self.retryCount > 10) {
        self.shouldMatchWhenNetworkAvailable = YES;
        return;
    }
    dispatch_time_t time = dispatch_time(DISPATCH_TIME_NOW, 10.0f * NSEC_PER_SEC);
    dispatch_after(time, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      [self performSelector:@selector(sendMatchingRequest) withObject:nil];
    });
}

- (void)updateTokenNotify {
    dispatch_async(dispatch_get_main_queue(), ^{
      [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:SPHLSKeyUtil_DidUpdateToken_Notification object:nil]];
    });
}

- (NSString *)previdMD5:(NSString *)previd {
    NSString *input = [previd stringByAppendingString:@"magicCC"];
    NSString *md5   = [SPUtils md5ForLowerCase:input];
    while (md5.length < 32) {
        md5 = [@"0" stringByAppendingString:md5];
    }

    return [md5 substringToIndex:12];
}

@end
