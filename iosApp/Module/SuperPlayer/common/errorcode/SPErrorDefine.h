//
//  SPErrorDefine.h
//  SPPlayer
//
//  Created by ethanyxliu on 2019/10/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

static int const SPErrorIPhonePlatformCode = 20;
static int const SPErrorIPadPlatformCode = 30;
static int const SPTVPlatformCode = 50;

#define SP_ERROR_KEY @"error"
#define SP_ERROR_MESSAGE_KEY @"errMsg"
#define SP_ERROR_MODEL_ID @"modelId"
#define SP_FULL_ERROR_CODE_STR_KEY @"fullErrorCodeStr"
#define SP_EX_ERROR_CODE_KEY @"exErrorCode"
#define SP_EX_ERROR_MSG_KEY @"exErrMsg"
#define SP_EX_ERROR_DATA_KEY @"data"

/**
 * 错误码按平台号、模块号进行划分，同时根据不同的场景加错误头，错误头由2位10进制数组成；
 * 错误头请见SPErrorCategory的定义
 */
// 模块号定义
typedef NS_ENUM(NSUInteger, SPModule) {
    SPModuleVInfo             = 101,  // vinfo
    SPModuleVKey              = 102,  // vkey
    SPModuleVBKey             = 103,  // vbkey
    SPModuleLiveVInfo         = 104,  // 直播cgi
    SPModulePlayer            = 200,  // 播放器
    SPModuleOnlineP2PPlay     = 211,  // 在线下载模块
    SPModuleOfflineP2PPlay    = 212,  // 离线下载模块
    SPModuleFairPlay          = 213,  // fairplay播放
    SPModuleLogic             = 400,  // 调用逻辑错误
    SPModuleDevice            = 500,  // 设备
    SPModuleEncoder           = 600,  // 编码写入错误
    SPModuleCompositionPlayer = 700,  // 编辑播放器
    SPModuleImageGenerator    = 701,  // 图片生成
};

// 错误头（错误范围）定义
typedef NS_ENUM(NSUInteger, SPErrorCategory) {
    SPErrorCategoryLogic         = 10,  // 业务侧逻辑错误
    SPErrorCategoryAVPlayer      = 11,  // 系统播放器错误
    SPErrorCategoryCGI           = 12,  // CGI 格式错误
    SPErrorCategoryServer        = 13,  // 后台服务错误
    SPErrorCategoryNetwork       = 14,  // 网络连接错误
    SPErrorCategoryFile          = 15,  // 文件操作/存储错误
    SPErrorCategoryDB            = 16,  // DB错误
    SPErrorCategoryCDN           = 17,  // CDN返回码
    SPErrorCategoryDeviceCapture = 20,  //camera,麦克风等媒体捕捉导致的错误
    SPErrorCategoryEncoder       = 21,  //编码写入时的错误
    SPErrorCategoryWidget        = 22,  //拍摄录制时，widget报错
};

// 直播，server返回错误码 retCode
typedef NS_ENUM(NSInteger, SPLiveServerErrorCode) {
    SPLiveServerErrorCodeOK                    = 0,     //数据正确返回
    SPLiveServerErrorCodeBanNotImportantArea   = 132,   //非重点区域限制观看
    SPLiveServerErrorCodeBandwidthLimitArea    = 133,   //带宽达到一定比例，限制非重点区域观看
    SPLiveServerErrorCodeBandwidthLimit        = 134,   //带宽整体已满，限制观看
    SPLiveServerErrorCodeBanAbroad             = 135,   //海外无版权
    SPLiveServerErrorCodeNoSignal              = 136,   //频道信号中断
    SPLiveServerErrorCodeProgramEnd            = 138,   //节目已经结束
    SPLiveServerErrorCodeNeedToWait            = 139,   //需要排队
    SPLiveServerErrorCodeNoCopyright           = 1312,  //节目当前时段无版权
    SPLiveServerErrorCodeAuthFailedInPay       = 1323,  //请求付费鉴权模块失败（网络超时或解析出错）提示试看已结束购买
    SPLiveServerErrorCodeLostLoginInfo         = 1325,  //无用户登录信息（登录态cookie缺少必填字段） 提示登录 试看
    SPLiveServerErrorCodeLoginInfoVerifyFailed = 1328,  //请求登录验证模块失败（网络超时或解析出错）提示登录 试看
    SPLiveServerErrorCodeNoPay                 = 1330,  //当前节目未付费 提示直接购买
    SPLiveServerErrorCodeNoLogin               = 1331,  //qq登录验证失败 提示登录 试看
    SPLiveServerErrorCodeCKEYVerifyFailed      = 1332,  //CKEY验证失败
    SPLiveServerErrorCodeTryWatchChanceUsed    = 1345,  //试看次数达到上限 提示试看已结束购买
    SPLiveServerErrorCodeGetPreviewCountFailed = 1347,  //试看计数失败（网络错误） 重试
    SPLiveServerErrorCodeWeixinVerifyFailed    = 1348,  //微信登录验证失败
    SPLiveServerErrorCodeWeixinVerifyTimeOut   = 1349,  //微信登录验证超时
    SPLiveServerErrorCodeGetPreviewInfoFailed  = 1350   //获取试看信息失败（网络错误） 重试
};

// 逻辑错误码定义
typedef NS_ENUM(NSInteger, SPAdLogicErrorCode) {
    SPADLogicErrorCodeOK               = 0,   //正确返回
    SPADLogicErrorCodeInvalidItemCount = 10,  //传入播放器的播放item不正确，小于等于0
    SPADLogicErrorCodeInvalidView      = 11,  //传入的view为空
    SPADLogicErrorCodeInvalidUrl       = 12,  //传入播放器的播放地址不正确，没有合法的地址
};

// cgi自定义错误码
typedef NS_ENUM(NSInteger, SPCgiCustomErrorCode) {
    SPCgiCustomErrorCodeUInfoIsNull = 1006,
    SPCgiCustomErrorCodeURLIsNull   = 1007,
    SPCgiCustomErrorCodeNoAudioInfo = 1008,  //请求多音轨，但是后台没有返回多音轨信息
};

// jons解析错误，CGI格式错误以12打头
typedef NS_ENUM(NSInteger, SPJsonErrorCode) {
    SPJsonErrorCodeOK        = 0,       // 解析成功
    SPJsonErrorCodeDataError = 125000,  // json数据错误，无法解析
    SPJsonErrorCodeParseFail = 125001,  // json解析失败
    SPJsonErrorCodeEmpty     = 125002,  // json内容为空
    SPJsonErrorCodeJsonError = 125003   // json数据错误，没有解析到响应字段
};

// xml解析错误，CGI格式错误以12打头
typedef NS_ENUM(NSUInteger, SPXMLParseErrorCode) {
    SPXMLParseErrorCodeOK = 0,
    SPXMLParseErrorCodeParseFail = 126000,
};


// 录制拍摄的错误码定义
typedef NS_ENUM(NSUInteger, SPMediaRecordError) {
    SPMediaRecordAddInputAudioError         = 100,  //无法添加音频到会话中，无法进行音频数据的采集
    SPMediaRecordAddOutputAudioError        = 101,  //无法添加音频数据输出，无法进行音频的采集和展示
    SPMediaRecordAddInputVidioError         = 102,  //无法添加视频到会话中，无法进行视频数据的预览
    SPMediaRecordAddOutputVidioError        = 103,  //无法添加视频数据输出，无法进行视频数据的预览和采集
    SPMediaRecordMediaRecordPermissionError = 104,  //没有录制视频的权限，需要开启camera的权限
    SPMediaRecordMicrophonePermissionError  = 105,  //没有录制音频的权限，需要开启麦克风录制的权限
    SPMediaRecordNoAudioDataOutputError     = 106,  // 录制时，没有音频数据，此时可以进行重试操作
    SPMediaRecordNoVideoDataOutputError     = 107,  // 录制时，没有视频数据，此时可以进行重试操作
    SPMediaRecordCannotSetUpInputError      = 108   // 录制写入时，无法设置AVAssetWriter
};

// 通用逻辑错误码定义，业务侧逻辑错误以10打头，这里直接加上错误头，方便使用
typedef NS_ENUM(NSUInteger, SPCommonLogicError) {
    SPCommonLogicErrorInvalidState           = 101000,  //播放状态非法
    SPCommonLogicErrorAuthInvalid            = 101001,  //sdk鉴权失败
    SPCommonLogicErrorPermissionTimeOut      = 101003,  //限播错误，不对外，通过特定delegate通知
    SPCommonLogicErrorInvalidParam           = 101004,  //参数错误
};

// AirPlay错误码定义
typedef NS_ENUM(NSUInteger, SPAirPlayError) {
    SPAirPlayErrorStartError     = 102000,  // 启动失败
    SPAirPlayErrorPreparingError = 102001,  // 准备过程中失败
    SPAirPlayErrorPlayingError   = 102002,  // 播放过程中失败
    SPAirPlayErrorActiveWhenOpen = 102003,  // 起播时就发现是airplay状态，这种情况也定义了一个错误，主要是为写代码方便
};

// Fairplay错误码定义
typedef NS_ENUM(NSUInteger, SPFairplayError) {
    SPFairplayRequestError = 103000,
};


static const NSInteger SPServerErrorCodeBegin = 130;   // server错误码起始
static const NSInteger SPServerErrorCodeEnd   = 1399;  // server错误码上限

static const NSInteger SPNetworkErrorCodeBegin = 140000;  // 网络错误码起始
static const NSInteger SPNetworkErrorCodeEnd   = 149999;  // 网络错误码上限

static const NSInteger SPJsonParseErrorCodeBegin = 120000;  // json解析错误码起始
static const NSInteger SPJsonParseErrorCodeEnd   = 129999;  // json解析错误码上限

static const NSInteger SPSubTitleErrorCodeBegin = 115000;  // 字幕加载错误码起始
