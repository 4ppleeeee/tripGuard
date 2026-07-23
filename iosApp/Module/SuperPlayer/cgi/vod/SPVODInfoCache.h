//
//  SPVODInfoCache.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/12.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPVODRequestParam.h"
#import "SPGetVInfoData.h"


typedef void (^SPVODInfoCacheCompletion)(NSString *xml, SPVODRequestParam *requestParam);

@interface SPVODInfoCache : NSObject

+ (void)saveGetVInfoXML:(NSString *)xmlString
           getVInfoData:(SPGetVInfoData *)getVInfoData
           requestParam:(SPVODRequestParam *)requestparam;

+ (void)saveGetVBKeyXML:(NSString *)xmlString
           getVInfoData:(SPGetVInfoData *)getVInfoData
           requestParam:(SPVODRequestParam *)requestparam;

+ (void)readLocalGetVInfoXML:(SPVODRequestParam *)requestParam
                  completion:(SPVODInfoCacheCompletion)completion;

+ (void)readLocalGetVBKeyXML:(SPVODRequestParam *)requestParam
                  completion:(SPVODInfoCacheCompletion)completion;

// 获取IPV6出错的标记
+ (BOOL)isIPV6EverError;

// 存储双栈网络下IPV6出错的标记
+ (void)saveIPV6Error;

/// 缓存换链结果的key
+ (NSString *)cachedKeyWithRequestParam:(SPVODRequestParam *)requestParam;

@end

