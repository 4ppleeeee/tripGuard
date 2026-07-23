/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKParamsMgr.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/6
 Description : 公共参数管理类
 History     : 17/2/6 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPUserInfo.h"
// 用户QQ号
#define SPSDKPARAMS_QUERY_UIN [SPSDKParamsMgr sharedInstance].userInfo.uin
// 用户微信openId
#define SPSDKPARAMS_QUERY_WX_OPENID [SPSDKParamsMgr sharedInstance].userInfo.wx_openId
// 用户微信appId
#define SPSDKPARAMS_QUERY_WX_APPID [SPSDKParamsMgr sharedInstance].userInfo.wx_appId
// 用户vuserId
#define SPSDKPARAMS_QUERY_V_USER_ID [SPSDKParamsMgr sharedInstance].userInfo.vuserId
// 用户cookie,用于getvinfo请求时
#define SPSDKPARAMS_QUERY_LOGIN_COOKIE [SPSDKParamsMgr sharedInstance].userInfo.cookie
// 是否是vip
#define SPSDKPARAMS_QUERY_IS_VIP [SPSDKParamsMgr sharedInstance].userInfo.isVip
// 用户登录类型
#define SPSDKPARAMS_QUERY_LOGIN_TYPE [SPSDKParamsMgr sharedInstance].userInfo.loginType
// 设备guid
#define SPSDKPARAMS_GUID [SPSDKParamsMgr sharedInstance].guid
// 设备qimei
#define SPSDKPARAMS_QIMEI [SPSDKParamsMgr sharedInstance].qimei
// 用户id.qq登陆的时，为qq号;微信登陆时，是微信openid,用于拉取配置
#define SPSDKPARAMS_UID [SPSDKParamsMgr sharedInstance].uid
// 用户扩展属性信息
#define SPSDKPARAMS_QUERY_USERINFO_EXTRA_DICTTIONARY [SPSDKParamsMgr sharedInstance].userInfo.extraDictionary

@class SPUserInfo;
@class SPSDKGetVInfoModel;

/**
 公共参数管理类
 */
@interface SPSDKParamsMgr : NSObject

+ (SPSDKParamsMgr *)sharedInstance;

@property (atomic, strong) SPUserInfo *userInfo;              // app设置的用户信息
@property (nonatomic, copy) NSString *platform;               // 播放平台号
@property (nonatomic, copy) NSArray<SPSDKGetVInfoModel *> *sdkGetVInfoModels;   // SDK防盗链配置参数列表
@property (nonatomic, copy) NSString *playerChannelId;        // 播放渠道号
@property (nonatomic, copy) NSString *productChannelId;       // 产品关注的channelid, 主要用于推荐拉流等
@property (nonatomic, copy) NSString *playerConfigId;         // 播放器的配置id
@property (nonatomic, readonly, copy) NSString *guid;         // guid
@property (nonatomic, copy) NSString *qimei;                  // qimei
@property (nonatomic, copy) NSString *spptype;                // 付费类型状态, 字符串，见SPCGIDefines.h的定义
@property (nonatomic, readonly, strong) NSString *userAgent;  // 用户的agent,播放请求的时候使用
@property (nonatomic, readonly, assign) BOOL isExternalGuid;  // 是否是外部app设置的guid
@property (nonatomic, strong) NSString *uid;                  // 用户id.qq登陆的时，为qq号;微信登陆时，是微信openid,用于拉取配置
@property (nonatomic, strong) NSString *decoderStrategy;      // 视频解码模式，string类型，数值参考TPDecoderStrategy

@property (nonatomic, assign) BOOL reportPluginDisabled;
@property (nonatomic, assign) BOOL idleTimerPluginDisabled;

/**
 设置guid，如果外部有设置guid，则优先使用外部的，否则使用内部的

 @param guid guid
 @param isExternalSet 是否由外部设置
 */
- (void)setGuid:(NSString *)guid external:(BOOL)isExternalSet;

@end

@interface SPSDKGetVInfoModel : NSObject

@property (nonatomic, copy) NSString *platform;               // 播放平台号
@property (nonatomic, copy) NSString *sdtfrom;                // 播放来源，运维统计流量用
@property (nonatomic, copy) NSString *vsAppkey;               // 具体业务key，需要向mingyuewan(万明月)申请

@end
