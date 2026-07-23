//
//  SPPlayerWrapperException.h
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayingContext.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger,LEVEL) {
    LevelWarning = 0x00,   //告警级别，处理方式为日志输出
    LevelError   = 0x01,   //错误级别，直接报错通知给上层
    LevelFatal   = 0x02,   //崩溃级别，开发版本主动crash，发布版本转成error处理
    LevelRetry   = 0x03,   //重试级别，sp模块以换源来重试
};

typedef NS_ENUM(NSInteger,RetryMode) {
    RetryModeSource = 0x00,         //重试模式：换源重试，sp工程目前采用的方式
    RetryModePlayer = 0x01,         //重试模式：换播放器，sp工程目前不采用，tp工程采用
};


typedef NS_ENUM(NSInteger,LogMode) {
    LogModeImmediate = 0x00,    //日志级别：全部输出
    LogModeMedium    = 0x01,    //日志级别：中等频率，按照时间间隔输出，高频日志会丢弃
    LogModeLow       = 0x02,    //日志级别：低等频率，按照时间间隔输出，高频日志会丢弃
    LogModeDisCard   = 0x03,    //日志级别：不输出日志，上线后没有必要的日志输出采用
};



@interface CommonInfo : NSObject
@property (nonatomic , strong) NSString *message;   //异常的信息描述
@property (nonatomic , assign) int64_t   position;  //异常发生的播放位置
@property (nonatomic , assign) LEVEL     level;     //异常的级别
@property (nonatomic , assign) LogMode   logMode;   //异常日志输出的模式
@property (nonatomic , strong) NSString *state;     //异常发生的状态描述
@end

@interface ErrorInfo : NSObject
@property (nonatomic , assign) NSUInteger model;   //错误信息：模块号
@property (nonatomic , assign) NSUInteger type;    //错误信息：错误类别
@property (nonatomic , assign) NSUInteger code;    //错误信息：错误码
@property (nonatomic , copy)   NSString *exCode;   //额外错误：额外错误码
@property (nonatomic , copy)   NSString *exMessage;//额外错误：额外错误信息
@end

@interface RetryInfo : NSObject
@property (nonatomic , assign) RetryMode retryMode; //重试信息：重试类别，目前只有换源重试
@property (nonatomic , strong) SPPlayingContext* requestInfo;
@end

@interface SPPlayerWrapperException : NSObject
@property (nonatomic , strong) CommonInfo* commonInfo;  //异常信息：概要信息
@property (nonatomic , strong) ErrorInfo* errorInfo;    //异常信息：错误信息
@property (nonatomic , strong) RetryInfo* retryInfo;    //异常信息：重试信息
@end

NS_ASSUME_NONNULL_END
