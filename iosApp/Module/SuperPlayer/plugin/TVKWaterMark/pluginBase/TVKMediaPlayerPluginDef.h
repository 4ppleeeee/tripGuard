/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKMediaPlayerPluginDef.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/9/4
 Description :
 History     : 2017/9/4 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@class TVKMediaPlayerInfoEventSender;
@class TVKMediaPlayer;
@class TVKVideoView;
@class SPMediaInfo;
@class TVKEventCenter;
@class SPMediaPlayInfo;
@class SPPlayCommonParam;

// 播放上下文，创建TVKMediaPlayerPluginMgr时作为参数传入
@interface TVKMediaPlayerContext : NSObject

@property (nonatomic, strong) SPMediaInfo *mediaInfo;  // 媒体基本信息

@property (nonatomic, strong) TVKMediaPlayerInfoEventSender *eventSender;  // 播放事件分发器，用于监听播放事件

@property (nonatomic, strong) TVKEventCenter *pluginEventCenter;  // 插件事件分发，用于发送插件事件
/** commonparam, 用于设置dispatch_queue_t等 */
@property (nonatomic, strong) SPPlayCommonParam *commonParam;

@end

// 创建插件时传递给各个插件的上下文
@interface TVKMediaPlayerPluginContext : NSObject

@property (atomic, strong) SPMediaPlayInfo *mediaPlayInfo;

@property (nonatomic, strong) TVKMediaPlayerInfoEventSender *eventSender;

@property (nonatomic, strong) TVKEventCenter *pluginEventCenter;
/** commonparam, 用于设置dispatch_queue_t等 */
@property (nonatomic, strong) SPPlayCommonParam *commonParam;
@end
