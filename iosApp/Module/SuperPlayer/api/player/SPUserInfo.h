/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPUserInfo.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/5
 Description :
 History     : 17/2/5 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

@interface SPUserInfo : NSObject

/**
 @brief qq号码
 */
@property (nonatomic, copy) NSString *uin;

/**
 * @brief 登陆用户的cookie，格式：qq=xxx;skey=xxx;lskey=xxx;用于正片
 */
@property (nonatomic, copy) NSString *cookie;
/**
 * @brief 登陆用户的cookie，用于广告，有广告的务必设置
 */
@property (nonatomic, copy) NSArray<NSHTTPCookie *> *adCookie;
/**
 * @brief 是否是视频会员.用于广告请求，数据上报等场景。有会员功能播放时，必须设置
 */
@property (nonatomic, assign) BOOL isVip;

/**
 * @brief 会员类型.用于广告请求，有会员功能播放时，必须设置
 */
@property (nonatomic, assign) SPVipType vipType;
/**
 * @brief 微信的openid
 */
@property (nonatomic, copy) NSString *wx_openId;

/**
 * @brief 微信的appid
 */
@property (nonatomic, copy) NSString *wx_appId;

/**
 * @brief 用户vuserid
 */
@property (nonatomic, copy) NSString *vuserId;

/**
 * @brief 用户的主登录类型，即用户要以哪种身份到后台进行鉴权
 */
@property (nonatomic, assign) SPLoginType loginType;

/**
 * @brief cdn请求的http header. 用于一些cdn鉴权使用
 */
@property (nonatomic, strong) NSDictionary *cdnHttpHeader;

/// 额外扩展的标记用户属性的信息，注意key和value都是String类型！
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *extraDictionary;

@end
