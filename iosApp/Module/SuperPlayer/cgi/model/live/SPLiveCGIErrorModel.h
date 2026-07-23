//
//  SPLiveCGIErrorModel.h
//  SPPlayer
//
//  Created by hemanli on 2019/10/7.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * 直播信息cgi返回的错误码
 */
typedef NS_ENUM(NSUInteger, SPLiveCgiRetCode) {
    SPLiveCgiRetCodeNeedToWait            = 9,   // 需要排队
    SPLiveCgiRetCodeAuthFailedInPay       = 23,  // 请求付费鉴权模块失败（网络超时或解析出错）提示试看已结束购买
    SPLiveCgiRetCodeLostLoginInfo         = 25,  // 无用户登录信息（登录态cookie缺少必填字段） 提示登录 试看
    SPLiveCgiRetCodeLoginInfoVerifyFailed = 28,  // 请求登录验证模块失败（网络超时或解析出错）提示登录 试看
    SPLiveCgiRetCodeNoPay                 = 30,  // 当前节目未付费 提示直接购买
    SPLiveCgiRetCodeNoLogin               = 31,  // 用户未登录 提示登录 试看
    SPLiveCgiRetCodeCKEYVerifyFailed      = 32,  // CKEY验证失败
    SPLiveCgiRetCodeTryWatchChanceUsed    = 45,  // 试看次数达到上限 提示试看已结束购买
    SPLiveCgiRetCodeGetPreviewCountFailed = 47,  // 试看计数失败（网络错误） 重试
    SPLiveCgiRetCodeWeixinVerifyFailed    = 48,  // 微信登录验证失败（网络错误） 试看
    SPLiveCgiRetCodeWeixinVerifyTimeOut   = 49,  // 微信登录验证超时
    SPLiveCgiRetCodeGetPreviewInfoFailed  = 50   // 获取试看信息失败（网络错误） 重试
};

NS_ASSUME_NONNULL_BEGIN

@interface SPLiveCGIErrorModel : NSObject

@property (nonatomic, assign) int retCode;

@property (nonatomic, assign) int retDetailCode;

@property (nonatomic, assign) int type;  //ckey细分错误码，当iretcode为32时(ckey校验出错)时有效

@property (nonatomic, copy) NSString *errInfo;  //错误详细说明，由返回码决定

@property (nonatomic, copy) NSString *errorTitle;  //错误标题详细说明，由返回码决定

@property (nonatomic, assign) int64_t curSeverTime;

@property (nonatomic, copy) NSString *randFlag;

@end

NS_ASSUME_NONNULL_END
