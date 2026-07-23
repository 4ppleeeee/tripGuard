//
//  SPPlayerWrapperInfo.h
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPMediaPlayInfo.h"
#import "SPPlayerMediaSource.h"
#import "SPPlayingContext.h"
#import <Foundation/Foundation.h>
#import <ThumbPlayer/TPPlayerCoreType.h>
#import <ThumbPlayer/TPVideoDecoderType.h>
#import <ThumbPlayer/TPDownloadProgressInfo.h>
#import <ThumbPlayer/ITPMediaAsset.h>

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXTERN NSString *const SPPlayerHlsTagTime;
FOUNDATION_EXTERN NSString *const SPPlayerHlsTagAd;

//地址构造的类型
typedef NS_ENUM(NSUInteger, SPSourceType) {
    SPSourceTypeUrl,      //单url类型
    SPSourceTypeAsset,    //多url组合类型
};

@interface SPSourceInfo : NSObject
@property (nonatomic, assign)           SPSourceType       type;       //正片播放地址类型
@property (nonatomic, strong, nullable) NSString            *url;       //正片播放地址 (SPSourceTypeUrl)
@property (nonatomic, strong, nullable) id<ITPMediaAsset>   asset;      //正片播放地址 (SPSourceTypeAsset)
//@property (nonatomic, strong, nullable) TPVideoInfo         *videoInfo; //正片videoinfo信息(用于P2P）
- (void)  clean;
- (BOOL) validate;
@end

//播放器信息封装(包含是否使用p2p)
@interface SPWrapperPlayerInfo : NSObject
@property (nonatomic, assign) TPVideoDecoderType videoDecoder; //记录视频解码器的类型
@property (nonatomic, assign) TPVideoDecoderType audioDecoder; //记录音频解码器的类型
@property (nonatomic, assign) BOOL  isUseProxy; //记录是否使用p2p
@property (nonatomic, assign) BOOL  dumped;//用于控制打印频率，换源后会重置
@end

//视频源信息封装
@interface SPStreamInfo : NSObject
@property (nonatomic, strong, nullable) NSString      *containerFormat;   //流媒体封装格式
@property (nonatomic, strong, nullable) NSString      *videoCodec;        //视频编码格式
@property (nonatomic, strong, nullable) NSString      *videoProfile;      //视频profile
@property (nonatomic, strong, nullable) NSString      *codecMimeType;     //codecMimeType
@property (nonatomic, assign)           NSUInteger    videoWidth;          //视频画面宽
@property (nonatomic, assign)           NSUInteger    videoHeight;        //视频画面高
@property (nonatomic, assign)           long          videoBitRate;       //视频码率
@property (nonatomic, strong, nullable) NSString      *audioCodec;        //音频编码格式
@property (nonatomic, assign)           long          audioBitRate;       //音频码率
@property (nonatomic, strong)           NSString      *audioProfile;      //音频profile
@property (nonatomic, assign)           int           audioChannels;      //音频channel
@property (nonatomic, assign)           long          audioSampRate;      //音频采样率
@property (nonatomic, assign)           int64_t       durationMs;         //流媒体总时长
@property (nonatomic, assign)           int           videoRotation;      //视频画面的rotation
@property (nonatomic, strong, nullable) NSString      *infoString;        //文本描述
@end

//wrapper层的运行时封装
@interface SPPlayerWrapperInfo : NSObject
@property (nonatomic, strong, readonly) SPSourceInfo   *sourceInfo;            //url数据结构
@property (nonatomic, strong, readonly) SPWrapperPlayerInfo   *playerInfo;     //播放器使用信息
@property (nonatomic, strong) SPStreamInfo             *streamInfo;            //多媒体参数
@property (nonatomic, strong) SPMediaPlayInfo          *mediaPlayInfo;         //视频源的网络媒体信息
@property (nonatomic, strong) SPPlayingContext         *requestInfo;           //当前应用的CGI请求信息
@property (nonatomic, strong) TPDownloadProgressInfo   *downloadInfo;          //下载组件返回的下载进度信息
@property (nonatomic, assign) int64_t                   position;               //最后的播放器位置
@property (nonatomic, assign) SPMediaFormat             mediaFormat;            //当前的视频格式
@property (nonatomic, assign) int64_t                   seekPosWhenPrepared;    //prepared之前记录的seek位置
@property (nonatomic, assign) SPSeekMode                seekModeWhenPrepared;   //prepared之前记录的seek模式
@property (nonatomic, assign) BOOL                      isDRM;                  //当前播放的源是否是DRM加密
@property (nonatomic, assign) BOOL                      isH265;                 //当前播放的源是否是HEVC格式
@property (nonatomic, assign) BOOL                      isPreplay;              //当前播放的源是否是限播视频
@property (nonatomic, assign) BOOL                      isUseThumbPlayer;       //记录是否使用自研播放器
@property (nonatomic, assign) BOOL                      isLivePlayBack;         //当前播放的模式是否是直播回看
@property (nonatomic, assign) int64_t                   liveBackTime;           //当前直播回看的位置信息记录
@property (nonatomic, assign) BOOL                      isPipPlaying;           //当前是否处于画中画播放
@property (nonatomic, assign) int64_t                   downloadDuration;       //记录最后回调过来的下载时长
@property (nonatomic, assign) int64_t                   playableDuration;       //最后记录的可播放的时长
@property (nonatomic, assign) BOOL                      bufferStarting;         //记录运行过程中缓冲开始
@property (nonatomic, assign) BOOL                      isFirstFrameNotified;   //记录该事件是否通知


- (void)rebuildSourceInfo;

- (void)clearAll;

- (void)clearWhenInnerStop;
@end


NS_ASSUME_NONNULL_END
