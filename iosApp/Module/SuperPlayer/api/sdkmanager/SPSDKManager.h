/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKManager.h
 Author      : chen
 Version     : 1.0
 Date        : 14-7-16
 Description :
 History     : 14-7-16 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPLogDelegate.h"

#define SP_SDK_MGR_INST SPSDKManager.sharedInstance

/**
 *  SDK管理类，用户获取和设置SDK的一些公共参数
 */
@interface SPSDKManager : NSObject

/**
 *  获得单例
 *
 *  @return 管理类单例
 */
@property (class, readonly, strong) SPSDKManager *sharedInstance;

/**
 *  获取SDK版本号
 */
@property (nonatomic, readonly) NSString* version;

/**
 *  guid，cgi请求会带上，若不填写则sdk会使用自己生成的guid
 */
@property (nonatomic, copy) NSString* guid;

/**
 * 用户id.qq登陆的时，请传递qq号;微信登陆时，请传递微信openid。请求相关SDK配置时使用。在registWithAppkey之前设置生，否则不生效
 */
@property (nonatomic, copy) NSString* uid;

/*
 * 外面设置进来，用于打log
 */
@property (nonatomic, readonly, weak) id<SPLogDelegate> logDelegate;

/*
 * 外面设置进来，用于日志上传
 */
@property (nonatomic, readonly, weak) id<SPLogReportDelegate> logReportDelegate;

/*
 * 是否关闭灯塔日志上报，默认NO
 */
@property (nonatomic, assign) BOOL reportPluginDisabled;

/*
 * 是否关闭播放中常亮，默认NO
 */
@property (nonatomic, assign) BOOL idleTimerPluginDisabled;

/**
 *  初始化SDK
 *
 *  @param platform platform
 *  @return register成功返回yes，否则为no
 */
- (BOOL)registerWithPlatform:(NSString *)platform;

/**
设置下载组件信息

@param dataDir 数据目录用来存储数据(需保证存在并且可以访问)
 */
- (void)setDownloadDataDir:(NSString *)dataDir;

/**
 *  添加换链platform，sdtFrom，vsAppkey
 *
 *  @param platform platform
 *  @param sdtFrom sdtFrom
 *  @param vsAppkey 具体业务key，需要向mingyuewan(万明月)申请
 *  @return 鉴权成功返回yes，否则为no
*/
- (BOOL)addGetVInfoPlatform:(NSString *)platform sdtFrom:(NSString *)sdtFrom vsAppkey:(NSString *)vsAppkey;

/**
 * 设置日志打印delegate
 *
 * @param logDelegate SPLogDelegate类型的实现
 */
- (void)setLogDelegate:(id<SPLogDelegate>)logDelegate;

/**
 * 本地日志上传回调实现。为便于一些播放等问题的定位，需要将本地的日志上传到后台，便于进一步的问题分析定位
 * 此接口被调用时，触发上传本地日志并携带logInfo信息到后天,用于问题分析.此接口主要用于腾讯视频。
 *
 * @param logReportDelegate SPLogReportDelegate实现类
 */
//- (void)setLogReportDelegate:(id<SPLogReportDelegate>)logReportDelegate;

@end
