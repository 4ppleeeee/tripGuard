//
//  SPVBKeyRequest.m
//  SPPlayer
//
//  Created by liyukuan on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPVBKeyRequest.h"
#import "SPURLManager.h"
#import "SPVODRequestParam.h"
#import "SPVODQueryBuilder.h"
#import "SPHLSKeyUtil.h"
#import "SPNetWorkManager.h"
#import "SPVODInfoParser.h"
#import "SPPlayerErrorCode.h"
#import "SPVODInfoCache.h"
#import "SPCGIRequestIDMgr.h"

#define DEFAULT_MAX_RETRY_TIMES 6

#define GETVBKEY_MAX_CLIP_COUNT_ONCE 10

@implementation SPVBKeyRequestParam
@end

@interface SPVBKeyRequest ()

@property (nonatomic, copy) NSString *logTag;

@property (nonatomic, assign) int requestID;

@property (nonatomic, strong) SPVBKeyRequestParam *requestParam;

@property (nonatomic, assign) BOOL stopped;

@property (nonatomic, assign) int retryTimes;

@property (nonatomic, assign) int currentBeginIndex;

@property (nonatomic, strong) SPGetVBKeyData *getVBKeyData;

@property (nonatomic, strong) NSRecursiveLock *lock;

@property (nonatomic, assign) BOOL useIPV6;

@end

@implementation SPVBKeyRequest

- (instancetype)initWithParam:(SPCGIInitParam *)param {
    if ((self = [super initWithParam:param])) {
        _lock = [[NSRecursiveLock alloc] init];
    }
    
    return self;
}

- (int)requestWithParam:(SPVBKeyRequestParam *)requestParam {
    [self.lock lock];
    SPLOGS(self.cgiInitParam.logTag, @"requestWithParam, vid=%@, beginIndex=%d, endIndex=%d",
            requestParam.vodReqParam.vid, requestParam.beginIndex, requestParam.endIndex);
    [self stop];
    
    self.stopped = NO;
    self.requestID = [[SPCGIRequestIDMgr sharedInstance] generateGetVBKeyRequestID];
    self.requestParam = requestParam;
    self.retryTimes = 1;
    [self requestInternalWithParam:requestParam];
    [self.lock unlock];
    return self.requestID;
}

- (void)stop {
    [self.lock lock];
    SPLOGS(self.cgiInitParam.logTag, @"stop:%d", self.requestID);
    if (!self.stopped) {
        self.stopped = YES;
        self.retryTimes = 0;
        self.requestParam = nil;
    }
    [self.lock unlock];
}

#pragma mark - internal methods
- (void)requestInternalWithParam:(SPVBKeyRequestParam *)vkeyRequestParam {
    @weakify(self)
    [SPVODInfoCache readLocalGetVBKeyXML:vkeyRequestParam.vodReqParam
                               completion:^(NSString *xml, SPVODRequestParam *requestParam) {
        @strongify(self)
        [self.lock lock];
        if (xml.length > 0) {
            SPLOGS(self.cgiInitParam.logTag, @"read local vbkey successfully, requestID=%d", self.requestID);
            [self processXML:xml];
        } else {
            self.currentBeginIndex = vkeyRequestParam.beginIndex;  // 先将currentBeginIndex赋值为传进来的beginIndex
            [self requestFromServerWithParam:vkeyRequestParam];
        }
        [self.lock unlock];
    }];
}

- (void)requestFromServerWithParam:(SPVBKeyRequestParam *)requestParam {
    NSMutableDictionary<NSString *, NSString *> *paramDict = [[NSMutableDictionary alloc] init];
    SPVODRequestParam *vodRequestParam = requestParam.vodReqParam;
    [self buildCommonParam:paramDict requestParam:vodRequestParam];
    [self buildBasicParam:paramDict requestParam:requestParam];
    
    NSString *urlStr = [self buildUrlWithParmDict:paramDict requestParam:requestParam];
    [self executeRequestWithURL:urlStr cookie:requestParam.vodReqParam.commonParams.cookie];
}

- (void)retryRequest:(BOOL)increaseRetryTimes {
    if (increaseRetryTimes) {
        self.retryTimes++;
    }
    [self requestFromServerWithParam:self.requestParam];
}

- (void)buildBasicParam:(NSMutableDictionary *)paramDict requestParam:(SPVBKeyRequestParam *)vkeyRequestParam {
    SPVODRequestParam *vodRequestParam = (SPVODRequestParam *)vkeyRequestParam.vodReqParam;
    SPVODPlayInfo *vodPlayInfo = vkeyRequestParam.getvinfoData.vodPlayInfo;
    [paramDict spSetString:vodPlayInfo.vid forKey:@"vid"];
    [paramDict spSetString:vodRequestParam.srccontenid forKey:@"srccontenid"];
    [paramDict spSetString:vodPlayInfo.currentDefinition.fileid forKey:@"format"];
    NSString *clipIndexStr = [self clipIndexStrWithCurrentBeginIndex:self.currentBeginIndex finalEndIndex:vkeyRequestParam.endIndex];
    [paramDict spSetString:clipIndexStr forKey:@"idx"];
    [paramDict spSetString:(vodRequestParam.needCharge ? @"1" : @"0") forKey:@"charge"];
    
    SPVODUIInfo *uiInfo = vkeyRequestParam.getvinfoData.uiInfoArray.firstObject;
    [paramDict spSetString:uiInfo.vt forKey:@"vt"];
    [paramDict spSetString:@"2" forKey:@"linkver"];
    [paramDict spSetString:vodPlayInfo.link forKey:@"lnk"];
    [paramDict spSetString:vodRequestParam.flowID forKey:@"flowId"];
    [self buildFreeFlowParam:paramDict
               freeflowParam:vkeyRequestParam.vodReqParam.freeFlowParam
                 uiInfoArray:vkeyRequestParam.getvinfoData.uiInfoArray];
}

- (void)buildFreeFlowParam:(NSMutableDictionary *)paramDict
             freeflowParam:(NSDictionary<NSString *, NSString *> *)freeflowParam
               uiInfoArray:(NSArray<SPVODUIInfo *> *)uiInfoArray {
    if (freeflowParam.count > 0) {
        [paramDict addEntriesFromDictionary:freeflowParam];
        
        SPVODUIInfo *firstUIInfo = [uiInfoArray firstObject];
        NSMutableString *spip = [NSMutableString stringWithString:firstUIInfo.spip];
        NSMutableString *spport = [NSMutableString stringWithString:firstUIInfo.spport];
        NSMutableString *path = [NSMutableString stringWithString:firstUIInfo.path];
        
        for (NSUInteger i = 1; i < uiInfoArray.count; i++) {
            SPVODUIInfo *uiInfo = [uiInfoArray objectAtIndex:i];
            [spip appendString:@"|"];
            [spport appendString:@"|"];
            [path appendString:@"|"];
            
            [spip appendString:uiInfo.spip];
            [spport appendString:uiInfo.spport];
            [path appendString:uiInfo.path];
        }
        
        [paramDict spSetString:spip forKey:@"spip"];
        [paramDict spSetString:spport forKey:@"spport"];
        [paramDict spSetString:path forKey:@"path"];
    }
}

- (void)buildCommonParam:(NSMutableDictionary *)paramDict requestParam:(SPVODRequestParam *)vodRequestParam {
    [SPVODQueryBuilder buildVODCommonParam:paramDict requestCommonParam:vodRequestParam.commonParams];
}

- (NSString *)clipIndexStrWithCurrentBeginIndex:(int)currentBeginIndex finalEndIndex:(int)finalEndIndex {
    NSMutableString *clipIndexStr = [NSMutableString string];
    int endIndex = currentBeginIndex + GETVBKEY_MAX_CLIP_COUNT_ONCE - 1;
    if (endIndex > finalEndIndex) {
        endIndex = finalEndIndex;
    }
    for (int i = currentBeginIndex; i <= endIndex; i++) {
        [clipIndexStr appendFormat:@"%d|", i];
    }
    
    return clipIndexStr;
}

- (NSString *)getVBKeyProtocol {
    return (self.requestParam.vodReqParam.options.useHttps ? @"https://" : @"http://");
}

- (NSString *)getVBKeyHost {
    SPCGIRequestOptions *options = self.requestParam.vodReqParam.options;
    if (options.ipStack == SPCGIIPStackIPV6 ||
        (options.ipStack == SPCGIIPStackDual &&
         options.preferIPV6 &&
         ![SPVODInfoCache isIPV6EverError])) { // 双栈网络下，IPV6出国错，就不走IPV6了
        // 这里跟android不一样，android因为在双栈网络下，会走IPV4的地址，所以要自己解析DNS得到IPV6的地址，强制走IPV6.
        // 但iOS在双栈网络下，默认走的就是IPV6地址，所以直接返回IP6域名即可
        self.useIPV6 = YES;
        SPLOGS(self.cgiInitParam.logTag, @"getvbkey use IPV6");
        return [SPURLManager getVInfoIPV6Host];
    } else {
        self.useIPV6 = NO;
        // 主备份域名交替重试
        if ((self.retryTimes % 2) == 1) {
            return [SPURLManager getvinfoHost];
        } else {
            return [SPURLManager getVInfoBackHost];
        }
    }
}

- (NSString *)buildUrlWithParmDict:(NSDictionary *)paramDict requestParam:(SPVBKeyRequestParam *)vkeyRequestParam {
    NSString *protocol = [self getVBKeyProtocol];
    NSString *host = [self getVBKeyHost];
    NSString *path = @"getvbkey?";
    NSMutableString *url = [NSMutableString stringWithFormat:@"%@%@/%@otype=xml", protocol, host, path];
    
    NSString *keyValStr = [SPUtils keyValueStringWithUrlEncodeFromDictionary:paramDict];
    NSString *ckeyFields = [self ckeyFields:vkeyRequestParam];
    [url appendString:keyValStr];
    [url appendString:ckeyFields];
    
    return url;
}

- (NSString *)ckeyFields:(SPVBKeyRequestParam *)vkeyRequestParam {
    SPCKeyParam *ckeyParam = [[SPCKeyParam alloc] init];
    ckeyParam.videoIDForCKey = vkeyRequestParam.getvinfoData.vodPlayInfo.vid;
    ckeyParam.platform = vkeyRequestParam.vodReqParam.commonParams.platform;
    ckeyParam.sdtFrom = vkeyRequestParam.vodReqParam.commonParams.sdtFrom;
    ckeyParam.isDownload = NO;
    ckeyParam.isRender = vkeyRequestParam.vodReqParam.isDLNA || vkeyRequestParam.vodReqParam.isAirplay;
    return [[SPHLSKeyUtil sharedInstance] createCKeyUrlWithParam:ckeyParam];
}

- (void)executeRequestWithURL:(NSString *)urlStr cookie:(NSString *)cookie {
    NSMutableDictionary   *requestHeaders = [[NSMutableDictionary alloc] init];
    [requestHeaders setValue:cookie forKey:@"Cookie"];
    [requestHeaders setValue:@"qqlive" forKey:@"User-Agent"];
    int requestID = self.requestID;
    SPLOGS(self.cgiInitParam.logTag, @"getvbkey request url=%@, requestID=%d", urlStr, self.requestID);
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
    
    if (error != nil) {
        [self processNetworkError:error];
    } else {
        NSString *xmlString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
        [self processXML:xmlString];
    }
}

- (void)processXML:(NSString *)xmlString {
    SPLOGS(self.cgiInitParam.logTag, @"getvbkey response xml:%@", xmlString);
    BOOL freeFlow = self.requestParam.vodReqParam.freeFlowParam.count > 0;
    SPGetVBKeyData *vbkeyData = [SPVODInfoParser parseGetVBKeyString:xmlString freeFlow:freeFlow];
    if (SPXMLParseErrorCodeOK == vbkeyData.parseResult) {
//        [self trySaveGetVBKeyXMLString:xmlString];  // TODO：因为时分批拉取的，不能这样存了，
        [self processGetVBKeyData:vbkeyData];
    } else {
        NSError *errorObj = [SPPlayerErrorCode buildErrorWithErrorCode:vbkeyData.parseResult
                                                                 errMsg:@""
                                                                 module:SPModuleVInfo
                                                                 domain:@"cgi"];
        [self notifyError:errorObj];
    }
}

- (void)processGetVBKeyData:(SPGetVBKeyData *)vbkeyData {
    if (vbkeyData.cgiErrorModel.em != 0) {
        [self processServerErrorWithData:vbkeyData];
    } else {
        [self appendNewGetVBKeyData:vbkeyData];
        if (self.getVBKeyData.maxClipIndex < self.requestParam.endIndex) {  // 还没请求完
            SPLOGS(self.cgiInitParam.logTag, @"getvbkey not complete, resume");
            self.currentBeginIndex = self.getVBKeyData.maxClipIndex + 1;
            [self requestFromServerWithParam:self.requestParam];
        } else {
            SPLOGS(self.cgiInitParam.logTag, @"getvbkey complete");
            [self notifyGetVBKeyData:self.getVBKeyData];
            self.getVBKeyData = nil;
        }
    }
}

- (void)processNetworkError:(NSError *)error {
    if (self.useIPV6) { // 当前请求走了IPV6域名，并且出错了，记下标记。如果是双栈网络，当前app生命周期不再走IPV6
        [SPVODInfoCache saveIPV6Error];
    }
    if ([self isExceedMaxRetryCount]) {
        NSError *newError = [SPPlayerErrorCode rebuildNetWorkError:error module:SPModuleVInfo];
        [self notifyError:newError];
    } else {
        [self retryRequest:YES];
    }
}

- (void)processServerErrorWithData:(SPGetVBKeyData *)vbkeyData {
    if (vbkeyData.cgiErrorModel.em == 85 &&
        vbkeyData.cgiErrorModel.exem == -3) {
        [[SPHLSKeyUtil sharedInstance] onGetCurrentServerTime:vbkeyData.cgiErrorModel.curSeverTime];
        [[SPHLSKeyUtil sharedInstance] onGetRandFlag:vbkeyData.cgiErrorModel.randFlag];
        [self retryRequest:NO];
        return;
    }
    
    NSInteger errorCode = [SPPlayerErrorCode convertServerErrorCode:vbkeyData.cgiErrorModel.em];
    NSError *error = [SPPlayerErrorCode buildErrorWithErrorCode:errorCode
                                                          errMsg:vbkeyData.cgiErrorModel.errMsg
                                                       exErrCode:vbkeyData.cgiErrorModel.exem
                                                        exErrMsg:vbkeyData.cgiErrorModel.exInfo
                                                          module:SPModuleVBKey
                                                          domain:@"cgi"];
    [self notifyError:error];
}

- (BOOL)isExceedMaxRetryCount {
    int maxRetryTimes = self.requestParam.vodReqParam.options.maxRetryTimes;
    if (maxRetryTimes <= 0) {
        maxRetryTimes = DEFAULT_MAX_RETRY_TIMES;
    }
    return self.retryTimes >= maxRetryTimes;
}

- (void)appendNewGetVBKeyData:(SPGetVBKeyData *)getVBKeyData {
    if (self.getVBKeyData == nil) {
        self.getVBKeyData = getVBKeyData;
    } else {
        NSMutableDictionary<NSNumber *, SPClipInfo *> *mClipInfoDict =
        [[NSMutableDictionary alloc] initWithDictionary:self.getVBKeyData.clipInfoDict];
        [mClipInfoDict addEntriesFromDictionary:getVBKeyData.clipInfoDict];
        self.getVBKeyData.clipInfoDict = mClipInfoDict;
        self.getVBKeyData.maxClipIndex = getVBKeyData.maxClipIndex;
    }
}

- (void)notifyGetVBKeyData:(SPGetVBKeyData *)vbkeyData {
    if ([self.delegate respondsToSelector:@selector(request:onGeSPeyData:requestID:)]) {
        [self.delegate request:self onGeSPeyData:vbkeyData requestID:self.requestID];
    }
    
    self.requestParam = nil;
}

- (void)notifyError:(NSError *)error {
    if ([self.delegate respondsToSelector:@selector(request:onGeSPeyFailed:requestID:)]) {
        [self.delegate request:self onGeSPeyFailed:error requestID:self.requestID];
    }
    
    self.requestParam = nil;
}

- (void)trySaveGetVBKeyXMLString:(NSString *)xmlString {
    if (self.requestParam.getvinfoData.cgiErrorModel.em == 0) {
        [SPVODInfoCache saveGetVBKeyXML:xmlString
                            getVInfoData:self.requestParam.getvinfoData
                            requestParam:self.requestParam.vodReqParam];
    }
}
@end
