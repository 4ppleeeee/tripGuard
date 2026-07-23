/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKMediaPlayerPlugin.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/3/3
 Description : 插件基类等相关的定义
 History     : 17/3/3 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "TVKMediaPlayerPluginDef.h"
#import "SPPlayerBase.h"

typedef NS_ENUM(NSUInteger, TVKMediaPlayerPluginViewId) {
    TVKMediaPlayerPluginViewIdWaterMark = 1001,                 //任意指定一值
    TVKMediaPlayerPluginViewIdRecommend = 1002,                 // 结束推荐
    TVKMediaPlayerPluginViewIdSubTitle = 1003,                  // 字幕
    TVKMediaPlayerPluginViewIdProductReporter = 1004,           // 播放结束上报boss_cmd_vod(ios的事件定义是8)上报
    TVKMediaPlayerPluginViewIdPlayCountReporter = 1005,         // 播放量上报boss_cmd_vv(iOS的上报事件是13)
    TVKMediaPlayerPluginViewIdLivePeriodReport = 1006,          // 直播打点上报
    TVKMediaPlayerPluginViewIdPlayerQualityReport = 1007,       // 飞天播放质量上报
    TVKMediaPlayerPluginViewIdSinglePlayerQualityReport = 1008, // 播放器飞天扩展上报
    TVKMediaPlayerPluginViewIdPlayerEvents = 1009               // 播放器细分事件
};

@protocol TVKMediaPlayerPluginDelegate <NSObject>

@optional
- (UIView *)onGetContainerWithId:(TVKMediaPlayerPluginViewId)viewId;

@end

// 插件基类
@interface TVKMediaPlayerPlugin : SPPlayerBase

@property (nonatomic, weak) id<TVKMediaPlayerPluginDelegate> delegate;

@property (nonatomic, strong) TVKMediaPlayerPluginContext *context;
/** pluginId. 用于唯一标识此plugin */
@property (nonatomic, assign) TVKMediaPlayerPluginViewId pluginId;

/**
 * 创建和初始化一个TVKMediaPlayerPlugin实例
 * @param context 插件所需要的播放上下文
 * @return 一个TVKMediaPlayerPlugin实例
 */
- (instancetype)initWithContext:(TVKMediaPlayerPluginContext *)context;

/**
 * 插件加载，插件可以在这个方法做一些初始化的事情
 */
- (void)load;

/**
 * 插件卸载，插件可以在这个方法做一些反初始化的事情
 */
- (void)unLoad;

/**
 * 上下文更新时调用此方法
 * @param context 新的上下文
 * @param key 指定哪一个字段发生了更新，key的值取自TVKMediaPlayerPluginContext的各个property的name
 */
- (void)onContextUpdated:(TVKMediaPlayerPluginContext *)context key:(NSString *)key;

@end
