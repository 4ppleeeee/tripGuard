/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPLOGD.m
 Author      : thomasliu
 Version     : 1.0
 Date        : 12-8-27
 Description :
 History     : 12-8-27 初始版本
 ***********************************************************/

#import "SPLog.h"

void SPLog(SPLogLevel logLevel, NSString *tag, const char *file, const char *function, NSUInteger line, NSString *format, ...) {
    if ([SP_SDK_MGR_INST.logDelegate respondsToSelector:@selector(logWithLevel:tag:file:function:line:message:)]) {
        //当日志中args部分被url encode过，有很大几率会crash，decode一次来规避
        NSString *decode = [format stringByRemovingPercentEncoding];
        decode = decode ?: format;
        if (decode.length == 0) {
            return;
        }
        
        va_list arglist;
        va_start(arglist, format);
        NSString *message = [[NSString alloc] initWithFormat:decode arguments:arglist];
        if ([SP_SDK_MGR_INST.logDelegate respondsToSelector:@selector(logWithLevel:tag:file:function:line:message:)]) {
            [SP_SDK_MGR_INST.logDelegate logWithLevel:logLevel tag:tag file:file function:function line:line message:message];
        }
        va_end(arglist);
    }
}
