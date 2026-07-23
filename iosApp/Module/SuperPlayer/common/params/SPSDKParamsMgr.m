/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKParamsMgr.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/2/6
 Description :
 History     : 17/2/6 初始版本
 ***********************************************************/

#import "SPSDKParamsMgr.h"
#import "SPSDKManager.h"
#import "SPVcSystemInfo.h"
#import "SPNetworkChecker.h"
#import "SPUtils.h"

static NSString *gTag = @"sdkparams";

@interface SPSDKParamsMgr ()

@property (nonatomic, readwrite, copy) NSString *guid;
@property (nonatomic, readwrite, assign) BOOL isExternalGuid;  //是否是外部app设置的guid

@end

@implementation SPSDKParamsMgr

+ (SPSDKParamsMgr *)sharedInstance {
    static SPSDKParamsMgr *s_paramsMgr = nil;
    static dispatch_once_t onceToken = 0;
    dispatch_once(&onceToken, ^{
        s_paramsMgr = [[SPSDKParamsMgr alloc] init];
        s_paramsMgr.spptype = @"4,5,6,7,8,9,10,11,12";  //付费类型默认状态, 字符串，见SPCGIDefines.h的定义  视频侧rinazhou要求写死此入参spptype。 20210425
    });

    return s_paramsMgr;
}

- (void)setGuid:(NSString *)guid external:(BOOL)isExternalSet {
    if (guid.length > 0 && (isExternalSet || !_guid)) {  // guid非空, 外部设置的可以覆盖内部的
        SPLOGI(gTag, @"setGuid guid is:%@, isExternalSet:%d", guid, isExternalSet);
        _guid = guid;
        _isExternalGuid = isExternalSet;
    }
}

- (NSString *)guid {
    if (_guid.length == 0) {
        _guid = [SPVcSystemInfo sharedInstance].localGuid;
        if (_guid.length == 0) {  //本地没有,生成uuid作为唯一guid
            SPLOGI(gTag, @"localGuid is empty, use uuid instead");
            _guid = [SPVcSystemInfo sharedInstance].deviceId;
        }

        if (_guid.length == 0) {
            SPLOGI(gTag, @"device id is empty, use uuid instead");
            _guid = [SPUtils generateUUID];
        }
        _isExternalGuid = NO;
    }
    return _guid;
}

- (NSString *)uid {
    if (_userInfo) {
        if (SPLoginTypeQQ == SPSDKPARAMS_QUERY_LOGIN_TYPE) {
            return SPSDKPARAMS_QUERY_UIN;
        } else if (SPLoginTypeWx == SPSDKPARAMS_QUERY_LOGIN_TYPE) {
            return SPSDKPARAMS_QUERY_WX_OPENID;
        } else if (SPLoginTypeNone == SPSDKPARAMS_QUERY_LOGIN_TYPE) {
            return @"";
        }
    } else if (_uid.length > 0) {
        return _uid;
    } else {
        return @"";
    }

    return @"";
}
- (NSString *)userAgent {
    return @"qqlive";
}

@end

@implementation SPSDKGetVInfoModel

@end
