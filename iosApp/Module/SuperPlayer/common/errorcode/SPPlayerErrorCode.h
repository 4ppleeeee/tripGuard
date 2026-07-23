/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPPlayerErrorCode.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/3/27
 Description : 错误码定义描述，统一错误码定义格式见：http://tapd.oa.com/fly/markdown_wikis/#1010146281006098067
 History     : 17/3/27 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPErrorDefine.h"


@interface SPPlayerErrorCode : NSObject

/**
 server错误码加前缀13

 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertServerErrorCode:(NSInteger)code;

/**
 网络错误码加上前缀14
 
 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertNetworkErrorCode:(NSInteger)code;

/**
 系统播放器错误码加上前缀11

 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertSystemPlayerErrorCode:(NSInteger)code;

/**
 逻辑错误，错误码加上前缀10
 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertLogicErrorCode:(NSInteger)code;

/**
 录制时设备的错误，错误码加上前缀20
 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertDeviceCaptureErrorCode:(NSInteger)code;

/**
 编码写入的时候错误，错误码加上前缀21
 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertEncoderErrorCode:(NSInteger)code;

/**
 AVError错误码，负变正
 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertAVErrorcode:(NSInteger)code;

/**
 widget错误码，错误码加上前缀22
 @param code 转换前的错误码
 @return 转换后的错误码
 */
+ (NSInteger)convertWidgetErrorcode:(NSInteger)code;

/**
 完整错误码字符串，平台号+模块号.详细错误码，比如ipad上，getvinfo，详细错误码为1362，则fullErrorCodeStr为 30101.1362
 @param module 模块号
 @param errorCode 转换前的错误码
 @return 转换后的完整错误码
 */
+ (NSString *)fullErrorCodeStrWithModule:(SPModule)module errorCode:(NSInteger)errorCode;

/**
 重建错误码，拼接上平台号
 @param error 原始通过rebuildError等生成的错误
 */
+ (NSError *)buildFullErrorCodeWithError:(NSError *)error;

/**
 重建错误码，使用errorCode替代error中的errorCode

 @param error 错误码
 @param errorCode 错误码code值
 @param module 模块号
 @return 转换后的错误码
 */
+ (NSError *)rebuildError:(NSError *)error errorCode:(NSInteger)errorCode module:(SPModule)module;

/**
 重建错误码，使用errorCode替代error中的errorCode

 @param error 错误码
 @param module 模块号
 @return 转换后的NSError实例
 */
+ (NSError *)rebuildNetWorkError:(NSError *)error module:(SPModule)module;

/**
 构建错误码

 @param errorCode error code值
 @param errMsg 错误信息描述
 @param module 模块号
 @param domain domain值
 @return 转换后的错误码
 */
+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode errMsg:(NSString *)errMsg module:(SPModule)module domain:(NSString *)domain;

/**
 构建错误码

 @param errorCode error code值
 @param errMsg 错误信息描述
 @param module 模块号
 @param domain domain
 @param data userinfo中的data
 @return 构建的错误码
 */
+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode errMsg:(NSString *)errMsg module:(SPModule)module domain:(NSString *)domain data:(id)data;

// @param errCodeStr：包含细分错误码的字符串，比如13080.1
+ (NSError *)buildErrorWithDetailErrorCodeStr:(NSString *)errCodeStr errMsg:(NSString *)errMsg module:(SPModule)module domain:(NSString *)domain;

/**
 传入下载组件的错误码，然后解析成和sdk一样的标准错误码

 @param errCodeStr 下载组件错误 错误码格式为：模块号；错误码.详细错误码 eg：101.1300061.2(详细错误码可能不存在)
 @param errMsg 错误信息
 @return sdk的错误码
 */
+ (NSError *)buildErrorWithP2pDetailErrorCodeStr:(NSString *)errCodeStr errMsg:(NSString *)errMsg;

/**
 构建错误码

 @param errorCode error code值
 @param errMsg 错误信息描述
 @param userInfo userinfo信息
 @param module 模块号
 @param domain domain
 @return 构建的错误码
 */
+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode
                              errMsg:(NSString *)errMsg
                            userInfo:(NSDictionary *)userInfo
                              module:(SPModule)module
                              domain:(NSString *)domain;

+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode
                              errMsg:(NSString *)errMsg
                           exErrCode:(NSInteger)exErrCode
                            exErrMsg:(NSString *)exErrMsg
                              module:(SPModule)module
                              domain:(NSString *)domain;

/**
 是否是播放器特殊的错误码，用于播放器的特殊处理逻辑

 @param errorCode 错误码
 @return 结果
 */
+ (BOOL)isPlayerSpecialErrorCode:(int)errorCode;

@end
