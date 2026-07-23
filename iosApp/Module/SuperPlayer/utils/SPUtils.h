/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPUtils.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 12-12-18
 Description : 公共工具方法
 History     : 12-12-18 初始版本
 ***********************************************************/

#ifndef SPUTILS_H
#define SPUTILS_H

#import <Foundation/Foundation.h>
#import "SPFileHelper.h"

#define SP_BOOL_STR(value) (value) ? @"YES" : @"NO"

/*
 @class SPUtils
 用户存放 iphone ipad 通用的函数代码
 包含通用的 sdk 文件头
 */
@interface SPUtils : NSObject

/**
 获取APP版本号，不带build号，形式如1.7.1

 @return APP版本号
 */
+ (NSString *)getAppVersion;

/**
 获取APP版本号，带build号，形式如1.7.1.2001

 @return APP版本号
 */
+ (NSString *)getAppBuildVersion;

/**
 获取 build 号

 @return build号
 */
+ (NSString *)getAppBuildNumber;

/**
 获取bundle id,如com.tencent.live4iphone

 @return bundle id
 */
+ (NSString *)bundleID;

/**
 获取新的UUID

 @return UUID字符串
 */
+ (NSString *)generateUUID;

/**
 字符串转NSDate,字符串形式如yyyy-MM-dd HH:mm:ss

 @param dateString 日期字符串
 @return NSDNSDate类型日期
 */
+ (NSDate *)dateFromString:(NSString *)dateString;

/**
 NSDate类型日期转日期字符串

 @param date date日期
 @return 日期字符串
 */
+ (NSString *)stringFromDate:(NSDate *)date;

/**
 计算两个日期之间的差距，过了多少天

 @param date 日期1
 @param saveDate 日期2
 @return 相差天数
 */
+ (NSInteger)getDateToDateDays:(NSDate *)date withSaveDate:(NSDate *)saveDate;

/**
 encode URL 字符串

 @param urlstr url 字符串
 @return encode后的结果
 */
+ (NSString *)safeEncodeURLString:(NSString *)urlstr;

/**
 获取url中指定参数的值

 @param url url
 @param key 指定参数key
 @return key对应的值
 */
+ (NSString *)parameterWithURL:(NSString *)url forKey:(NSString *)key;

/**
 是否允许arbitrary load

 @return 结果
 */
+ (BOOL)isAllowsArbitraryLoads;

/**
 替换url中的指定key的值

 @param url url字符串
 @param key 指定的key值
 @param value 替换的value值
 @return 替换后的结果
 */
+ (NSString *)replaceUlr:(NSString *)url key:(NSString *)key value:(NSString *)value;

/**
 当前Route的设备

 @return route的设备名称
 */
+ (NSString *)currentRouteDevice;

/**
 将字典转换为@"key1=value1&key1=value2"的字符串

 @param dictionary 要转换的字典
 @return 转换后的字符串
 */
+ (NSString *)keyValueStringFromDictionary:(NSDictionary<NSString *, NSString *> *)dictionary;

/**
 将字典转换为@"key1=value1&key1=value2"的字符串。并且，字符串是进行转义过的。

 @param dictionary 要转换的字典
 @return 转换后的字符串
 */
+ (NSString *)keyValueStringWithUrlEncodeFromDictionary:(NSDictionary<NSString *, NSString *> *)dictionary;

/**
 url中是否是带域名

 @param url url
 @return 结果
 */
+ (BOOL)isDomainUrl:(NSURL *)url;

/**
 url字符串是否是带域名的

 @param urlString urlString
 @return 结果
 */
+ (BOOL)isDomainUrlString:(NSString *)urlString;

/**
 url 列表中是否有url包含域名地址

 @param urlArray url列表
 @return 返回结果
 */
+ (BOOL)urlsContainDomain:(NSArray<NSURL *> *)urlArray;

/**
 字典转json字符串

 @param dict 字典
 @return 字符串
 */
+ (NSString *)jsonStringFromDict:(NSDictionary *)dict;

/**
 输入字符串的MD5值

 @param input 输入字符串
 @return 字符串的MD5值
 */
+ (NSString *)md5ForLowerCase:(NSString *)input;

/**
 获取obj对象的所有property名字

 @param obj obj对象
 @return property名字列表
 */
+ (NSArray *)getAllProperties:(id)obj;
@end

@interface NSObject (SPUtils)

/**
 判断当前object是否是NSString，如果是，则返回当前字符串，如果不是则返回当前对象的description

 @return 字符串
 */
- (NSString *)safeDescription;

/**
 判断当前object是否是NSString，如果不是则返回@""

 @return 字符串
 */
- (NSString *)safeString;
@end

@interface NSString (SPUtils)

/**
 用于保证解决格式化输出到文案上不显示NULL字样

 @param format 格式
 @return 字符串
 */
+ (NSString *)stringWithFormatSafely:(NSString *)format, ...;

@end

#endif
