/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : NSURL+SPUtils.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/9
 Description :
 History     : 17/2/9 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@interface NSURL (SPUtils)

/**
 将类似@"key1=value1&key1=value2"的字符串转换成字典， 允许包含重复的键值对

 @param queryStr URL请求参数
 @return 解析后的键值对字典
 */
+ (NSMutableDictionary *)spParseQueryComponentsFromQueryString:(NSString *)queryStr;

/**
 将类似@"key1=value1&key1=value2"的字符串转换成字典.根据includingDuplicateParamName决定是否要保留URL中重复的键值对

 @param queryStr URL请求参数
 @param includingDuplicateParamName 是否允许包含重复的参数
 @return 解析后的键值对字典
 */
+ (NSMutableDictionary *)spParseQueryComponentsFromQueryString:(NSString *)queryStr includingDuplicateParamName:(BOOL)includingDuplicateParamName;
@end
