/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPCommonDef.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/3/30
 Description : 公共的宏定义
 History     : 17/3/30 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

/** 数据异常字符串定义 */

// 服务器返回了非 json 数据
static NSString *const gSPExpNotJsonData = @"not json data found";
// json 数据中缺少 相关字段
#define SPExpNoTopicFound(topic) [NSString stringWithFormat:@"not found topic %@", topic]

/** 功能方法定义 */

// 当前字符串为nil时，则返回@""
#define SPSafeString(str) str ? str : @""

/** weakify 和strongify定义 */

// 定义weakify，方便在block等场景使用弱引用
#ifndef weakify
#if DEBUG
#if __has_feature(objc_arc)
#define weakify(object) \
    autoreleasepool {}  \
    __weak __typeof__(object) weak##_##object = object;
#else
#define weakify(object) \
    autoreleasepool {}  \
    __block __typeof__(object) block##_##object = object;
#endif
#else
#if __has_feature(objc_arc)
#define weakify(object) \
    try {               \
    } @finally {        \
    }                   \
    {}                  \
    __weak __typeof__(object) weak##_##object = object;
#else
#define weakify(object) \
    try {               \
    } @finally {        \
    }                   \
    {}                  \
    __block __typeof__(object) block##_##object = object;
#endif
#endif
#endif

// 定义strongify，方便在block等场景使用强引用
#ifndef strongify
#if DEBUG
#if __has_feature(objc_arc)
#define strongify(object)                        \
    autoreleasepool {}                           \
    __typeof__(object) object = weak##_##object; \
    if (!object) return;
#else
#define strongify(object)                         \
    autoreleasepool {}                            \
    __typeof__(object) object = block##_##object; \
    if (!object) return;
#endif
#else
#if __has_feature(objc_arc)
#define strongify(object)                        \
    try {                                        \
    } @finally {                                 \
    }                                            \
    __typeof__(object) object = weak##_##object; \
    if (!object) return;
#else
#define strongify(object)                         \
    try {                                         \
    } @finally {                                  \
    }                                             \
    __typeof__(object) object = block##_##object; \
    if (!object) return;
#endif
#endif
#endif
