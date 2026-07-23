/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMark.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/3/3
 Description :
 History     : 17/3/3 初始版本
 ***********************************************************/

#import "TVKMediaPlayerPlugin.h"
#import "TVKWaterMarkPluginBase.h"
#import "TVKWaterMarkInfo.h"

@class TVKWaterMarkInfo;
@class TVKWaterMarkModel;

@interface TVKStaticWaterMarkPlugin : TVKWaterMarkPluginBase

@property (nonatomic,strong) TVKWaterMarkModel* waterMarkModel;

- (id)initWithContext:(TVKWaterMarkCGIInfo *)waterMarkCGIinfo extraInfo:(TVKWaterMarkExtraInfo*)extraInfo;

@end
