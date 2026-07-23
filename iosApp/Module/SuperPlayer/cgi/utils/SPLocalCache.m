//
//  SPLocalCache.m
//  SPPlayer
//
//  Created by haitend on 2019/10/17.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPLocalCache.h"
/** 存储文件名 这里写死 FIXME 后续外部传递*/
static NSString *const SPCacheFile = @"/cgi_info";

/** 默认缓存超时时间 -1 不限制 */
static const NSInteger defaultCacheMaxCacheTime = 60 * 60 * 2;
/** disk 限制大小 */
static const NSUInteger defaultCacheMaxCacheSize = 20 * 1024 * 1024;
/** 内存里面只限制条数，不限制大小 */
static const NSUInteger defaultCacheMaxCacheItemCount = 1000;
/** 每次清理时，删除的条数 */
static const int defaultClearItemCount = 10;

/** 存储cacheInfo 的文件名 */
static NSString *const SPCacheInfoKey = @"cacheInfo";
/** 根据存入时间先后顺序存储每个item的文件名 */
static NSString *const SPCacheItemArrayKey = @"cacheItemArray";

/** cache 超时时间key*/
static NSString *const SPCacheExpireTimeKey = @"expireTime";
/** disk 存储条数的key */
static NSString *const SPCacheItemCountKey = @"cacheItem";
/** disk 存储大小的key */
static NSString *const SPCacheSizeKey = @"cacheSize";

@interface SPCacheObject : NSObject

@property (nonatomic, assign) NSTimeInterval expireTime;

@property (nonatomic, strong) NSString *value;

@property (nonatomic, strong) NSString *key;

@property (nonatomic, assign) NSTimeInterval saveTime;

- (BOOL)isDue;
@end

@implementation SPCacheObject
/** memery 存储是否超时 */
- (BOOL)isDue {
    NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
    if (self.expireTime == -1) {
        return NO;
    }
    if (currentTime - self.saveTime > self.expireTime) {
        return YES;
    }
    return NO;
}
@end

@interface SPLocalCache ()

/** 存储内存，如果key相同，则会被覆盖；FIXME：尽量不要存储相同key存储不同类型 */
@property (nonatomic, strong) NSMutableDictionary<NSString *, SPCacheObject *> *memeryCacheMap;
/** 存储内存中的key，主要用于先进先出的管理 */
@property (nonatomic, strong) NSMutableArray *memeryItemArray;

@property (nonatomic, strong) NSRecursiveLock *lock;
/** 记录disk存储路径 */
@property (nonatomic, strong) NSString *cachePath;
/** 记录当前disk 存储条数 */
@property (nonatomic, assign) int diskItemCount;
/** 记录当前disk 存储大小 */
@property (nonatomic, assign) int64_t diskItemCacheSize;
/** 记录当前disk存储的文件名集合 */
@property (nonatomic, strong) NSMutableArray *diskItemArray;

@end

/** 存储，先存储内存，然后存入沙盒
 *  缓存时间默认两个小时，外部可以设置
 *  初始化时,读取存储文件信息，包含已经存储的大小，存储的条数
 *  存储时，判断是否超过限制，如果超过，则清理；
 *  真正写入时，记录已经存储的条数和字符长度
 *
 *  读取，先读取内存，再读取沙盒
 *  比较是否过期，过期了就删除，然后返回nil
 */
@implementation SPLocalCache

+ (SPLocalCache *)sharedInstance {
    static SPLocalCache *cacheMgr = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cacheMgr = [[SPLocalCache alloc] initWithContext];
    });
    return cacheMgr;
}

- (instancetype)initWithContext {
    if (self = [super init]) {
        _lock = [[NSRecursiveLock alloc] init];
        _memeryCacheMap = [[NSMutableDictionary alloc] init];
        _memeryItemArray = [[NSMutableArray alloc] init];
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *filePath = [paths[0] stringByAppendingPathComponent:SPCacheFile];
        BOOL isDir = YES;
        if (![[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        _cachePath = [filePath stringByAppendingString:@"/"];
        [self initDefaultDisk];
    }
    return self;
}

- (void)initDefaultDisk {
    NSString *infoPath = [self.cachePath stringByAppendingString:SPCacheInfoKey];
    BOOL isDir = YES;
    if (![[NSFileManager defaultManager] fileExistsAtPath:infoPath isDirectory:&isDir]) {
        [[NSFileManager defaultManager] createFileAtPath:infoPath contents:nil attributes:nil];
        [_diskItemArray removeAllObjects];
        _diskItemCacheSize = 0;
        _diskItemCount = 0;
    }
    NSData *infoData = [NSData dataWithContentsOfFile:infoPath];
    if (infoData != nil) {
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:infoData options:NSJSONReadingMutableLeaves error:nil];
        NSNumber *count = [dict objectForKey:SPCacheItemCountKey];
        _diskItemCount = count.intValue > 0?count.intValue:0;
        NSNumber *size = [dict objectForKey:SPCacheSizeKey];
        _diskItemCacheSize = size.intValue;
    } else {
        _diskItemCount = 0;
        _diskItemCacheSize = 0;
    }

    NSString *itemPath = [self.cachePath stringByAppendingString:SPCacheItemArrayKey];
    if (![[NSFileManager defaultManager] fileExistsAtPath:itemPath isDirectory:&isDir]) {
        [[NSFileManager defaultManager] createFileAtPath:itemPath contents:nil attributes:nil];
    }
    NSData *itemData = [NSData dataWithContentsOfFile:itemPath];
    NSMutableArray *itemArray = nil;
    if (itemData != nil) {
        itemArray = [NSKeyedUnarchiver unarchiveObjectWithData:itemData];
    }
    if (itemArray != nil) {
        NSSet *set = [NSSet setWithArray:itemArray];
        _diskItemArray = [[set allObjects] mutableCopy];
        _diskItemCount = [_diskItemArray count];
    } else {
        _diskItemArray = [[NSMutableArray alloc] init];
    }
}

- (void)put:(NSString *)key value:(NSString *)value cacheTime:(NSTimeInterval)time saveDisk:(BOOL)saveDisk {
    [self.lock lock];
    if (key == nil || value == nil) {
        [self.lock unlock];
        return;
    }
    /** memery cache */
    if ([self.memeryCacheMap.allKeys containsObject:key] == YES) {
        [self.memeryCacheMap removeObjectForKey:key];
    }

    SPCacheObject *object = [[SPCacheObject alloc] init];
    object.expireTime = time;
    object.key = key;
    object.value = value;
    object.saveTime = [[NSDate date] timeIntervalSince1970];
    if ([self.memeryCacheMap count] > defaultCacheMaxCacheItemCount) {
        [self clearMemery];
    }
    [self.memeryCacheMap setObject:object forKey:key];
    [self.memeryItemArray addObject:key];

    if (saveDisk == YES) {
        BOOL ret = [self writeToFile:self.cachePath cacheObject:object];
        if (!ret) {
            SPLOGS(self.logTag, @"write failed or local exist!");
            [self.lock unlock];
            return;
        }
        [self addInfoToFile:object.key cacheSize:[self getFileSize:self.cachePath key:object.key]];
        if (self.diskItemCount + 1 > defaultCacheMaxCacheItemCount || self.diskItemCacheSize + [object.value length] > defaultCacheMaxCacheSize) {
            SPLOGS(self.logTag, @"due to clear itemCount= %d,cacheSize= %lld,filesize=%lld", self.diskItemCount, self.diskItemCacheSize,
                    [object.value length]);
            /** 清理 */
            [self clearCache];
            [self.lock unlock];
            /** 异步清理*/
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [self clearDiskCache];
            });
            return;
        }
    }
    [self.lock unlock];
}

- (void)put:(NSString *)key value:(NSString *)value cacheTime:(NSTimeInterval)time {
    [self put:key value:value cacheTime:time saveDisk:YES];
}

- (void)put:(NSString *)key value:(NSString *)value {
    [self put:key value:value cacheTime:defaultCacheMaxCacheTime];
}

- (NSString *)get:(NSString *)key {
    NSString *data = [self get:key memeryOnly:NO];
    return data;
}

- (NSString *)get:(NSString *)key memeryOnly:(BOOL)memeryOnly {
    [self.lock lock];
    if (key == nil) {
        [self.lock unlock];
        return nil;
    }
    SPLOGS(self.logTag, @"get enter,key = :%@", key);

    /**如果内存里面没有过期，则直接返回,否则删除内存 */
    if ([self.memeryCacheMap.allKeys containsObject:key] == YES) {
        SPCacheObject *cacheObject = [self.memeryCacheMap objectForKey:key];
        if ([cacheObject isDue] == NO) {
            [self.lock unlock];
            return cacheObject.value;
        } else {
            [self.memeryCacheMap removeObjectForKey:key];
            [self.memeryItemArray removeObject:key];
        }
    }
    /** 内存没找到或者已经过期，则disk查找 */
    if (memeryOnly == NO) {
        SPLOGS(self.logTag, @"read from disk,key = :%@", key);
        NSString *path = [self.cachePath stringByAppendingString:key];
        BOOL isDir = YES;
        if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
            [self.lock unlock];
            return nil;
        }
        NSData *itemData = [NSData dataWithContentsOfFile:path];
        if (itemData == nil) {
            [self.lock unlock];
            return nil;
        }
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:itemData options:NSJSONReadingMutableLeaves error:nil];
        NSTimeInterval expireTime = [[dict objectForKey:SPCacheExpireTimeKey] doubleValue];
        NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
        /** 没有过期 */
        if (expireTime == -1 || currentTime - [self getFileLastModifyTime:self.cachePath key:key] <= expireTime) {
            NSString *ret = [dict objectForKey:key];
            [self.lock unlock];
            SPLOGS(self.logTag, @"read sucess,key = :%@", key);
            return ret;
        } else {
            SPLOGS(self.logTag, @"read clear,key = :%@", key);
            [self removeOneItem:key];
            [self writeItemArray:self.diskItemArray];
            [self writeItemInfo:self.diskItemCacheSize itemCount:self.diskItemCount];
        }
    }
    [self.lock unlock];
    SPLOGS(self.logTag, @"write to disk nil,key = :%@", key);
    return nil;
}
- (int64_t)getFileSize:(NSString *)cachePath key:(NSString *)key {
    NSString *path = [cachePath stringByAppendingString:key];
    int64_t size = [[[[NSFileManager defaultManager] attributesOfItemAtPath:path error:nil] objectForKey:NSFileSize] longLongValue];
    return size;
}
- (NSTimeInterval)getFileLastModifyTime:(NSString *)cachePath key:(NSString *)key {
    NSString *path = [cachePath stringByAppendingString:key];
    NSDate *time = [[[NSFileManager defaultManager] attributesOfItemAtPath:path error:nil] objectForKey:NSFileModificationDate];
    return time.timeIntervalSince1970;
}

- (void)clearMemery {
    for (int count = 0; count < defaultClearItemCount; count++) {
        [self.memeryCacheMap removeObjectForKey:self.memeryItemArray[0]];
        [self.memeryItemArray removeObjectAtIndex:0];
    }
}

/** 每次清除最后10 项 */
- (void)clearCache {
    SPLOGS(self.logTag, @"clearCache");
    for (int count = 0; count < defaultClearItemCount; count++) {
        if ([self.diskItemArray count] > 0) {
            [self removeOneItem:self.diskItemArray[0]];
        }
    }
    [self writeItemArray:self.diskItemArray];
    [self writeItemInfo:self.diskItemCacheSize itemCount:self.diskItemCount];
}
- (void)removeOneItem:(NSString *)key {
    if (key == nil) {
        return;
    }
    SPLOGS(self.logTag, @"removeOneItem,key = :%@", key);

    int64_t cacheSize = [self getFileSize:self.cachePath key:key];
    [self removeItemPath:self.cachePath itemKey:key];
    [self.diskItemArray removeObject:key];
    if (cacheSize > 0) {
        self.diskItemCount--;
        self.diskItemCacheSize = self.diskItemCacheSize - cacheSize;
    }
}

- (BOOL)writeToFile:(NSString *)cachePath cacheObject:(SPCacheObject *)cacheObject {
    BOOL isDir = YES;
    BOOL isExist = YES;
    NSString *path = [cachePath stringByAppendingString:cacheObject.key];
    /** 没有这个文件，则创建 */
    if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        isExist = NO;
        [[NSFileManager defaultManager] createFileAtPath:path contents:nil attributes:nil];
    }

    NSDictionary *dict = @{ cacheObject.key : cacheObject.value, SPCacheExpireTimeKey : @(cacheObject.expireTime) };
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];
    BOOL ret = [data writeToFile:path atomically:YES];
    if (ret) {
        SPLOGS(self.logTag, @"write to disk sucess,path = :%@", path);
    } else {
        SPLOGS(self.logTag, @"write to disk failed,path = :%@", path);
    }
    return ret && !isExist;
}

- (void)addInfoToFile:(NSString *)key cacheSize:(int64_t)cacheSize {
    [self.diskItemArray addObject:key];
    self.diskItemCount = self.diskItemCount + 1;
    self.diskItemCacheSize = self.diskItemCacheSize + cacheSize;
    SPLOGS(self.logTag, @"addInfoToFile to disk,%d,%lld", self.diskItemCount, self.diskItemCacheSize);

    [self writeItemArray:self.diskItemArray];
    [self writeItemInfo:self.diskItemCacheSize itemCount:self.diskItemCount];
}

- (void)removeItemPath:(NSString *)cachePath itemKey:(NSString *)itemKey {
    BOOL isDir = YES;
    SPLOGS(self.logTag, @"removeItemPath,key = :%@", itemKey);

    NSString *path = [cachePath stringByAppendingString:itemKey];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        return;
    }
    [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
}

/** 写入info 到文件 */
- (void)writeItemInfo:(int64_t)cacheSize itemCount:(int)itemCount {
    NSString *path = [self.cachePath stringByAppendingString:SPCacheInfoKey];
    BOOL isDir = YES;
    if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        [[NSFileManager defaultManager] createFileAtPath:path contents:nil attributes:nil];
    }
    NSDictionary *dict = @{ SPCacheSizeKey : @(cacheSize), SPCacheItemCountKey : @(itemCount) };
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];
    [data writeToFile:path atomically:YES];
    return;
}

/** 写入文件名列表 */
- (void)writeItemArray:(NSArray *)itemArray {
    NSString *path = [self.cachePath stringByAppendingString:SPCacheItemArrayKey];
    BOOL isDir = YES;
    if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        [[NSFileManager defaultManager] createFileAtPath:path contents:nil attributes:nil];
    }
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:itemArray];
    [data writeToFile:path atomically:YES];
    return;
}

- (void)removeWithKey:(NSString *)key {
    [self.lock lock];
    SPLOGS(self.logTag, @"removeWithKey,key = :%@", key);

    if (key == nil) {
        [self.lock unlock];
        return;
    }
    /** memery cache */
    if ([self.memeryCacheMap.allKeys containsObject:key] == YES) {
        [self.memeryCacheMap removeObjectForKey:key];
    }
    [self removeOneItem:key];

    [self writeItemArray:self.diskItemArray];
    [self writeItemInfo:self.diskItemCacheSize itemCount:self.diskItemCount];

    [self.lock unlock];
}

- (void)removeAll {
    [self.lock lock];
    SPLOGS(self.logTag, @"removeAll");

    /** memery cache */
    [self.memeryCacheMap removeAllObjects];
    for (int count = 0; count < self.diskItemCount; count++) {
        if ([self.diskItemArray count] > count) {
            [self removeItemPath:self.cachePath itemKey:self.diskItemArray[count]];
        }
    }
    self.diskItemCacheSize = 0;
    self.diskItemCount = 0;
    [self.diskItemArray removeAllObjects];

    [self writeItemArray:self.diskItemArray];
    [self writeItemInfo:self.diskItemCacheSize itemCount:self.diskItemCount];

    [self.lock unlock];
}
/** 清理缓存 */
- (void)clearDiskCache {
    [self.lock lock];
    NSArray *itemArray = [self.diskItemArray copy];
    for (int count = 0; count < [itemArray count]; count++) {
        NSString *path = [self.cachePath stringByAppendingString:itemArray[count]];
        BOOL isDir = YES;
        if (![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
            continue;
        }
        NSData *itemData = [NSData dataWithContentsOfFile:path];
        if (itemData == nil) {
            continue;
        }
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:itemData options:NSJSONReadingMutableLeaves error:nil];
        NSTimeInterval expireTime = [[dict objectForKey:SPCacheExpireTimeKey] doubleValue];
        NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
        /** 没有过期 */
        if (expireTime == -1 || currentTime - [self getFileLastModifyTime:self.cachePath key:itemArray[count]] <= expireTime) {
            continue;
        } else {
            [self removeOneItem:itemArray[count]];
        }
    }
    [self writeItemArray:self.diskItemArray];
    [self writeItemInfo:self.diskItemCacheSize itemCount:self.diskItemCount];
    [self.lock unlock];
}

- (NSString *)logTag {
    return @"SPLocalCache";
}
@end
