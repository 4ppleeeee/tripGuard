//
//  SPCGIManagerHelper.m
//  SPPlayer
//
//  Created by liyukuan on 2019/10/4.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIManagerHelper.h"
#import "SPVODRequestParam.h"
#import "SPLiveRequestParam.h"
#import "SPCGICapabilityParam.h"
#import "SPSDKParamsMgr.h"
#import "SPVcSystemInfo.h"
#import "SPNetworkChecker.h"
#import "SPPlayerUtils.h"
#import "SPPrepareUtils.h"
#import "SPIPStackCheck.h"
#import "SPPlayingContextHelper.h"

#define LOG_TAG @"SPCGIManagerHelper"

@implementation SPCGIManagerHelper

+ (SPCGIRequestParam *)buildCGIRequestParamWithPlayParam:(SPPlayParam *)playParam {
    if (SPCGIRequestTypeOfflineDownload == playParam.requestType) {
        // 离线现在单独处理单独处理
        return [self buildVODDownloadRequestParamWithPlayParam:playParam];
    }

    if (SPCGIRequestTypeURLGetter == playParam.requestType) {
        // URL直出单独处理
        return [self buildURLGetterRequestParamWithPlayParam:playParam];
    }

    if (SPPlayTypeOnlineLive == playParam.mediaInfo.playType) {
        return [self buildLiveRequestParamWithPlayParam:playParam];
    } else {
        return [self buildVODRequestParamWithPlayParam:playParam];
    }
}

/**
 * 点播请求参数构建
 */
+ (SPVODRequestParam *)buildVODRequestParamWithPlayParam:(SPPlayParam *)playParam {
    SPVODRequestParam *vodRequestParam = [[SPVODRequestParam alloc] init];
    [self fillVODBasicParam:vodRequestParam playParam:playParam];

    vodRequestParam.commonParams = [self buildRequestCommonParamWithPlayParam:playParam];
    vodRequestParam.capabilityParam = [self buildVODCapabilityParamWithPlayParam:playParam];
    return vodRequestParam;
}

/**
 * 直播请求参数构建
 */
+ (SPLiveRequestParam *)buildLiveRequestParamWithPlayParam:(SPPlayParam *)playParam {
    SPLiveRequestParam *liveRequestParam = [[SPLiveRequestParam alloc] init];
    [self fillLiveBasicParam:liveRequestParam playParam:playParam];

    liveRequestParam.commonParams = [self buildRequestCommonParamWithPlayParam:playParam];
    liveRequestParam.capabilityParam = [self buildLiveCapabilityParamWithPlayParam:playParam];
    return liveRequestParam;
}

/**
 * 离线下载请求参数构建。离线下载的能力参数与在线播放不同，所以单独抽离一个函数
 */
+ (SPVODRequestParam *)buildVODDownloadRequestParamWithPlayParam:(SPPlayParam *)playParam {
    SPVODRequestParam *vodRequestParam = [[SPVODRequestParam alloc] init];
    [self fillVODBasicParam:vodRequestParam playParam:playParam];

    vodRequestParam.commonParams = [self buildRequestCommonParamWithPlayParam:playParam];

    // 离线下载特有的设置
    vodRequestParam.getvinfoReqType = SPGetVInfoRequestTypeDownload;
    if (vodRequestParam.options == nil) {
        vodRequestParam.options = [[SPCGIRequestOptions alloc] init];
    }
    vodRequestParam.options.useCache = NO;  // 离线下载的getvinfo不走cgi缓存
    return vodRequestParam;
}

/**
 * URL直出请求参数构建。URL直出的能力参数与在线播放不同，所以单独抽离一个参数
 */
+ (SPCGIRequestParam *)buildURLGetterRequestParamWithPlayParam:(SPPlayParam *)playParam {
    if (SPPlayTypeOnlineLive == playParam.mediaInfo.playType) {
        return [self buildLiveUrlGetterRequestParamWithPlayParam:playParam];
    } else {
        return [self buildVODUrlGetterRequestParamWithPlayParam:playParam];
    }
}

/**
 * 点播URL直出
 */
+ (SPVODRequestParam *)buildVODUrlGetterRequestParamWithPlayParam:(SPPlayParam *)playParam {
    SPVODRequestParam *vodRequestParam = [[SPVODRequestParam alloc] init];
    [self fillVODBasicParam:vodRequestParam playParam:playParam];

    vodRequestParam.commonParams = [self buildRequestCommonParamWithPlayParam:playParam];

    // 以下是URL直出特有的设置
    vodRequestParam.getvinfoReqType = SPGetVInfoRequestTypeOnline;
    // URL直出不带能力参数，但spwm要带上
    SPVODCapabilityParam *capabilityParam = [[SPVODCapabilityParam alloc] init];
    capabilityParam.spwm = TVKWaterMarkCapabilityAction;
    vodRequestParam.capabilityParam = capabilityParam;
    if (vodRequestParam.options == nil) {
        vodRequestParam.options = [[SPCGIRequestOptions alloc] init];
    }
    vodRequestParam.options.useCache = NO;  // URL直出不走cgi缓存
    return vodRequestParam;
}

/**
 * 直播URL直出
 */
+ (SPLiveRequestParam *)buildLiveUrlGetterRequestParamWithPlayParam:(SPPlayParam *)playParam {
    SPLiveRequestParam *liveRequestParam = [[SPLiveRequestParam alloc] init];
    [self fillLiveBasicParam:liveRequestParam playParam:playParam];

    liveRequestParam.commonParams = [self buildRequestCommonParamWithPlayParam:playParam];
    return liveRequestParam;
}

+ (void)fillVODBasicParam:(SPVODRequestParam *)vodRequestParam playParam:(SPPlayParam *)playParam {
    [self fillCommonBasicParams:vodRequestParam playParam:playParam];

    vodRequestParam.startPosition = playParam.mediaInfo.startPosition;
    vodRequestParam.skipEndPosition = playParam.mediaInfo.skipEndPosition;
    vodRequestParam.mediaFormat = playParam.mediaInfo.mediaFormat;
    vodRequestParam.currentPlayPosition = playParam.playContext.currentPlayPosition;
    vodRequestParam.previd = [SPPlayerUtils previdFromMediaInfo:playParam.mediaInfo];
    vodRequestParam.getvinfoReqType = [self getvinfoTypeWithPlayParam:playParam];
}

+ (void)fillLiveBasicParam:(SPLiveRequestParam *)liveRequestParam playParam:(SPPlayParam *)playParam {
    [self fillCommonBasicParams:liveRequestParam playParam:playParam];

    liveRequestParam.requestType =
        ([SPPlayingContextHelper isLiveGetPreviewWithPlayContext:playParam.playContext] ? SPLiveRequestTypePreview : SPLiveRequestTypePlay);
    liveRequestParam.userLiveSeeBackTime = playParam.playContext.liveSeebackTime;
}

+ (void)fillCommonBasicParams:(SPCGIRequestParam *)requestParam playParam:(SPPlayParam *)playParam {
    requestParam.flowID = playParam.flowID;
    requestParam.vid = playParam.mediaInfo.videoId;
    requestParam.cid = playParam.mediaInfo.coverId;
    requestParam.srccontenid = playParam.mediaInfo.srccontenid;
    requestParam.definition = [self definitionWithPlayParam:playParam];
    requestParam.mediaFormat = [self mediaFormatWithPlayContext:playParam.playContext];
    requestParam.needCharge = playParam.mediaInfo.isNeedCharge;
    requestParam.freeFlowParam = playParam.mediaInfo.freeFlowParam;
    requestParam.isDLNA = [SPPlayingContextHelper isDLNAWithPlayContext:playParam.playContext];
    requestParam.isAirplay = [SPPlayerUtils isAirPlayWithMediaInfo:playParam.mediaInfo];
    requestParam.extraParams = [self buildExtrParamWithPlayParam:playParam];
    requestParam.options = [self buildRequestOptions];
    requestParam.options.useCache = playParam.mediaInfo.useVInfoGetterCache;
}

+ (SPCGIRequestCommonParam *)buildRequestCommonParamWithPlayParam:(SPPlayParam *)playParam {
    SPCGIRequestCommonParam *commonParam = [[SPCGIRequestCommonParam alloc] init];
    commonParam.platform = playParam.mediaInfo.platform ? : [SPSDKParamsMgr sharedInstance].sdkGetVInfoModels.firstObject.platform;
    if (playParam.requestType == SPCGIRequestTypeOfflineDownload) {
        id obj = [playParam.playContext.extraConfig objectForKey:@"offline_sdtfrom"];
        if ([obj isKindOfClass:[NSString class]]) {
            commonParam.sdtFrom = (NSString *)obj;
        }
    }
    if (commonParam.sdtFrom == nil) {
        commonParam.sdtFrom = playParam.mediaInfo.sdtfrom ? : [SPSDKParamsMgr sharedInstance].sdkGetVInfoModels.firstObject.sdtfrom;
    }

    commonParam.sysVer = [[SPVcSystemInfo sharedInstance] systemVer];
    // TODO:确认跟[SPVcSystemInfo deviceModel]是否一样
    NSString *deviceModel = [[[UIDevice currentDevice] model] stringByReplacingOccurrencesOfString:@" " withString:@"_"];
    commonParam.deviceModel = deviceModel;
    commonParam.netType = [self cgiNetType];
    commonParam.loginType = [self cgiLoginType];
    commonParam.localeIdentifier = [[NSLocale currentLocale] localeIdentifier];
    commonParam.guid = SPSDKPARAMS_GUID;
    commonParam.qimei = SPSDKPARAMS_QIMEI;
    commonParam.uin = SPSDKPARAMS_QUERY_UIN;
    commonParam.userID = SPSDKPARAMS_QUERY_V_USER_ID;
    commonParam.wxOpenID = SPSDKPARAMS_QUERY_WX_OPENID;
    commonParam.cookie = SPSDKPARAMS_QUERY_LOGIN_COOKIE;
    commonParam.isVIP = SPSDKPARAMS_QUERY_IS_VIP;
    commonParam.userAgent = [[SPSDKParamsMgr sharedInstance] userAgent];
    return commonParam;
}

+ (SPVODCapabilityParam *)buildVODCapabilityParamWithPlayParam:(SPPlayParam *)playParam {
    SPVODCapabilityParam *capabilityParam = [[SPVODCapabilityParam alloc] init];
    capabilityParam.hevcLevel = [self hevcLevelWithPlayParam:playParam];
    capabilityParam.spvideo = [self spvideoWithPlayParam:playParam];
    capabilityParam.spaudio = [self spaudioWithPlayParam:playParam];
    capabilityParam.spwm = [SPPrepareUtils supportWaterMarkCapablity];
    capabilityParam.defnPayVer = [SPPrepareUtils supportDefnPayVerBitSet];
    capabilityParam.spptype = [SPSDKParamsMgr sharedInstance].spptype;
    capabilityParam.drm = [self vodDrmCapabilityWithPlayParam:playParam];
    if (SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_GETVINFO_CARRY_M3U8)) {
        capabilityParam.sphls = 2;
        capabilityParam.spgzip = 1;
    }
    return capabilityParam;
}

+ (SPLiveCapabilityParam *)buildLiveCapabilityParamWithPlayParam:(SPPlayParam *)playParam {
    SPLiveCapabilityParam *capabilityParam = [[SPLiveCapabilityParam alloc] init];
    capabilityParam.hevcLevel = [self hevcLevelWithPlayParam:playParam];
    capabilityParam.spvideo = [self spvideoWithPlayParam:playParam];
    capabilityParam.spaudio = [self spaudioWithPlayParam:playParam];
    capabilityParam.drm = [self liveDrmCapabilityWithPlayParam:playParam];
    capabilityParam.active_sp = SPSDKCONF_LIVE_ACTIVE_SP;
    capabilityParam.enableLiveQueue = SPSDKCONF_ENABLE_LIVE_QUEUE;
    return capabilityParam;
}

+ (SPCGIRequestOptions *)buildRequestOptions {
    SPCGIRequestOptions *options = [[SPCGIRequestOptions alloc] init];
    SPLocalIPStack ipStack = sp_local_ipstack_cetect();
    if (ipStack == SPLocalIPStack_IPv6) {
        options.ipStack = SPCGIIPStackIPV6;
    } else if (ipStack == SPLocalIPStack_Dual) {
        options.ipStack = SPCGIIPStackDual;
    } else {
        options.ipStack = SPCGIIPStackIPV4;
    }
    options.preferIPV6 = SPSDKCONF_PREFER_IPV6_IN_IP_STACK_DUAL;
    options.useHttps = SPSDKCONF_CGI_USE_HTTPS;
    options.maxRetryTimes = SPSDKCONF_CGI_RETRY_MAX_TIMES;
    options.useCache = SPSDKCONF_CGI_USE_CACHE;
    return options;
}

+ (NSDictionary<NSString *, NSString *> *)buildExtrParamWithPlayParam:(SPPlayParam *)playParam {
    NSMutableDictionary<NSString *, NSString *> *extraParams = [[NSMutableDictionary alloc] init];
    if (playParam.mediaInfo.extraRequestParamsMap) {
        [extraParams addEntriesFromDictionary:playParam.mediaInfo.extraRequestParamsMap];
    }

    if (playParam.playContext.extraRequestParams) {
        [extraParams addEntriesFromDictionary:playParam.playContext.extraRequestParams];
    }
    
    if (SPSDKPARAMS_QUERY_USERINFO_EXTRA_DICTTIONARY) {
        [extraParams addEntriesFromDictionary:SPSDKPARAMS_QUERY_USERINFO_EXTRA_DICTTIONARY];
    }

    return extraParams;
}

+ (SPCGINetType)cgiNetType {
    SPNetworkCheckerNewNetType netType = [SPNetworkChecker newNetType];
    SPCGINetType cgiNetType = SPCGINetTypeNone;
    switch (netType) {
        case SPNetworkCheckerNewNetTypeNetInavailable:
            cgiNetType = SPCGINetTypeNone;
            break;
        case SPNetworkCheckerNewNetTypeWiFi:
            cgiNetType = SPCGINetTypeWifi;
            break;
        case SPNetworkCheckerNewNetType2G:
            cgiNetType = SPCGINetType2G;
            break;
        case SPNetworkCheckerNewNetType3G:
            cgiNetType = SPCGINetType3G;
            break;
        case SPNetworkCheckerNewNetType4G:
            cgiNetType = SPCGINetType4G;
            break;
        case SPNetworkCheckerNewNetType5G:
            cgiNetType = SPCGINetType5G;
            break;
        default:
            break;
    }

    return cgiNetType;
}

+ (SPGetVInfoRequestType)getvinfoTypeWithPlayParam:(SPPlayParam *)playParam {
    // 注意，必须先判断一些必须走在线播放的场景，因为mediaInfo.playType虽然可能为SPPlayTypeOfflineVod，但可能要走在线播放，比如离线播放AirPlay，所以不能先判mediaInfo.playType
    if ([SPPlayerUtils isAirPlayWithMediaInfo:playParam.mediaInfo]) {
        return SPGetVInfoRequestTypeOnline;
    } else if (playParam.requestType == SPCGIRequestTypeVKeyExpire ||
               playParam.requestType == SPCGIRequestTypeNoMoreData) {  // vkey过期和NoMoreData都走在线
        return SPGetVInfoRequestTypeOnline;
    } else if (playParam.mediaInfo.playType == SPPlayTypeOfflineVod ||
               playParam.mediaInfo.playType == SPPlayTypeDownloadingVod) {  // 离线播放和边下边播都标记为离线播放，都要从下载组件取getvinfo
        return SPGetVInfoRequestTypeOfflinePlay;
    } else {
        return SPGetVInfoRequestTypeOnline;  // 其他的都返回在线播放类型
    }
}

+ (SPCGILoginType)cgiLoginType {
    SPLoginType loginType = SPSDKPARAMS_QUERY_LOGIN_TYPE;
    SPCGILoginType cgiLoginType;
    switch (loginType) {
        case SPLoginTypeNone:
            cgiLoginType = SPCGILoginTypeNone;
            break;
        case SPLoginTypeQQ:
            cgiLoginType = SPCGILoginTypeQQ;
            break;
        case SPLoginTypeWx:
            cgiLoginType = SPCGILoginTypeWx;
            break;
        default:
            cgiLoginType = SPCGILoginTypeNone;
            break;
    }

    return cgiLoginType;
}

+ (SPMediaFormat)mediaFormatWithPlayContext:(SPPlayingContext *)playContext {
    if (playContext && playContext.requiredMediaFormat != SPMediaFormatAuto) {
        return playContext.requiredMediaFormat;
    }

    if (SPSDKCONF_GET_VINFO_DLTYPE == SPMediaDLTypeHttp) {
        int clip = SPSDKCONF_GET_VINFO_CLIP;
        if (clip == 4) {
            return SPMediaFormatOneMp4;
        } else {
            return SPMediaFormatMultiMp4;
        }
    } else {
        return SPMediaFormatHLS;
    }
}

+ (SPHEVCLevel)hevcLevelWithPlayParam:(SPPlayParam *)playParam {
    // 这里必须对playContext判空，如果为空，则不受playConte的影响，继续往下走。
    if (playParam.playContext && !playParam.playContext.enableHEVC) {
        return SPHEVCLevelNone;
    }

    if ([SPPlayerUtils isAirPlayWithMediaInfo:playParam.mediaInfo]) {
        SPLOGS(gSPPlayerDefaultTagPrefix, @"airplay not request hevc");
        return SPHEVCLevelNone;
    }

    return [SPPlayerUtils hevcLevel];
}

+ (int)spvideoWithPlayParam:(SPPlayParam *)playParam {
    int spvideo = 0;
    return spvideo;
}

+ (int)spaudioWithPlayParam:(SPPlayParam *)playParam {
    int spaudio = 0;
    spaudio += [SPPrepareUtils supportAudioPlayBitSet];
    return spaudio;
}

+ (NSString *)definitionWithPlayParam:(SPPlayParam *)playParam {
    if (playParam.playContext && playParam.playContext.requiredDefinition.length > 0) {
        return playParam.playContext.requiredDefinition;
    }

    return playParam.mediaInfo.definition;
}

+ (BOOL)enableHLSEncrptyWithPlayParam:(SPPlayParam *)playParam {
    if ([SPPlayerUtils isAirPlayWithMediaInfo:playParam.mediaInfo]) {
        SPLOGS(LOG_TAG, @"airplay not request HLS encrypt");
        return NO;
    }

    return YES;
}

#pragma - mark Drm
+ (int)vodDrmCapabilityWithPlayParam:(SPPlayParam *)playParam {
    int drm = 0;
    
    //fairpaly 加密, 系统播放器支持
    if (SPSDKCONF_ENABLE_FAIRPLAY &&
        [self enableFairPlayWithPlayParam:playParam]) {
        drm += SPDRMCapabilityFairplay;
    }

    //自研加密，下载组件支持
    if (SPSDKCONF_ENABLE_ONLINE_VOD_P2P &&
        SPSDKCONF_ENABLE_SELF_ENCRYPTION &&
        [self enableHLSEncrptyWithPlayParam:playParam]) {
        drm += SPDRMCapabilityHLSEncrypt;
    }
    
    return drm;
}

+ (int)liveDrmCapabilityWithPlayParam:(SPPlayParam *)playParam {
    int drm = 0;
    
    //fairpaly 加密, 系统播放器支持
    if (SPSDKCONF_ENABLE_FAIRPLAY &&
        [self enableFairPlayWithPlayParam:playParam]) {
        drm += SPDRMCapabilityFairplay;
    }

    //自研加密，下载组件支持
    if ((SPSDKCONF_ENABLE_LIVE_FLV_P2P || SPSDKCONF_ENABLE_LIVE_HLS_P2P) &&
        SPSDKCONF_LIVE_ENABLE_SELF_ENCRYPTION &&
        [self enableHLSEncrptyWithPlayParam:playParam]) {
        drm += SPDRMCapabilityHLSEncrypt;
    }
    
    return drm;
}

+ (BOOL)enableFairPlayWithPlayParam:(SPPlayParam *)playParam {
//    if (playParam.mediaInfo.isAirPlay) {
//        SPLOGS(LOG_TAG, @"airplay not request fairplay");
//        return NO;
//    }

    if (playParam.playContext == nil) {
        return YES;
    }

    return playParam.playContext.enableFairPlay;
}

#pragma - mark TypeToString
+ (NSString *)stringOfRequestType:(SPCGIRequestType)requestType {
    switch (requestType) {
        case SPCGIRequestTypeNormal:
            return @"normal";
            break;
        case SPCGIRequestTypeSwitchDefnSeamless:
            return @"seamless switch definition";
            break;
        case SPCGIRequestTypeSwitchDefnReOpen:
            return @"reopen switch definition";
            break;
        case SPCGIRequestTypeErrorRetry:
            return @"error retry";
            break;
        case SPCGIRequestTypeLiveSeekBack:
            return @"live seek back";
            break;
        case SPCGIRequestTypeVKeyExpire:
            return @"vkey expire";
            break;
        case SPCGIRequestTypePIP:
            return @"picture in picture";
            break;
        case SPCGIRequestTypeOfflineDownload:
            return @"offline download";
            break;
        case SPCGIRequestTypeNoMoreData:
            return @"no more data";
            break;
        case SPCGIRequestTypeURLGetter:
            return @"url getter";
            break;
        default:
            return [@(requestType) stringValue];
            break;
    }
}

+ (NSString *)stringOfGetVInfoRequestType:(SPGetVInfoRequestType)getvinfoRequestType {
    switch (getvinfoRequestType) {
        case SPGetVInfoRequestTypeOnline:
            return @"online play";
            break;
        case SPGetVInfoRequestTypeOfflinePlay:
            return @"offline play";
            break;
        case SPGetVInfoRequestTypeDownload:
            return @"download";
            break;
        default:
            return [@(getvinfoRequestType) stringValue];
            break;
    }
}
@end
