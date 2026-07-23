//
//  SPLocalCache.h
//  SPPlayer
//
//  Created by haitend on 2019/10/17.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SPLocalCache : NSObject

+ (SPLocalCache *)sharedInstance;

/**
 * @param key 存储key
 * @param value 存储内容
 * @param time 超时时间
 * @param saveDisk 是否存disk
 */
- (void)put:(NSString *)key value:(NSString *)value cacheTime:(NSTimeInterval)time saveDisk:(BOOL)saveDisk;
- (void)put:(NSString *)key value:(NSString *)value cacheTime:(NSTimeInterval)time;
- (void)put:(NSString *)key value:(NSString *)value;
/** 根据key 获取存储内容 */
- (NSString *)get:(NSString *)key;
/**
 * @param key 存储key
 * @param memeryOnly 是否只在内存查找
 */
- (NSString *)get:(NSString *)key memeryOnly:(BOOL)memeryOnly;

/** 删除指定key的缓存
 *
 * @param key 需要删除的key
 */
- (void)removeWithKey:(NSString *)key;
/**
 * 移除所有缓存
 */
- (void)removeAll;

@end

NS_ASSUME_NONNULL_END
