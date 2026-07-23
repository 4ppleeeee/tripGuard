//
//  SPVideoInfoRequest.m
//  SPPlayer
//
//  Created by liyukuan on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPGetVInfoRequest.h"
#import "SPVODInfoParser.h"
#import "SPVODRequestParam.h"
#import "SPNetWorkManager.h"
#import "SPURLManager.h"
#import "SPHLSKeyUtil.h"
#import "SPVODQueryBuilder.h"
#import "SPPlayerErrorCode.h"
#import "SPVODInfoCache.h"
#import "SPCGIRequestIDMgr.h"
#import "SPCertificateMgr.h"
#import <DownloadProxyFramework/TPDownloadProxyHelper.h>

#define DEFAULT_MAX_RETRY_TIMES 6


@interface SPGetVInfoRequest ()

@property (nonatomic, assign) int requestID;

@property (nonatomic, strong) SPVODRequestParam *requestParam;

@property (nonatomic, assign) BOOL stopped;

@property (nonatomic, assign) int retryTimes;

@property (nonatomic, strong) NSString *logTag;

@property (nonatomic, strong) NSRecursiveLock *lock;

@property (nonatomic, assign) BOOL useIPV6;

@end

@implementation SPGetVInfoRequest

- (instancetype)initWithParam:(SPCGIInitParam *)param {
    if ((self = [super initWithParam:param])) {
        _lock = [[NSRecursiveLock alloc] init];
        _logTag = param.logTag;
    }
    
    return self;
}

- (int)requestWithParam:(SPCGIRequestParam *)requestParam {
    [self.lock lock];
    [self stopWithRequestID:self.requestID];
    
    self.stopped = NO;
    
    self.requestID = [[SPCGIRequestIDMgr sharedInstance] generateGetVInfoRequestID];
    self.logTag = [self.cgiInitParam.logTag stringByAppendingFormat:@"_R%d", self.requestID];
    if (![requestParam isKindOfClass:[SPVODRequestParam class]]) {
        SPLOGS(self.logTag, @"getvinfo invalid request param");
        NSError *error = [SPPlayerErrorCode buildErrorWithErrorCode:SPCommonLogicErrorInvalidParam
                                                              errMsg:@"invalid param"
                                                              module:SPModuleVInfo
                                                              domain:@"cgi"];
        [self asyncNotifyError:error requestID:self.requestID];
        [self.lock unlock];
        return self.requestID;
    }
    
    SPLOGS(self.logTag, @"requestWithParam, vid=%@, cid=%@, defn=%@", requestParam.vid, requestParam.cid, requestParam.definition);
    
    SPVODRequestParam *vodReqParam= (SPVODRequestParam *)requestParam;
    self.requestParam = vodReqParam;
    self.retryTimes = 1;
    [self requestInternalWithParam:vodReqParam];
    [self.lock unlock];
    return self.requestID;
}

- (void)stopWithRequestID:(int)requestID {
    [self.lock lock];
    SPLOGS(self.logTag, @"stopWithRequestID:%d", self.requestID);
    if (!self.stopped) {
        self.stopped = YES;
        self.retryTimes = 0;
    }
    [self.lock unlock];
}

#pragma mark - internal methods

- (void)requestInternalWithParam:(SPVODRequestParam *)vodReqParam {
    if (SPGetVInfoRequestTypeOfflinePlay == vodReqParam.getvinfoReqType) { // 离线播放先从下载组件读取
        SPLOGS(self.logTag, @"offline play, read from proxy, vid=%@, defn=%@", vodReqParam.vid, vodReqParam.definition);
        NSString *xmlString = [TPDownloadProxyHelper getOfflineRecordVinfo:vodReqParam.vid withFormat:vodReqParam.definition];
        if (xmlString.length > 0) {
            SPLOGS(self.logTag, @"offline play, read from proxy success");
            SPGetVInfoData *getvinfoData = [SPVODInfoParser parseOfflineGetVInfoXMLString:xmlString];
            [self asyncGetVInfoData:getvinfoData requestID:self.requestID];
            return;
        }
    }
//    @weakify(self)
//    [SPVODInfoCache readLocalGetVInfoXML:vodReqParam
//                               completion:^(NSString *xml, SPVODRequestParam *requestParam) {
//        @strongify(self)
//        [self.lock lock];
//        if (xml.length > 0) {
//            SPLOGS(self.logTag, @"read local getvinfo successfully, requestID:%d", self.requestID);
//            SPLOGS(self.logTag, @"getvinfo local response xml:%@", xml);
//            SPGetVInfoData *getvinfoData = [SPVODInfoParser parseGetVInfoXMLString:xml];
//            [self processXML:getvinfoData];
//        } else {
    [self requestFromServerWithParam:vodReqParam];
//        }
//        [self.lock unlock];
//    }];
}

- (void)requestFromServerWithParam:(SPVODRequestParam *)vodReqParam {
    NSMutableDictionary<NSString *, NSString *> *paramDict = [[NSMutableDictionary alloc] init];
    [SPVODQueryBuilder buildVODQuery:paramDict vodRequestParam:vodReqParam];
    
    NSString *protocol = [self getvinfoProtocol];
    NSString *host = [self getvinfoHost];
    host = [protocol stringByAppendingString:host];
    NSString *ckeyFields = [self ckeyFields:vodReqParam];
    NSString *urlStr = [self buildUrlWithHost:host
                                    paramDict:paramDict
                                   ckeyFields:ckeyFields];
    
    NSMutableDictionary *requestHeaders = [[NSMutableDictionary alloc] init];
    [requestHeaders setValue:vodReqParam.commonParams.cookie forKey:@"Cookie"];
    [requestHeaders setValue:@"qqlive" forKey:@"User-Agent"];
    if (SPMediaDLTypeHLS == paramDict[@"dType"].intValue &&
        1 == vodReqParam.capabilityParam.spgzip) {
        [requestHeaders setValue:@"gzip" forKey:@"Accept-Encoding"];
    }
    [self executeRequestWithUrl:urlStr headers:requestHeaders];
}

- (void)executeRequestWithUrl:(NSString *)urlStr headers:(NSDictionary *)headers {
    SPLOGS(self.logTag, @"getvinfo request url=%@", urlStr);
    SPLOGS(self.logTag, @"getvinfo request heders=%@", headers);
    int requestID = self.requestID;  // 这里利用block的特性把requestID存下来
    @weakify(self)
    [[SPNetWorkManager shareInstance] getRequest:urlStr
                                   requestHeaders:headers
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
        SPLOGS(self.logTag, @"requestID not match,%d,%d", requestID, self.requestID);
        return;
    }
    
    if (error != nil) {
        SPLOGS(self.logTag, @"getvinfo network error:%@", error);
        [self processNetworkError:error];
    } else {
        NSString *xmlString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
        SPLOGS(self.logTag, @"getvinfo response xml:%@", xmlString);
        SPGetVInfoData *getvinfoData = [SPVODInfoParser parseGetVInfoXMLString:xmlString];
//        [self trySaveGetVInfoXML:xmlString getvinfoData:getvinfoData];
        [self processXML:getvinfoData];
    }
}

- (void)retryRequest:(BOOL)increaseRetryTimes {
    if (increaseRetryTimes) {
        self.retryTimes++;
    }
    
    SPLOGS(self.logTag, @"getvinfo the %dth retry", self.retryTimes);
    [self requestFromServerWithParam:self.requestParam];
}

- (NSString *)buildUrlWithHost:(NSString *)host
                     paramDict:(NSDictionary *)paramDict
                    ckeyFields:(NSString *)ckeyFields {
    NSString *path = @"getvinfo?";
    NSMutableString *url = [NSMutableString stringWithFormat:@"%@/%@otype=xml", host, path];
    
    NSString *keyValStr = [SPUtils keyValueStringWithUrlEncodeFromDictionary:paramDict];
    [url appendString:keyValStr];  // 附上免流参数
    [url appendString:ckeyFields];
    return url;
}

- (NSString *)getvinfoProtocol {
    return (self.requestParam.options.useHttps ? @"https://" : @"http://");
}

- (NSString *)getvinfoHost {
    NSString *host;
    SPCGIRequestOptions *options = self.requestParam.options;
    
    if (options.ipStack == SPCGIIPStackIPV6 ||
        (options.ipStack == SPCGIIPStackDual &&
         options.preferIPV6 &&
         ![SPVODInfoCache isIPV6EverError])) { // 如果双栈网络下IPV6出过错，就不再使用IPV6域名了
        SPLOGS(self.logTag, @"getvinfo use IPV6, IPStack=%d", (int)options.ipStack);
        // 这里跟android不一样，android因为在双栈网络下，会走IPV4的地址，所以要自己解析DNS得到IPV6的地址，强制走IPV6.
        // 但iOS在双栈网络下，默认走的就是IPV6地址，所以直接返回IP6域名即可
        host = [SPURLManager getVInfoIPV6Host];
        self.useIPV6 = YES;
    } else {
        self.useIPV6 = NO;
        // 主备份域名交替重试
        if ((self.retryTimes % 2) == 1) {
            host = [SPURLManager getvinfoHost];
        } else {
            host = [SPURLManager getVInfoBackHost];
        }
    }
    
    return host;
}

- (NSString *)ckeyFields:(SPVODRequestParam *)requestParam {
    SPCKeyParam *ckeyParam = [[SPCKeyParam alloc] init];
    if (requestParam.previd.length != 0) {
        ckeyParam.previd = requestParam.previd;
    } else {
        ckeyParam.videoIDForCKey = requestParam.vid;
    }
    
    ckeyParam.platform = requestParam.commonParams.platform;
    ckeyParam.sdtFrom = requestParam.commonParams.sdtFrom;
    ckeyParam.isDownload = NO;
    ckeyParam.isRender = requestParam.isDLNA || requestParam.isAirplay;
    return [[SPHLSKeyUtil sharedInstance] createCKeyUrlWithParam:ckeyParam];
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

- (void)processXML:(SPGetVInfoData *)getvinfoData {
    if (SPXMLParseErrorCodeOK == getvinfoData.parseResult) {
        [self processGetVInfoData:getvinfoData];
    } else {
        NSError *errorObj = [SPPlayerErrorCode buildErrorWithErrorCode:getvinfoData.parseResult
                                                                 errMsg:@""
                                                                 module:SPModuleVInfo
                                                                 domain:@"cgi"];
        [self notifyError:errorObj];
    }
}

- (void)processNetworkError:(NSError *)error {
    if (self.useIPV6) { // 当前请求走了IPV6域名，并且出错了，记下标记。如果是双栈网络，当前app生命周期不再走IPV6
        [SPVODInfoCache saveIPV6Error];
    }
    
    if ([self isExceedMaxRetryCount]) {
        SPLOGS(self.logTag, @"exceed max retry times:%d", self.retryTimes);
        NSError *newError = [SPPlayerErrorCode rebuildNetWorkError:error module:SPModuleVInfo];
        [self notifyError:newError];
        return;
    }
    
    [self retryRequest:YES];
}

- (void)processGetVInfoData:(SPGetVInfoData *)getvinfoData {
    if (getvinfoData.cgiErrorModel.em != 0) {
        SPLOGS(self.logTag, @"getvinfo server error, em=%d, exem=%d", getvinfoData.cgiErrorModel.em, getvinfoData.cgiErrorModel.exem);
        [self processServerErrorWithData:getvinfoData];
    } else {
        [self checkIfFairplay:getvinfoData.vodPlayInfo block:^(int requestID) {
            if (!self.stopped && requestID == self.requestID) {
                [self notifyGetVInfoData:getvinfoData];
            }
        }];
    }
}

- (void)processServerErrorWithData:(SPGetVInfoData *)getvinfoData {
    BOOL needRetry = NO;
    BOOL increaseRetryTimes = YES;
    if (getvinfoData.cgiErrorModel.needRetry) {
        SPLOGS(self.logTag, @"getvinfo need retry");
        // 85.-3不计入重试次数
        if (getvinfoData.cgiErrorModel.em == 85 &&
            getvinfoData.cgiErrorModel.exem == -3) {
            [[SPHLSKeyUtil sharedInstance] onGetCurrentServerTime:getvinfoData.cgiErrorModel.curSeverTime];
            [[SPHLSKeyUtil sharedInstance] onGetRandFlag:getvinfoData.cgiErrorModel.randFlag];
            needRetry = YES;
            increaseRetryTimes = NO;
        } else {
            if (![self isExceedMaxRetryCount]) {
                needRetry = YES;
            }
        }
    }
    
    if (needRetry) {
        [self retryRequest:increaseRetryTimes];
    } else {
        NSInteger errorCode = [SPPlayerErrorCode convertServerErrorCode:getvinfoData.cgiErrorModel.em];
        NSError *error = [SPPlayerErrorCode buildErrorWithErrorCode:errorCode
                                                              errMsg:getvinfoData.cgiErrorModel.errMsg
                                                           exErrCode:getvinfoData.cgiErrorModel.exem
                                                            exErrMsg:getvinfoData.cgiErrorModel.exInfo
                                                              module:SPModuleVInfo
                                                              domain:@"cgi"];
        [self notifyError:error];
    }
}

- (BOOL)isExceedMaxRetryCount {
    int maxRetryTimes = self.requestParam.options.maxRetryTimes;
    if (maxRetryTimes <= 0) {
        maxRetryTimes = DEFAULT_MAX_RETRY_TIMES;
    }
    return self.retryTimes >= maxRetryTimes;
}

- (void)asyncGetVInfoData:(SPGetVInfoData *)getvInfoData requestID:(int)requestID {
    @weakify(self)
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @strongify(self)
        [self.lock lock];
        if (!self.stopped && requestID == self.requestID) {
            [self notifyGetVInfoData:getvInfoData];
        }
        [self.lock unlock];
    });
}

- (void)notifyGetVInfoData:(SPGetVInfoData *)getvInfoData {
    if ([self.delegate respondsToSelector:@selector(request:onGetVInfoData:requestID:)]) {
        [self.delegate request:self onGetVInfoData:getvInfoData requestID:self.requestID];
    }
    
    self.requestParam = nil;
}

- (void)notifyError:(NSError *)error {
    if ([self.delegate respondsToSelector:@selector(request:onGetVInfoFailed:requestID:)]) {
        [self.delegate request:self onGetVInfoFailed:error requestID:self.requestID];
    }
    
    self.requestParam = nil;
}

- (void)trySaveGetVInfoXML:(NSString *)xmlString getvinfoData:(SPGetVInfoData *)getvinfoData {
    if (SPXMLParseErrorCodeOK == getvinfoData.parseResult
        && getvinfoData.cgiErrorModel.em == 0) {
        [SPVODInfoCache saveGetVInfoXML:xmlString
                            getVInfoData:getvinfoData
                            requestParam:self.requestParam];
    }
}

- (void)checkIfFairplay:(SPVODPlayInfo *)vodPlayInfo block:(void(^)(int requestID))block {
    int requestID = self.requestID;
    if (vodPlayInfo.drmModel != nil) {
        SPLOGS(self.logTag, @"download fairplay certificate, url=%@", vodPlayInfo.drmModel.cerUrl);
        @weakify(self)
        [[SPCertificateMgr sharedInstance] getCertificateWithUrl:vodPlayInfo.drmModel.cerUrl
                                                       completion:^(NSData *cerData, NSError *error) {
            @strongify(self)
            [self.lock lock];
            vodPlayInfo.drmModel.cerData = cerData;
            block(requestID);
            [self.lock unlock];
        }];
    } else {
        block(requestID);
    }
}

@end
