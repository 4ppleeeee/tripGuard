/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKActionWaterMarkSceneDisplay.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/8/24
 Description :
 History     : 2017/8/24 初始版本
 ***********************************************************/

#import "TVKActionWaterMarkSceneDisplay.h"
#import "TVKRawWaterMarkInfo.h"
#import "TVKWaterMarkInfo.h"
#import "TVKWaterMarkView.h"
#import "TVKWaterMarkUtil.h"
#import "TVKWaterMarkViewFactory.h"

typedef NS_ENUM(NSUInteger, TVKActionDisplayStatus) {
    TVKActionDisplayStatusOut,  //未显示
    TVKActionDisplayStatusIn,   //显示
};

@interface TVKActionWaterMarkSceneDisplay () {
    TVKActionDisplayStatus _displayStatus;
    TVKActionWaterMarkScene *_actionScene;
    UIView *_container;
    CGSize _videoSize;
    NSMutableArray<TVKWaterMarkInfo *> *_waterMarkInfoArray;
    SPVideoStretchMode _stretchMode;
}

@property (nonatomic, strong) TVKActionWaterMarkScene *actionScene;
@end

@implementation TVKActionWaterMarkSceneDisplay

- (instancetype)initWithWaterMarkScene:(TVKActionWaterMarkScene *)actionScene
                             container:(UIView *)container
                             videoSize:(CGSize)videoSize
                                    rw:(int)rw {
    if ((self = [super init])) {
        _actionScene        = actionScene;
        _displayStatus      = TVKActionDisplayStatusOut;
        _container          = container;
        _videoSize          = videoSize;
        _waterMarkInfoArray = [TVKWaterMarkUtil waterMarkDisplayInfosFromWaterMarkInfos:actionScene.waterMarkInfos rw:rw];
    }

    return self;
}

- (void)setTimePoint:(int)timePoint {
    if (_actionScene.inTime == 0 && _actionScene.outTime == 0) {
        [self hide];
        return;
    }

    if (timePoint >= _actionScene.inTime && timePoint <= _actionScene.outTime) {
        [self show];
    } else {
        [self hide];
    }
}

- (void)setStretchModel:(SPVideoStretchMode)mode {
    _stretchMode = mode;
    for (TVKWaterMarkInfo *waterMarkInfo in _waterMarkInfoArray) {
        TVKWaterMarkView *waterMarkView = [self.factory queryWaterMarkView:waterMarkInfo];
        [waterMarkView setStretchMode:mode];
    }
}

- (void)onVideoViewSizeChanged:(CGSize)videoViewSize {
    for (TVKWaterMarkInfo *waterMarkInfo in _waterMarkInfoArray) {
        TVKWaterMarkView *waterMarkView = [self.factory queryWaterMarkView:waterMarkInfo];
        [waterMarkView setVideoViewSize:videoViewSize];
    }
}

- (void)onVideoSizeChanged:(CGSize)videoSize {
    _videoSize = videoSize;
    for (TVKWaterMarkInfo *waterMarkInfo in _waterMarkInfoArray) {
        TVKWaterMarkView *waterMarkView = [self.factory queryWaterMarkView:waterMarkInfo];
        [waterMarkView setVideoSize:_videoSize];
    }
}

- (void)requestLayout {
    for (TVKWaterMarkInfo *waterMarkInfo in _waterMarkInfoArray) {
        TVKWaterMarkView *waterMarkView = [self.factory queryWaterMarkView:waterMarkInfo];
        [waterMarkView requestLayout];
    }
}

- (TVKWaterMarkViewFactory *)factory {
    if (_factory == nil) {
        _factory = [[TVKWaterMarkViewFactory alloc] init];
    }

    return _factory;
}

- (void)show {
    if (_displayStatus == TVKActionDisplayStatusIn) {
        return;
    }

    for (TVKWaterMarkInfo *waterMarkInfo in _waterMarkInfoArray) {
        TVKWaterMarkView *waterMarkView = [self.factory createWaterMarkView:waterMarkInfo];
        CGRect containerFrame = [[_container valueForKey:@"frame"] CGRectValue];
        [waterMarkView setVideoViewSize:containerFrame.size];
        [waterMarkView setVideoSize:_videoSize];
        [waterMarkView setStretchMode:_stretchMode];
        waterMarkView.container = _container;
        [waterMarkView show];
    }

    _displayStatus = TVKActionDisplayStatusIn;
}

- (void)hide {
    if (_displayStatus == TVKActionDisplayStatusOut) {
        return;
    }

    for (TVKWaterMarkInfo *waterMarkInfo in _waterMarkInfoArray) {
        TVKWaterMarkView *waterMarkView = [self.factory queryWaterMarkView:waterMarkInfo];
        [waterMarkView hide];
    }
    _displayStatus = TVKActionDisplayStatusOut;
}
@end
