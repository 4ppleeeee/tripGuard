/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKDefaultConfig.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/7/31
 Description : app通过彩蛋设置更改配置，更改播放器执行逻辑
 History     : 2017/7/31 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@interface SPSDKDefaultConfig : NSObject

+ (SPSDKDefaultConfig *)sharedInstance;

- (void)setBool:(BOOL)value forKey:(NSString *)key;

- (void)setInt:(int)value forKey:(NSString *)key;

- (BOOL)hasValueForKey:(NSString *)key;

- (int)intValueForKey:(NSString *)key;

- (BOOL)boolValueForKey:(NSString *)key;

- (NSDictionary *)dictValueForKey:(NSString *)key;

- (NSArray *)arrayValueForKey:(NSString *)key;

- (void)removeValueForKey:(NSString *)key;

- (NSArray<NSString *> *)customKeyList;

- (void)setValue:(NSString *)value customKey:(NSString *)key;

- (NSString *)valueForCustomKey:(NSString *)key;
@end
