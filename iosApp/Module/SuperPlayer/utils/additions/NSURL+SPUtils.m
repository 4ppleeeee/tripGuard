/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : NSURL+SPUtils.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/9
 Description :
 History     : 17/2/9 初始版本
 ***********************************************************/

#import "NSURL+SPUtils.h"

@implementation NSURL (SPUtils)

//将url参数字符串转换为字典
+ (NSMutableDictionary *)spParseQueryComponentsFromQueryString:(NSString *)queryStr {
    return [NSURL spParseQueryComponentsFromQueryString:queryStr includingDuplicateParamName:YES];
}
+ (NSMutableDictionary *)spParseQueryComponentsFromQueryString:(NSString *)queryStr includingDuplicateParamName:(BOOL)includingDuplicateParamName {
    NSMutableDictionary *results = [NSMutableDictionary dictionary];
    if (queryStr && queryStr.length) {
        NSArray *components = [queryStr componentsSeparatedByString:@"&"];
        for (NSString *component in components) {
            //检查kv的长度，有可能没value甚至没key
            /*NSArray *kv = [component componentsSeparatedByString:@"="];
             NSString *key = kv.count > 0 ? [kv objectAtIndex:0] : nil;
             NSString *value = kv.count > 1 ? [kv objectAtIndex:1] : nil;*/
            NSRange range = [component rangeOfString:@"="];
            NSString *key, *value;
            if (range.location == NSNotFound) {
                key = component;
                value = @"";
            } else {
                key = [component substringToIndex:range.location];
                value = [component substringFromIndex:range.location + 1];
            }
            if (value == nil) value = @"";
            //必须至少有个key，value默认为空字符串
            if (key && key.length && value) {
                id existedValue = [results objectForKey:key];
                if (existedValue) {
                    //如果key已经存在且需要考虑重名参数，则将key对应的值改成一个数组
                    if (includingDuplicateParamName) {
                        if ([existedValue isKindOfClass:[NSMutableArray class]]) {
                            [existedValue addObject:value];
                        } else {
                            [results setObject:[NSMutableArray arrayWithObjects:existedValue, value, nil] forKey:key];
                        }
                    }
                } else {
                    [results setObject:value forKey:key];
                }
            }
        }
    }
    return results;
}

@end
