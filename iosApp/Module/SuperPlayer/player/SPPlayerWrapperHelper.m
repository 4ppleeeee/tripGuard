//
//  SPPlayerWrapperHelper.m
//  SPPlayer
//
//  Created by 郭力 on 2019/10/10.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperHelper.h"
#import "SPPlayerWrapperDefine.h"
#import "SPPlayerWrapperException.h"
#import "SPPlayerWrapperInfo.h"
#import "SPPlayerWrapperParam.h"
#import "SPPlayerWrapperStateManager.h"
#import "SPVODPlayInfo.h"
//#import "SPVideoView.h"
#import <ThumbPlayer/TPPlayerCoreType.h>
#import <ThumbPlayer/ITPMultiMediaAsset.h>
#import <ThumbPlayer/TPPlayerState.h>
#import <ThumbPlayer/TPOnInfoID.h>
/// lowryhe TPMediaComposition被替换成了ITPMultiMediaAsset，但是即使是使用ITPMultiMediaAsset也会存在莫名其妙的找不到类的问题，都先注释掉吧
//#import <ThumbPlayer/TPMediaComposition.h>
//#import <ThumbPlayer/TPMediaCompositionFactory.h>
/// lowryhe TPPlayerDefines已经不再使用了，先注释掉
//#import <ThumbPlayer/TPPlayerDefines.h>
//#import <ThumbPlayer/TPPlayerMsg.h>
//#import <ThumbPlayer/TPPlayerStrategy.h>
@implementation SPPlayerWrapperHelper

+ (NSString *)buildVideoErrorMessageWithType:(SPCGIRequestType)type module:(NSUInteger)module errCode:(NSUInteger)erroCode {
    return @"播放失败，请重试";
}

+ (BOOL)isValidMediaPlayInfo:(SPMediaPlayInfo *)playInfo {
    return playInfo != nil;
}

+ (void)fillStreamInfo:(SPStreamInfo *)streamInfo fromInfoString:(NSString *)infoString {
    if (!infoString || [infoString isEqualToString:@""]) {
        streamInfo.infoString = @"";
        return;
    }
    NSMutableDictionary<NSString * ,NSString *> *infoMap = [NSMutableDictionary dictionary];
    NSArray<NSString *> *infoArray = [infoString componentsSeparatedByString:@"\n"];
    [infoArray enumerateObjectsUsingBlock:^(NSString * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSString *string = [obj stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        
        if (string.length == 0 || [string hasPrefix:@"#"]) {
            return;
        }
        NSArray<NSString *> *kv = [string componentsSeparatedByString:@"="];
        if (kv.count != 2) {
            return;
        }
        [infoMap setObject:[kv lastObject] forKey:[kv firstObject]];
    }];
    
    streamInfo.infoString = infoString;
    streamInfo.containerFormat = infoMap[@"ContainerFormat"];
    streamInfo.videoCodec = infoMap[@"VideoCodec"];
    streamInfo.videoProfile = infoMap[@"VideoProfile"];
    streamInfo.videoWidth = [infoMap[@"Width"] intValue];
    streamInfo.videoHeight = [infoMap[@"Height"] intValue];
    streamInfo.videoBitRate = [infoMap[@"VideoBitRate"] intValue];
    
    streamInfo.audioCodec = infoMap[@"AudioCodec"];
    streamInfo.audioBitRate = [infoMap[@"AudioBitRate"] intValue];
    streamInfo.audioProfile = infoMap[@"AudioProfile"];
    streamInfo.audioChannels = [infoMap[@"Channels"] intValue];
    streamInfo.audioSampRate = [infoMap[@"SampleRate"] intValue];
}


+ (NSArray<SPSection *> *)buildSectionArrayWithUrl:(NSString *)url {
    SPSection *section = [[SPSection alloc] init];
    section.url = url;
    NSArray *sections = [[NSArray alloc] initWithObjects:section, nil];
    return sections;
}

+ (SPPlayerWrapperState)convertWrapperStateFromTPPlayerState:(TPPlayerState)tPPlayerState {
    SPPlayerWrapperState state = SPPlayerWrapperStateUnknown;
    switch (tPPlayerState) {
        case TPPlayerStateIdle:
            state = SPPlayerWrapperStateUnknown;
            break;
        case TPPlayerStatePreparing:
            state = SPPlayerWrapperStatePreparing;
            break;
        case TPPlayerStatePrepared:
            state = SPPlayerWrapperStatePrepared;
            break;
        case TPPlayerStateStarted:
            state = SPPlayerWrapperStatePlaying;
            break;
        case TPPlayerStatePaused:
            state = SPPlayerWrapperStateUserPaused;
            break;
        case TPPlayerStateComplete:
            state = SPPlayerWrapperStateComplete;
            break;
        case TPPlayerStateStopped:
            state = SPPlayerWrapperStateStopped;
            break;
        case TPPlayerStateError:
            state = SPPlayerWrapperStateError;
            break;
        default:
            break;
    }
    return state;
}

+ (NSString *)buildUrlForLiveBackPlayWithUrl:(NSString *)url andPosition:(int64_t)position {
    NSString *currentTime = [NSString stringWithFormat:@"%lld" , position];
    NSString *liveBackUrl = [SPUtils replaceUlr:url key:SP_SDK_LANG_STRING(@"wsStreamTimeABS") value:currentTime];
    return liveBackUrl;
}

+ (NSString*)buildTPDownloadFileIdWithPlayInfo:(SPMediaPlayInfo *)playInfo {
    NSMutableString* fileId = [NSMutableString stringWithString:@""];
    
    /**外部url的fileid*/
    if (playInfo.mediaInfo.playType == SPPlayTypeExternalUrl) {
        if (playInfo.mediaInfo.fileId.length > 0) {
            [fileId appendString:SPSafeString(playInfo.mediaInfo.fileId)];
        } else {
            [fileId appendString:SPSafeString([SPUtils md5ForLowerCase:playInfo.mediaInfo.url])];
        }
        return fileId;
    }
    
    /**直播fileId*/
    if (playInfo.bizType == SPMediaPlayBizTypeLive) {
        if (playInfo.mediaInfo.playType == SPPlayTypeLiveExternalUrl) {
            [fileId appendString:SPSafeString([SPUtils md5ForLowerCase:playInfo.mediaInfo.url])];
        } else {
            [fileId appendString:SPSafeString(playInfo.vid)];
        }
        return [fileId copy];
    }
    /**点播hls*/
    if (playInfo.mediaType == SPMediaFormatHLS) {
        if (((SPVODPlayInfo*)playInfo).extraInfo.keyID.length == 0) {
            [fileId appendString:SPSafeString(playInfo.vid)];
            [fileId appendString:@"."];
            [fileId appendString:SPSafeString(playInfo.currentDefinition.fileid)];
            [fileId appendString:@".hls"];
        }
        else {
            [fileId appendString:SPSafeString(((SPVODPlayInfo*)playInfo).extraInfo.keyID)];
            [fileId appendString:@".hls"];
        }
    }
    /**点播分片*/
    else if (playInfo.mediaType == SPMediaFormatMultiMp4) {
        [fileId appendString:SPSafeString(playInfo.vid)];
        [fileId appendString:@"."];
        [fileId appendString:SPSafeString(playInfo.currentDefinition.fileName)];
    }
    /**点播整段*/
    else if (playInfo.mediaType == SPMediaFormatOneMp4) {
        [fileId appendString:SPSafeString(playInfo.vid)];
        [fileId appendString:@"."];
        if (playInfo.mediaInfo.definition.length != 0) {
            [fileId appendString:SPSafeString(playInfo.mediaInfo.definition)];
        }
        else {
            [fileId appendString:SPSafeString(playInfo.currentDefinition.fileName)];
        }
        
        // 整段 mp4 试看 keyid 特殊处理，下载组件试看和正片任务 keyid 如果相同，会导致下载组件任务复用
        // 正片任务播放复用试看任务数据，导致正片任务也只能播放试看任务长度
        if (((SPVODPlayInfo *)playInfo).isPreWatch) {
            [fileId appendString:@"."];
            [fileId appendFormat:@"%f", [[NSDate date] timeIntervalSince1970]];
        }
    }
    /**点播其他*/
    else {
        [fileId appendString:playInfo.vid];
        [fileId appendString:@"."];
        [fileId appendString:SPSafeString(playInfo.currentDefinition.fileid)];
    }
    
    return fileId;
    
    
}

+ (BOOL)isAllowSeamlessSwitchDefinitionTypeForPlayInfo:(SPMediaPlayInfo *)playInfo
                                               withTag:(NSString *)tag{
// #lizard forgives
    
    //强制系统播放器配置
    BOOL isFairP = playInfo && playInfo.currentDefinition && playInfo.currentDefinition.drm == SPDrmTypeFairPlay;
    
    //强制自研播放器配置
    BOOL isHEVC = playInfo && playInfo.isHevc;
    BOOL isSR   = playInfo && playInfo.currentDefinition && playInfo.currentDefinition.sr;
    
    //目前全场景均是使用中台播放器
    BOOL isThumbPlayer = YES;
    
    //当前自研播放器 + 需要系统播放器的组合判断， 返回不允许无缝切换
    if (isThumbPlayer && isFairP) {
        SPLOGW(tag, @"switch definition : video info suc , current player is thumb player");
        SPLOGW(tag, @"switch definition : video info suc , media info is drm (fair play)");
        SPLOGW(tag, @"switch definition : video info suc , can't allow seamless switch definition , need reopen");
        return NO;
    }
    
    //当前系统播放器 + 需要自研播放器的组合判断，返回不允许无缝切换
    if (!isThumbPlayer && isHEVC) {
        SPLOGW(tag, @"switch definition : video info suc , current player is system player");
        SPLOGW(tag, @"switch definition : video info suc , media info is HEVC");
        SPLOGW(tag, @"switch definition : video info suc , can't allow seamless switch definition , need reopen");
        return NO;
    }
    if (!isThumbPlayer && isSR) {
        SPLOGW(tag, @"switch definition : video info suc , current player is system player");
        SPLOGW(tag, @"switch definition : video info suc , media info is SuperDefinition (SR)");
        SPLOGW(tag, @"switch definition : video info suc , can't allow seamless switch definition , need reopen");
        return NO;
    }
    
    //允许无缝切换
    SPLOGI(tag, @"switch definition : video info suc , allow seamless switch definition , continue seamless");
    return YES;
}


#pragma mark - log prints
+ (void)printWrapperApiCallWithTag:(NSString *)tag api:(SPPlayerWrapperAPI)api params:(id)params {
// #lizard forgives 忽略圈复杂度的检测
    switch (api) {
        case SPPlayerWrapperAPIOpen:
            SPLOGI(tag, @"api call : open media");
            break;
        case SPPlayerWrapperAPIPlay:
            SPLOGI(tag, @"api call : start");
            break;
        case SPPlayerWrapperAPIPrepare:
            SPLOGI(tag, @"api call : prepare");
            break;
        case SPPlayerWrapperAPIPause:
            SPLOGI(tag, @"api call : pause");
            break;
        case SPPlayerWrapperAPISeekTo:
            SPLOGI(tag, @"api call : seekTo %@" ,params);
            break;
        case SPPlayerWrapperAPISeekLive:
            SPLOGI(tag, @"api call : seekTo, live %@" ,params);
            break;
        case SPPlayerWrapperAPIStop:
          SPLOGI(tag, @"api call : stop");
          break;
        case SPPlayerWrapperAPISetParam:
            SPLOGI(tag, @"api call : %@", (NSString*)params);
            break;
        case SPPlayerWrapperAPIGetRunTimeInfo:
            SPLOGI(tag, @"api call : ");
            break;
        case SPPlayerWrapperAPISwitchDefinition:
            SPLOGI(tag, @"api call : switch definition %@" , (NSString*)params);
            break;
        case SPPlayerWrapperAPIRefreshPlayer:
            SPLOGI(tag, @"api call : refresh player" , (NSString*)params);
            break;
        case SPPlayerWrapperAPICaptureImage:
            SPLOGI(tag, @"api call : capture image");
            break;
        case SPPlayerWrapperAPIStartPip:
            SPLOGI(tag, @"api call : start picture in pipture");
            break;
        case SPPlayerWrapperAPIStopPip:
            SPLOGI(tag, @"api call : stop picture in picture");
            break;
        case SPPlayerWrapperAPIPauseDownload:
            SPLOGI(tag, @"api call : pause download");
            break;
        case SPPlayerWrapperAPIResumeDonwload:
            SPLOGI(tag, @"api call : resume download");
            break;
        case SPPlayerWrapperAPIRealTimeInfo:
            SPLOGI(tag, @"api call : set realtime info");
            break;
        default:
            break;
    }
}

+ (void)printWrapperCallBackWithTag:(NSString *)tag andCallBack:(SPPlayerWrapperCB)callback {
    switch (callback) {
        case SPPlayerWrapperCBOnPrepared:
            SPLOGI(tag, @"on player callback : on preapred");
            break;
        case SPPlayerWrapperCBOnCompletion:
            SPLOGI(tag, @"on player callback : on complete");
            break;
        case SPPlayerWrapperCBOnPlayerError:
            SPLOGE(tag, @"on player callback : on player error");
            break;
        case SPPlayerWrapperCBOnSeekComplete:
            SPLOGI(tag, @"on player callback : on player seek complete");
            break;
        case SPPlayerWrapperCBOnVideoSizeChange:
            SPLOGI(tag, @"on player callback : on player video size change");
            break;
        case SPPlayerWrapperCBOnData:
            //这边不要打印日志，太多了
            break;
        case SPPlayerWrapperCBOnCGISuc:
            SPLOGI(tag, @"on player callback : on player video size change");
            break;
        case SPPlayerWrapperCBOnInfo:
            SPLOGI(tag, @"on player callback : on video info got sucess");
            break;
        case SPPlayerWrapperCBOnPip:
            //独立的日志打印，此处不打印
            break;
        case SPPlayerWrapperCBOnCGIError:
            SPLOGE(tag, @"on player callback : on video info got failed");
            break;
        case SPPlayerWrapperCBOnCGIUpdate:
            SPLOGI(tag, @"on player callback : on video info update");
            break;
        case SPPlayerWrapperCBAirPlay:
            SPLOGI(tag, @"on player callback : on player air play");
            break;
        case SPPlayerWrapperCBOnStateChange:
            SPLOGI(tag, @"on player callback : on player state change");
            break;
        default:
            break;
    }
}

+ (void)printWrapperStreamInfoWithTag:(NSString *)tag andStreamInfo:(SPStreamInfo *)streamInfo {
    NSString *prefix = @"media info : ";
    SPLOGI(tag, @"%@ ==================================" , prefix);
    SPLOGI(tag, @"%@ ==================================" , prefix);
    SPLOGI(tag, @"%@ ============MediaInfo=============" , prefix);
    SPLOGI(tag, @"%@ ==================================" , prefix);
    
    if (!streamInfo.infoString || [streamInfo.infoString isEqualToString:@""]) {
        SPLOGI(tag, @"%@ obtain from system player" , prefix);
        SPLOGI(tag, @"%@ video_width  : %d" , prefix, streamInfo.videoWidth);
        SPLOGI(tag, @"%@ video_height : %d" , prefix, streamInfo.videoHeight);
        SPLOGI(tag, @"%@ duration : %llu s" , prefix , (streamInfo.durationMs / 1000));
    } else {
        SPLOGI(tag, @"%@ obtain from thumb player", prefix);
        SPLOGI(tag, @"%@ ##container", prefix);
        SPLOGI(tag, @"%@ container format : %@" , prefix , streamInfo.containerFormat);
        SPLOGI(tag, @"%@ duration : %lld s" , prefix , (streamInfo.durationMs / 1000));
        
        SPLOGI(tag, @"%@ ##video info", prefix);
        SPLOGI(tag, @"%@ video_codec  : %@" , prefix, streamInfo.videoCodec);
        SPLOGI(tag, @"%@ video_profile : %@" , prefix, streamInfo.videoProfile);
        SPLOGI(tag, @"%@ video_width  : %d" , prefix, streamInfo.videoWidth);
        SPLOGI(tag, @"%@ video_height : %d" , prefix, streamInfo.videoHeight);
        SPLOGI(tag, @"%@ video_bitrate : %llu" , prefix, streamInfo.videoBitRate);
        
        SPLOGI(tag, @"%@ ##audio info", prefix);
        SPLOGI(tag, @"%@ audio_codec  : %@" , prefix, streamInfo.audioCodec);
        SPLOGI(tag, @"%@ audio_profile : %@" , prefix, streamInfo.audioProfile);
        SPLOGI(tag, @"%@ audio_bitrate  : %llu" , prefix, streamInfo.audioBitRate);
        SPLOGI(tag, @"%@ audio_channnel : %d" , prefix, streamInfo.audioChannels);
        SPLOGI(tag, @"%@ audio_sampleRate : %llu" , prefix, streamInfo.audioSampRate);
    }
    
    SPLOGI(tag, @"%@ ==================================" , prefix);
}

+ (void)printWrapperException:(SPPlayerWrapperException *)exception withTag:(NSString *)tag{
    NSString *prefix = @"player exception : ";
    switch (exception.commonInfo.level) {
        case LevelWarning:
            SPLOGW(tag, @"%@ ============================================" , prefix);
            SPLOGW(tag, @"%@ level : warning (ps : just log or notify)" , prefix);
            SPLOGW(tag, @"%@ cause : %@" , prefix , exception.commonInfo.message);
            SPLOGW(tag, @"%@ state : %@" , prefix , exception.commonInfo.state);
            SPLOGW(tag, @"%@ ============================================" , prefix);
            break;
        case LevelFatal:
            SPLOGE(tag, @"%@ ============================================" , prefix);
            SPLOGE(tag, @"%@ level : fatal (ps : active crash app)" , prefix);
            SPLOGE(tag, @"%@ cause : %@" , prefix , exception.commonInfo.message);
            SPLOGW(tag, @"%@ state : %@" , prefix , exception.commonInfo.state);
            SPLOGW(tag, @"%@ ============================================" , prefix);
            break;
        case LevelError:
            SPLOGE(tag, @"%@ ============================================" , prefix);
            SPLOGE(tag, @"%@ level :  error (ps : need notify error to app)" , prefix);
            SPLOGE(tag, @"%@ cause : %@" , prefix , exception.commonInfo.message);
            SPLOGW(tag, @"%@ state : %@" , prefix , exception.commonInfo.state);
            SPLOGE(tag, @"%@ error.model : %d" , prefix , exception.errorInfo.model);
            SPLOGE(tag, @"%@ error.type : %d" , prefix , exception.errorInfo.type);
            SPLOGE(tag, @"%@ error.code : %d" , prefix , exception.errorInfo.code);
            SPLOGE(tag, @"%@ ============================================" , prefix);
            break;
        
        case LevelRetry:
            SPLOGE(tag, @"%@ ============================================" , prefix);
            SPLOGE(tag, @"%@ level :  retry (ps : need retry CGI or retry other player)" , prefix);
            SPLOGE(tag, @"%@ cause : %@" , prefix , exception.commonInfo.message);
            SPLOGW(tag, @"%@ state : %@" , prefix , exception.commonInfo.state);
            SPLOGE(tag, @"%@ error.model : %d" , prefix , exception.errorInfo.model);
            SPLOGE(tag, @"%@ error.type : %d" , prefix , exception.errorInfo.type);
            SPLOGE(tag, @"%@ error.code : %d" , prefix , exception.errorInfo.code);
            SPLOGE(tag, @"%@ ============================================" , prefix);
            
            if (exception.retryInfo.retryMode == RetryModeSource) {
                SPLOGE(tag, @"%@ retryInfo.enableHevc : %@" , prefix , exception.retryInfo.requestInfo.enableHEVC? @"enable" : @"disable");
                SPLOGE(tag, @"%@ retryInfo.enableDrm : %@" , prefix , exception.retryInfo.requestInfo.enableFairPlay ? @"enable" : @"disable");
                SPLOGE(tag, @"%@ retryInfo.definition : %@" , prefix , exception.retryInfo.requestInfo.requiredDefinition);
            }
            
            if (exception.retryInfo.retryMode == RetryModePlayer) {
                SPLOGE(tag, @"%@ mode : player retry can not supported currently" , prefix);
            }
            
            SPLOGE(tag, @"%@ ============================================" , prefix);
            
            break;
    }
}

+ (NSString *)stringValueForWrapperState:(SPPlayerWrapperState)state {
    switch (state) {
        case SPPlayerWrapperStateUnknown:
            return @"unknown";
            break;
        case SPPlayerWrapperStateCGIing:
            return @"CGIING";
            break;
        case SPPlayerWrapperStateCGIed:
            return @"CGIED";
            break;
        case SPPlayerWrapperStatePreparing:
            return @"Preparing";
            break;
        case SPPlayerWrapperStatePrepared:
            return @"Prepared";
            break;
        case SPPlayerWrapperStatePlaying:
            return @"Playing";
            break;
        case SPPlayerWrapperStateUserPaused:
            return @"Paused";
            break;
        case SPPlayerWrapperStateStopped:
            return @"Stopped";
            break;
        case SPPlayerWrapperStateComplete:
            return @"Complete";
            break;
        case SPPlayerWrapperStateError:
            return @"Error";
            break;
        default:
            break;
    }
}

+ (NSString *)stringValueForWrapperStage:(SPPlayerWrapperStage)stage {
    switch (stage) {
        case SPPlayerWrapperStageMain:
            return @"Main";
            break;
        case SPPlayerWrapperStageReOpenSwitchDefinition:
            return @"ReOpenSwitchDefinition";
            break;
        case SPPlayerWrapperStageSwitchDefinition:
            return @"SwitchDefinition";
            break;
        case SPPlayerWrapperStageErrorRetry:
            return @"ErrorRetry";
            break;
        case SPPlayerWrapperStageLiveBackPlay:
            return @"LiveBackPlay";
            break;
        case SPPlayerWrapperStageStartPipPlay:
            return @"PipStarting";
            break;
        case SPPlayerWrapperStageStopPipPlay:
            return @"PipStoping";
            break;
        case SPPlayerWrapperStageRefreshPlayer:
            return @"RefreshPlayer";
            break;
    }
}

+ (void)printSeekLiveBackInfo:(NSString *)tag position:(int64_t)position info:(SPPlayerWrapperInfo *)info {
    if (position != -1 && info.isLivePlayBack) {
        SPLOGI(tag, @"seek for live : position : %lld" , position);
        SPLOGI(tag, @"seek for live : current is live back play , and seek to other position , no need request video info");
        return;
    }
    
    if (position == -1 && info.isLivePlayBack) {
        SPLOGI(tag, @"seek for live : position : %lld" , position);
        SPLOGI(tag, @"seek for live : current is live back play and back to original live play, need request video info");
        return;
    }
    
    if (position != -1 && !info.isLivePlayBack) {
        SPLOGI(tag, @"seek fo live : position : %lld" , position);
        SPLOGI(tag, @"seek for live : current is original live play , first seek for live , need request video info");
        return;
    }
    
    else {
        SPLOGI(tag, @"seek fo live : position : %lld" , position);
        SPLOGI(tag, @"seek for live : current is original live play , do nothing .");
        return;
    }
}

+ (void)printWrapperParams:(SPPlayerWrapperParam *)param withTag:(NSString *)tag{
    SPLOGI(tag, @"player initial params : ***********************************");
    // stretch mode
    if (param.stretchMode == SPVideoStretchModeAspectFit) {
        SPLOGI(tag, @"player initial params : strech mode : resize aspect");
    }
    if (param.stretchMode == SPVideoStretchModeAspectFill) {
        SPLOGI(tag, @"player initial params : strech mode : resize fill");
    }
    if (param.stretchMode == SPVideoStretchModeFullScreen) {
        SPLOGI(tag, @"player initial params : strech mode : resize full");
    }
    
    // volume
    SPLOGI(tag, @"player initial params : volume : %lf" , param.volume);
    
    // play speed
    SPLOGI(tag, @"player initial params : play speed : %lf" , param.playRate);
    
    // loopback
    if (param.loopback) {
        SPLOGI(tag, @"player initial params : loop back : %@" , param.loopback ? @"YES":@"NO");
        SPLOGI(tag, @"player initial params : loop back start pos : %ll" , param.loopbackStartPosMs);
        SPLOGI(tag, @"player initial params : loop back end   pos : %ll" , param.loopbackEndPosMs);
    } else {
        SPLOGI(tag, @"player initial params : loop back : %@" , @"NO");
    }
    
    SPLOGI(tag, @"player initial params : ***********************************");
}

+ (BOOL)isReachPreivewTimeWithCurrentPosition:(NSTimeInterval)currentPositon playInfo:(SPMediaPlayInfo *)playInfo {
    if (playInfo.bizType == SPMediaPlayBizTypeVod &&
        playInfo.isPreWatch &&
        (currentPositon >= ((SPVODPlayInfo *)playInfo).vodPreViewEnd
         || currentPositon < ((SPVODPlayInfo *)playInfo).vodPreviewStart)) {
            return YES;
    }
    
    return NO;
}

+ (BOOL)isCorrectPlayInfo:(SPMediaPlayInfo *)playInfo withLastRequestInfo:(SPPlayingContext *)requestInfo withTag:(NSString *)tag {
    if (!playInfo) {
        return YES;
    }
    
    if (!requestInfo) {
        return YES;
    }
        
    BOOL isH265  = playInfo.isHevc;
    BOOL isDRM   = playInfo.currentDefinition.drm == SPDrmTypeFairPlay;
    
    SPLOGI(tag, @"retry info check : ============================================");
    SPLOGI(tag, @"retry info check : retry request is enable hevc  : %@ , retry play info is hevc  : %@" , requestInfo.enableHEVC          ? @"yes" : @"no" ,isH265  ? @"yes" : @"no");
    SPLOGI(tag, @"retry info check : retry request is enable drm   : %@ , retry play info is drm   : %@" , requestInfo.enableFairPlay      ? @"yes" : @"no" ,isDRM   ? @"yes" : @"no");

    
    if (isH265 && !requestInfo.enableHEVC) {
        SPLOGI(tag, @"retry info check : retry request info not match retry play info");
        SPLOGI(tag, @"retry info check : failed , interrupt retry , call error to app");
        SPLOGI(tag, @"retry info check : ============================================");
        return NO;
    }
    
    if (isDRM && !requestInfo.enableFairPlay) {
        SPLOGI(tag, @"retry info check : retry request info not match retry play info");
        SPLOGI(tag, @"retry info check : failed , interrupt retry , call error to app");
        SPLOGI(tag, @"retry info check : ============================================");
        return NO;
    }
    
    SPLOGI(tag, @"retry info check : retry request info match retry play info ");
    SPLOGI(tag, @"retry info check : sucess , continue retry");
    SPLOGI(tag, @"retry info check : ============================================");
    return YES;
}

@end
