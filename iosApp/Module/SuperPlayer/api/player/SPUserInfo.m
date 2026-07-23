/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPUserInfo.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/5
 Description :
 History     : 17/2/5 初始版本
 ***********************************************************/

#import "SPUserInfo.h"

@implementation SPUserInfo

- (NSString *)description {
    return [NSString stringWithFormat:@"uin:%@, isVip:%d, vipType:%@, wx_openId:%@, wx_appId%@, vuserId:%@, loginType:%@, cdnhttpHeader:%@",
                                      _uin, _isVip, [self nameStringOfVipType:_vipType], _wx_openId, _wx_appId, _vuserId,
                                      [self nameStringOfLoginType:_loginType], _cdnHttpHeader];
}

- (NSString *)nameStringOfLoginType:(SPLoginType)loginType {
    NSString *loginTypeNameStr = @"未知";
    switch (loginType) {
        case SPLoginTypeQQ:
            loginTypeNameStr = @"QQ登陆";
            break;
        case SPLoginTypeWx:
            loginTypeNameStr = @"微信登陆";
            break;
        case SPLoginTypeNone:
            loginTypeNameStr = @"未登录";
            break;
    }
    return loginTypeNameStr;
}
- (NSString *)nameStringOfVipType:(SPVipType)vipType {
    NSString *vipTypeNameStr = @"未知";
    switch (vipType) {
        case SPVipTypeTencentVideo:
            vipTypeNameStr = @"腾讯视频会员";
            break;
        case SPVipTypeSupplementCard:
            vipTypeNameStr = @"腾讯视频附属卡会员";
            break;
        case SPVipTypeNotLogin:
            vipTypeNameStr = @"未登陆";
            break;
        case SPVipTypeLogin:
            vipTypeNameStr = @"登陆的普通用户";
            break;
    }
    return vipTypeNameStr;
}
@end
