//
//  SPPlayerWrapperInfo.m
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperInfo.h"
#import "SPPlayerUtils.h"
#import "SPPlayerWrapperHelper.h"
#import "SPLivePlayInfo.h"
#import <ThumbPlayer/TPPlayerCoreType.h>

NSString *const SPPlayerHlsTagTime = @"#EXT-X-PROGRAM-DATE-TIME";
NSString *const SPPlayerHlsTagAd = @"#EXT-QQHLS-AD";

@implementation SPSourceInfo
- (void)clean {
    self.type = SPSourceTypeUrl;
    self.url = nil;
    self.asset = nil;
    ///lowryhe 中台不再支持 videoInfo,这里将其注释掉
//    self.videoInfo = nil;
}
- (BOOL)validate {
    if (SPSourceTypeUrl == self.type) {
        return self.url != nil && ![self.url isEqualToString:@""];
    }
    
    if (SPSourceTypeAsset == self.type) {
        return self.asset != nil;
    }
    
    return YES;
}
@end

@implementation SPStreamInfo
@end

@implementation SPWrapperPlayerInfo

@end

@interface SPPlayerWrapperInfo ()
@property (nonatomic, strong) SPSourceInfo   *sourceInfo;




@end

@implementation SPPlayerWrapperInfo

-(instancetype) init{
    if (self = [super init]) {
        _playerInfo = [[SPWrapperPlayerInfo alloc] init];
        _sourceInfo = [[SPSourceInfo alloc] init];
        _streamInfo = [[SPStreamInfo alloc] init];
        _requestInfo = [[SPPlayingContext alloc] init];
    }
    return self;
}

-(BOOL)isUseThumbPlayer {
    //目前都是使用中台播放器
    return YES;
}

- (BOOL)isPreplay {
    return  self.mediaPlayInfo &&
            self.mediaPlayInfo.currentDefinition &&
            self.mediaPlayInfo.isPreWatch;
}

- (BOOL)isDRM {
    return  self.mediaPlayInfo &&
            self.mediaPlayInfo.currentDefinition &&
            self.mediaPlayInfo.currentDefinition.drm == SPDrmTypeFairPlay;
}

- (void)rebuildSourceInfo {
    ///lowryhe TPVideoInfo不再可用，注释掉 sourceInfo.videoInfo 所有相关的方法
    [self.sourceInfo clean];

    //1 : 外部url构建方式
    if (self.mediaPlayInfo.mediaInfo.playType == SPPlayTypeExternalUrl) {
        self.sourceInfo.type = SPSourceTypeUrl;
        self.sourceInfo.url  = [self.mediaPlayInfo.sectionArray firstObject].url;
//        self.sourceInfo.videoInfo = [SPPlayerWrapperHelper buildTPVideoInfoWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
        return;
    }

    //2 : 本地文件的构建方式
    if (self.mediaPlayInfo.mediaInfo.playType == SPPlayTypeLocalFile) {
        self.sourceInfo.type = SPSourceTypeUrl;
        self.sourceInfo.url  = [self.mediaPlayInfo.sectionArray firstObject].url;
//        self.sourceInfo.videoInfo = [SPPlayerWrapperHelper buildTPVideoInfoWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
        return;
    }

    //3 : vid方式，并且直播回看
    if (self.mediaPlayInfo.mediaInfo.playType == SPPlayTypeOnlineLive && self.isLivePlayBack) {
        SPLivePlayInfo *liveInfo = (SPLivePlayInfo *)self.mediaPlayInfo;
        self.sourceInfo.type = SPSourceTypeUrl;
        self.sourceInfo.url = [SPPlayerWrapperHelper buildUrlForLiveBackPlayWithUrl:liveInfo.seeBackBaseInfo.seeBackUrl
                                                                        andPosition:self.liveBackTime];
//        self.sourceInfo.videoInfo = nil;
        return;
    }

    //4 : vid方式，直播
    if (self.mediaPlayInfo.mediaInfo.playType == SPPlayTypeOnlineLive) {
        self.sourceInfo.type = SPSourceTypeUrl;
        self.sourceInfo.url = [self.mediaPlayInfo.sectionArray firstObject].url;
//        self.sourceInfo.videoInfo = [SPPlayerWrapperHelper buildTPVideoInfoWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
        return;
    }


    //5 : vid方式，点播， 分片
    if (self.mediaPlayInfo.mediaType == SPMediaFormatMultiMp4) {
        self.sourceInfo.type = SPSourceTypeAsset;
        self.sourceInfo.url = [self.mediaPlayInfo.sectionArray firstObject].url;
//        self.sourceInfo.asset = [SPPlayerWrapperHelper buildTPMediaAssetWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
//        self.sourceInfo.videoInfo = [SPPlayerWrapperHelper buildTPVideoInfoWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
        return;
    }

    //6 : url方式，直播
    if (self.mediaPlayInfo.mediaInfo.playType == SPPlayTypeLiveExternalUrl) {
        self.sourceInfo.type = SPSourceTypeUrl;
        self.sourceInfo.url = [self.mediaPlayInfo.sectionArray firstObject].url;
//        self.sourceInfo.videoInfo = [SPPlayerWrapperHelper buildTPVideoInfoWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
        return;
    }

    //7 : vid方式，点播， 整段
    self.sourceInfo.type = SPSourceTypeUrl;
    self.sourceInfo.url = [self.mediaPlayInfo.sectionArray firstObject].url;
//    self.sourceInfo.videoInfo = [SPPlayerWrapperHelper buildTPVideoInfoWithMediaInfo:self.mediaPlayInfo.mediaInfo andPlayInfo:self.mediaPlayInfo];
}

- (void)clearAll {
    [self.sourceInfo setType:SPSourceTypeUrl];
    [self.sourceInfo setUrl:nil];
    [self.sourceInfo setAsset:nil];
    ///lowryhe 中台不再支持 videoInfo,这里将其注释掉
//    [self.sourceInfo setVideoInfo:nil];
    
    [self.streamInfo setContainerFormat:nil];
    [self.streamInfo setVideoCodec:nil];
    [self.streamInfo setVideoProfile:nil];
    [self.streamInfo setCodecMimeType:nil];
    [self.streamInfo setVideoWidth:0];
    [self.streamInfo setVideoHeight:0];
    [self.streamInfo setVideoBitRate:0];
    [self.streamInfo setAudioCodec:nil];
    [self.streamInfo setAudioChannels:0];
    [self.streamInfo setAudioSampRate:0];
    [self.streamInfo setDurationMs:0];
    [self.streamInfo setVideoRotation:0];
    [self.streamInfo setInfoString:nil];
    
    [self.requestInfo setEnableHEVC:YES];
    [self.requestInfo setEnableFairPlay:YES];
    [self.requestInfo setExtraInfo:nil];
    [self.requestInfo setExtraRequestParams:nil];
    [self.requestInfo setRequiredDefinition:nil];
    [self.requestInfo setCurrentPlayPosition:0];
    
    [self.playerInfo setVideoDecoder:TPVideoDecoderTypeUnknown];
    [self.playerInfo setAudioDecoder:TPVideoDecoderTypeUnknown];
    [self.playerInfo setIsUseProxy:NO];
    [self.playerInfo setDumped:NO];
    
    [self setPosition:0];
    [self setMediaFormat:SPMediaFormatAuto];
    [self setSeekPosWhenPrepared:0];
    [self setSeekModeWhenPrepared:SPSeekModeNormal];
    [self setIsLivePlayBack:NO];
    [self setLiveBackTime:-1];
    [self setIsPipPlaying:NO];
    [self setPlayableDuration:0];
    [self setBufferStarting:NO];
    [self setIsFirstFrameNotified:NO];
}



- (void)clearWhenInnerStop {
    //清理流媒体信息
    [self.streamInfo setContainerFormat:nil];
    [self.streamInfo setVideoCodec:nil];
    [self.streamInfo setVideoProfile:nil];
    [self.streamInfo setCodecMimeType:nil];
    [self.streamInfo setVideoWidth:0];
    [self.streamInfo setVideoHeight:0];
    [self.streamInfo setVideoBitRate:0];
    [self.streamInfo setAudioCodec:nil];
    [self.streamInfo setAudioChannels:0];
    [self.streamInfo setAudioSampRate:0];
    [self.streamInfo setDurationMs:0];
    [self.streamInfo setVideoRotation:0];
    [self.streamInfo setInfoString:nil];
    
    //清理运行态的播放器信息
    [self.playerInfo setVideoDecoder:TPVideoDecoderTypeUnknown];
    [self.playerInfo setAudioDecoder:TPVideoDecoderTypeUnknown];
    [self.playerInfo setIsUseProxy:NO];
    [self.playerInfo setDumped:NO];
    
    //清理运行态维护的变量
    [self setMediaFormat:SPMediaFormatAuto];
    [self setSeekPosWhenPrepared:0];
    [self setSeekModeWhenPrepared:SPSeekModeNormal];
    [self setIsLivePlayBack:NO];
    [self setLiveBackTime:-1];
    [self setIsPipPlaying:NO];
    [self setPlayableDuration:0];
    [self setIsFirstFrameNotified:NO];
    
}
@end
