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
 * 错误码userInfo字段定义，在对外的错误码我们统一使用该格式。
 * @discussion 我们使用NSError的code属性携带错误码，而NSError的userInfo携带modelId, errMsg, exErrorCode(扩展错误码), exErrMsg,
 *             errMsg可能为空。只有个别错误码有exErrorCode和exErrMsg，
 * @example 错误码为1380，modelId为20101，扩展错误码为3
 *          code:1380
 *          userInfo[@"modelId"]: 20101
 *          userInfo[@"errMsg"]: @"xxx"
 *          userInfo[@"exErrorCode"]: 3
 *          userInfo[@"exErrorMsg"]: @"yyy"
 *          userInfo[@"exObj"]: some extra info when necessary
 */
static NSString *const kSPErrorModelIdKey = @"modelId";

static NSString *const kSPErrorMessageKey = @"errMsg";

static NSString *const kSPExErrorCodeKey = @"exErrorCode";

static NSString *const kSPExErrorMsgKey = @"exErrMsg";

static NSString *const kSPExErrorDataKey = @"data";

extern NSString *const kSPVideoSwitchDefinitionTypeKey;

/**
 * 事件信息等返回时，携带的extraInfo字典的相关key值定义
 *
 */
extern NSString *const kSPSelfAdaptiveSwitchDefinitionTypeKey;  //自动切换的清晰度key
extern NSString *const kSPIsSelfAdaptiveSwitchDefinitionBoolKey;
extern NSString *const kSPMediaInfoKey;
extern NSString *const kSPNextMediaInfoKey;
extern NSString *const kSPNetVideoInfoKey;
extern NSString *const kSPPlayerErrorKey;
extern NSString *const kSPPlayerIsSucessKey;

/**
 *  播放类型
 */
typedef enum {
    SPPlayTypeOnlineVod,        // 腾讯视频在线点播
    SPPlayTypeOfflineVod,       // 腾讯视频离线点播
    SPPlayTypeDownloadingVod,   // 腾讯视频边下边播
    SPPlayTypeOnlineLive,       // 腾讯视频在线直播
    SPPlayTypeWillDownLoadVod,  // 腾讯视频原生边下载边播放
    SPPlayTypeDidDownLoadVod,   // 腾讯视频原生完整下载后播放
    SPPlayTypeLocalFile,        // 本地文件
    SPPlayTypeExternalUrl,      // 外部播放链接地址
    SPPlayTypeLiveExternalUrl,  // 外部播放链接直播
} SPPlayType;

/**
 *  登录类型
 */
typedef enum {
    SPLoginTypeNone,  // 未登录
    SPLoginTypeQQ,    // 主登录态为qq登录
    SPLoginTypeWx     // 主登录态为微信登录
} SPLoginType;

/**
 *  会员类型
 */
typedef enum {
    SPVipTypeNotLogin = 0,       //未登录
    SPVipTypeLogin = 1,          //登录的普通用户
    SPVipTypeTencentVideo = 2,   // 腾讯视频会员（好莱坞会员）
    SPVipTypeSupplementCard = 3  // 腾讯视频会员附属卡
} SPVipType;

/**
 *  流类型
 */
typedef enum {
    SPMediaFormatAuto,     // 自动
    SPMediaFormatMultiMp4, // 分片MP4地址
    SPMediaFormatOneMp4,   // 整片MP4地址
    SPMediaFormatHLS,      // HLS
    SPMediaFormatFLV,      // FLV
    SPMediaFormatRTMP,     // RTMP
} SPMediaFormat;

/**
 *  播放器功能类型
 */
typedef enum {
    SPMediaPlayerFunctionNone = 0,                                      //无意义
    SPMediaPlayerFunctionContinuePlayOnlineForWillDownLoadVod = 1 << 0  // 腾讯视频原生边下边播，播放完离线视频后，是否转到在线播放功能。默认是关闭
} SPMediaPlayerFunction;


/**
 *  视频清晰度
 *  注:后台会根据platform进行限制，限制放开前只返回高清mp4，默认不放开
 */
static NSString *const kSPMediaDefinitionAuto = @"auto";    // 自动，由后台决定返回的清晰度（分段MP4或HLS）
static NSString *const kSPMediaDefinitionAudio = @"audio";  // 纯音频
static NSString *const kSPMediaDefinitionMSD = @"msd";      // 流畅（分段MP4或HLS）
static NSString *const kSPMediaDefinitionSD = @"sd";        // 标清（分段MP4或HLS）
static NSString *const kSPMediaDefinitionHD = @"hd";        // 高清（分段MP4或HLS）
static NSString *const kSPMediaDefinitionSHD = @"shd";      // 超清（分段MP4或HLS）
static NSString *const kSPMediaDefinitionFHD = @"fhd";      // 全高清
static NSString *const kSPMediaDefinitionUHD = @"uhd";      // 超高清
static NSString *const kSPMediaDefinitionHDR10 = @"hdr10";  // 全高清
static NSString *const kSPMediaDefinitionDOLBY = @"dolby";  // 杜比

/**
 *  播放器播放视频时的状态
 */
typedef enum {
    SPMediaPlayerStateUnknown = 0,  // 初始状态
    SPMediaPlayerStatePreparing,    // 正片获取信息中
    SPMediaPlayerStatePrepared,     // 正片播放准备完毕
    SPMediaPlayerStatePlaying,      // 正片播放中
    SPMediaPlayerStateUserPaused,   // 正片播放用户行为导致暂停
    SPMediaPlayerStateInterrupt,    // 正片播放中断
    SPMediaPlayerStateStopped,      // 正片播放停止(用户主动调用停止后，进入此状态)
    SPMediaPlayerStateComplete,     // 正片播放完毕
    SPMediaPlayerStateError,        // 正片播放失败
} SPMediaPlayerState;

/**
 * 播放器播放视频时的发生的事件信息。例如缓冲信息，发生缓冲时，app可以进行UI提示。
 * 个别事件抛出时会携带额外参数，详情请见下面的注释
 */
typedef enum {
    SPMediaPlayerEventUnkown = 0,          // 初始状态
    SPMediaPlayerEventFirstFrameRendered,  // 首帧渲染
    SPMediaPlayerEventBufferingStart,      // 播放器发生缓冲.
    SPMediaPlayerEventBufferingEnd,        // 播放器缓冲结束.
    SPMediaPlayerEventSeekingStart,        // 播放器开始处理seek操作
    SPMediaPlayerEventSeekingEnd,          // 播放器处理seek结束

    /**
     * 切换清晰度开始. 事件中的额外参数，用于是否要指定无缝切换等.为nil时，表示进行非无缝切换.字典内容格式：
     * key1: kSPVideoSwitchDefinitionTypeKey 切换清晰度的类型，直接使用SPPlayerDefine.h中的kSPVideoSwitchDefinitionTypeKey定义即可。
     * value1: SPVideoSwitchDefinitionType列举值的NSNumber类型
     */
    SPMediaPlayerEventSwitchDefinitionStart,

    /**
     * 切换清晰度结束. 事件中的额外参数，用于是否要指定无缝切换等.为nil时，表示进行非无缝切换.字典内容格式：
     * key1: kSPVideoSwitchDefinitionTypeKey 切换清晰度的类型，直接使用SPPlayerDefine.h中的kSPVideoSwitchDefinitionTypeKey定义即可。
     * value1: SPVideoSwitchDefinitionType列举值的NSNumber类型
     */
    SPMediaPlayerEventSwitchDefinitionEnd,

    SPMediaPlayerEventReachHLSAdTag,  // reach the #EXT-QQHLS-AD tag when playing live hls
    
    SPMediaPlayerEventOneLoopComplete,  // 一次循环播放结束，只有设置了循环播的时候会抛这个事件
    SPMediaPlayerEventRefreshPlayerStart,     // refreshPlayer开始
    SPMediaPlayerEventRefreshPlayerEnd,    // refreshPlayer结束
    
    SPMediaPlayerEventDownloadProgressUpdate, // 视频文件下载进度更新
    SPMediaPlayerEventAllDownloadFinish,    // 视频文件下载完成
    SPMediaPlayerEventDownloadError,    // 视频文件下载失败
} SPMediaPlayerEvent;

/**
 *  拉伸模式
 */
typedef NS_ENUM(NSUInteger, SPVideoStretchMode) {
    SPVideoStretchModeAspectFit = 0,   // 按原始比例缩放，适配videoView大小，未铺满部分添加黑边
    SPVideoStretchModeAspectFill = 1,  // 按原始比例缩放，视频内容铺满videoView，会有一部分在边界之外
    SPVideoStretchModeFullScreen = 2,  // 不按比例缩放，视频内容铺满videoview，某个方向可能会拉伸，但不会超出videoView边界
};

/**
 *  无缝切换清晰度形式
 */
typedef NS_ENUM(NSUInteger, SPVideoSwitchDefinitionType) {
    SPVideoSwitchDefinitionTypeNormal = 0,    // 正常的传统切换清晰度
    SPVideoSwitchDefinitionTypeSeamless = 1,  // 无缝切换清晰度
};

/**
 *  seek Mode
 */
typedef NS_ENUM(NSUInteger, SPSeekMode) {
    SPSeekModeNormal = 0,        // 普通seek，seek后播放位置可能与指定位置有偏差
    SPSeekModeAccuratePosition,  // 精确seek，seek后播放位置准确但可能起播更耗时
};

/**
 * 直播信息cgi返回的错误码
 */
typedef NS_ENUM(NSUInteger, SPLiveMediaPlayError) {
    SPLiveMediaPlayErrorOK = 0,                        // 数据正确返回
    SPLiveMediaPlayErrorNeedToWait = 139,              // 需要排队
    SPLiveMediaPlayErrorAuthFailedInPay = 1323,        // 请求付费鉴权模块失败（网络超时或解析出错）提示试看已结束购买
    SPLiveMediaPlayErrorLostLoginInfo = 1325,          // 无用户登录信息（登录态cookie缺少必填字段） 提示登录 试看
    SPLiveMediaPlayErrorLoginInfoVerifyFailed = 1328,  // 请求登录验证模块失败（网络超时或解析出错）提示登录 试看
    SPLiveMediaPlayErrorNoPay = 1330,                  // 当前节目未付费 提示直接购买
    SPLiveMediaPlayErrorNoLogin = 1331,                // 用户未登录 提示登录 试看
    SPLiveMediaPlayErrorCKEYVerifyFailed = 1332,       // CKEY验证失败
    SPLiveMediaPlayErrorTryWatchChanceUsed = 1345,     // 试看次数达到上限 提示试看已结束购买
    SPLiveMediaPlayErrorGetPreviewCountFailed = 1347,  // 试看计数失败（网络错误） 重试
    SPLiveMediaPlayErrorWeixinVerifyFailed = 1348,     // 微信登录验证失败（网络错误） 试看
    SPLiveMediaPlayErrorWeixinVerifyTimeOut = 1349,    // 微信登录验证超时
    SPLiveMediaPlayErrorGetPreviewInfoFailed = 1350    // 获取试看信息失败（网络错误） 重试
};

/**
 * 免流状态 freeflowType
 */
typedef NS_ENUM(NSUInteger, SPFreeFlowType) {
    SPFreeFlowTypeNone = 0,     // 不免流
    SPFreeFlowTypeUnicom = 1,   // 联通免流
    SPFreeFlowTypeMobile = 2,   // 移动免流
    SPFreeFlowTypeTelecom = 3,  // 电信免流
};

/**
 *  实时信息的key定义
 */
typedef NS_ENUM(NSUInteger, SPRealTimeInfoKey) {
    SPRealTimeInfoKeySkipPos = 1,                 // 跳过片头片尾的时间
    SPRealTimeInfoKeyBackgroundAudioPlay = 2,     // 用来控制音频后台播放的打开和关闭，音频后台播放是指虽然不是audio清晰度，切到后台时依然播放声音。
    SPRealTimeInfoKeyEnableSetNextMediaInfo = 3,  // 用来控制是否可以在播放当前视频时，同时设置下一个视频，在播放完毕当前视频，可以无缝切换到另一个视频。
};

typedef NS_ENUM(NSUInteger, SPSetNextMediaInfoSupportLevel) {
    SPSetNextMediaInfoSupportLevelUnknow = 0,               // 未知，一般是当前播放没有起播，无法判断是否支持
    SPSetNextMediaInfoSupportLevelSupport = 1,              // 当前播放支持设置下一个视频
    SPSetNextMediaInfoSupportLevelDeviceUnsupport = 2,      // 当前设备不支持,调用setNextMediaInfo无法生效，需要更好的设备
    SPSetNextMediaInfoSupportLevelVideoUnsupport = 3,       // 当前片源不支持,调用setNextMediaInfo无法生效。后台的片源是fairplay加密等原因导致
    SPSetNextMediaInfoSupportLevelDefinitionUnsupport = 4,  // 当前清晰度不支持，可以考虑切换到更低的清晰度，比如1080P(含)之下的清晰度
};

extern NSString *const kSPSkipStartPosKey;
extern NSString *const kSPSkipEndPosKey;
