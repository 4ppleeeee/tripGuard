/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPPlayerUtils.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/22
 Description :
 History     : 17/2/22 初始版本
 ***********************************************************/

#import "SPPlayerUtils.h"
#import "NSDictionary+SPSafeDictionary.h"
#import "SPDefinitionModel.h"
#import "SPDefinitionUtil.h"
#import "SPLivePlayInfo.h"
#import "SPMediaInfo.h"
#import "SPNetVideoInfo.h"
#import "SPNetworkChecker.h"
#import "SPPlayerDefine.h"
#import "SPPlayerWrapperDefine.h"
#import "SPUtils.h"
#import "SPVODPlayInfo.h"
#import "SPVcSystemInfo.h"

@implementation SPPlayerUtils

+ (SPNetVideoInfo *)netVideoInfoFromPlayInfo:(SPMediaPlayInfo *)playInfo {
    SPNetVideoInfo *netVideoInfo = [[SPNetVideoInfo alloc] init];

    netVideoInfo.videoId = playInfo.vid;

    // 清晰度
    netVideoInfo.currentDefinition = [self definitionInfoFromDefnModel:playInfo.currentDefinition];

    NSMutableArray *defList = [[NSMutableArray alloc] initWithCapacity:playInfo.defnModelList.count];
    for (SPDefinitionModel *defnModel in playInfo.defnModelList) {
        SPNetMediaDefinitionInfo *defInfo = [self definitionInfoFromDefnModel:defnModel];
        [defList addObject:defInfo];
    }

    netVideoInfo.definitionList = defList;

    netVideoInfo.mediaFormat = playInfo.mediaType;

    netVideoInfo.isVR = (playInfo.vr != 0);

    // 点播特有信息
    if ([playInfo isKindOfClass:[SPVODPlayInfo class]]) {
        SPVODPlayInfo *vodPlayInfo = (SPVODPlayInfo *)playInfo;
        [self converVODPlayInfo:vodPlayInfo netVideoInfo:netVideoInfo];
    }

    // 直播特有信息
    if ([playInfo isKindOfClass:[SPLivePlayInfo class]]) {
        SPLivePlayInfo *livePlayInfo = (SPLivePlayInfo *)playInfo;
        [self convertLivePlayInfo:livePlayInfo netVideoInfo:netVideoInfo];
    }

    NSMutableArray<NSURL *> *urlArray = [[NSMutableArray alloc] init];
    NSMutableArray<NSNumber *> *timeArray = [[NSMutableArray alloc] init];
    for (SPSection *section in playInfo.sectionArray) {
        if (section.url) {
            [urlArray addObject:[NSURL URLWithString:section.url]];
        }

        [timeArray addObject:@(section.duration)];
    }
    netVideoInfo.videoUrlArray = urlArray;
    netVideoInfo.videoTimeArray = timeArray;

    netVideoInfo.flowId = playInfo.flowId;

    // cdnPlayUrl仅在iPad中用于网络诊断，就直接返回第一个地址吧，ethanyxliu
    netVideoInfo.cdnPlayUrl = playInfo.sectionArray.firstObject.url;
    netVideoInfo.sshot = playInfo.sshot;
    netVideoInfo.mshot = playInfo.mshot;
    
    return netVideoInfo;
}

+ (void)converVODPlayInfo:(SPVODPlayInfo *)vodPlayInfo netVideoInfo:(SPNetVideoInfo *)netVideoInfo {
    netVideoInfo.duration = vodPlayInfo.duration;
    netVideoInfo.lnk = vodPlayInfo.link;

    netVideoInfo.aspectRation = vodPlayInfo.aspectRatio;

    netVideoInfo.vodPreviewTime = vodPlayInfo.vodPreViewTime;
    netVideoInfo.vodPreviewStart = vodPlayInfo.vodPreviewStart;

    // 付费状态、视频状态
    netVideoInfo.chargeState = vodPlayInfo.chargeState;
    netVideoInfo.state = vodPlayInfo.videoState;
    netVideoInfo.mediaState = vodPlayInfo.mediaState;

    netVideoInfo.fVideo = vodPlayInfo.fVideo;

    // fairplay
    netVideoInfo.drm = vodPlayInfo.currentDefinition.drm;

    // 秒播用到,TODO:这么填是否可以，最好跟charli确认一下，ethanyxliu
//    netVideoInfo.isP2PPlayMode = SPSDKCONF_enable_online_vod_P2P; // 先直接取开关的值
    netVideoInfo.isP2PPlayMode = NO;
    netVideoInfo.isP2POfflinePlay = NO; // 中台播放器秒播走的都是在线

    SPSection *section = vodPlayInfo.sectionArray.firstObject;
    if (section.index < section.vtList.count) {
        netVideoInfo.cdnId = section.vtList[section.index];
    }
}

+ (void)convertLivePlayInfo:(SPLivePlayInfo *)livePlayInfo netVideoInfo:(SPNetVideoInfo *)netVideoInfo {
    //试看
    SPNetLivePreviewInfo *livePreviewInfo = [[SPNetLivePreviewInfo alloc] init];
    livePreviewInfo.playTime = livePlayInfo.livePlayTime;
    livePreviewInfo.previewTime = livePlayInfo.livePreviewTime;
    livePreviewInfo.previewCount = livePlayInfo.livePreviewCount;
    livePreviewInfo.restPreviewCount = livePlayInfo.liveRestPreviewCount;
    netVideoInfo.livePreviewInfo = livePreviewInfo;

    // 付费状态
    netVideoInfo.needPay = livePlayInfo.needPay;  //是否需要付费
    netVideoInfo.isPay = livePlayInfo.isUserPay;  //用户是否已经付费

    // 直播回看
    if (livePlayInfo.seeBackBaseInfo) {
        SPNetLiveSeebackInfo *seebackInfo = [[SPNetLiveSeebackInfo alloc] init];
        seebackInfo.seebackStartTime =
            (NSUInteger)livePlayInfo.seeBackBaseInfo.seeBackstartTime;
        seebackInfo.maxSeebackTime =
            (NSUInteger)livePlayInfo.seeBackBaseInfo.maxSeeBacktime;
        seebackInfo.serverTime =
            (NSUInteger)livePlayInfo.seeBackBaseInfo.serverTime;
        seebackInfo.isSeebackState = livePlayInfo.seeBackBaseInfo.isSeeBackState;
        netVideoInfo.seebackInfo = seebackInfo;
    }

    // 直播排队
    SPLiveQueueInfo *liveQueueInfo = [[SPLiveQueueInfo alloc] init];
    liveQueueInfo.queue_status = livePlayInfo.queueStatus;
    liveQueueInfo.queue_rank = (int)livePlayInfo.queueRank;
    liveQueueInfo.queue_vip_jump = livePlayInfo.queueVipJump;
    liveQueueInfo.queue_session_key = livePlayInfo.queueSessionKey;
    netVideoInfo.liveQueueInfo = liveQueueInfo;
}

+ (SPNetMediaDefinitionInfo *)definitionInfoFromDefnModel:(SPDefinitionModel *)fileInfo {
    SPNetMediaDefinitionInfo *definitionInfo = [[SPNetMediaDefinitionInfo alloc] init];
    definitionInfo.definition = fileInfo.fileName;

    definitionInfo.fullText = fileInfo.fullText;
    definitionInfo.resolutionText = fileInfo.resolutionText;
    definitionInfo.definitionShowShortName = fileInfo.shortText;
    definitionInfo.isNeedVip = fileInfo.isVip;
    definitionInfo.fileSize = fileInfo.videoFileSize;
    definitionInfo.videoCodec = fileInfo.video;
    definitionInfo.audioCodec = fileInfo.audio;
    definitionInfo.postProcess = fileInfo.sr ? 1 : 0;
    return definitionInfo;
}

+ (NSString *)stringForPlayerWrapperState:(SPPlayerWrapperState)state {
    NSString *playerStateString = @"";
    switch (state) {
        case SPPlayerWrapperStateUnknown:
            playerStateString = @"SPPlayerWrapperStateUnknown";
            break;
        case SPPlayerWrapperStateCGIing:
             playerStateString = @"SPPlayerWrapperStateCGIing";
            break;
        case SPPlayerWrapperStateCGIed:
            playerStateString = @"SPPlayerWrapperStateCGIed";
            break;
        case SPPlayerWrapperStatePreparing:
            playerStateString = @"SPPlayerWrapperStatePreparing";
            break;
        case SPPlayerWrapperStatePrepared:
            playerStateString = @"SPPlayerWrapperStatePrepared";
            break;
        case SPPlayerWrapperStatePlaying:
            playerStateString = @"SPPlayerWrapperStatePlaying";
            break;
        case SPPlayerWrapperStateUserPaused:
            playerStateString = @"SPPlayerWrapperStateUserPaused";
            break;
        case SPPlayerWrapperStateStopped:
            playerStateString = @"SPPlayerWrapperStateStopped";
            break;
        case SPPlayerWrapperStateComplete:
            playerStateString = @"SPPlayerWrapperStateComplete";
            break;
        case SPPlayerWrapperStateError:
            playerStateString = @"SPPlayerWrapperStateError";
            break;
        default:
            playerStateString = [NSString stringWithFormat:@"%lu", (unsigned long)state];
            break;
    }
    return playerStateString;
}

// NOLINTNEXTLINE
+ (NSString *)stringForPlayerWrapperEvent:(SPPlayerWrapperEvent)event {
    NSString *playerEventString = @"";
    switch (event) {
        case SPPlayerWrapperEventUnkown:
            playerEventString = @"SPPlayerWrapperEventUnkown";
            break;
        case SPPlayerWrapperEventBufferingStart:
            playerEventString = @"SPPlayerWrapperEventBufferingStart";
            break;
        case SPPlayerWrapperEventBufferingEnd:
            playerEventString = @"SPPlayerWrapperEventBufferingEnd";
            break;
        case SPPlayerWrapperEventKeyPacketRead:
            playerEventString = @"SPPlayerWrapperEventKeyPacketRead";
            break;
        case SPPlayerWrapperEventFirstClipOpened:
            playerEventString = @"SPPlayerWrapperEventFirstClipOpened";
            break;
        case SPPlayerWrapperEventFirstAudioDecoderStart:
            playerEventString = @"SPPlayerWrapperEventFirstAudioDecoderStart";
            break;
        case SPPlayerWrapperEventFirstVideoDecoderStart:
            playerEventString = @"SPPlayerWrapperEventFirstVideoDecoderStart";
            break;
        case SPPlayerWrapperEventFirstVideoFrameRendered:
            playerEventString = @"SPPlayerWrapperEventFirstVideoFrameRendered";
            break;
        case SPPlayerWrapperEventFirstAudioFrameRendered:
            playerEventString = @"SPPlayerWrapperEventFirstAudioFrameRendered";
            break;
        case SPPlayerWrapperEventFirstPacketRead:
            playerEventString = @"SPPlayerWrapperEventFirstPacketRead";
            break;
        case SPPlayerWrapperEventClipEOS:
            playerEventString = @"SPPlayerWrapperEventClipEOS";
            break;
        case SPPlayerWrapperEventSeekingStart:
            playerEventString = @"SPPlayerWrapperEventSeekingStart";
            break;
        case SPPlayerWrapperEventSeekingEnd:
            playerEventString = @"SPPlayerWrapperEventSeekingEnd";
            break;
        case SPPlayerWrapperEventSwitchDefinitionStart:
            playerEventString = @"SPPlayerWrapperEventSwitchDefinitionStart";
            break;
        case SPPlayerWrapperEventSwitchDefinitionEnd:
            playerEventString = @"SPPlayerWrapperEventSwitchDefinitionEnd";
            break;
        case SPPlayerWrapperEventReachSpecifiedHLSTag:
            playerEventString = @"SPPlayerWrapperEventReachSpecifiedHLSTag";
            break;
        case SPPlayerWrapperEventOneLoopStart:
            playerEventString = @"SPPlayerWrapperEventOneLoopStart";
            break;
        case SPPlayerWrapperEventOneLoopComplete:
            playerEventString = @"SPPlayerWrapperEventOneLoopComplete";
            break;
        case SPPlayerWrapperEventVideoPtsBigJump:
            playerEventString = @"SPPlayerWrapperEventVideoPtsBigJump";
            break;
        case SPPlayerWrapperEventAudioPtsBigJump:
            playerEventString = @"SPPlayerWrapperEventAudioPtsBigJump";
            break;
        case SPPlayerWrapperEventPlayerType:
            playerEventString = @"SPPlayerWrapperEventPlayerType";
            break;
        case SPPlayerWrapperEventAudioDecoderType:
            playerEventString = @"SPPlayerWrapperEventAudioDecoderType";
            break;
        case SPPlayerWrapperEventVideoDecoderType:
            playerEventString = @"SPPlayerWrapperEventVideoDecoderType";
            break;
        case SPPlayerWrapperEventVideoCrop:
            playerEventString = @"SPPlayerWrapperEventVideoCrop";
            break;
        case SPPlayerWrapperEventAllDownloadFinish:
            playerEventString = @"SPPlayerWrapperEventAllDownloadFinish";
            break;
        case SPPlayerWrapperEventDownloadError:
            playerEventString = @"SPPlayerWrapperEventDownloadError";
            break;
        case SPPlayerWrapperEventProxyPlayCdnUrlUpdate:
            playerEventString = @"SPPlayerWrapperEventProxyPlayCdnUrlUpdate";
            break;
        case SPPlayerWrapperEventProxyPlayCdnInfoUpdate:
            playerEventString = @"SPPlayerWrapperEventProxyPlayCdnInfoUpdate";
            break;
        case SPPlayerWrapperEventProxyDownloadStatusUpdate:
            playerEventString = @"SPPlayerWrapperEventProxyDownloadStatusUpdate";
            break;
        case SPPlayerWrapperEventProxyProtocolUpdate:
            playerEventString = @"SPPlayerWrapperEventProxyProtocolUpdate";
            break;
        case SPPlayerWrapperEventProxyDownloadProgressUpdate:
            playerEventString = @"SPPlayerWrapperEventProxyDownloadProgressUpdate";
            break;
        case SPPlayerWrapperEventProxyUrlExpire:
            playerEventString = @"SPPlayerWrapperEventProxyUrlExpire";
            break;
        case SPPlayerWrapperEventProxyNotMoreData:
            playerEventString = @"SPPlayerWrapperEventProxyNotMoreData";
            break;
        case SPPlayerWrapperEventProxyIsUseProxy:
            playerEventString = @"SPPlayerWrapperEventProxyIsUseProxy";
            break;
        default:
            playerEventString = [NSString stringWithFormat:@"%lu", (unsigned long)event];
            break;
    }
    return playerEventString;
}

+ (NSString *)stringForMediaPlayerState:(SPMediaPlayerState)state {
    NSString *mediaPlayerStateString = @"";
    switch (state) {
        case SPMediaPlayerStateUnknown:
            mediaPlayerStateString = @"SPMediaPlayerStateUnknown";
            break;
        case SPMediaPlayerStatePreparing:
            mediaPlayerStateString = @"SPMediaPlayerStatePreparing";
            break;
        case SPMediaPlayerStatePrepared:
            mediaPlayerStateString = @"SPMediaPlayerStatePrepared";
            break;
        case SPMediaPlayerStatePlaying:
            mediaPlayerStateString = @"SPMediaPlayerStatePlaying";
            break;
        case SPMediaPlayerStateUserPaused:
            mediaPlayerStateString = @"SPMediaPlayerStateUserPaused";
            break;
        case SPMediaPlayerStateInterrupt:
            mediaPlayerStateString = @"SPMediaPlayerStateInterrupt";
            break;
        case SPMediaPlayerStateStopped:
            mediaPlayerStateString = @"SPMediaPlayerStateStopped";
            break;
        case SPMediaPlayerStateComplete:
            mediaPlayerStateString = @"SPMediaPlayerStateComplete";
            break;
        case SPMediaPlayerStateError:
            mediaPlayerStateString = @"SPMediaPlayerStateError";
            break;
        default:
            mediaPlayerStateString = [NSString stringWithFormat:@"%lu", (unsigned long)state];
            break;
    }
    return mediaPlayerStateString;
}

+ (NSString *)stringForMediaPlayerEvent:(SPMediaPlayerEvent)event {
    NSString *mediaPlayerEventString = @"";
    switch (event) {
        case SPMediaPlayerEventUnkown:
            mediaPlayerEventString = @"SPMediaPlayerEventUnkown";
            break;
        case SPMediaPlayerEventBufferingStart:
            mediaPlayerEventString = @"SPMediaPlayerEventBufferingStart";
            break;
        case SPMediaPlayerEventBufferingEnd:
            mediaPlayerEventString = @"SPMediaPlayerEventBufferingEnd";
            break;
        case SPMediaPlayerEventSeekingStart:
            mediaPlayerEventString = @"SPMediaPlayerEventSeekingStart";
            break;
        case SPMediaPlayerEventSeekingEnd:
            mediaPlayerEventString = @"SPMediaPlayerEventSeekingEnd";
            break;
        case SPMediaPlayerEventSwitchDefinitionStart:
            mediaPlayerEventString = @"SPMediaPlayerEventSwitchDefinitionStart";
            break;
        case SPMediaPlayerEventSwitchDefinitionEnd:
            mediaPlayerEventString = @"SPMediaPlayerEventSwitchDefinitionEnd";
            break;
        case SPMediaPlayerEventReachHLSAdTag:
            mediaPlayerEventString = @"SPMediaPlayerEventReachHLSAdTag";
            break;
        case SPMediaPlayerEventFirstFrameRendered:
            mediaPlayerEventString = @"SPMediaPlayerEventFirstFrameRendered";
            break;
        case SPMediaPlayerEventOneLoopComplete:
            mediaPlayerEventString = @"SPMediaPlayerEventOneLoopComplete";
            break;
        default:
            mediaPlayerEventString = [NSString stringWithFormat:@"%lu", (unsigned long)event];
            break;
    }
    return mediaPlayerEventString;
}

+ (NSString *)stringOfMediaFormat:(SPMediaFormat)mediaType {
    switch (mediaType) {
        case SPMediaFormatAuto:
            return @"auto";
            break;
        case SPMediaFormatOneMp4:
            return @"OneMp4";
            break;
        case SPMediaFormatMultiMp4:
            return @"MultiMp4";
            break;
        case SPMediaFormatHLS:
            return @"HLS";
            break;
        case SPMediaFormatFLV:
            return @"FLV";
            break;
        default:
            break;
    }

    return [@(mediaType) stringValue];
}

+ (NSComparisonResult)compareDefinition:(NSString *)defn1 second:(NSString *)defn2 {
    int level1 = [SPDefinitionModel codeOfDefinitionName:defn1];
    int level2 = [SPDefinitionModel codeOfDefinitionName:defn2];
    if (level1 == level2) {
        return NSOrderedSame;
    } else if (level1 < level2) {
        return NSOrderedAscending;
    } else {
        return NSOrderedDescending;
    }
}

+ (int)nettypeForGetVInfo {
    int reportNetWorkType = 0;
    if ([SPNetworkChecker activeWLAN]) {
        reportNetWorkType = 1;
    } else {
        if (SPNetworkCheckerCellNetType2G == [SPNetworkChecker sharedInstance].cellNetType) {
            reportNetWorkType = 2;
        } else if (SPNetworkCheckerCellNetType3G == [SPNetworkChecker sharedInstance].cellNetType) {
            reportNetWorkType = 3;
        } else if (SPNetworkCheckerCellNetType4G == [SPNetworkChecker sharedInstance].cellNetType) {
            reportNetWorkType = 4;
        } else if (SPNetworkCheckerCellNetType5G == [SPNetworkChecker sharedInstance].cellNetType) {
            reportNetWorkType = 5;
        } else {
            reportNetWorkType = 0;
        }
    }

    return reportNetWorkType;
}

+ (BOOL)isEnableQuickPlayWithMediaInfo:(SPMediaInfo *)mediaInfo {
    id value = [mediaInfo.configMap objectForKey:@"enable_quick_play"];
    if (![value isKindOfClass:[NSString class]]) {
        return NO;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

+ (BOOL)isQuickPlayWithMediaInfo:(SPMediaInfo *)mediaInfo {
    id obj = [mediaInfo.extraRequestParamsMap objectForKey:@"previd"];
    return obj != nil;  // previd不为空，则为秒播
}

+ (void)removeQuickPlayInfoOfMediaInfo:(SPMediaInfo *)mediaInfo {
    NSMutableDictionary<NSString *, NSString *> *newExtraParamMap = [NSMutableDictionary dictionaryWithDictionary:mediaInfo.extraRequestParamsMap];
    [newExtraParamMap removeObjectForKey:@"previd"];
    mediaInfo.extraRequestParamsMap = newExtraParamMap;

    NSMutableDictionary<NSString *, NSString *> *newConfigMap = [NSMutableDictionary dictionaryWithDictionary:mediaInfo.configMap];
    [newConfigMap removeObjectForKey:@"history_vid"];
    mediaInfo.configMap = newConfigMap;
}

+ (NSString *)previdFromMediaInfo:(SPMediaInfo *)mediaInfo {
    id obj = [mediaInfo.extraRequestParamsMap objectForKey:@"previd"];
    if ([obj isKindOfClass:[NSString class]]) {
        return obj;
    } else {
        return nil;
    }
}

+ (NSString *)historyVidFromMediaInfo:(SPMediaInfo *)mediaInfo {
    id obj = [mediaInfo.configMap objectForKey:@"history_vid"];
    if ([obj isKindOfClass:[NSString class]]) {
        return obj;
    } else {
        return nil;
    }
}

+ (BOOL)allowAutoReduceDefinitionWithMediaInfo:(SPMediaInfo *)mediaInfo {
    id value = [mediaInfo.configMap objectForKey:@"auto_reduce_definition"];
    if (![value isKindOfClass:[NSString class]]) {
        return YES;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

+ (NSString *)stringForPlayType:(SPPlayType)playType {
    NSString *strType = nil;
    switch (playType) {
        case SPPlayTypeOnlineVod:  // 腾讯视频在线点播
            strType = @"在线点播";
            break;
        case SPPlayTypeOfflineVod:  // 腾讯视频离线点播
            strType = @"离线点播";
            break;
        case SPPlayTypeDownloadingVod:  //腾讯视频边下边播
            strType = @"p2p边下边播";
            break;
        case SPPlayTypeOnlineLive:  // 腾讯视频在线直播
            strType = @"在线直播";
            break;
        case SPPlayTypeWillDownLoadVod:  //腾讯视频原生边下载边播放
            strType = @"原生边下边播";
            break;
        case SPPlayTypeDidDownLoadVod:  //腾讯视频原生完整下载后播放
            strType = @"原生离线播放";
            break;
        case SPPlayTypeLocalFile:  // 本地文件
            strType = @"本地文件";
            break;
        case SPPlayTypeExternalUrl:  // 外部播放链接地址
            strType = @"外部链接";
            break;
        case SPPlayTypeLiveExternalUrl: // 外部播放链接地址直播
            strType = @"外部链接直播";
            break;
        default:
            strType = [NSString stringWithFormat:@"%d", (int)playType];
            break;
    }

    return strType;
}

+ (BOOL)needSkipStartAndEndWithMediaInfo:(SPMediaInfo *)mediaInfo {
    id value = [mediaInfo.configMap objectForKey:@"skip_start_and_end"];
    if (![value isKindOfClass:[NSString class]]) {
        return YES;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

+ (SPHEVCLevel)hevcLevel {
    // 自研开关打开、HEVC开关打开，才请求HEVC
//    if (!(SPSDKCONF_ENABLE_SELF_PLAYER && SPSDKCONF_ENABLE_HEVC)) {
//        return 0;
//    }
//
//    // 先取硬解能力
//    TPVCodecCapabilityForGet *capability = nil;
//    if (SPSDKCONF_VIDEO_DECODER_MODE == 0 || SPSDKCONF_VIDEO_DECODER_MODE == 1) {
//        capability = [TPCapability getVCodecDecoderMaxCapability:TPPlayerVideoDecoderVideoToolBox
//                                                         codecId:TPPlayerCodecTypeHEVC];
//        SPLOGS(SP_CGI_LOG_FILTER, @"HEVC hard decode maxLumaSamples=%d", capability.maxLumaSamples);
//    }
//
//    if (capability == nil || capability.maxLumaSamples <= 0) {
//        capability = [TPCapability getVCodecDecoderMaxCapability:TPPlayerVideoDecoderFFMpeg
//                                                         codecId:TPPlayerCodecTypeHEVC];
//        SPLOGS(SP_CGI_LOG_FILTER, @"HEVC hard soft maxLumaSamples=%d", capability.maxLumaSamples);
//    }
//
//    SPHEVCLevel supportedLevel = [SPDefinitionUtil hevcLevelFromLumaSamples:capability.maxLumaSamples];
//    int configedLevel = SPSDKCONF_HEVC_LEVEL;
//    SPLOGS(SP_CGI_LOG_FILTER, @"supportedLevel=%d, configedLevel=%d", supportedLevel, configedLevel);
//    if (configedLevel < 0) {
//        return supportedLevel;
//    }
//
//    // TODO:这里枚举和数字的转换重新考虑,hemnli
//    return (int)supportedLevel < configedLevel ? supportedLevel : (SPHEVCLevel)configedLevel;
    
    ///lowryhe 这里不太清楚要怎么修改，先注释掉所有代码，返回固定值
    return SPHEVCLevelFHD;
}

///lowryhe 这里不太清楚要怎么修改，先注释掉整个方法
//+ (int)supportH264Level {
//    TPVCodecCapabilityForGet *capability = nil;
//    if (SPSDKCONF_VIDEO_DECODER_MODE == 0) {
//        capability = [TPCapability getVCodecDecoderMaxCapability:TPPlayerVideoDecoderVideoToolBox
//                                                         codecId:TPPlayerCodecTypeH264];
//    } else {
//        capability = [TPCapability getVCodecDecoderMaxCapability:TPPlayerVideoDecoderFFMpeg
//                                                         codecId:TPPlayerCodecTypeH264];
//    }
//
//    return (int)capability.maxLevel;
//}


+ (BOOL)isLiveQAFromMediaInfo:(SPMediaInfo *)mediaInfo {
    id value = [mediaInfo.configMap objectForKey:@"live_type"];
    if (![value isKindOfClass:[NSString class]]) {
        return NO;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

+ (BOOL)isForceOnlineWithMediaInfo:(SPMediaInfo *)mediaInfo {
    id value = [mediaInfo.configMap objectForKey:@"force_online"];
    if (![value isKindOfClass:[NSString class]]) {
        return NO;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

+ (int64_t)seeBackTimeWithMediaInfo:(SPMediaInfo *)mediaInfo {
    id value = [mediaInfo.configMap objectForKey:@"see_back_time"];
    if (![value isKindOfClass:[NSString class]]) {
        return -1;
    }

    return [(NSString *)value longLongValue];
}

+ (NSString *)adaptiveTypeFromMediaInfo:(SPMediaInfo *)mediaInfo {
    //1、判断起播类型：1）正常起播、手动切换清晰度起播，2）自动适应码率引起的切换清晰度起播
    //2、判断是否需要自动适应码率

    //kSPIsSelfAdaptiveSwitchDefinitionBoolKey:
    //没有值: 正常起播；
    //0: 手动切换清晰度起播
    //1: 自动适应码率引起的切换清晰度起播

    BOOL isSelfAdative = [[mediaInfo.configMap spStringForKeySafeModel:kSPIsSelfAdaptiveSwitchDefinitionBoolKey] boolValue];

    if (isSelfAdative) {  //自动适应码率引起的切换清晰度起播
        return @"10";     //自动适应码率引起的切换清晰度起播，不使用历史带宽选择清晰度
    } else {              //正常起播、或者手动切换清晰度起播
        NSString *obj = [mediaInfo.configMap spStringForKeySafeModel:@"adaptive_type"];
        if ([obj isKindOfClass:[NSString class]] && [(NSString *)obj isEqualToString:@"1"]) {
            return @"1";  //需要自适应码率
        }
        return @"0";  //不需要自适应码率
    }
}

+ (NSString *)logTagWithPlayerSeq:(int)playerSeq playSeq:(int)playSeq {
    return [NSString stringWithFormat:@"play#%d#%d", playerSeq, playSeq];
}

+ (BOOL)isAirPlayWithMediaInfo:(SPMediaInfo *)mediaInfo {
    return [mediaInfo.configMap[@"is_airplay"] boolValue];
}

+ (void)getVt:(NSString **)vt urlIndex:(NSUInteger *)urlIndex byUrl:(NSString *)urlString mediaPlayInfo:(SPMediaPlayInfo *)mediaPlayInfo {
    __block NSString *vtString = @"0";
    *urlIndex = 0;
    if (urlString.length > 0) {
        NSURL *currentCdnUrl = [NSURL URLWithString:urlString];
        [mediaPlayInfo.sectionArray.firstObject.urlList enumerateObjectsUsingBlock:^(NSString *urlString, NSUInteger idx, BOOL *stop) {
            NSURL *url = [NSURL URLWithString:urlString];
            if ([currentCdnUrl.host isEqualToString:url.host]) {
                *urlIndex = idx;
                if (mediaPlayInfo.sectionArray.firstObject.vtList.count > idx) {
                    vtString = mediaPlayInfo.sectionArray.firstObject.vtList[idx];
                }
                *stop = YES;
            }
        }];
    }
    *vt = vtString;
}

@end
