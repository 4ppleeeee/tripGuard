/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPLogDelegate.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/4/7
 Description : 日志打印协议和本地日志上传接口定义
 History     : 17/4/7 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
/**
 @discussion 日志等级.
 */
typedef enum {
    SPLogLevelVerbose,
    SPLogLevelDebug,
    SPLogLevelInfo,
    SPLogLevelSystem,
    SPLogLevelWarning,
    SPLogLevelError,
} SPLogLevel;

/**
@brief 播放器内部没有日志打印模块，为便于问题定位，APP要实现此日志打印接口，方便将播放器内的日志打印到APP的日志中，方便问题定位
*/
@protocol SPLogDelegate <NSObject>

@required

/**
@brief 日志打印接口
@param logLevel 日志打印级别
@param tag 日志tag
@param file 文件名称
@param function 函数名称
@param line 代码行
@param message message
*/
- (void)logWithLevel:(SPLogLevel)logLevel
                 tag:(NSString *)tag
                file:(const char *)file
            function:(const char *)function
                line:(NSUInteger)line
             message:(NSString *)message;

@end

/**
@brief 本地日志上传定义，为便于一些播放等问题的定位，需要将本地的日志上传到后台，便于进一步的问题分析定位
*/
@protocol SPLogReportDelegate <NSObject>

@required

/**
@brief 实现此接口后，当此接口被调用时，触发上传本地日志并携带logInfo信息到后天,用于问题分析.此接口主要用于腾讯视频
@param logInfo 上报的信息，用于腾讯视频后台解析过滤
*/
- (void)onLogReport:(NSDictionary *)logInfo;

@end
