/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKLogManager.m
 Author      : andygao
 Version     : 1.0
 Date        : 2017/10/19
 Description :
 History     : 2017/10/19 初始版本
 ***********************************************************/

#import "SPSDKLogManager.h"
#import "SPVcSystemInfo.h"
#import "SPSDKParamsMgr.h"

@implementation SPSDKLogManager

+ (SPSDKLogManager *)sharedInstance {
    static SPSDKLogManager *logManagerInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        logManagerInstance = [[SPSDKLogManager alloc] init];
    });
    return logManagerInstance;
}

- (void)uploadLogAsyncAfter:(NSTimeInterval)timeInterval {
    SPLOGI(SP_CONFIG_LOG_FILTER, @"uploadLogAsyncAfter delay:%lf", timeInterval);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, timeInterval * NSEC_PER_SEC),

                   dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                       [self uploadLogSync:nil];
                   });
}

- (void)uploadLogSync:(NSDictionary *)logInfo {
    NSMutableDictionary *upLoadLogInfos = [[NSMutableDictionary alloc] init];
    NSString *uid = SPSDKPARAMS_UID;
    if (SPSDKPARAMS_QUERY_UIN.length > 0 && SPSDKPARAMS_QUERY_LOGIN_TYPE == SPLoginTypeQQ) {  //如果后面有播放，则使用最后播放的uid
        uid = SPSDKPARAMS_QUERY_UIN;
    } else if (SPSDKPARAMS_QUERY_WX_OPENID.length > 0 && SPSDKPARAMS_QUERY_LOGIN_TYPE == SPLoginTypeWx) {
        uid = SPSDKPARAMS_QUERY_UIN;
    } else if (SPSDKPARAMS_QUERY_V_USER_ID.length > 0) {
        uid = SPSDKPARAMS_QUERY_V_USER_ID;
    }
    [upLoadLogInfos setObject:SPSafeString(uid) forKey:@"qq"];
    [upLoadLogInfos setObject:SPSafeString(SPSDKPARAMS_GUID) forKey:@"guid"];
    [upLoadLogInfos setObject:SPSafeString([SPVcSystemInfo sharedInstance].appver) forKey:@"app_version"];
    if (logInfo) {
        [upLoadLogInfos addEntriesFromDictionary:logInfo];
    }
    if ([[SP_SDK_MGR_INST logReportDelegate] respondsToSelector:@selector(onLogReport:)]) {
        [[SP_SDK_MGR_INST logReportDelegate] onLogReport:upLoadLogInfos];
        SPLOGI(SP_REPORT_LOG_FILTER, @"logReoportByCallback, logReport success");
    } else {
        SPLOGW(SP_REPORT_LOG_FILTER, @"logReoportByCallback, logReportDelegate is not realized");
    }
}
- (bool)isSpecialUid:(NSString *)specialUid {
    if (!SPSDKCONF_ENABLE_UPLOAD_SPECIAL_UID_LOG || specialUid.length <= 0) {
        return NO;
    }

    long long uid = [specialUid longLongValue];
    if (uid > 0 && uid >= SPSDKCONF_UID_RANGE_LOW && uid <= SPSDKCONF_UID_RANGE_HIGH) {  // is special qq
        return YES;
    }
    // 后台下发的经过base 64编码的special id
    NSArray *specialUidArray = SPSDKCONF_SPECIAL_UID_ARRAY;
    if (specialUidArray.count > 0) {
        NSData *specialUidData = [specialUid dataUsingEncoding:NSUTF8StringEncoding];
        if ([specialUidData respondsToSelector:@selector(base64EncodedStringWithOptions:)]) {  //只针对ios 7+
            NSString *specialUidDataBase64 = [specialUidData base64EncodedStringWithOptions:0];
            if ([specialUidArray containsObject:specialUidDataBase64]) {
                return YES;
            }
        }
    }
    return NO;
}

@end
