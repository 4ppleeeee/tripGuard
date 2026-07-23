/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPHLSKeyUtil.h
 Author      : 周辰
 Version     : 1.0
 Date        : 13-5-10
 Description :
 History     : 13-5-10 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPNetworkChecker.h"

@interface SPCKeyParam : NSObject

@property (nonatomic, retain) NSString *videoIDForCKey;
@property (nonatomic, copy) NSString *previd;
@property (nonatomic, copy) NSString *platform;
@property (nonatomic, copy) NSString *sdtFrom;
@property (nonatomic, assign) BOOL isDownload;  //是否为下载
@property (nonatomic, assign) BOOL isRender;    //是否为投射dlna或airplay

@end

@interface SPHLSKeyUtil : NSObject <SPNetworkCheckerDelegate>


+ (SPHLSKeyUtil *)sharedInstance;

- (BOOL)initCkeyWithGuid:(NSString*)guid vsAppKey:(NSString*)vsAppKey;

- (NSString *)createCKeyWithParam:(SPCKeyParam *)ckeyParam;
- (NSString *)createCKeyUrlWithParam:(SPCKeyParam *)ckeyParam;
/**
 更新后台的时间，用于生成ckey

 @param serverTime 后台的时间
 */
- (void)onGetCurrentServerTime:(NSTimeInterval)serverTime;

/**
 更新后台的rand flag, 用于生成ckey

 @param randFlag rand flag
 */
- (void)onGetRandFlag:(NSString *)randFlag;

//当前服务器时间戳
- (NSTimeInterval)getCurrentSystemTimeInterval;

@end
