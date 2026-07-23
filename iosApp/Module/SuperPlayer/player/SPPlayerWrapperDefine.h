/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPPlayerDefine.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17-1-19
 Description :
 History     : 17-1-19 初始版本
 ***********************************************************/

/**
 *  播放器播放视频时的状态
 */
typedef enum {
    SPPlayerWrapperStateUnknown = 0,  // 初始状态
    SPPlayerWrapperStateCGIing,       // 正片换取地址中
    SPPlayerWrapperStateCGIed,        // 正片换取地址完成
    SPPlayerWrapperStatePreparing,    // 正片获取信息中
    SPPlayerWrapperStatePrepared,     // 正片播放准备完毕
    SPPlayerWrapperStatePlaying,      // 正片播放中
    SPPlayerWrapperStateUserPaused,   // 正片播放用户行为导致暂停
    SPPlayerWrapperStateStopped,      // 正片播放停止(用户主动调用停止后，进入此状态)
    SPPlayerWrapperStateComplete,     // 正片播放完毕
    SPPlayerWrapperStateError,        // 正片播放失败
} SPPlayerWrapperState;

/**
 * 播放器播放视频的特殊场景
 */
typedef NS_ENUM(NSUInteger, SPPlayerWrapperScene) {
    SPPlayerWrapperNormalPlay  = 0, //无特殊场景
    SPPlayerWrapperSceneAirPaly,    //airplay场景
    SPPlayerWrapperScenePip,        //画中画场景
};

/**
 * 播放器播放视频时的发生的事件信息。例如缓冲信息，发生缓冲时，app可以进行UI提示。
 * 个别事件抛出时会携带额外参数，详情请见下面的注释
 */
typedef NS_ENUM(NSUInteger, SPPlayerWrapperEvent) {
    SPPlayerWrapperEventUnkown = 0,                // 初始状态
    SPPlayerWrapperEventBufferingStart,            // 播放器发生缓冲.
    SPPlayerWrapperEventBufferingEnd,              // 播放器缓冲结束.
    
    SPPlayerWrapperEventKeyPacketRead,
    SPPlayerWrapperEventFirstClipOpened,
    SPPlayerWrapperEventFirstAudioDecoderStart,
    SPPlayerWrapperEventFirstVideoDecoderStart,
    SPPlayerWrapperEventFirstVideoFrameRendered,   // 首帧渲染
    SPPlayerWrapperEventFirstAudioFrameRendered,
    SPPlayerWrapperEventFirstPacketRead,
    SPPlayerWrapperEventClipEOS,
    
    SPPlayerWrapperEventSeekingStart,        // 播放器开始处理seek操作
    SPPlayerWrapperEventSeekingEnd,          // 播放器处理seek结束
    
    SPPlayerWrapperEventCGIRequest,        //CGI开始事件
    SPPlayerWrapperEventCGIResponse,
    
    SPPlayerWrapperEventSwitchDefinitionStart,  // 切换清晰度开始. 事件中的额外参数类型为SPMediaPlayerInfoSwitchDefinitionStartParam
    SPPlayerWrapperEventSwitchDefinitionSetToPlayer,  //切换清晰度过程中，设置地址给播放时的事件通知
    SPPlayerWrapperEventSwitchDefinitionEnd,  // 切换清晰度结束. 事件中的额外参数为SPMediaPlayerInfoSwitchDefinitionEndParam类型参数

    SPPlayerWrapperEventReachSpecifiedHLSTag,  // reach the 指定 m3u8 tag when playing hls
    
    SPPlayerWrapperEventOneLoopStart,
    SPPlayerWrapperEventOneLoopComplete,        // 一次循环播放结束，只有设置了循环播的时候会抛这个事件
    
    SPPlayerWrapperEventVideoPtsBigJump,        // 视频时间戳发生较大跳变
    SPPlayerWrapperEventAudioPtsBigJump,        // 音频时间戳发生较大跳变
    
    SPPlayerWrapperEventPlayerType,
    SPPlayerWrapperEventAudioDecoderType,
    SPPlayerWrapperEventVideoDecoderType,
    SPPlayerWrapperEventPostProcessEffectType,
    
    SPPlayerWrapperEventVideoCrop,
    SPPlayerWrapperEventAllDownloadFinish,
    SPPlayerWrapperEventDownloadError,
    
    SPPlayerWrapperVideoViewSizeChange,
    
    SPPlayerWrapperEventProxyPlayCdnUrlUpdate, //CDN 地址变动更新开始（包含起播时的主地址）. 携带参数NSString, 表示cdn地址
    SPPlayerWrapperEventProxyPlayCdnInfoUpdate, //CDN 地址变动更新结束（包含起播时的主地址). 携带参数TPCdnUrlInfo, 表示cdn信息
    SPPlayerWrapperEventProxyDownloadStatusUpdate, //下载组件下载数据时下载时的状态通知，用于缓冲上报. 携带参数为Number, 可转换为int类型，表示当前下载状态，缓冲的原因
    SPPlayerWrapperEventProxyProtocolUpdate,
    SPPlayerWrapperEventProxyDownloadProgressUpdate, // 携带参数TPDownloadProgressInfo
    SPPlayerWrapperEventProxyUrlExpire,
    SPPlayerWrapperEventProxyNotMoreData,
    SPPlayerWrapperEventProxyIsUseProxy,
    SPPlayerWrapperEventRefreshPlayerStart,
    SPPlayerWrapperEventRefreshPlayerEnd,
};

//阶段状态机，便于记录一些中断的中间状态
typedef NS_ENUM(NSUInteger, SPPlayerWrapperStage) {
    SPPlayerWrapperStageMain = 0,                  //正片播放
    SPPlayerWrapperStageReOpenSwitchDefinition,    //重开切换清晰度
    SPPlayerWrapperStageSwitchDefinition,          //无缝切换清晰度
    SPPlayerWrapperStageErrorRetry,                //错误重试
    SPPlayerWrapperStageLiveBackPlay,              //直播回看
    SPPlayerWrapperStageStartPipPlay,              //启动画中画
    SPPlayerWrapperStageStopPipPlay,               //关闭画中画阶段
    SPPlayerWrapperStageRefreshPlayer,             //刷新播放器
};

//枚举出wrapper层的所以api，用于api调用的状态机过滤
typedef NS_ENUM(NSUInteger, SPPlayerWrapperAPI) {
    SPPlayerWrapperAPIOpen = 0,
    SPPlayerWrapperAPIPlay,
    SPPlayerWrapperAPIPrepare,
    SPPlayerWrapperAPIPause,
    SPPlayerWrapperAPISeekTo,
    SPPlayerWrapperAPISeekLive,
    SPPlayerWrapperAPIStop,
    SPPlayerWrapperAPISetParam,
    SPPlayerWrapperAPIGetRunTimeInfo,
    SPPlayerWrapperAPISwitchDefinition,
    SPPlayerWrapperAPIRefreshPlayer,
    SPPlayerWrapperAPICaptureImage,
    SPPlayerWrapperAPIStartPip,
    SPPlayerWrapperAPIStopPip,
    SPPlayerWrapperAPIPauseDownload,
    SPPlayerWrapperAPIResumeDonwload,
    SPPlayerWrapperAPIRealTimeInfo,
    SPPlayerWrapperAPIGetRunTimeInfoFromStartPosition,
};

typedef NS_ENUM(NSUInteger, SPPlayerWrapperCB) {
  SPPlayerWrapperCBOnPrepared = 0,
  SPPlayerWrapperCBOnCompletion,
  SPPlayerWrapperCBOnPlayerError,
  SPPlayerWrapperCBOnSeekComplete,
  SPPlayerWrapperCBOnVideoSizeChange,
  SPPlayerWrapperCBOnData,
  SPPlayerWrapperCBOnCGISuc,
  SPPlayerWrapperCBOnInfo,
  SPPlayerWrapperCBOnPip,
  SPPlayerWrapperCBOnCGIError,
  SPPlayerWrapperCBOnCGIUpdate,
  SPPlayerWrapperCBAirPlay,
  SPPlayerWrapperCBOnStateChange,
};
