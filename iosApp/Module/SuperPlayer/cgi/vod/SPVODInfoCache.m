//
//  SPVODInfoCache.m
//  SPPlayer
//
//  Created by liyukuan on 2019/10/12.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPVODInfoCache.h"
#import "SPLocalCache.h"
#import "SPUtils.h"

@implementation SPVODInfoCache

+ (void)saveGetVInfoXML:(NSString *)xmlString
           getVInfoData:(SPGetVInfoData *)getVInfoData
           requestParam:(SPVODRequestParam *)requestparam {
    if (![self needCachedWithRequestParam:requestparam vodPlayInfo:getVInfoData.vodPlayInfo]) {
        return;
    }
    NSString *cachedKey = [self cachedKeyForGetVInfoWithRequestParam:requestparam];
    [[SPLocalCache sharedInstance] put:cachedKey value:xmlString];
}

+ (void)saveGetVBKeyXML:(NSString *)xmlString
           getVInfoData:(SPGetVInfoData *)getVInfoData
           requestParam:(SPVODRequestParam *)requestparam {
    if (![self needCachedWithRequestParam:requestparam vodPlayInfo:getVInfoData.vodPlayInfo]) {
        return;
    }
    
    NSString *cachedKey = [self cachedKeyForGetVBKeyWithRequestParam:requestparam];
    [[SPLocalCache sharedInstance] put:cachedKey value:xmlString];
}

+ (void)readLocalGetVInfoXML:(SPVODRequestParam *)requestParam
                  completion:(SPVODInfoCacheCompletion)completion {
    if (![self needCachedWithRequestParam:requestParam vodPlayInfo:nil]) {
        completion(nil, requestParam);
        return;
    }
    
    NSString *cachedKey = [self cachedKeyForGetVInfoWithRequestParam:requestParam];
    [self readLocalXMLWithKey:cachedKey requestParam:requestParam completion:completion];
}

+ (void)readLocalGetVBKeyXML:(SPVODRequestParam *)requestParam
                  completion:(SPVODInfoCacheCompletion)completion {
    if (![self needCachedWithRequestParam:requestParam vodPlayInfo:nil]) {
        completion(nil, requestParam);
        return;
    }
    
    NSString *cachedKey = [self cachedKeyForGetVBKeyWithRequestParam:requestParam];
    [self readLocalXMLWithKey:cachedKey requestParam:requestParam completion:completion];
}

+ (void)readLocalXMLWithKey:(NSString *)key
               requestParam:(SPVODRequestParam *)requestParam
                 completion:(SPVODInfoCacheCompletion)completion {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *xmlString = [[SPLocalCache sharedInstance] get:key];
        completion(xmlString, requestParam);
    });
}

+ (NSString *)cachedKeyForGetVInfoWithRequestParam:(SPVODRequestParam *)requestParam {
    NSString *cachedKey = [self cachedKeyWithRequestParam:requestParam];
    return [cachedKey stringByAppendingString:@"_getvinfo"];
}

+ (NSString *)cachedKeyForGetVBKeyWithRequestParam:(SPVODRequestParam *)requestParam {
    NSString *cachedKey = [self cachedKeyWithRequestParam:requestParam];
    return [cachedKey stringByAppendingString:@"_getvbkey"];
}

+ (NSString *)cachedKeyWithRequestParam:(SPVODRequestParam *)requestParam {
    NSString *cookieMD5 = [SPUtils md5ForLowerCase:requestParam.commonParams.cookie];
    NSString *key = [NSString stringWithFormat:@"Vod_%@_%@_%d_%d_%d_%d_%d_%@_%@_%f_%@",
                     requestParam.vid,
                     requestParam.definition,
                     (int)requestParam.mediaFormat,
                     (int)requestParam.capabilityParam.hevcLevel,
                     requestParam.capabilityParam.spvideo,
                     requestParam.capabilityParam.spaudio,
                     requestParam.capabilityParam.drm,
                     requestParam.track,
                     requestParam.srccontenid,
                     requestParam.startPosition,
                     cookieMD5];
    SPLOGS(gSPPlayerDefaultTagPrefix, @"cached key=%@, vid=%@", key, requestParam.vid);
    return [SPUtils md5ForLowerCase:key];
}

+ (BOOL)needCachedWithRequestParam:(SPVODRequestParam *)requestParam
                       vodPlayInfo:(SPVODPlayInfo *)vodPlayInfo {
    if (![self needCachedWithRequestParam:requestParam]) {
        return NO;
    }
        
    if (SPCGINetTypeWifi != requestParam.commonParams.netType) {
        SPLOGS(gSPPlayerDefaultTagPrefix, @"needCached: wwan not use cache, vid=%@", requestParam.vid);
        return NO; // 考虑到免流情况，移动网络下不缓存
    }
    
    SPLOGS(gSPPlayerDefaultTagPrefix, @"needCached: can use cache, vid=%@", requestParam.vid);
    return YES;
}

+ (BOOL)needCachedWithRequestParam:(SPVODRequestParam *)requestParam {
    if (!requestParam.options.useCache) {
        SPLOGS(gSPPlayerDefaultTagPrefix, @"needCached: useCache is NO, vid=%@", requestParam.vid);
        return NO;
    }
    
    if (requestParam.isAirplay) {
        SPLOGS(gSPPlayerDefaultTagPrefix, @"needCached: airplay not use cache, vid=%@", requestParam.vid);
        return NO;
    }
    
    if (requestParam.previd.length > 0) {
        SPLOGS(gSPPlayerDefaultTagPrefix, @"needCached: quick play not use cache, vid=%@", requestParam.vid);
        return NO; // 秒播不缓存
    }
    
    if (SPCGINetTypeWifi != requestParam.commonParams.netType) {
        SPLOGS(gSPPlayerDefaultTagPrefix, @"needCached: wwan not use cache, vid=%@", requestParam.vid);
        return NO; // 考虑到免流情况，移动网络下不缓存
    }

    return YES;
}

#define GETVINFO_IPV6_ERROR_KEY @"GETVINFO_IPV6_ERROR_KEY"

+ (BOOL)isIPV6EverError {
    NSString *ipv6Error = [[SPLocalCache sharedInstance] get:GETVINFO_IPV6_ERROR_KEY memeryOnly:YES];
    SPLOGS(gSPPlayerDefaultTagPrefix, @"getvinfo ipv6 error flag:%@", ipv6Error);
    return ipv6Error.intValue > 0;
}

// 存储双栈网络下IPV6出错的标记，一旦出错，当前生命周期就不走IPV6了
+ (void)saveIPV6Error {
    // 只在app一次生命周期有效，存在内存就可以了
    // SPLocalCache仅支持存储NSString，暂时这样用。后续SPLocalCache最好扩展为支持id对象
    SPLOGS(gSPPlayerDefaultTagPrefix, @"getvinfo save ipv6 error flag");
    [[SPLocalCache sharedInstance] put:GETVINFO_IPV6_ERROR_KEY value:@"1" cacheTime:3600 * 10 saveDisk:NO];
}
@end
