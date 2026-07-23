/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : QLConfigDataManager.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 14/7/15
 Description :
 History     : 14/7/15 初始版本
 ***********************************************************/

#import <AVFoundation/AVFoundation.h>
#import "SPSDKConfigDataManager.h"
#import "SPSDKParamsMgr.h"
#import "SPVcSystemInfo.h"
#import "SPResource.h"
//#import "SPReportCtlMgr.h"
//#import "SPStatusReportCtl.h"
#import "SPNetWorkManager.h"
#import "SPATSHTTPRequest.h"
#import "SPJSONResponse.h"
#import "SPNetworkChecker.h"
#import "SPSDKDefaultConfig.h"
#import "SPSDKLogManager.h"
#import "SPFileHelper.h"
//#import "SPReportMap.h"
#import "SPPlayerErrorCode.h"

#define SET_CONFIG_KEY_VALUE(key, value) [self.configCacheDict setObject:value forKey:key]
// 多语言配置 key
#define SPSDK_LANG_LIST_KEY @"spsdk_lang_list"
#define SPSDK_URLS_LIST_KEY @"spsdk_urls_list"

static NSString const *gGlobalTopic = @"Topic";
static NSString const *gSPSDKConfigDefaultPrefixKey = @"SPSDK_Config_Defaults_Key";
static NSString *gDelayUploadLogKey = @"on_launch_upload_log_delay_ms";
static NSString *gEnableForceUploadLogKey = @"enable_on_launch_upload_log";

@interface SPSDKConfigDataManager ()

// 在线配置的数据, 启动时，读取上一次保存到本地的在线配置，后台下发新配置，指向新配置.
@property (readwrite, strong) NSDictionary *propConfigDict;
// 本地默认的配置数据, 不可变
@property (readwrite, strong) NSMutableDictionary *configCacheDict;

// 内存存储语言字典
// 在线配置的语言和域名链接，本地配置没有默认值
@property (readwrite, strong) NSDictionary *langDict;
@property (readwrite, strong) NSDictionary *urlsDict;
@property (nonatomic, copy) NSString *requestingURL;
@property (nonatomic, assign) NSTimeInterval lastRequestCongfigTime;
@property (nonatomic, strong) NSString *sdkConfigDefaultKey;

@property (nonatomic, strong) SPATSHTTPRequest *httpRequest;

@end

@implementation SPSDKConfigDataManager

static const NSString *kOnlineConfigRequestKey = @"onlineConfig";

+ (SPSDKConfigDataManager *)instance {
    static SPSDKConfigDataManager *s_inst = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_inst = [[SPSDKConfigDataManager alloc] init];
    });
    return s_inst;
}

- (id)init {
    if (self = [super init]) {
        SPLOGI(SP_CONFIG_LOG_FILTER, @"config new init begin -----");

        //        [self setUpDefaultConfig];
        self.lastRequestCongfigTime = 0.0;
        // 读取保存在本地的在线配置
        self.sdkConfigDefaultKey = [NSString stringWithFormat:@"%@_%@", gSPSDKConfigDefaultPrefixKey, SPSDKManager.sharedInstance.version];
        NSDictionary *dicCache = [[NSUserDefaults standardUserDefaults] objectForKey:_sdkConfigDefaultKey];
        if (dicCache && [dicCache isKindOfClass:[NSDictionary class]]) {
            self.propConfigDict = dicCache;
        } else {
            self.propConfigDict = nil;
        }

        [self setUpDefaultConfig];
        SPLOGI(SP_CONFIG_LOG_FILTER, @"config new init  local config:%@, cache config:%@", self.configCacheDict, self.propConfigDict);
        [self syncLangAndUrlList];
        [SPSDKParamsMgr sharedInstance].playerConfigId = [self.propConfigDict spStringForKeySafeModel:@"player_confid"];
        // 监听切前台切换
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(applicationEnterForegroud)
                                                     name:UIApplicationWillEnterForegroundNotification
                                                   object:nil];
        SPLOGI(SP_CONFIG_LOG_FILTER, @"config new init end -----");
    }

    return self;
}
#pragma-- mark 本地默认配置
// NOLINTNEXTLINE
- (void)setUpDefaultConfig {
    self.configCacheDict = [NSMutableDictionary dictionaryWithCapacity:0];
    NSMutableArray *playerSpecialErrorList = [NSMutableArray arrayWithCapacity:0];
    [playerSpecialErrorList addObject:@(11800)];
    [playerSpecialErrorList addObject:@(11839)];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAYER_SPECIAL_ERROR_LIST, playerSpecialErrorList);
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_DELAY_LAUNCH_REPORT_MS, @(1000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SUPPORT_DOLBY_AUDIO_PLAY, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SUPPORTMTA, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_LOG_SECOND_BUFFERING_TIMES_MAX, @(3));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_LOG_PLAYER_BIG_JUMP_TIMES_MAX, @(5));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_MAX_CODEC_ERROR_COUNT, @(100));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LIVEQUEUEENABLE, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_HEVC_OPTIMIZATION, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_LIVE_QUEUE, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_RESET_NETWORK_SPEED_PEROID, @(60));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REQUEST_CONFIG_MIN_INTERVAL, @(1800));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_ON_LAUNCH_UPLOAD_LOG, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_AUDIO_PLAY, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_UNIT_OF_TIME_TO_JUDGE_SKIP_FRAMES, @(5000000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_VIDEO_DECODER_MODE, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_SEAMLESS_SWITCH_DEFINITION, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_PRELOAD_NEXT_VID, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_TCP_RETRY_TIMES, @(5));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_SEEK_INFO_MAX, @(20));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SYS_PLAYER_SWITCH_TO_SELF_WHEN_TIME_OUT, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_FAIRPLAY, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_GETVINFO_ENV, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CONFIG_ENABLE_UPDATE, @(0));
    NSMutableArray *specialUidArray = [NSMutableArray arrayWithCapacity:0];
    [specialUidArray addObject:@"MzkzMzIzMjA2"];
    [specialUidArray addObject:@"MzMxNDI0MzA5"];
    [specialUidArray addObject:@"NzY0ODY5NzA="];
    [specialUidArray addObject:@"MzkyNzIzMTQ4"];
    [specialUidArray addObject:@"MTA3MjMyNDI1"];
    [specialUidArray addObject:@"MTU1MjE5OTgwNQ=="];
    [specialUidArray addObject:@"NjM0OTc2NjI="];
    [specialUidArray addObject:@"MTQxNDQ5MzQyNA=="];
    [specialUidArray addObject:@"MTkyODczODYwOQ=="];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SPECIAL_UID_ARRAY, specialUidArray);
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_NEED_CHARGE_P2P, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_UID_RANGE_LOW, @(10001));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_GET_VINFO_CLIP, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LIVE_PACKET_BUFFER_TIME_MS, @(10000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_GET_VINFO_CAN_USE_HTTPS, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_LOG_PRINT_ENABLE, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_MTA_PRINT_LOG, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_FEITIAN_PLAYER_REPORT_SAMPLE, @(100));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_SELF_PLAYER, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_LOG, @(32775));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_AUDIO_DECODER_MODE, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_SECOND_BUFFER_INFO_MAX, @(20));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAYER_OFFLINE_BUFFERING_TIME_OUT, @(5));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SUPPORT_SDR_PLUS, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_HEVC_LEVEL, @(-1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_LOG_FIRST_LOADING_TIME_MAX_MS, @(3000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAYER_ONLINE_BUFFERING_TIME_OUT, @(12));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAYER_EXTERNAL_URL_BUFFERING_TIME_OUT, @(40));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_VTB_OUTPUT_PIC_TYPE, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ACCURATE_SEEK_DURATION_THRESHOLD, @(300));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ACCURATE_SEEK_START_POSITION_THRESHOLD, @(10000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_VOD_MP4_PACKET_BUFFER_TIME_MS, @(12000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_TCP_TIME_OUT_MS, @(5000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_START_BUFFER_DELAY, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_DELAY_GET_CONFIG_MS, @(1000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_MIN_BUFFER_SIZE_AFTER_SEEK, @(100));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_GET_VINFO_DLTYPE, @(3));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAYER_RENDER_TYPE, @(102));  // TODO:这里不能定义成枚举值，hemanli
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_AUDIO_RENDER_MODE, @(-1));    // 和SPPlayerRenderMode的音频模式对齐
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_LIVE_HLS_P2P, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_LIVE_FLV_P2P, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_REPORT_CACHE_MIN_INTERVAL_MS, @(1200));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_START_BUFFER_DELAY_TIME_MS, @(500));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_DOLBY_VISION, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_HDR10, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK_USE_PROXY, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_DOLBY_OUTPUT_REFERENCE_LEVEL, @(-17));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_LIVE_DOLBY_AUDIO, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_LIVE_DOLBY_VISION, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_DOLBY_AUDIO_SOFT_DECODE, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LIVE_ACTIVE_SP, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_BLU_RAY_BUFFER_TIME_OUT_MS, @(60000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_UPLOAD_SPECIAL_UID_LOG, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PRELOAD_BUFFER_TIME_MS, @(2000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CHECK_NETWORK_SPEED_PEROID_MS, @(1000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_UID_RANGE_HIGH, @(10030));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CKEY_VERSION, @"4.2");
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_NON_BLU_RAY_BUFFER_TIME_OUT_MS, @(60000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ON_LAUNCH_UPLOAD_LOG_DELAY_MS, @(2000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_NEED_TAKE_SAMPLE, @(0));
    NSMutableDictionary *testDict = [NSMutableDictionary dictionaryWithCapacity:0];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_P2PHTTPPROXYCONFIG, testDict);
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_VOD_HLS_PACKET_BUFFER_TIME_MS, @(6000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_VOD_OFFLINE_HLS_PACKET_BUFFER_TIME_MS, @(40000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_WATERMARK, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_LIVE_WATERMARK, @(1));
    NSMutableArray *supportDolbyAudioDeviceList = [NSMutableArray arrayWithCapacity:0];
    [supportDolbyAudioDeviceList addObject:@"iPhone6,1"];
    [supportDolbyAudioDeviceList addObject:@"iPhone6,2"];
    [supportDolbyAudioDeviceList addObject:@"iPhone7,1"];
    [supportDolbyAudioDeviceList addObject:@"iPhone7,2"];
    [supportDolbyAudioDeviceList addObject:@"iPhone8,1"];
    [supportDolbyAudioDeviceList addObject:@"iPhone8,2"];
    [supportDolbyAudioDeviceList addObject:@"iPhone8,4"];
    [supportDolbyAudioDeviceList addObject:@"iPhone9,1"];
    [supportDolbyAudioDeviceList addObject:@"iPhone9,2"];
    [supportDolbyAudioDeviceList addObject:@"iPhone9,3"];
    [supportDolbyAudioDeviceList addObject:@"iPhone9,4"];
    [supportDolbyAudioDeviceList addObject:@"iPhone10,1"];
    [supportDolbyAudioDeviceList addObject:@"iPhone10,2"];
    [supportDolbyAudioDeviceList addObject:@"iPhone10,3"];
    [supportDolbyAudioDeviceList addObject:@"iPhone10,4"];
    [supportDolbyAudioDeviceList addObject:@"iPhone10,5"];
    [supportDolbyAudioDeviceList addObject:@"iPhone10,6"];
    [supportDolbyAudioDeviceList addObject:@"iPhone11,2"];
    [supportDolbyAudioDeviceList addObject:@"iPhone11,4"];
    [supportDolbyAudioDeviceList addObject:@"iPhone11,6"];
    [supportDolbyAudioDeviceList addObject:@"iPhone11,8"];
    [supportDolbyAudioDeviceList addObject:@"iPod7,1"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,1"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,2"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,3"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,4"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,5"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,6"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,7"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,8"];
    [supportDolbyAudioDeviceList addObject:@"iPad4,9"];
    [supportDolbyAudioDeviceList addObject:@"iPad5,1"];
    [supportDolbyAudioDeviceList addObject:@"iPad5,2"];
    [supportDolbyAudioDeviceList addObject:@"iPad5,3"];
    [supportDolbyAudioDeviceList addObject:@"iPad5,4"];
    [supportDolbyAudioDeviceList addObject:@"iPad6,3"];
    [supportDolbyAudioDeviceList addObject:@"iPad6,4"];
    [supportDolbyAudioDeviceList addObject:@"iPad6,7"];
    [supportDolbyAudioDeviceList addObject:@"iPad6,8"];
    [supportDolbyAudioDeviceList addObject:@"iPad6,11"];
    [supportDolbyAudioDeviceList addObject:@"iPad6,12"];
    [supportDolbyAudioDeviceList addObject:@"iPad7,1"];
    [supportDolbyAudioDeviceList addObject:@"iPad7,2"];
    [supportDolbyAudioDeviceList addObject:@"iPad7,3"];
    [supportDolbyAudioDeviceList addObject:@"iPad7,4"];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SUPPORT_DOLBY_AUDIO_DEVICE_LIST, supportDolbyAudioDeviceList);

    NSMutableArray *dolbyVisionBlackList = [NSMutableArray arrayWithCapacity:0];
    [dolbyVisionBlackList addObject:@"iPad1,1"];
    [dolbyVisionBlackList addObject:@"iPad2,1"];
    [dolbyVisionBlackList addObject:@"iPad2,2"];
    [dolbyVisionBlackList addObject:@"iPad2,3"];
    [dolbyVisionBlackList addObject:@"iPad2,4"];
    [dolbyVisionBlackList addObject:@"iPad2,5"];
    [dolbyVisionBlackList addObject:@"iPad2,6"];
    [dolbyVisionBlackList addObject:@"iPad2,7"];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_DOLBY_VISION_BLACKLIST, dolbyVisionBlackList);
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_WWAN_P2P, @(1));

    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CGI_RETRY_MAX_TIMES, @(3));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CGI_USE_CACHE, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_NETWORK_TIME_OUT_INTERVAL, @(10));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_SELF_ENCRYPTION, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LIVE_ENABLE_SELF_ENCRYPTION, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PREFER_IPV6_IN_IP_STACK_DUAL, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_GET_VINFO_CAN_USE_HTTPS, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_ONLINE_VOD_P2P, @(1));  //走下载组件
    NSMutableDictionary *dlnaHLSDeviceList = [NSMutableDictionary dictionaryWithCapacity:0];
    [dlnaHLSDeviceList setObject:@(1) forKey:@"Hisilicon MediaRenderer_*_*"];
    [dlnaHLSDeviceList setObject:@(1) forKey:@"YunTVPlayer_1.0_www.yunos.com"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"*_*_SkyworthSRI"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"BCM7405_*_TOPWAY"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"Xbox One_*_Microsoft Corporation"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"*_*_Skyworth"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"*_*_Hisense"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"100TV Media Render_1.0_100TV"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"TCL Media Render_*_TCL"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"Smart TV_*_ChangHong Electronics"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"mikan_tv_*_*"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"*_MINT1.7.0.1_Sony Corporation"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"*_AllShare1.0_Samsung Electronics"];
    [dlnaHLSDeviceList setObject:@(2) forKey:@"Auto-generated device_*_Intel Labs"];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_FREE_FLOW_P2P_PLAY, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LOG_AUTO_UPLOAD_SAMPLE, @(10));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LOG_AUTO_UPDATE_ERROR_CODE, @"0");
    NSMutableArray *supportDolbyVisionDeviceList = [NSMutableArray array];
    [supportDolbyVisionDeviceList addObject:@"iPhone10,1"];
    [supportDolbyVisionDeviceList addObject:@"iPhone10,2"];
    [supportDolbyVisionDeviceList addObject:@"iPhone10,3"];
    [supportDolbyVisionDeviceList addObject:@"iPhone10,4"];
    [supportDolbyVisionDeviceList addObject:@"iPhone10,5"];
    [supportDolbyVisionDeviceList addObject:@"iPhone10,6"];
    [supportDolbyVisionDeviceList addObject:@"iPhone11,2"];
    [supportDolbyVisionDeviceList addObject:@"iPhone11,4"];
    [supportDolbyVisionDeviceList addObject:@"iPhone11,6"];
    [supportDolbyVisionDeviceList addObject:@"iPhone11,8"];
    [supportDolbyVisionDeviceList addObject:@"iPad7,1"];
    [supportDolbyVisionDeviceList addObject:@"iPad7,2"];
    [supportDolbyVisionDeviceList addObject:@"iPad7,3"];
    [supportDolbyVisionDeviceList addObject:@"iPad7,4"];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SUPPORT_DOLBY_VISION_DEVICE_LIST, supportDolbyVisionDeviceList);
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_SUPPORT_HDR10_DEVICE_LIST, supportDolbyVisionDeviceList);  // 跟dolbyvision相同
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_POSITION_UPDATE_INTERVAL, @(0.4));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_H264_LEVEL, @(-1));
    NSMutableDictionary *resolutionDict = [NSMutableDictionary dictionaryWithCapacity:0];

    NSMutableDictionary *hdDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [hdDict setObject:@(2) forKey:@"pos"];
    [hdDict setObject:@"高清 480P" forKey:@"fullname"];
    [hdDict setObject:@"高清" forKey:@"name"];
    [hdDict setObject:@"高清增强 480P" forKey:@"srname"];
    [resolutionDict setObject:hdDict forKey:@"hd"];

    NSMutableDictionary *mp4Dict = [NSMutableDictionary dictionaryWithCapacity:0];
    [mp4Dict setObject:@(2) forKey:@"pos"];
    [mp4Dict setObject:@"高清 480P" forKey:@"fullname"];
    [mp4Dict setObject:@"高清" forKey:@"name"];
    [mp4Dict setObject:@"高清增强 480P" forKey:@"srname"];
    [resolutionDict setObject:mp4Dict forKey:@"mp4"];

    NSMutableDictionary *flvDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [flvDict setObject:@(4) forKey:@"pos"];
    [flvDict setObject:@"流畅 180P" forKey:@"fullname"];
    [flvDict setObject:@"流畅" forKey:@"name"];
    [flvDict setObject:@"流畅增强 180P" forKey:@"srname"];
    [resolutionDict setObject:flvDict forKey:@"flv"];

    NSMutableDictionary *fhdDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [fhdDict setObject:@(0) forKey:@"pos"];
    [fhdDict setObject:@"蓝光 1080P" forKey:@"fullname"];
    [fhdDict setObject:@"蓝光" forKey:@"name"];
    [fhdDict setObject:@"蓝光增强 1080P" forKey:@"srname"];
    [resolutionDict setObject:fhdDict forKey:@"fhd"];

    NSMutableDictionary *sdDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [sdDict setObject:@(3) forKey:@"pos"];
    [sdDict setObject:@"标清 270P" forKey:@"fullname"];
    [sdDict setObject:@"标清" forKey:@"name"];
    [sdDict setObject:@"标清增强 270P" forKey:@"srname"];
    [resolutionDict setObject:sdDict forKey:@"sd"];

    NSMutableDictionary *msdDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [msdDict setObject:@(4) forKey:@"pos"];
    [msdDict setObject:@"流畅 180P" forKey:@"fullname"];
    [msdDict setObject:@"流畅" forKey:@"name"];
    [msdDict setObject:@"流畅增强 180P" forKey:@"srname"];
    [resolutionDict setObject:msdDict forKey:@"msd"];

    NSMutableDictionary *shdDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [shdDict setObject:@(1) forKey:@"pos"];
    [shdDict setObject:@"超清 720P" forKey:@"fullname"];
    [shdDict setObject:@"超清" forKey:@"name"];
    [shdDict setObject:@"超清增强 720P" forKey:@"srname"];
    [resolutionDict setObject:shdDict forKey:@"shd"];

    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LIVE_PERIOD_INTERVAL_MS, @(60000));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAYER_RETRY_COUNT, @(5));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_HEVC, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_HEVC_OFFLINE, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_USE_METAL_MIN_SYS_VER, @(9.0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAY_IN_SUB_THREAD, @(1));

    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_EXTERNAL_URL_PLAY_STRATEGY, @(2));  // 和SPPlayerScheduleStrategy定义对齐

    NSMutableDictionary *urlDict = [NSMutableDictionary dictionaryWithCapacity:0];
    [urlDict setObject:@"https://sdkconfig.video.qq.com/getmfomat?platform=iphone_sdk&otype=json" forKey:@"sdkConfigURL"];
    [urlDict setObject:@"https://vv.video.qq.com/checktime" forKey:@"checkTimeURL"];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_URLS_LIST, urlDict);
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_URLS_LIST, urlDict);

    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_GETVINFO_CARRY_M3U8, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CGI_USE_HTTPS, @(1));
    
    NSMutableArray *p2pDeviceBlackList = [NSMutableArray arrayWithCapacity:0];
    [p2pDeviceBlackList addObject:@"iPod5,1"];
    [p2pDeviceBlackList addObject:@"iPhone4,1"];
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_P2P_DEVICE_BLACKLIST, p2pDeviceBlackList);

    //上报开关
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_MTA_REPORT, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_BEACON_REPORT, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_UPDATE_POSITION_LOG_PRINT_FREQUENCE, @(10));

// SDK与主线不相同的配置,重新设置配置
#if SP_TARGET_PLAYER_SDK
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_default_switch_deinition_type, @(1));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_enable_new_system_player, @(1));  //速看
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_LOG_AUTO_UPLOAD_SAMPLE, @(100));  //速看
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_PLAY_IN_SUB_THREAD, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_DOLBY_AUDIO_SOFT_DECODE, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK, @(0));
    SET_CONFIG_KEY_VALUE(SPSDKCONFKEY_CKEY_VERSION, @"4.2");
#endif
}
- (void)syncLangAndUrlList {
    @synchronized(self.langDict) {
        NSDictionary *langList = [self.propConfigDict objectForKey:SPSDK_LANG_LIST_KEY];
        if (self.propConfigDict && [langList isKindOfClass:[NSDictionary class]]) {
            // 多语言配置
            self.langDict = langList;
        }
    }

    @synchronized(self.urlsDict) {
        NSDictionary *urllist = [self.propConfigDict objectForKey:SPSDK_URLS_LIST_KEY];
        if (self.propConfigDict && [urllist isKindOfClass:[NSDictionary class]]) {
            // 多地址配置
            self.urlsDict = urllist;
        }
    }
}

- (void)setConfigWithDict:(NSDictionary *)dict saveConfig:(BOOL)isSaveConfig {
    if ([dict objectForKey:@"errorcode"] && 0 != [[dict objectForKey:@"errorcode"] intValue] &&
        -9996 != [[dict objectForKey:@"errorcode"] intValue])  //无记录不上报错误
    {
        // 拉取多终端配置错误，不处理
        return;
    }

    @synchronized(self.propConfigDict) {
        self.propConfigDict = dict;
        [self syncLangAndUrlList];
    }

    NSString *player_configid = [dict spStringForKeySafeModel:@"player_confid"];
    NSString *oldPlayerConfigId = [SPSDKParamsMgr sharedInstance].playerConfigId;
    [SPSDKParamsMgr sharedInstance].playerConfigId = player_configid;
    if (player_configid.length > 0 && ![oldPlayerConfigId isEqualToString:player_configid]) {
        BOOL enbaleUploadLog = [self getConfigPropertyBool:gEnableForceUploadLogKey];
        SPLOGI(SP_CONFIG_LOG_FILTER, @"enbaleUploadLog:%d", enbaleUploadLog);
        if (enbaleUploadLog) {
            NSTimeInterval uploadLogAsyncTimeInterval = [self getConfigPropertyDouble:gDelayUploadLogKey] / 1000.0;
            [[SPSDKLogManager sharedInstance] uploadLogAsyncAfter:uploadLogAsyncTimeInterval];
        }
    }
    if (isSaveConfig) {
        SPLOGI(SP_CONFIG_LOG_FILTER, @"after request, onlineconfig, delay_get_config_ms:%d", SPSDKCONF_DELAY_GET_CONFIG_MS);
        [[NSUserDefaults standardUserDefaults] setObject:(dict ?: @{}) forKey:self.sdkConfigDefaultKey];
    } else {
        //        SPLOGI(@"saved onlineconfig , delay_get_config_ms:%d",SPSDKCONF_DELAY_GET_CONFIG_MS);
    }
}

- (void)updateOnlineConfig {
    if (!SPSDKCONF_CONFIG_ENABLE_UPDATE) {
        return;
    }
    SPLOGI(SP_CONFIG_LOG_FILTER, @"getOnlineConfig begin");
    SPLOGI(SP_CONFIG_LOG_FILTER, @"before request, delay_get_config_ms:%d", SPSDKCONF_DELAY_GET_CONFIG_MS);
    if (SPSDKCONF_DELAY_GET_CONFIG_MS == 0) {
        [self getOnlineConfigRequest];
    } else {
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(getOnlineConfigRequest) object:nil];
        int delay_get_config_ms = SPSDKCONF_DELAY_GET_CONFIG_MS;
        NSTimeInterval delay = delay_get_config_ms / 1000.0;
        SPLOGI(SP_CONFIG_LOG_FILTER, @"getOnlineConfig delay:%lf", delay);
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delay * NSEC_PER_SEC), dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self getOnlineConfigRequest];
        });
    }
}

// p2p动态配置
// NOLINTNEXTLINE
- (NSString *)sdkConfigURL {
    NSString *appPath = [SPFileHelper getAppPath];
    NSFileManager *fileMgr = [NSFileManager defaultManager];
    NSDictionary *appAttributes = [fileMgr attributesOfItemAtPath:appPath error:nil];
    NSDate *appFileModifyDate = [appAttributes objectForKey:NSFileModificationDate];
    NSTimeInterval time = [appFileModifyDate timeIntervalSince1970];
    NSNumber *timeNum = [NSNumber numberWithDouble:time];

    // 更换成当前登录的用户id
    NSString *qqNO = SPSDKPARAMS_UID;

#if CONFIG_TEST
    SPLOGI(SP_CONFIG_LOG_FILTER, @"use test configuration");
    NSString *configURL = @"http://10.156.36.16:8080/getmfomat?platform=iphone_sdk&otype=json";
    NSString *guid = nil;
    if ([SPSDKParamsMgr sharedInstance].isExternalGuid || [SPSDKPARAMS_GUID isEqualToString:[SPVcSystemInfo sharedInstance].localGuid]) {
        guid = SPSDKPARAMS_GUID;
    } else {
        guid = @"";
    }
    // NOLINTNEXTLINE
    NSString *ret = [NSString stringWithFormat:@"%@&player_channel_id=%@&uin=%@&appver=%@&lang=%@&guid=%@&device_id=%@&market_id=%d&mac=%@&install_time=%llu&width=%d&height=%d&model=%@&submodel=%@&osver=%@&sysver=%@&network_type=%d&native_version=%@&cpuarch=%lu&cpuname=%@&cpufreq=%ld&numofcpucore=%ld&ipflag=%d&random=%u",
                                               configURL,
                                               @"197",                                             //player_channel_id
                                               (qqNO ? qqNO : @""),                                //uin
                                               @"V2.0.197.1055",                                   //appver sdk的版本号
                                               [[NSLocale currentLocale] localeIdentifier],        //lang 语言
                                               guid,                                               //guid
                                               [SPVcSystemInfo sharedInstance].deviceId,          //device_id
                                               ([SPVcSystemInfo sharedInstance].isJBOS ? 1 : 0),  //market_id 复用为是否越狱
                                               [SPVcSystemInfo sharedInstance].macAddress,        //mac address
                                               [timeNum longLongValue],
                                               (int)[SPVcSystemInfo sharedInstance].screenWidth,               //width
                                               (int)[SPVcSystemInfo sharedInstance].screenHeight,              //height
                                               [SPVcSystemInfo sharedInstance].deviceMachineFamily,            //model
                                               [SPVcSystemInfo sharedInstance].deviceMachineConventionalName,  //submodel
                                               [SPVcSystemInfo sharedInstance].osVer,                          //osver
                                               [SPVcSystemInfo sharedInstance].systemVer,                      //sysver
                                               ([SPNetworkChecker activeWLAN] ? 1 : [SPNetworkChecker sharedInstance].cellNetType),
                                               [[SPP2PManager sharedInstance] getVersion],  // native_version下载组件的版本号
                                               [SPVcSystemInfo sharedInstance].cpuSubtype,  //cpuarch
                                               @"",                                          //cpuname, cpu名称
                                               [SPVcSystemInfo sharedInstance].cpuFrequency,
                                               [SPVcSystemInfo sharedInstance].cpuCount,
                                               0,                        //ipflag 是否返回地域信息
                                               arc4random_uniform(1000)  //随机数，一定程度防止劫持
    ];
    return ret;

#else
    NSString *configURL = SP_RESOURCE_URL(sdkConfigURL);
    //#endif

    if (!configURL.length) {
        // 终极异常保护
        configURL = @"https://sdkconfig.video.qq.com/getmfomat?platform=iphone_sdk&otype=json";
    }
    NSString *guid = nil;
    if ([SPSDKParamsMgr sharedInstance].isExternalGuid || [SPSDKPARAMS_GUID isEqualToString:[SPVcSystemInfo sharedInstance].localGuid]) {
        guid = SPSDKPARAMS_GUID;
    } else {
        guid = @"";
    }
    
    // NOLINTNEXTLINE
    NSString *ret = [NSString stringWithFormat:@"%@&player_channel_id=%@&uin=%@&appver=%@&lang=%@&guid=%@&device_id=%@&market_id=%d&mac=%@&install_time=%llu&width=%d&height=%d&model=%@&submodel=%@&osver=%@&sysver=%@&network_type=%d&cpuarch=%lu&cpuname=%@&cpufreq=%ld&numofcpucore=%ld&ipflag=%d&random=%u",
                                               configURL,
                                               [SPSDKParamsMgr sharedInstance].playerChannelId,   //player_channel_id
                                               (qqNO ? qqNO : @""),                                //uin
                                               [SPSDKManager.sharedInstance version],           //appver sdk的版本号
                                               [[NSLocale currentLocale] localeIdentifier],        //lang 语言
                                               guid,                                               //guid
                                               [SPVcSystemInfo sharedInstance].deviceId,          //device_id
                                               ([SPVcSystemInfo sharedInstance].isJBOS ? 1 : 0),  //market_id 复用为是否越狱
                                               [SPVcSystemInfo sharedInstance].macAddress,        //mac address
                                               [timeNum longLongValue],
                                               (int)[SPVcSystemInfo sharedInstance].screenWidth,               //width
                                               (int)[SPVcSystemInfo sharedInstance].screenHeight,              //height
                                               [SPVcSystemInfo sharedInstance].deviceMachineFamily,            //model
                                               [SPVcSystemInfo sharedInstance].deviceMachineConventionalName,  //submodel
                                               [SPVcSystemInfo sharedInstance].osVer,                          //osver
                                               [SPVcSystemInfo sharedInstance].systemVer,                      //sysver
                                               ([SPNetworkChecker activeWLAN] ? 1 : [SPNetworkChecker sharedInstance].cellNetType),
                                               [SPVcSystemInfo sharedInstance].cpuSubtype,  //cpuarch
                                               @"",                                          //cpuname, cpu名称
                                               [SPVcSystemInfo sharedInstance].cpuFrequency,
                                               [SPVcSystemInfo sharedInstance].cpuCount,
                                               0,                        //ipflag 是否返回地域信息
                                               arc4random_uniform(1000)  //随机数，一定程度防止劫持
    ];
    return ret;
#endif
}

// NOLINTNEXTLINE
- (void)getOnlineConfigRequest {
    SPLOGI(SP_CONFIG_LOG_FILTER, @"getOnlineConfigRequest begin");

    NSString *configURL = [self sdkConfigURL];
    configURL = [configURL
        stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet characterSetWithCharactersInString:@"`#%^{}\"[]|\\<> "].invertedSet];

    self.requestingURL = configURL;
    if (!_httpRequest) {
        [_httpRequest cancel];
        _httpRequest = nil;
    }

    SPLOGI(SP_CONFIG_LOG_FILTER, @"拉取配置开始 url:%@", _requestingURL);
    SPATSHTTPRequest *request = [[SPNetWorkManager shareInstance]
               getRequest:_requestingURL
           requestHeaders:nil
        completionHandler:^(NSData *_Nullable responseData, NSError *_Nullable error) {

            if (!error) {
                self.lastRequestCongfigTime = [[NSDate date] timeIntervalSince1970];
                NSError *resultErr = error;
                SPJSONResponse *response = nil;
                if (resultErr == nil) {
                    response = [[SPJSONResponse alloc] init];
                    resultErr = [response processResponseData:responseData];
                }

                if (resultErr) {
                    SPLOGW(SP_CONFIG_LOG_FILTER, @"request error:%@", resultErr);
                    self.requestingURL = nil;
                    SPLOGE(SP_CONFIG_LOG_FILTER, @"拉取配置失败 error:%@", error);
                } else {
                    NSDictionary *root = response.rootObject;
                    if (root) {
                        SPLOGI(SP_CONFIG_LOG_FILTER, @"拉取配置结束 content:%@", root);

                        [self setConfigWithDict:root saveConfig:YES];

                        // Get GUID
                        NSString *guid = [root spStringForKeySafeModel:@"guid"];
                        if (guid.length && [guid rangeOfString:@" "].location != NSNotFound) {
//                            [[SPReportCtlMgr sharedInstance] reportEventIdentifier:@"app_cgi_guid_contain_space" params:@{ @"guid" : guid }];
                            // 判断 guid 合法性，不能包含空格 ethanyxliu 20150228
                            guid = [guid stringByReplacingOccurrencesOfString:@" " withString:@""];
                        }
                        //更新guid，外部设置的guid不保存在sdk的存储中.
                        if (guid.length > 0 && ![SPSDKParamsMgr sharedInstance].isExternalGuid) {
                            [[SPVcSystemInfo sharedInstance] updateLocalGuid:guid];
                        }
                        SPLOGI(SP_CONFIG_LOG_FILTER, @"after request,localGuid:%@", [SPVcSystemInfo sharedInstance].localGuid);
                        SPLOGI(SP_CONFIG_LOG_FILTER, @"after request,is using guid:%@", SPSDKPARAMS_GUID);
                        // 判断是否返回了错误吗，如果返回，上报异常
                        if ([root objectForKey:@"errorcode"] && 0 != [[root objectForKey:@"errorcode"] intValue] &&
                            -9996 != [[root objectForKey:@"errorcode"] intValue])  //无记录不上报错误
                        {
                            SPLOGI(SP_CONFIG_LOG_FILTER, @"拉取多终端配置失败:%@", root);

//                            [SPStatusReportCtl reportException:enumSPModuleGetAppConfigurations
//                                                            url:self.requestingURL
//                                                           info:SPExpNoTopicFound(@"p2p_onlineConfig")
//                                                      errorCode:[[root objectForKey:@"errorcode"] intValue]];
                        }

                    } else {
                        //上报异常
//                        [SPStatusReportCtl reportException:enumSPModuleGetAppConfigurations
//                                                        url:self.requestingURL
//                                                       info:SPExpNotJsonData
//                                                  errorCode:SPJsonErrorCodeJsonError];
                    }
                }

            } else {
                SPLOGW(SP_CONFIG_LOG_FILTER, @"request error:%ld", error.code);
            }

        }];

    self.httpRequest = request;

    SPLOGI(SP_CONFIG_LOG_FILTER, @"getOnlineConfigRequest end");
}

- (BOOL)checkObject:(id)anObj type:(Class)aType {
    if (!anObj) {
        return NO;
    }

    return [anObj isKindOfClass:aType];
}

- (int)getConfigPropertyInt:(NSString *)propString {
    if (!propString.length) {
        return 0;
    }
    if ([[SPSDKDefaultConfig sharedInstance] hasValueForKey:propString]) {
        return [[SPSDKDefaultConfig sharedInstance] intValueForKey:propString];
    }
    NSNumber *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSNumber class]]) {
            return [resultObj intValue];
        } else if ([self checkObject:resultObj type:[NSString class]]) {
            // 字符串类型兼容
            return [(NSString *)resultObj intValue];
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }
    if ([self checkObject:resultObj type:[NSNumber class]]) {
        return [resultObj intValue];
    } else if ([self checkObject:resultObj type:[NSString class]]) {
        // 字符串类型兼容
        return [(NSString *)resultObj intValue];
    }

    return 0;
}

- (float)getConfigPropertyFloat:(NSString *)propString {
    if (!propString.length) {
        return 0;
    }
    NSNumber *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSNumber class]]) {
            return [resultObj floatValue];
        } else if ([self checkObject:resultObj type:[NSString class]]) {
            // 字符串类型兼容
            return [(NSString *)resultObj floatValue];
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }
    if ([self checkObject:resultObj type:[NSNumber class]]) {
        return [resultObj floatValue];
    } else if ([self checkObject:resultObj type:[NSString class]]) {
        // 字符串类型兼容
        return [(NSString *)resultObj floatValue];
    }

    return 0;
}

- (double)getConfigPropertyDouble:(NSString *)propString {
    if (!propString.length) {
        return 0;
    }
    NSNumber *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSNumber class]]) {
            return [resultObj doubleValue];
        } else if ([self checkObject:resultObj type:[NSString class]]) {
            // 字符串类型兼容
            return [(NSString *)resultObj doubleValue];
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }
    if ([self checkObject:resultObj type:[NSNumber class]]) {
        return [resultObj doubleValue];
    } else if ([self checkObject:resultObj type:[NSString class]]) {
        // 字符串类型兼容
        return [(NSString *)resultObj doubleValue];
    }

    return 0;
}

- (BOOL)getConfigPropertyBool:(NSString *)propString {
    if (!propString.length) {
        return NO;
    }
    if ([[SPSDKDefaultConfig sharedInstance] hasValueForKey:propString]) {
        return [[SPSDKDefaultConfig sharedInstance] boolValueForKey:propString];
    }
    NSNumber *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSNumber class]]) {
            return [resultObj boolValue];
        } else if ([self checkObject:resultObj type:[NSString class]]) {
            // 字符串类型兼容
            return [(NSString *)resultObj boolValue];
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }
    if ([self checkObject:resultObj type:[NSNumber class]]) {
        return [resultObj boolValue];
    } else if ([self checkObject:resultObj type:[NSString class]]) {
        // 字符串类型兼容
        return [(NSString *)resultObj boolValue];
    }

    return NO;
}

- (NSString *)getConfigPropertyString:(NSString *)propString {
    if (!propString.length) {
        return @"";
    }
    NSString *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSString class]]) {
            return resultObj;
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }
    if ([self checkObject:resultObj type:[NSString class]]) {
        return resultObj;
    }

    return @"";
}

- (NSDictionary *)getConfigPropertyObject:(NSString *)propString {
    if (!propString.length) {
        return nil;
    }
    if ([[SPSDKDefaultConfig sharedInstance] hasValueForKey:propString]) {
        return [[SPSDKDefaultConfig sharedInstance] dictValueForKey:propString];
    }

    NSDictionary *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSDictionary class]]) {
            return resultObj;
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }
    if ([self checkObject:resultObj type:[NSDictionary class]]) {
        return resultObj;
    }

    return nil;
}

- (NSArray *)getConfigPropertyArray:(NSString *)propString {
    if (!propString.length) {
        return nil;
    }
    if ([[SPSDKDefaultConfig sharedInstance] hasValueForKey:propString]) {
        return [[SPSDKDefaultConfig sharedInstance] arrayValueForKey:propString];
    }

    NSArray *resultObj = nil;
    @synchronized(self.propConfigDict) {
        // 优先读取在线配置
        resultObj = [self.propConfigDict objectForKey:propString];
        if ([self checkObject:resultObj type:[NSArray class]]) {
            return resultObj;
        }
    }
    @synchronized(self.configCacheDict) {
        resultObj = [self.configCacheDict objectForKey:propString];
    }

    if ([self checkObject:resultObj type:[NSArray class]]) {
        return resultObj;
    }

    return nil;
}

- (NSString *)getLangString:(NSString *)string {
    NSString *resultObj = nil;
    @synchronized(self.langDict) {
        // 优先读取在线配置
        resultObj = [self.langDict objectForKey:string];
        if ([self checkObject:resultObj type:[NSString class]]) {
            return resultObj;
        }
    }

    return string;
}

- (NSString *)getUrlConfString:(NSString *)serverKey {
    NSString *resultObj = nil;
    @synchronized(self.urlsDict) {
        // 优先读取在线配置
        resultObj = [self.urlsDict objectForKey:serverKey];
        if ([self checkObject:resultObj type:[NSString class]]) {
            return resultObj;
        }
    }

    return resultObj;
}

/**
设置指定key的value值

 @param propString key
 @param object value值
 */
- (void)setConfigObjectForKey:(NSString *)propString
                       object:(id)object {
    if (!propString.length || !object) {
        return;
    }
    SET_CONFIG_KEY_VALUE(propString, object);
}

#pragma mark 前后台切换
- (void)applicationEnterForegroud {
    if (!SPSDKCONF_CONFIG_ENABLE_UPDATE) {
        return;
    }
    
#if CONFIG_TEST

    NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
    NSTimeInterval difference = currentTime - _lastRequestCongfigTime;
    SPLOGI(SP_CONFIG_LOG_FILTER,
            @"applicationEnterForegroud,currentTime:%lf, lastRequestTime:%lf, difference:%lf, request_config_min_interval:%lf",
            currentTime,
            _lastRequestCongfigTime,
            difference,
            SPSDKCONF_REQUEST_CONFIG_MIN_INTERVAL);
    [self getOnlineConfigRequest];

#else

    NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
    NSTimeInterval difference = currentTime - _lastRequestCongfigTime;
    SPLOGI(SP_CONFIG_LOG_FILTER,
            @"applicationEnterForegroud,currentTime:%lf, lastRequestTime:%lf, difference:%lf, request_config_min_interval:%lf",
            currentTime,
            _lastRequestCongfigTime,
            difference,
            SPSDKCONF_REQUEST_CONFIG_MIN_INTERVAL);
    if (difference > SPSDKCONF_REQUEST_CONFIG_MIN_INTERVAL) {
        [self getOnlineConfigRequest];
    }

#endif
}

@end
