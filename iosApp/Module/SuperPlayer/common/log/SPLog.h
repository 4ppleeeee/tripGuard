/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPLog.h
 Author      : thomasliu
 Version     : 1.0
 Date        : 12-8-27
 Description :日志打印模块
 History     : 12-8-27 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPLogDelegate.h"

// 各个模块的日志tag定义
// 配置
#define SP_CONFIG_LOG_FILTER @"SPConfig"
// 播放地址请求
#define SP_CGI_LOG_FILTER @"SPPlayFlow-CGI"
// 播放正片
#define SP_PLAYER_LOG_FILTER @"SPPlayFlow-Play"
// 上报
#define SP_REPORT_LOG_FILTER @"SPReport"
// MTA的内部日志打印
#define SP_MTA_LOG_FILTER @"SPMTA"
// 水印模块
#define SP_WATER_MARK_LOG_FILTER @"SPPlayFlow-WaterMark"

/**
 日志打印回调

 @param logLevel 日志打印的level级别
 @param tag 日志tag描述
 @param file 文件名称
 @param function 函数名称
 @param line 行号
 @param format format
 @param ... format对应字段
 */
FOUNDATION_EXPORT void SPLog(SPLogLevel logLevel, NSString *tag, const char *file, const char *function, NSUInteger line, NSString *format, ...);

// 简化日志打印宏，分日志level级别
#define SPLOGV(tag, frmt, ...) SPLog(SPLogLevelVerbose, tag, __FILE__, __PRETTY_FUNCTION__, __LINE__, (frmt), ##__VA_ARGS__)
#define SPLOGE(tag, frmt, ...) SPLog(SPLogLevelError, tag, __FILE__, __PRETTY_FUNCTION__, __LINE__, (frmt), ##__VA_ARGS__)
#define SPLOGI(tag, frmt, ...) SPLog(SPLogLevelInfo, tag, __FILE__, __PRETTY_FUNCTION__, __LINE__, (frmt), ##__VA_ARGS__)
#define SPLOGD(tag, frmt, ...) SPLog(SPLogLevelDebug, tag, __FILE__, __PRETTY_FUNCTION__, __LINE__, (frmt), ##__VA_ARGS__)
#define SPLOGS(tag, frmt, ...) SPLog(SPLogLevelSystem, tag, __FILE__, __PRETTY_FUNCTION__, __LINE__, (frmt), ##__VA_ARGS__)
#define SPLOGW(tag, frmt, ...) SPLog(SPLogLevelWarning, tag, __FILE__, __PRETTY_FUNCTION__, __LINE__, (frmt), ##__VA_ARGS__)
