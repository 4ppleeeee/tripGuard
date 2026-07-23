/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKActionWaterMarkPlugin.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/8/24
 Description :
 History     : 2017/8/24 初始版本
 ***********************************************************/

#import "TVKMediaPlayerPlugin.h"
#import "TVKWaterMarkPluginBase.h"
#import "TVKWaterMarkInfo.h"

// 动态水印插件
@interface TVKActionWaterMarkPlugin : TVKWaterMarkPluginBase

/**
 * 初始化一个TVKActionWaterMarkPlugin实例
 */
- (id)initWithContext:(TVKWaterMarkCGIInfo *)waterMarkCGIinfo extraInfo:(TVKWaterMarkExtraInfo *)extraInfo;

@end
