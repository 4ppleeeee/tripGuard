/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKManager.m
 Author      : chen
 Version     : 1.0
 Date        : 14-7-16
 Description :
 History     : 14-7-16 初始版本
 ***********************************************************/

#import "SPSDKManager.h"
//#import "SPReportCtlMgr.h"
//#import "SPMTAReporter.h"
#import "SPSDKParamsMgr.h"
#import "SPHLSKeyUtil.h"
//#import "SPReportFileManager.h"
#import "SPNetworkChangeManager.h"
//#import "SPBeaconReporter.h"


#define SDK_VERSION @"V1.5.0"  //版本号
#define SDK_BUILD_VERSION 0001  // build号

/////////////////////////////////////////////////////////////////////
//////////////////视频地址SDK参数配置//////////////////////////////////

/////////////////////////////////////////////////////////////////////

@interface SPSDKManager ()

@property (nonatomic, copy) NSString *platform;

@property (nonatomic, copy) NSString *sdtFrom;

@property (nonatomic, copy) NSMutableArray<SPSDKGetVInfoModel *> *sdkGetVInfoModels;   // SDK防盗链配置参数列表

@property (nonatomic, readwrite, weak) id<SPLogDelegate> logDelegate;

@property (nonatomic, assign) BOOL isRegistered;


@end

@implementation SPSDKManager

@synthesize guid = _guid;
@synthesize uid = _uid;

+ (SPSDKManager *)sharedInstance {
    static SPSDKManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[SPSDKManager alloc] init];
    });
    return manager;
}

- (id)init {
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (NSString *)version {
    return [NSString stringWithFormat:@"%@.%d", SDK_VERSION, SDK_BUILD_VERSION];
}

- (BOOL)registerWithPlatform:(NSString *)platform {
    if (_isRegistered) {
        return YES;
    }
    @synchronized(self) {
        if (platform.length <= 0) {
            SPLOGS(SP_CONFIG_LOG_FILTER, @"register failed! app platform:%@ invalid!!", platform);
            return NO;
        }
        
        [[SPSDKConfigDataManager instance] updateOnlineConfig];
        //更新信息
        self.platform = platform;
        
//        [SPReportCtlMgr sharedInstance].mtaDelegate = [SPMTAReporter sharedInstance];
//        [SPReportCtlMgr sharedInstance].beaconReporter = [SPBeaconReporter sharedInstance];
//        [SPReportCtlMgr sharedInstance].appStartTime = [[NSDate date] timeIntervalSince1970] * 1000;

        if ([NSThread isMainThread]) {
            [SPNetworkChangeManager sharedInstance];  //必须放在主线程，否则检测失效
        } else {
            dispatch_async(dispatch_get_main_queue(), ^{
                [SPNetworkChangeManager sharedInstance];  //必须放在主线程，否则检测失效
            });
        }

//        [self reportLastLaunchSavedData];

//        [[SPSRModel sharedInstance] requestSRModel];  // 请求超分模型

        _isRegistered = YES;

        return YES;
    }
}

- (BOOL)addGetVInfoPlatform:(NSString *)platform sdtFrom:(NSString *)sdtFrom vsAppkey:(NSString *)vsAppkey {
    if (platform.length <= 0 || sdtFrom.length <= 0) {
        SPLOGS(SP_CONFIG_LOG_FILTER, @"register failed! app platform:%@ sdtFrom:%@ invalid!!", platform, sdtFrom);
        return NO;
    }
    
    if (![[SPHLSKeyUtil sharedInstance] initCkeyWithGuid:_guid vsAppKey:vsAppkey]) {
        SPLOGS(SP_CONFIG_LOG_FILTER, @"register failed! init ckey failed: %@", vsAppkey);
        return NO;
    }
    
    @synchronized(self) {
        SPSDKGetVInfoModel *sdkGetVInfoModel = [[SPSDKGetVInfoModel alloc] init];
        sdkGetVInfoModel.platform = platform;
        sdkGetVInfoModel.sdtfrom = sdtFrom;
        sdkGetVInfoModel.vsAppkey = vsAppkey;
        
        if (_sdkGetVInfoModels == nil) {
            _sdkGetVInfoModels = [NSMutableArray<SPSDKGetVInfoModel *> array];
        }
        [_sdkGetVInfoModels addObject:sdkGetVInfoModel];
        [SPSDKParamsMgr sharedInstance].sdkGetVInfoModels = [_sdkGetVInfoModels copy];
        return YES;
    }
}

- (void)setGuid:(NSString *)guid {
    _guid = guid;
    [[SPSDKParamsMgr sharedInstance] setGuid:guid external:true];
}

- (void)setUid:(NSString *)uid {
    _uid = uid;
    [[SPSDKParamsMgr sharedInstance] setUid:uid];
}

- (void)setLogDelegate:(id<SPLogDelegate>)logDelegate {
    _logDelegate = logDelegate;
    // 我们在这个时机给内核设置logDelegate，因为内核可能需要在没有播放的情况下输出log
}

- (void)setPlatform:(NSString *)platform {
    _platform = platform;
    [SPSDKParamsMgr sharedInstance].platform = platform;
}

- (void)setDownloadDataDir:(NSString *)dataDir {

}

//- (void)setLogReportDelegate:(id<SPLogReportDelegate>)logReportDelegate {
//    _logReportDelegate = logReportDelegate;
//}

//- (void)reportLastLaunchSavedData {
//    NSTimeInterval delay = SPSDKCONF_delay_launch_report_ms / 1000.0;
//    SPLOGI(SP_REPORT_LOG_FILTER, @"reportLastLaunchSavedData delay:%lf", delay);
//    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC), dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//        SPLOGI(SP_REPORT_LOG_FILTER, @"reportLastLaunchSavedData");
//        [[SPReportFileManager sharedInstance] reportAllSavedData];
//    });
//}

- (void)setReportPluginDisabled:(BOOL)reportPluginDisabled {
    _reportPluginDisabled = reportPluginDisabled;
    [SPSDKParamsMgr sharedInstance].reportPluginDisabled = reportPluginDisabled;
}

- (void)setIdleTimerPluginDisabled:(BOOL)idleTimerPluginDisabled {
    _idleTimerPluginDisabled = idleTimerPluginDisabled;
    [SPSDKParamsMgr sharedInstance].idleTimerPluginDisabled = idleTimerPluginDisabled;
}

@end
