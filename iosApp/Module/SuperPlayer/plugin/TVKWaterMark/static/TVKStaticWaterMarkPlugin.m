/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMark.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 17/3/3
 Description :
 History     : 17/3/3 初始版本
 ***********************************************************/

#import "TVKStaticWaterMarkPlugin.h"
#import "TVKWaterMarkView.h"
#import "TVKWaterMarkView.h"
#import "TVKWaterMarkViewFactory.h"
//#import "TVKMediaPlayerInfoEventSender.h"
//#import "TVKMediaPlayerDefine.h"
#import "TVKRawWaterMarkInfo.h"
//#import "TVKUtils.h"
//#import "TVKMediaPlayer.h"
#import "TVKWaterMarkUtil.h"
#import "SPMediaPlayInfo.h"
#import "TVKThreadUtils.h"

@interface TVKStaticWaterMarkPlugin ()

@property (nonatomic, strong) TVKWaterMarkViewFactory *waterMarkFactroy;
@property (nonatomic, strong) NSMutableArray<TVKWaterMarkView *> *waterMarkViews;
@property (nonatomic, strong) TVKWaterMarkCGIInfo *waterMarkCGIinfo;
@property (nonatomic, assign) CGSize videoSize;
@property (nonatomic, assign) CGSize videoViewSize;
@property (nonatomic, assign) SPVideoStretchMode stretchMode;

@end

@implementation TVKStaticWaterMarkPlugin

- (instancetype)initWithContext:(TVKWaterMarkCGIInfo *)waterMarkCGIinfo extraInfo:(TVKWaterMarkExtraInfo *)extraInfo {
    if (self = [super init]) {
        self.pluginId = TVKMediaPlayerPluginViewIdWaterMark;
        _waterMarkModel = waterMarkCGIinfo.waterMarkModel;
        _waterMarkCGIinfo = waterMarkCGIinfo;
        _stretchMode = extraInfo.stretchMode;
        _videoSize = extraInfo.videoSize;
    }
    return self;
}

- (void)load {
    self.waterMarkViews = [[NSMutableArray alloc] init];
    self.waterMarkFactroy = [[TVKWaterMarkViewFactory alloc] init];
    if (self.waterMarkCGIinfo.bizType == SPMediaPlayBizTypeLive) {
        [self buildLiveWaterMarkInfoWithMediaPlayInfo:self.waterMarkCGIinfo];
    } else {
        [self buildVODWaterMarkInfoWithMediaPlayInfo:self.waterMarkCGIinfo];
    }

    [self showWaterMark];
}

- (void)unLoad {
    [super unLoad];
    [self.waterMarkViews removeAllObjects];
    self.waterMarkViews = nil;
    [self.waterMarkFactroy removeAll];  //先移除内部资源
    self.waterMarkFactroy = nil;
}
/** view 宽高改变 */
- (void)onViewSizeChanged:(CGSize)videoViewSize {
    self.videoViewSize = videoViewSize;
    for (TVKWaterMarkView *waterMarkView in self.waterMarkViews) {
        [waterMarkView setVideoViewSize:videoViewSize];
        [waterMarkView requestLayout];
    }
}
/** 显示模式改变 */
- (void)onStretchModeChanged:(SPVideoStretchMode)stretchMode {
    self.stretchMode = stretchMode;
    for (TVKWaterMarkView *waterMarkView in self.waterMarkViews) {
        [waterMarkView setStretchMode:stretchMode];
        [waterMarkView requestLayout];
    }
}

- (void)buildVODWaterMarkInfoWithMediaPlayInfo:(TVKWaterMarkCGIInfo *)waterMarkCGIinfo {
    NSArray *rawWaterMarkInfos = waterMarkCGIinfo.waterMarkModel.waterInfos;
    for (TVKVODWaterMarkInfo *rawInfo in rawWaterMarkInfos) {
        if (![rawInfo isKindOfClass:[TVKVODWaterMarkInfo class]]) {
            continue;
        }
        TVKWaterMarkInfo *waterMarkInfo = [TVKWaterMarkUtil waterMarkDisplayInfoFromVODWaterMarkInfo:rawInfo rw:0];

        UIView *parent = [self.delegate onGetContainerWithId:self.pluginId];
        TVKWaterMarkView *waterMarkView = [self.waterMarkFactroy createWaterMarkView:waterMarkInfo];
        waterMarkView.container = parent;
        [waterMarkView setStretchMode:self.stretchMode];
        [self.waterMarkViews addObject:waterMarkView];
    }
}

- (void)buildLiveWaterMarkInfoWithMediaPlayInfo:(TVKWaterMarkCGIInfo *)waterMarkCGIinfo {
    NSArray *rawWaterMarkInfos = waterMarkCGIinfo.waterMarkModel.waterInfos;
    for (TVKLiveWaterMarkInfo *rawInfo in rawWaterMarkInfos) {
        if (![rawInfo isKindOfClass:[TVKLiveWaterMarkInfo class]]) {
            continue;
        }
        TVKWaterMarkInfo *waterMarkInfo = [TVKWaterMarkUtil waterMarkDisplayInfoFromLiveWaterMarkInfo:rawInfo rw:0];

        UIView *parent = [self.delegate onGetContainerWithId:self.pluginId];
        TVKWaterMarkView *waterMarkView = [self.waterMarkFactroy createWaterMarkView:waterMarkInfo];
        waterMarkView.container = parent;
        [waterMarkView setStretchMode:self.stretchMode];
        [self.waterMarkViews addObject:waterMarkView];
    }
}

- (void)showWaterMark {
    UIView *parent = [self.delegate onGetContainerWithId:self.pluginId];
    CGSize videoSize = self.waterMarkCGIinfo.videoSize;

    if (videoSize.width == 0 || videoSize.height == 0) {
        videoSize = self.videoSize;
    }

    tvk_dispatch_main_async_safe(^{
        [self showWithVideoSize:videoSize videoViewSize:parent.frame.size];
    });
}

- (void)showWithVideoSize:(CGSize)videoSize videoViewSize:(CGSize)videoViewSize {
    SPLOGI(SP_WATER_MARK_LOG_FILTER, @"showWithvideoSize=(%f, %f), viewSize=(%f, %f,)", videoSize.width, videoSize.height, videoViewSize.width,
            videoViewSize.height);
    for (TVKWaterMarkView *waterMarkView in self.waterMarkViews) {
        [waterMarkView setVideoViewSize:videoViewSize];
        [waterMarkView setVideoSize:videoSize];
        [waterMarkView show];
    }
}
@end
