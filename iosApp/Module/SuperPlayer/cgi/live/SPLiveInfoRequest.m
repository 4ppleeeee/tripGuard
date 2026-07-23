//
//  SPLiveInfoRequest.m
//  SPPlayer
//
//  Created by liyukuan on 2019/10/5.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPLiveInfoRequest.h"
#import "SPLiveRequestParam.h"
#import "SPHLSKeyUtil.h"
#import "SPURLManager.h"
#import "SPLiveQueryBuilder.h"
#import "SPNetWorkManager.h"
#import "SPLiveInfoParser.h"
#import "SPPlayerErrorCode.h"
#import "SPCGIRequestIDMgr.h"
#import "SPPlayerUtils.h"
#import "SPNetVideoInfo.h"

#define DEFAULT_MAX_RETRY_TIMES 6

#define LIVE_SVR_TICK @"SP_live_svrtick"

@interface SPLiveInfoRequest ()

@property (nonatomic, assign) int requestID;

@property (nonatomic, strong) SPLiveRequestParam *requestParam;

@property (nonatomic, assign) BOOL stopped;

@property (nonatomic, assign) int retryTimes;

@property (nonatomic, strong) NSRecursiveLock *lock;

@end

@implementation SPLiveInfoRequest

- (instancetype)initWithParam:(SPCGIInitParam *)param {
    if ((self = [super initWithParam:param])) {
        _lock = [[NSRecursiveLock alloc] init];
    }
    
    return self;
}

- (int)requestWithParam:(SPCGIRequestParam *)requestParam {
    [self.lock lock];
    [self stopWithRequestID:self.requestID];  // 先停掉上一个
    
    self.stopped = NO;
    self.requestID = [[SPCGIRequestIDMgr sharedInstance] generateGetLiveRequestID];
    if (![requestParam isKindOfClass:[SPLiveRequestParam class]]) {
        SPLOGS(self.cgiInitParam.logTag, @"live cgi invalid request param");
        NSError *error = [SPPlayerErrorCode buildErrorWithErrorCode:SPCommonLogicErrorInvalidParam
                                                              errMsg:@"invalid param"
                                                              module:SPModuleLiveVInfo
                                                              domain:@"cgi"];
        [self asyncNotifyError:error requestID:self.requestID];
        [self.lock unlock];
        return self.requestID;
    }
    
    SPLOGS(self.cgiInitParam.logTag, @"requestWithParam, cnlid=%@, pid=%@, defn=%@, requestID=%d",
            requestParam.vid, requestParam.cid, requestParam.definition, self.requestID);
    SPLiveRequestParam *liveReqParam = (SPLiveRequestParam *)requestParam;
    self.requestParam = liveReqParam;
    self.retryTimes = 1;
    [self requestInternalWithParam:liveReqParam];
    [self.lock unlock];
    return self.requestID;
}

- (void)stopWithRequestID:(int)requestID {
    [self.lock lock];
    SPLOGS(self.cgiInitParam.logTag, @"stopWithRequestID:%d", self.requestID);
    self.stopped = YES;
    self.retryTimes = 0;
    [self.lock unlock];
}

#pragma mark-internal method
- (void)requestInternalWithParam:(SPLiveRequestParam *)liveReqParam {
    NSMutableDictionary<NSString *, NSString *> *paramDict = [[NSMutableDictionary alloc] init];
    [SPLiveQueryBuilder buildLiveQuery:paramDict liveRequestParam:liveReqParam];
    
    NSString *url = [self buildUrlWithParamDict:paramDict requestParam:liveReqParam];
    [self executeRequestWithUrl:url cookie:liveReqParam.commonParams.cookie];
}

- (void)retryRequest {
    self.retryTimes++;
    SPLOGS(self.cgiInitParam.logTag, @"live cgi the %dth retry", self.retryTimes);
    [self requestInternalWithParam:self.requestParam];
}

- (void)executeRequestWithUrl:(NSString *)urlStr cookie:(NSString *)cookie {
    NSMutableDictionary   *requestHeaders = [[NSMutableDictionary alloc] init];
    [requestHeaders setValue:cookie forKey:@"Cookie"];
    [requestHeaders setValue:@"qqlive" forKey:@"User-Agent"];
    int requestID = self.requestID;
    
    SPLOGS(self.cgiInitParam.logTag, @"live cgi request url=%@", urlStr);
    @weakify(self)
    [[SPNetWorkManager shareInstance] getRequest:urlStr
                                   requestHeaders:requestHeaders
                                completionHandler:^(NSData * _Nullable responseData,
                                                    NSError * _Nullable error) {
        @strongify(self)
        [self.lock lock];
        [self onNetworkResponse:responseData error:error requestID:requestID];
        [self.lock unlock];
    }];
}

- (void)onNetworkResponse:(NSData * _Nullable)responseData
                    error:(NSError * _Nullable)error
                requestID:(int)requestID {
    if (requestID != self.requestID || self.stopped) {
        SPLOGS(self.cgiInitParam.logTag, @"requestID not match,%d,%d", requestID, self.requestID);
        return;
    }
    
    [self processResponse:responseData error:error];
}

- (void)processResponse:(NSData *)data error:(NSError *)error {
    if (error != nil) {
        SPLOGS(self.cgiInitParam.logTag, @"network error:%@", error);
        [self processNetworkError:error];
        return;
    }
    
    NSString *jsonString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    SPLOGS(self.cgiInitParam.logTag, @"live cgi response json:%@", jsonString);
    SPLiveInfoData *liveInfoData = [SPLiveInfoParser parseLiveInfoJson:jsonString];
    if (SPJsonErrorCodeOK != liveInfoData.parseResult) {
        SPLOGS(self.cgiInitParam.logTag, @"live cgi response parse error:%d", (int)liveInfoData.parseResult);
        NSError *errorObj = [SPPlayerErrorCode buildErrorWithErrorCode:liveInfoData.parseResult errMsg:@"" module:SPModuleLiveVInfo domain:@"cgi"];
        [self notifyError:errorObj];
        return;
    }
    
    BOOL needRetry = NO;
    if ([self needRetry:liveInfoData.cgiErrorModel]) {
        SPLOGS(self.cgiInitParam.logTag, @"need retry, retcode=%d, retdetailcode=%d", liveInfoData.cgiErrorModel.retCode, liveInfoData.cgiErrorModel.retDetailCode);
        //32.-3不计入重试次数
        if (SPLiveCgiRetCodeCKEYVerifyFailed == liveInfoData.cgiErrorModel.retCode &&
            -3 == liveInfoData.cgiErrorModel.type) {
            [self updateServerTimeAndRandFlag:liveInfoData.cgiErrorModel];
            needRetry = YES;
        } else {
            if (![self isExceedMaxRetryCount]) {
                needRetry = YES;
                self.retryTimes++;
            }
        }
    }
    
    if (needRetry) {
        [self requestInternalWithParam:self.requestParam];
        return;
    }
    
    [SPLiveQueryBuilder storeServerTick:liveInfoData.cgiErrorModel.curSeverTime];
    
    [self processLiveInfoData:liveInfoData];
}

- (void)processLiveInfoData:(SPLiveInfoData *)liveInfoData {
    if (liveInfoData.cgiErrorModel.retCode != 0) {
        SPLOGS(self.cgiInitParam.logTag, @"live cgi server error, retcode=%d", liveInfoData.cgiErrorModel.retCode);
        NSInteger errorCode = [SPPlayerErrorCode convertServerErrorCode:liveInfoData.cgiErrorModel.retCode];
        // 与点播不一样，直播cgi返回错误也要把信息抛出去，netVideoInfo放在NSError.userInfo.data字段里
        SPNetVideoInfo *netVideoInfo = [SPPlayerUtils netVideoInfoFromPlayInfo:liveInfoData.livePlayInfo];
        NSError *error = [SPPlayerErrorCode buildErrorWithErrorCode:errorCode
                                                              errMsg:liveInfoData.cgiErrorModel.errInfo
                                                              module:SPModuleLiveVInfo
                                                              domain:@"cgi"
                                                                data:netVideoInfo];
        [self notifyError:error];
    } else {
        if (self.requestParam.userLiveSeeBackTime > 0) { // 是否是直播回看
            liveInfoData.livePlayInfo.seeBackBaseInfo.isSeeBackState = YES;
            SPSection *section = liveInfoData.livePlayInfo.sectionArray.firstObject;
            liveInfoData.livePlayInfo.seeBackBaseInfo.seeBackUrl = section.url;  // 存放原始的回看url，用于后续seek
        }
        
        [self notifyLiveInfo:liveInfoData.livePlayInfo];
    }
}

- (void)processNetworkError:(NSError *)error {
    if ([self isExceedMaxRetryCount]) {
        SPLOGS(self.cgiInitParam.logTag, @"exceed max retry count:%d", self.retryTimes);
        NSError *newError = [SPPlayerErrorCode rebuildNetWorkError:error module:SPModuleLiveVInfo];
        [self notifyError:newError];
        return;
    }
    
    [self retryRequest];
}

- (NSString *)buildUrlWithParamDict:(NSDictionary *)paramDict requestParam:(SPLiveRequestParam *)requestParam {
    NSString *host = [self getLiveInfoHost];
    NSString *protocol = [self getLiveInfoProtocol];
    NSMutableString *url = [NSMutableString stringWithFormat:@"%@%@/?cmd=2", protocol, host];
    
    NSString *keyValStr = [SPUtils keyValueStringWithUrlEncodeFromDictionary:paramDict];
    NSString *ckeyFields = [self ckeyFields:requestParam];
    [url appendString:keyValStr];
    [url appendString:ckeyFields];
    return url;
}

- (NSString *)getLiveInfoProtocol {
    return (self.requestParam.options.useHttps ? @"https://" : @"http://");
}

- (NSString *)getLiveInfoHost {
    // 主备份域名交替重试
    if (self.retryTimes % 2 == 1) {
        return [SPURLManager liveInfoHost];
    } else {
        return [SPURLManager liveInfoBackHost];
    }
}

- (NSString *)ckeyFields:(SPLiveRequestParam *)requestParam {
    SPCKeyParam *ckeyParam = [[SPCKeyParam alloc] init];
    ckeyParam.videoIDForCKey = requestParam.vid;
    ckeyParam.platform = requestParam.commonParams.platform;
    ckeyParam.sdtFrom = requestParam.commonParams.sdtFrom;
    ckeyParam.isDownload = NO;
    ckeyParam.isRender = requestParam.isDLNA || requestParam.isAirplay;
    return [[SPHLSKeyUtil sharedInstance] createCKeyUrlWithParam:ckeyParam];
}

- (BOOL)needRetry:(SPLiveCGIErrorModel *)cgiErrorModel {
    switch (cgiErrorModel.retCode) {
        case SPLiveCgiRetCodeAuthFailedInPay:
        case SPLiveCgiRetCodeLoginInfoVerifyFailed:
        case SPLiveCgiRetCodeNoLogin:
        case SPLiveCgiRetCodeCKEYVerifyFailed:
        case SPLiveCgiRetCodeGetPreviewCountFailed:
        case SPLiveCgiRetCodeWeixinVerifyFailed:
        case SPLiveCgiRetCodeWeixinVerifyTimeOut:
        case SPLiveCgiRetCodeGetPreviewInfoFailed:
        {
            return YES;
            break;
        }
        default:
            return NO;
            break;
    }
}
/**
 * 上一次存储选择的清晰度的时间戳，单位为秒
 */
- (int)lastServerTick {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    int fntick = [[userDefaults objectForKey:LIVE_SVR_TICK] intValue];
    if (fntick == 0) {
        fntick = [[NSDate date] timeIntervalSince1970];
    }
    return fntick;
}

- (void)storeServerTick:(int64_t)serverTick {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:[NSNumber numberWithLongLong:serverTick] forKey:LIVE_SVR_TICK];
    [userDefaults synchronize];
}

- (BOOL)isExceedMaxRetryCount {
    int maxRetryTimes = self.requestParam.options.maxRetryTimes;
    if (maxRetryTimes <= 0) {
        maxRetryTimes = DEFAULT_MAX_RETRY_TIMES;
    }
    return self.retryTimes >= maxRetryTimes;
}

- (void)asyncNotifyError:(NSError *)error requestID:(int)requestID {
    @weakify(self)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @strongify(self)
        [self.lock lock];
        if (!self.stopped && requestID == self.requestID) {
            [self notifyError:error];
        }
        [self.lock unlock];
    });
}

- (void)notifyError:(NSError *)error {
    if ([self.delegate respondsToSelector:@selector(request:onGetLiveInfoFailed:requestID:)]) {
        [self.delegate request:self onGetLiveInfoFailed:error requestID:self.requestID];
    }
}

- (void)notifyLiveInfo:(SPLivePlayInfo *)livePlayInfo {
    if ([self.delegate respondsToSelector:@selector(request:onGetLiveInfo:requestID:)]) {
        [self.delegate request:self onGetLiveInfo:livePlayInfo requestID:self.requestID];
    }
}

- (void)updateServerTimeAndRandFlag:(SPLiveCGIErrorModel *)cgiErrorModel {
    if (32 == cgiErrorModel.retCode && -3 == cgiErrorModel.type) {
        if (cgiErrorModel.curSeverTime > 0 && cgiErrorModel.randFlag.length) {
            SPLOGS(self.cgiInitParam.logTag, @"update server time:%lld, randFlag:%@", cgiErrorModel.curSeverTime, cgiErrorModel.randFlag);
            [[SPHLSKeyUtil sharedInstance] onGetCurrentServerTime:cgiErrorModel.curSeverTime];
            [[SPHLSKeyUtil sharedInstance] onGetRandFlag:cgiErrorModel.randFlag];
        }
    }
}

@end
