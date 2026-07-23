//
//  SPPlayerWrapperPlayerStrategy.m
//  SPPlayer
//
//  Created by 郭力 on 2019/10/19.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperPlayerStrategy.h"
#import "SPDefinitionUtil.h"
#import "SPPlayerLogContextUtil.h"
#import "SPPlayerUtils.h"
#import "SPPlayerWrapperHelper.h"
#import <ThumbPlayer/TPPlayerCoreType.h>
#import <ThumbPlayer/TPDecoderCapability.h>
#import "SPSDKParamsMgr.h"
#import "SPPlayerDefine.h"
#import "SPPlayerWrapperInfo.h"
#import "SPMediaInfo.h"

@implementation SPPlayerWrapperPlayerStrategy

// NOLINTNEXTLINE
+ (NSArray<NSNumber *> *)playerTypeListForPlayInfo:(SPMediaPlayInfo *)playInfo withTag:(NSString *)tag {
    if (playInfo.mediaInfo.playerTypeList.count > 0) {
        return playInfo.mediaInfo.playerTypeList;
    }
    
    if (!SPSDKCONF_ENABLE_SELF_PLAYER) {
        SPLOGI(tag, @"player type choose : choose by reason is : 自研开关关闭,全部用系统");
        SPLOGI(tag, @"player type choose : choose by reault is : system av player");
        return @[ @(TPPlayerCoreTypeSystemAVPlayer) ];
    }

    if ([SPPlayerUtils isLiveQAFromMediaInfo:playInfo.mediaInfo]) {
        SPLOGI(tag, @"player type choose : choose by reason is : 直播答题只能用自研");
        SPLOGI(tag, @"player type choose : choose by reault is : thumb player");
        return @[ @(TPPlayerCoreTypeSelfDevPlayer) ];
    }

    // 先判断格式，如果某些格式只能用某种播放器，直接返回
    if (playInfo.mediaType == SPMediaFormatFLV) {
        SPLOGI(tag, @"player type choose : choose by reason is : FLV用自研");
        SPLOGI(tag, @"player type choose : choose by reault is : thumb player");
        return @[ @(TPPlayerCoreTypeSelfDevPlayer) ];
    }
    
    // drm只能采用系统播放器
    if (playInfo.currentDefinition.drm == SPDrmTypeFairPlay) {
        SPLOGI(tag, @"player type choose : choose by reason is : Fair用系统");
        SPLOGI(tag, @"player type choose : choose by reault is : system av player");
        return @[ @(TPPlayerCoreTypeSystemAVPlayer) ];
    }

    // HEVC必须用自研
    if (playInfo.isHevc) {
        SPLOGI(tag, @"player type choose : choose by reason is : HEVC必须用自研");
        SPLOGI(tag, @"player type choose : choose by reault is : thumb player");
        return @[ @(TPPlayerCoreTypeSelfDevPlayer) ];
    }

    // 如果有分辨率，我们用分辨率判断，否则我们使用清晰度
    CGSize resolution = playInfo.videoSize;
    if (CGSizeEqualToSize(playInfo.videoSize, CGSizeZero)) {
        resolution = [SPDefinitionUtil resolutionForDefinitionName:playInfo.currentDefinition.fileName];
    }
    
    if (CGSizeEqualToSize(resolution, CGSizeZero)) {
        SPLOGI(tag, @"player type choose : choose by reason is : 未知分辨率视频，采用通用策略");
        SPLOGI(tag, @"player type choose : choose by reault is : [thumbplayer  | system av player]");
        return @[ @(TPPlayerCoreTypeSelfDevPlayer), @(TPPlayerCoreTypeSystemAVPlayer) ];
    }
    
    float fps = (playInfo.frameRate <= 0) ? 30 : playInfo.frameRate;
    BOOL supportCurResolution = [TPDecoderCapability videoDecoderCapability: TPVideoCodecTypeH264
                                                                      width: resolution.width
                                                                     height: resolution.height
                                                                  frameRate: fps];

    if (!supportCurResolution) {
        SPLOGI(tag, @"player type choose : choose by reason is : 低能力机型，不走自研，用系统");
        SPLOGI(tag, @"player type choose : choose by reault is : system av player");
        return @[ @(TPPlayerCoreTypeSystemAVPlayer) ];
    }

    SPLOGI(tag, @"player type choose : choose by reason is : 非特定的视频，采用通用策略");
    SPLOGI(tag, @"player type choose : choose by reault is : [thumbplayer  | system av player]");
    return @[ @(TPPlayerCoreTypeSelfDevPlayer), @(TPPlayerCoreTypeSystemAVPlayer) ];
}

+ (NSArray<NSNumber *> *)playerTypeListForPlayScene:(SPPlayerWrapperScene)scene withTag:(NSString *)tag {
    switch (scene) {
        case SPPlayerWrapperNormalPlay:
            return nil;
        case SPPlayerWrapperSceneAirPaly:
            SPLOGI(tag, @"player type choose : choose by reason is : air play");
            SPLOGI(tag, @"player type choose : choose by result is : use system av player");
            return @[ @(TPPlayerCoreTypeSystemAVPlayer) ];
        case SPPlayerWrapperScenePip:
            SPLOGI(tag, @"player type choose : choose by reason is : picture in picture");
            SPLOGI(tag, @"player type choose : choose by result is : use system av queue player");
            return @[ @(TPPlayerCoreTypeSystemAVQueuePlayer), @(TPPlayerCoreTypeSystemAVPlayer)];
    };
}

+ (SPVideoSwitchDefinitionType)switchTypeStrategyForMediaInfo:(SPMediaInfo *)mediaInfo
                                                 andExtralInfo:(nullable NSDictionary *)extralInfo
                                            andCurrentPlayInfo:(SPMediaPlayInfo *)playInfo
                                               andCurrentState:(SPPlayerWrapperState)state
                                                       withTag:(nullable NSString *)tag { // NOLINT
    tag = (tag == nil || [tag isEqualToString:@""]) ? gSPPlayerModeNameWrapper : tag;

    /**用户强行指定reopen，那么直接返回reopen*/
    if (extralInfo && [[extralInfo objectForKey:kSPVideoSwitchDefinitionTypeKey] intValue] == SPVideoSwitchDefinitionTypeNormal) {
        SPLOGI(tag, @"switch definition type choose : ************************************");
        SPLOGI(tag, @"switch definition type choose : choose reopen switch definition type");
        SPLOGI(tag, @"switch definition type choose : cause app force reopen type");
        SPLOGI(tag, @"switch definition type choose : *************************************");
        return SPVideoSwitchDefinitionTypeNormal;
    }

    /**当前的播放器状态不允许无缝切换*/
    if (state == SPPlayerWrapperStateCGIed || state == SPPlayerWrapperStatePreparing || state == SPPlayerWrapperStatePrepared) {
        SPLOGI(tag, @"switch definition type choose : ************************************");
        SPLOGI(tag, @"switch definition type choose : choose reopen switch definition type");
        SPLOGI(tag, @"switch definition type choose : cause wrapper player state not aloow seamless");
        SPLOGI(tag, @"switch definition type choose : *************************************");
        return SPVideoSwitchDefinitionTypeNormal;
    }
    
    BOOL isRequestSpecialStream = ([mediaInfo.definition isEqualToString:kSPMediaDefinitionDOLBY]);
    isRequestSpecialStream = (isRequestSpecialStream || mediaInfo.isDrm);
    isRequestSpecialStream = (isRequestSpecialStream || [mediaInfo.definition isEqualToString:kSPMediaDefinitionHDR10]);

    BOOL isCurrentSpecialStream = playInfo.currentDefinition.drm == SPDrmTypeFairPlay;
    
    //目前默认都是使用中台播放
    BOOL isCurrentThumbPlayer = YES;
    
    /**请求的DRM,当前的播放器是自研播放器*/
    if (mediaInfo.isDrm && isCurrentThumbPlayer) {
        SPLOGI(tag, @"switch definition type choose : ************************************");
        SPLOGI(tag, @"switch definition type choose : choose reopen switch definition type");
        SPLOGI(tag, @"switch definition type choose : cause current player is tpplayer , but request media info is drm");
        SPLOGI(tag, @"switch definition type choose : *************************************");
        return SPVideoSwitchDefinitionTypeNormal;
    }
    
    /**当前播放的需要系统播放器的特性流，但是请求的是非特性流，都走reopen*/
    if (isCurrentSpecialStream && !isCurrentThumbPlayer && !isRequestSpecialStream) {
        SPLOGI(tag, @"switch definition type choose : ************************************");
        SPLOGI(tag, @"switch definition type choose : choose reopen switch definition type");
        SPLOGI(tag, @"switch definition type choose : cause current is special video, request is not special video");
        SPLOGI(tag, @"switch definition type choose : *************************************");
        return SPVideoSwitchDefinitionTypeNormal;
    }

    /**剩下的全部情况，都统一走无缝切换*/
    SPLOGI(tag, @"switch definition type choose : ************************************");
    SPLOGI(tag, @"switch definition type choose : choose seamless switch definition type");
    SPLOGI(tag, @"switch definition type choose : *************************************");
    return SPVideoSwitchDefinitionTypeSeamless;
}

/**
 * 开启下载组件的策略
*/
+ (BOOL)proxyEnableStrategyForWrapperInfo:(SPPlayerWrapperInfo *)wrapperInfo {
    
    //默认开启下载组件
    BOOL proxyEnable = YES;
    SPMediaPlayInfo *playInfo = wrapperInfo.mediaPlayInfo;
    SPMediaInfo *mediaInfo = playInfo.mediaInfo;
    
    NSNumber *useDownloadProxy = mediaInfo.useDownloadProxy;
    if (useDownloadProxy) {
        proxyEnable = [useDownloadProxy boolValue];
    } else {
        //点播，判断开关是否关闭下载组件
        if (mediaInfo.playType == SPPlayTypeOnlineVod && !SPSDKCONF_ENABLE_ONLINE_VOD_P2P) {
            return NO;
        }
        
        //直播hls，判断开关是否关闭下载组件
        if (mediaInfo.playType == SPPlayTypeOnlineLive &&
            playInfo.mediaType == SPMediaFormatHLS &&
            !SPSDKCONF_ENABLE_LIVE_HLS_P2P) {
            return NO;
        }
        
        //直播flv，判断开关是否关闭下载组件
        if (mediaInfo.playType == SPPlayTypeOnlineLive &&
            playInfo.mediaType == SPMediaFormatFLV &&
            !SPSDKCONF_ENABLE_LIVE_FLV_P2P) {
            return NO;
        }
    }
    
    /**不使用下载组件的场景 - air play*/
    if ([SPPlayerUtils isAirPlayWithMediaInfo:mediaInfo]) {
        return NO;
    }
    
    /**不使用下载组件的场景 - rtmp不支持*/
    if (mediaInfo.playType == SPPlayTypeLiveExternalUrl && mediaInfo.mediaFormat == SPMediaFormatRTMP) {
        return NO;
    }
    
    /**直播回看不使用下载组件*/
    if (mediaInfo.playType == SPPlayTypeOnlineLive && wrapperInfo.isLivePlayBack) {
        return NO;
    }
    
    // 外部url，关闭下载组件
    if (mediaInfo.playType == SPPlayTypeExternalUrl) {
        return NO;
    }
    
    /**离线播放，不管任何场景，都得开启下载组件*/
    if (mediaInfo.playType == SPPlayTypeOfflineVod) {
        return YES;
    }
    
    /**边下边播，不管任何场景，都得开启下载组件*/
    if (mediaInfo.playType == SPPlayTypeDownloadingVod) {
        return YES;
    }
    
    // 其他情况，返回外部期望
    return proxyEnable;
}

@end
