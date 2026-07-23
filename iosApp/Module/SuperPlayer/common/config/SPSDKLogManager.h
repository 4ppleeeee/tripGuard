/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKLogManager.h
 Author      : andygao
 Version     : 1.0
 Date        : 2017/10/19
 Description : 上传本地日志到后台，便于问题分析
 History     : 2017/10/19 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@interface SPSDKLogManager : NSObject

+ (SPSDKLogManager *)sharedInstance;

/**
 指定时间后，将本地日志上传到后台

 @param timeInterval 时间
 */
- (void)uploadLogAsyncAfter:(NSTimeInterval)timeInterval;

/**
 是否是指定用户

 @param specialUid 用户id
 @return 结果
 */
- (bool)isSpecialUid:(NSString *)specialUid;
@end
