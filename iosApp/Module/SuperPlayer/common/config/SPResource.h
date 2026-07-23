/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPResource.h
 Author      : Odie
 Version     : 1.0
 Date        : 14-4-28
 Description : 设置APP环境，获取播放等相关请求的播放地址
 History     : 14-4-28 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
// 获取播放等相关请求的播放地址
#define SP_RESOURCE_URL(property) [[SPResource instance] URLWithKey:@ #property]

typedef enum {
    SPResouceEnvModeTest,        // 测试环境
    SPResouceEnvModePreRelease,  // 预发布环境
    SPResouceEnvModeRelease      // 正式环境
} SPResouceEnvMode;

@interface SPResource : NSObject

+ (SPResource *)instance;

/**
 根据key值，获取对应的URL

 @param key key值
 @return url地址
 */
- (NSString *)URLWithKey:(NSString *)key;

/**
 设置APP环境

 @param envMode 环境类型
 */
- (void)setEnvMode:(SPResouceEnvMode)envMode;

@end
