//
//  SPCGIManagerPrinter.m
//  SPPlayer
//
//  Created by liyukuan on 2019/11/28.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIManagerPrinter.h"
#import "SPVODRequestParam.h"
#import "SPLiveRequestParam.h"
#import "SPVODPlayInfo.h"
#import "SPLivePlayInfo.h"
#import "SPCGIManagerHelper.h"
#import "SPPlayerUtils.h"
#import "SPMediaPlayInfo+waterMark.h"
#import "TVKRawWaterMarkInfo.h"

@implementation SPCGIManagerPrinter

+ (void)printPlayParam:(SPPlayParam *)playParam logTag:(NSString *)logTag {
    SPLOGS(logTag, @"***************** print play param begin *********************");

    SPLOGS(logTag, @"request type: %@", [SPCGIManagerHelper stringOfRequestType:playParam.requestType]);
    SPLOGS(logTag, @"flowID: %@", playParam.flowID);
    SPLOGS(logTag, @"mediaInfo: vid : %@", playParam.mediaInfo.videoId);
    SPLOGS(logTag, @"mediaInfo: cid : %@", playParam.mediaInfo.coverId);
    SPLOGS(logTag, @"mediaInfo: srccontenid : %@", playParam.mediaInfo.srccontenid);
    SPLOGS(logTag, @"mediaInfo: definition : %@", playParam.mediaInfo.definition);
    SPLOGS(logTag, @"mediaInfo: startPosition : %f", playParam.mediaInfo.startPosition);
    SPLOGS(logTag, @"mediaInfo: skipEndPosition : %f", playParam.mediaInfo.skipEndPosition);
    SPLOGS(logTag, @"mediaInfo: extraRequestParamsMap : %@", playParam.mediaInfo.extraRequestParamsMap);
    SPLOGS(logTag, @"mediaInfo: configMap : %@", playParam.mediaInfo.configMap);
    SPLOGS(logTag, @"mediaInfo: freeFlowParam : %@", playParam.mediaInfo.freeFlowParam);

    SPLOGS(logTag, @"     **************** print context begin ************");
    SPLOGS(logTag, @"context: enableHEVC : %@", SP_BOOL_STR(playParam.playContext.enableHEVC));
    SPLOGS(logTag, @"context: enableFairPlay : %@", SP_BOOL_STR(playParam.playContext.enableFairPlay));
    SPLOGS(logTag, @"context: requiredDefinition : %@", playParam.playContext.requiredDefinition);
    SPLOGS(logTag, @"context: requiredMediaFormat : %@", [SPPlayerUtils stringOfMediaFormat:playParam.playContext.requiredMediaFormat]);
    SPLOGS(logTag, @"context: liveSeebackTime : %lld", playParam.playContext.liveSeebackTime);
    SPLOGS(logTag, @"context: extraInfo : %@", playParam.playContext.extraInfo);
    SPLOGS(logTag, @"context: extraRequestParams : %@", playParam.playContext.extraRequestParams);
    SPLOGS(logTag, @"context: extraConfig : %@", playParam.playContext.extraConfig);
    SPLOGS(logTag, @"     ****************  print context end  ************");

    SPLOGS(logTag, @"*****************  print play param end *********************");
}

+ (void)printRequestParam:(SPCGIRequestParam *)requestParam logTag:(NSString *)logTag {
    SPLOGS(logTag, @"***************** print request param begin *********************");
    SPLOGS(logTag, @"request param: vid : %@", requestParam.vid);
    SPLOGS(logTag, @"request param: definition : %@", requestParam.definition);
    SPLOGS(logTag, @"request param: mediaFormat : %@", [SPPlayerUtils stringOfMediaFormat:requestParam.mediaFormat]);
    SPLOGS(logTag, @"request param: isDLNA : %@", SP_BOOL_STR(requestParam.isDLNA));
    SPLOGS(logTag, @"request param: isAirPlay : %@", SP_BOOL_STR(requestParam.isAirplay));
    SPLOGS(logTag, @"request param: useCache : %@", SP_BOOL_STR(requestParam.options.useCache));

    if ([requestParam isKindOfClass:[SPVODRequestParam class]]) {
        SPVODRequestParam *vodRequestParam = (SPVODRequestParam *)requestParam;
        SPLOGS(logTag, @"request param: getvinfoType : %@", [SPCGIManagerHelper stringOfGetVInfoRequestType:vodRequestParam.getvinfoReqType]);
        SPLOGS(logTag, @"request param: hevc level : %d", vodRequestParam.capabilityParam.hevcLevel);
        SPLOGS(logTag, @"request param: spvideo : %d", vodRequestParam.capabilityParam.spvideo);
        SPLOGS(logTag, @"request param: spaudio : %d", vodRequestParam.capabilityParam.spaudio);
        SPLOGS(logTag, @"request param: spwm : %d", vodRequestParam.capabilityParam.spwm);
        SPLOGS(logTag, @"request param: defnPayVer : %d", vodRequestParam.capabilityParam.defnPayVer);
        SPLOGS(logTag, @"request param: spptype : %d", vodRequestParam.capabilityParam.spptype);
        SPLOGS(logTag, @"request param: spsrt : %d", vodRequestParam.capabilityParam.spsrt);
        SPLOGS(logTag, @"request param: drm : %d", vodRequestParam.capabilityParam.drm);
        SPLOGS(logTag, @"request param: sphls : %d", vodRequestParam.capabilityParam.sphls);
        SPLOGS(logTag, @"request param: spgzip : %d", vodRequestParam.capabilityParam.spgzip);
    }

    if ([requestParam isKindOfClass:[SPLiveRequestParam class]]) {
        SPLiveRequestParam *liveRequestParam = (SPLiveRequestParam *)requestParam;
        SPLOGS(logTag, @"request param: live request type : %d", liveRequestParam.requestType);
        SPLOGS(logTag, @"request param: hevc level : %d", liveRequestParam.capabilityParam.hevcLevel);
        SPLOGS(logTag, @"request param: spvideo : %d", liveRequestParam.capabilityParam.spvideo);
        SPLOGS(logTag, @"request param: spaudio : %d", liveRequestParam.capabilityParam.spaudio);
        SPLOGS(logTag, @"request param: active_sp : %d", liveRequestParam.capabilityParam.active_sp);
        SPLOGS(logTag, @"request param: enableLiveQueue : %@", SP_BOOL_STR(liveRequestParam.capabilityParam.enableLiveQueue));
    }

    SPLOGS(logTag, @"****************** print request param end **********************");
}

+ (void)printResponse:(SPMediaPlayInfo *)playInfo logTag:(NSString *)logTag {
    SPLOGS(logTag, @"***************** print response begin *********************");

    SPLOGS(logTag, @"response: vid : %@", playInfo.vid);
    SPLOGS(logTag, @"response: cur defn : %@", playInfo.currentDefinition.fileName);
    SPLOGS(logTag, @"response: format : %@", [SPPlayerUtils stringOfMediaFormat:playInfo.mediaType]);
    SPLOGS(logTag, @"response: codec : %@", playInfo.isHevc ? @"H265" : @"H264");
    SPLOGS(logTag, @"response: isPrewatch : %@", SP_BOOL_STR(playInfo.isPreWatch));
    SPLOGS(logTag, @"response: sshot : %@", @(playInfo.sshot));
    SPLOGS(logTag, @"response: mshot : %@", @(playInfo.mshot));
    
    int drm = playInfo.currentDefinition.drm;
    SPLOGS(logTag, @"response: drm type is : %d", drm);
    if (drm == SPDrmTypeFairPlay) {
        SPLOGS(logTag, @"response: drm type is : fair play");
    } else if (drm == SPDrmTypeSelfEnc) {
        SPLOGS(logTag, @"response: drm type is : chacha20");
    } else {
        SPLOGS(logTag, @"response: drm type is : not drm");
    }
    
    for (TVKRawWaterMarkInfo *wmInfo in playInfo.waterMarkModel.waterInfos) {
        SPLOGS(logTag, @"%@", wmInfo);
    }
    
    if (playInfo.waterMarkModel.actionUrl.length > 0) {
        SPLOGS(logTag, @"response: waterMark actionUrl : %@", playInfo.waterMarkModel.actionUrl);
    }

    SPLOGS(logTag, @"response: definition list:");
    for (SPDefinitionModel *defnModel in playInfo.defnModelList) {
        SPLOGS(logTag, @"%@", defnModel);
    }

    SPLOGS(logTag, @"response: section list:");
    for (SPSection *section in playInfo.sectionArray) {
        SPLOGS(logTag, @"%@", section);
    }

    if ([playInfo isKindOfClass:[SPVODPlayInfo class]]) {
        SPVODPlayInfo *vodPlayInfo = (SPVODPlayInfo *)playInfo;
        SPLOGS(logTag, @"response: duration : %f", vodPlayInfo.duration);
        SPLOGS(logTag, @"response: video state : %d", vodPlayInfo.videoState);
        SPLOGS(logTag, @"response: charge state : %d", vodPlayInfo.chargeState);
        SPLOGS(logTag, @"response: vodPreviewStart : %lf, vodPreViewTime : %lf", vodPlayInfo.vodPreviewStart, vodPlayInfo.vodPreViewTime);
    } else if ([playInfo isKindOfClass:[SPLivePlayInfo class]]) {
        SPLivePlayInfo *livePlayInfo = (SPLivePlayInfo *)playInfo;
        SPLOGS(logTag, @"response: needPlay : %@", SP_BOOL_STR(livePlayInfo.needPay));
        SPLOGS(logTag, @"response: isUserPlay : %@", SP_BOOL_STR(livePlayInfo.isUserPay));
        SPLOGS(logTag, @"response: see back info : %@", livePlayInfo.seeBackBaseInfo);
    }

    SPLOGS(logTag, @"*****************  print response end  *********************");
}

@end
