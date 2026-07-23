/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPFairPlayManager.m
 Author      : fusionxu(许福生)
 Version     : 1.0
 Date        : 14/01/2018
 Description :
 History     : 14/01/2018 初始版本
 ***********************************************************/

#import "SPFairPlayManager.h"
#import "SPJSONResponse.h"
#import "SPATSHTTPRequest.h"
#import "SPNetWorkManager.h"
#import "SPDefinitionModel.h"
#import "SPSDKParamsMgr.h"
#import "SPVcSystemInfo.h"
#import "SPPlayerErrorCode.h"
#import "SPLog.h"

@interface SPFairPlayManager ()

@property (nonatomic, strong) AVAssetResourceLoadingRequest *loadingRequest;

@property (nonatomic, strong) SPATSHTTPRequest *httpRequest;

@property (nonatomic, strong) SPVODPlayInfo *mediaPlayInfo;

@property (nonatomic, assign) int retryTime;

@end

@implementation SPFairPlayManager

- (instancetype)initWithMediaPlayInfo:(SPVODPlayInfo *)mediaPlayInfo {
    self = [super init];
    if (self) {
        self.mediaPlayInfo = mediaPlayInfo;
        self.retryTime = 0;
    }

    return self;
}

- (BOOL)startRequestCKCWithRequest:(AVAssetResourceLoadingRequest *)loadingRequest {
    if ([self isRetryTimeReachMax]) {
        return NO;
    }

    SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:startRequestCKCWithSKD=%@", loadingRequest.request.URL);

    self.loadingRequest = loadingRequest;

    NSData *applicationCertificate = [self requestApplicationCertificate];

    NSURL *contentKeyIdentifierURL = self.loadingRequest.request.URL;
    NSData *assetIDDate = [contentKeyIdentifierURL.host dataUsingEncoding:NSUTF8StringEncoding];

    NSError *error;

    if (!applicationCertificate) {
        //证书内容为空当播放错误重试
        SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:certificate is nil");
        [self.loadingRequest finishLoadingWithError:nil];
        return NO;
    }

    NSData *spcData = [self.loadingRequest streamingContentKeyRequestDataForApp:applicationCertificate
                                                              contentIdentifier:assetIDDate
                                                                        options:nil
                                                                          error:&error];

    if (!spcData) {
        SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:streamingContentKeyRequestDataForApp,spcData return nil");
        [self.loadingRequest finishLoadingWithError:nil];
        return NO;
    }

    [self requestContentKeyFromKeySecurityModuleWithSPCData:spcData andAssetID:assetIDDate];

    return YES;
}

- (NSData *)requestApplicationCertificate {
    return self.mediaPlayInfo.drmModel.cerData;
}

- (void)requestContentKeyFromKeySecurityModuleWithSPCData:(NSData *)spcData andAssetID:(NSData *)assetID {
    NSMutableDictionary *requestHeaders = [[NSMutableDictionary alloc] init];
    [requestHeaders setValue:@"application/x-www-form-urlencoded" forKey:@"Content-Type"];

    NSCharacterSet *cs = [NSCharacterSet characterSetWithCharactersInString:@"+`#%^{}\"[]|\\<>//"].invertedSet;

    NSString *spcDataString = [spcData base64EncodedStringWithOptions:0];
    NSString *assetIDString = [[NSString alloc] initWithData:assetID encoding:NSUTF8StringEncoding];

    int vodf = self.mediaPlayInfo.currentDefinition.fileid.intValue;

    NSString *requestParameter = [NSString stringWithFormat:@"assetId=%@&spc=%@", assetIDString, spcDataString];
    requestParameter = [requestParameter stringByAppendingFormat:
                        @"&vid=%@&fmt=%d&ip=%@&platform=%@&uin=%@&openid=%@&guid=%@&version=%d&iosVersion=%@",
                        self.mediaPlayInfo.vid,
                        vodf, @"",
                        [SPSDKParamsMgr sharedInstance].platform,
                        SPSDKPARAMS_QUERY_UIN,
                        SPSDKPARAMS_QUERY_WX_OPENID,
                        SPSDKPARAMS_GUID,
                        1,
                        [SPVcSystemInfo sharedInstance].osVer];

    SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:requestContentKeyFromKeySecurityModuleWithSPCData,requestHeader:%@", requestHeaders);
    SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:requestContentKeyFromKeySecurityModuleWithSPCData,requestParameter:%@", requestParameter);

    requestParameter = [requestParameter stringByAddingPercentEncodingWithAllowedCharacters:cs];

    NSData *requestBody = [requestParameter dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];

    self.httpRequest = [[SPNetWorkManager shareInstance] postRequest:self.mediaPlayInfo.drmModel.ckcUrl
                                                       requestHeaders:requestHeaders
                                                             postData:requestBody
                                                    completionHandler:^(NSData *_Nullable responseData, NSError *_Nullable error) {
                                                        [self requestDidFinishWithData:responseData andError:error];
                                                    }];
}

- (void)requestDidFinishWithData:(NSData *)responseData andError:(NSError *)error {
    NSString* responseString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
        
    SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:requestDidFinishWithData,responseString:%@ error:%@",responseString, error);

    if (error) { // 网络错误
        if (![self retry]) {
            NSError *newError = [SPPlayerErrorCode buildErrorWithErrorCode:error.code
                                                                     errMsg:error.domain
                                                                     module:SPModuleFairPlay
                                                                     domain:@"fairplay"];
            NSError *fullError = [SPPlayerErrorCode buildFullErrorCodeWithError:newError];
            [self.loadingRequest finishLoadingWithError:fullError];
        }
        return ;
    }
    
    [self handleResonseData:responseData];
}

- (void)handleResonseData:(NSData *)responseData {
    SPJSONResponse *response = [[SPJSONResponse alloc] init];
    NSError *parseError = [response processResponseData:responseData];
    
    if (parseError) {  // json解析错误
        if (![self retry]) {
            NSError *newError = [SPPlayerErrorCode buildErrorWithErrorCode:SPJsonErrorCodeDataError
                                                                     errMsg:@"invalid data, can not parse"
                                                                     module:SPModuleFairPlay
                                                                     domain:@"fairplay"];
            NSError *fullError = [SPPlayerErrorCode buildFullErrorCodeWithError:newError];
            [self.loadingRequest finishLoadingWithError:fullError];
        }
        return ;
    }
    
    NSData *ckcData = nil;
    NSInteger ret = -1;
    NSString *msg = @"";
    NSString *ckc = @"";
    id dict = response.rootObject;
    
    if ([dict isKindOfClass:[NSDictionary class]]) {
        NSNumber *code = [(NSDictionary *)dict spNumberForKeySafeModel:@"code"];
        ret = [code intValue];
        msg = [(NSDictionary *)dict spStringForKeySafeModel:@"msg"];
        ckc = [(NSDictionary *)dict spStringForKeySafeModel:@"ckc"];
    } else { // json 非字典
        if (![self retry]) {
            NSError *newError;
            newError = [SPPlayerErrorCode buildErrorWithErrorCode:SPJsonErrorCodeParseFail errMsg:@"invalid data, not json dictionary"
                                                            module:SPModuleFairPlay
                                                            domain:@"fairplay"];
            [self.loadingRequest finishLoadingWithError:newError];
        }
        
        return;
    }
    
    /*
     retcode
     
     0        成功
     -1001    assetId为空或不合法
     -1002    spc参数为空或不合法
     -1003    assetId在drmcontent中不存在
     -1004    非法请求，不是getvinfo返回的合法getckc地址
     -2001    drmcontent服务异常
     -2002    服务器内部异常
     */
    SPLOGS(SP_PLAYER_LOG_FILTER, @"SPFairPlayManager:requestDidFinishWithData, code:%ld, msg:%@", ret, msg);
    
    ckcData = [[NSData alloc] initWithBase64EncodedString:ckc options:0];
    
    if (ret == 0 && ckcData) {
        [self.loadingRequest.dataRequest respondWithData:ckcData];
        [self.loadingRequest finishLoading];
    } else {  // 后台返回错误
        if (![self retry]) {
            NSError *newError;
            newError = [SPPlayerErrorCode buildErrorWithErrorCode:ret errMsg:msg module:SPModuleFairPlay domain:@"fairplay"];
            [self.loadingRequest finishLoadingWithError:newError];
        }
        return;
    }
}

- (BOOL)retry {
    if (self.retryTime < 2) {
        self.retryTime++;
        [self startRequestCKCWithRequest:self.loadingRequest];
        return YES;
    }
    
    return NO;
}

- (BOOL)isRetryTimeReachMax {
    return self.retryTime >= 2;
}

- (void)cancelRequset {
    [self.httpRequest cancel];
    self.httpRequest = nil;
}

- (void)dealloc {
    [_httpRequest cancel];
    _httpRequest = nil;
}

@end
