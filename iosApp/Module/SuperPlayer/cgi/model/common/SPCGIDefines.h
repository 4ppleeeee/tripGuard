/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPCGIDefines.h
 * @brief    CGI公共类型生声明
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>

// 视频下载类型，熟知跟后台一致
typedef NS_ENUM(NSUInteger, SPMediaDLType) {
    SPMediaDLTypeAuto      = 0,
    SPMediaDLTypeHttp      = 1,
    SPMediaDLTypeP2P       = 2,
    SPMediaDLTypeHLS       = 3,
    SPMediaDLTypeHLSM3U8   = 8,  // M3U8直出
};

static const int kDefinitionSrcAutoReduce = 10;  // 自动降清晰度
static const int kDefinitionSrcCastAuto   = 5;   // 投射默认清晰度

typedef NS_ENUM(NSUInteger, SPMediaPlayBizType) {
    SPMediaPlayBizTypeVod,
    SPMediaPlayBizTypeLive,
};

/**
 点播编码类型
 */
typedef NS_ENUM(NSInteger, SPVODEncodeType) {
    SPVODEncodeTypeDefault    = 0,  //普通视频、普通音频
    SPVODEncodeTypeDolbyAudio = 1,  //普通视频、dolby音频
};

typedef NS_ENUM(NSUInteger, SPVideoType) {
    SPVideoTypeH264        = 1,
    SPVideoTypeH265        = 2,
    SPVideoTypeHDR10       = 3,
    SPVideoTypeDolbyVision = 4,
    SPVideoTypeAudio       = 5,  //纯音频码流
    SPVideoTypeSDRPlus     = 6,  // SDR+
    SPVideoTypeSDR         = 7,  // SDR
};

typedef NS_ENUM(NSUInteger, SPVODAudioType) {
    SPVODAudioTypeAAC = 1,
    SPVODAudioTypeDolbySurround,
    SPVODAudioTypeDolbyAtmos,
    SPVODAudioTypeDolbyTwo
};

typedef NS_ENUM(NSUInteger, SPLiveAudioType) {
    SPLiveAudioTypeAAC   = 1,
    SPLiveAudioTypeDolby = 2,
};

typedef NS_ENUM(NSUInteger, SPVODVideoState) {
    SPVODVideoStatePlayable   = 2,  // 只有等于2的时候为可播状态
    SPVODVideoStateNeedCharge = 8,  // 等于8的时候为收费状态，是试看
};

typedef NS_ENUM(NSUInteger, SPLimitType) {
    SPLimitTypePiracy      = 1,  // 防盗链限制
    SPLimitTypeOuter       = 2,  // 站外限时
    SPLimitTypeDefnPreview = 3,  // 清晰度试看
};

typedef NS_ENUM(NSUInteger, SPDrmType) {
    SPDrmTypeNone     = 0,
    SPDrmTypeSelfEnc  = 3, //自研加密，chacha20
    SPDrmTypeFairPlay = 4
};

/*
 * 支持的HEVC能力值
 */
typedef NS_ENUM(NSUInteger, SPHEVCLevel) {
    SPHEVCLevelNone = 0,
    SPHEVCLevelSD   = 11,
    SPHEVCLevelHD   = 16,
    SPHEVCLevelSHD  = 21,
    SPHEVCLevelFHD  = 26,
    SPHEVCLevelUHD  = 33, // Android SP端有个伪4K，对应能力值是28，iOS端不用，直接用33
};

/**
 * 支持的视频能力
 */
typedef NS_ENUM(NSUInteger, SPVideoCapability) {
    SPVideoCapabilityHDR10            = 0x4,    // 支持HDR10
    SPVideoCapabilityDolbyVision      = 0x8,    // 支持FFMP4 DolbyVision（旧属性，等同于SPVideoCapabilityFFMP4DolbyVision，请用下面新定义的枚举值）
    SPVideoCapabilityFFMP4DolbyVision = 0x10,   // 支持FFMP4的Dolby Vision
    SPVideoCapabilitySDRPlus          = 0x20,   // 支持SDR+
    SPVideoCapabilityTSDolbyVision    = 0x40,   // 支持TS分片的Dolby Vision
    SPVideoCapabilityHDR10Enhance     = 0x80,   // 支持HDR10增强
    SPVideoCapabilityIMAX             = 0x100,  // 支持IMAX
};

/**
 * 支持的音频能力
 */
typedef NS_ENUM(NSUInteger, SPAudioCapability) {
    SPAudioCapabilityAudioOnly     = 0x1,   // 支持HDR10
    SPAudioCapabilityDolbySurround = 0x2,   // 支持支持杜比环绕声
    SPAudioCapabilityDolbyAtoms    = 0x4,   // 支持FFMP4的Dolby Vision
    SPAudioCapabilityDolby2_0      = 0x8,   // 支持SDR+
    SPAudioCapabilityDTSHD         = 0x10,  // 支持TS分片的Dolby Vision
    SPAudioCapabilityDTSX          = 0x20,  // 支持HDR10增强
};

/**
 * 支持的软水印能力
 */
typedef NS_ENUM(NSUInteger, TVKWaterMarkCapability) {
    TVKWaterMarkCapabilityStaticNone = 0,  // 不支持
    TVKWaterMarkCapabilityStatic     = 1,  // 静态水印
    TVKWaterMarkCapabilityAction     = 2,  // 动态水印
};

/**
 * 支持的加密能力
 */
typedef NS_ENUM(NSUInteger, SPDRMCapability) {
    SPDRMCapabilityOrdinary    = 0x1,   // 支持普通DRM方案
    SPDRMCapabilityFakeDRM     = 0x2,   // 支持伪DRM
    SPDRMCapabilityUnitEnd     = 0x4,   // 支持数字太和DRM
    SPDRMCapabilityHLSEncrypt  = 0x8,   // 支持HLS加密
    SPDRMCapabilityFairplay    = 0x10,  // 支持Fairplay加密
    SPDRMCapabilityWidevine    = 0x20,  // 支持Widevine加密
};

/**
 * 清晰度付费能力
 */
typedef NS_ENUM(NSUInteger, SPDefnPayVer) {
    SPDefnPayVer1080P       = 0x1,  // 支持1080P付费
    SPDefnPayVer4K          = 0x2,  // 支持4K付费
    SPDefnPayVerDolbyVision = 0x4,  // 支持杜比付费
};

/*
 * 软字幕版本
 */
typedef NS_ENUM(NSUInteger, SPSRTCapability) {
    SPSRTCapabilityNone     = 0,  // 不支持软字幕
    SPSRTCapabilityMutex    = 1,  // 如果视频有硬字幕，则不下发软字幕
    SPSRTCapabilityNonMutex = 2,  // 不管是否有硬字幕，都下发软字幕
};

typedef NS_ENUM(NSUInteger, SPCGILoginType) {
    SPCGILoginTypeNone = 0,  // 未登录
    SPCGILoginTypeQQ   = 1,  // QQ
    SPCGILoginTypeWx   = 2,  // 微信
};

typedef NS_ENUM(NSUInteger, SPCGINetType) {
    SPCGINetTypeNone = 0,
    SPCGINetTypeWifi = 1,
    SPCGINetType2G   = 2,
    SPCGINetType3G   = 3,
    SPCGINetType4G   = 4,
    SPCGINetType5G   = 5,
};

typedef NS_ENUM(NSUInteger, SPCGIIPStack) {
    SPCGIIPStackIPV4 = 0,
    SPCGIIPStackIPV6 = 1,
    SPCGIIPStackDual = 2, // 双栈网络
};

// getvinfo请求类型
typedef NS_ENUM(NSUInteger, SPGetVInfoRequestType) {
    SPGetVInfoRequestTypeOnline        = 0,  // 在线播放（会尝试读取CGI缓存）
    SPGetVInfoRequestTypeOfflinePlay   = 1,  // 离线播放
    SPGetVInfoRequestTypeDownload      = 2,  // 离线下载
};

/**
 * getvinfo 返回的sshot定义
 */
typedef NS_ENUM(NSInteger, SPCGISShot) {
    SPCGISShotAppLogic         = 0,    // 使用 app 逻辑判断
    SPCGISShotForbidden        = 1,    // 不可截屏/录屏
    SPCGISShotSystemForbidden  = 2,    // 系统不可但 app 可截屏/录屏
    SPCGISShotAll              = 3,    // 系统和 app 均可截屏/录屏
};


//付费类型状态--视频侧rinazhou要求写死此入参spptype。 20210425
//http://tapd.oa.com/qqvideo_prj/markdown_wikis/show/#1210114481001144857
//pay_status4 会员用券(非会员可以单片购买/会员用券支付观看)
//5 包月单点(非会员可以单片购买观看; 会员可以免费观看)
//6 包月only(非会员不可以观看; 会员可以免费观看)
//7 单片付费(单片购买观看，会员可设置折扣价)
//8 免费
//9 单片付费plus，7的基础上增加了单集单个视频的付费
//10 会员升高等级 ，会员高等级可观看（线上还没上正式cid）
//12 会员付费解锁（超前点播）
