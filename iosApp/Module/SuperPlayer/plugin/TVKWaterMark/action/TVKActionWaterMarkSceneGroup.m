/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKActionWaterMarkSceneGroup.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/8/24
 Description :
 History     : 2017/8/24 初始版本
 ***********************************************************/

#import "TVKActionWaterMarkSceneGroup.h"
#import "TVKActionWaterMarkSceneDisplay.h"
#import "TVKRawWaterMarkInfo.h"
#import "TVKWaterMarkViewFactory.h"

//TVKWaterMarkView、TVKWaterMarkFactory
@interface TVKActionWaterMarkSceneGroup () {
    NSArray<TVKActionWaterMarkSceneDisplay *> *_sceneDisplayArray;
    TVKActionWaterMarkModel *_actionWaterMarkModel;
}
@property (nonatomic, strong) TVKWaterMarkViewFactory *waterMarkFactory;
@end

@implementation TVKActionWaterMarkSceneGroup

- (instancetype)initWithActionWaterMarkModel:(TVKActionWaterMarkModel *)model container:(UIView *)container videoSize:(CGSize)videoSize {
    if ((self = [super init])) {
        _actionWaterMarkModel = model;
        [self buildSceneDisplayWithContainer:container videoSize:videoSize];
    }

    return self;
}

- (void)setPlayPosition:(NSTimeInterval)position {
    if (_actionWaterMarkModel.duration == 0) {
        return;
    }

    int64_t msPos = (int64_t)(position * 1000);
    if (msPos < _actionWaterMarkModel.start) {
        [self setTimePoint:-1];  // 传负值不显示
    } else {
        if (_actionWaterMarkModel.repeat != 0 &&
            msPos > _actionWaterMarkModel.start + (int64_t)_actionWaterMarkModel.duration * _actionWaterMarkModel.repeat) {
            [self setTimePoint:-1];
        } else {
            int timePoint = (msPos - _actionWaterMarkModel.start) % _actionWaterMarkModel.duration;
            [self setTimePoint:timePoint];
        }
    }
}

- (void)setRelativeTime:(NSTimeInterval)time {
    if (_actionWaterMarkModel.duration == 0) {
        return;
    }

    int64_t msTime = (int64_t)(time * 1000);
    if (msTime < _actionWaterMarkModel.start) {
        [self setTimePoint:-1];  // 传负值不显示
    } else {
        int curRepeat = ((int)msTime - _actionWaterMarkModel.start) / _actionWaterMarkModel.duration;
        int timePoint = (msTime - _actionWaterMarkModel.start) % _actionWaterMarkModel.duration;
        [self setTimePoint:timePoint curRepeat:curRepeat];
    }
}

// 设置一个周期内的时间点
- (void)setTimePoint:(int)timePoint {
    for (TVKActionWaterMarkSceneDisplay *sceneDisplay in _sceneDisplayArray) {
        [sceneDisplay setTimePoint:timePoint];
    }
}

- (void)setTimePoint:(int)timePoint curRepeat:(int)curRepeat {
    for (TVKActionWaterMarkSceneDisplay *sceneDisplay in _sceneDisplayArray) {
        if (curRepeat >= sceneDisplay.actionScene.start && curRepeat <= sceneDisplay.actionScene.end) {
            [sceneDisplay setTimePoint:timePoint];
        } else {
            [sceneDisplay setTimePoint:-1];
        }
    }
}

- (void)setStretchMode:(SPVideoStretchMode)mode {
    for (TVKActionWaterMarkSceneDisplay *sceneDisplay in _sceneDisplayArray) {
        [sceneDisplay setStretchModel:mode];
    }
}

- (void)onVideoViewSizeChanged:(CGSize)videoViewSize {
    for (TVKActionWaterMarkSceneDisplay *sceneDisplay in _sceneDisplayArray) {
        [sceneDisplay onVideoViewSizeChanged:videoViewSize];
    }
}

- (void)onVideoSizeChange:(CGSize)videoSize {
    for (TVKActionWaterMarkSceneDisplay *sceneDisplay in _sceneDisplayArray) {
        [sceneDisplay onVideoSizeChanged:videoSize];
    }
}

- (void)requestLayout {
    for (TVKActionWaterMarkSceneDisplay *sceneDisplay in _sceneDisplayArray) {
        [sceneDisplay requestLayout];
    }
}

- (void)destroy {
    [self.waterMarkFactory removeAll];
}
- (void)buildSceneDisplayWithContainer:(UIView *)container videoSize:(CGSize)videoSize {
    self.waterMarkFactory = [[TVKWaterMarkViewFactory alloc] init];
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:_actionWaterMarkModel.actionWaterMarkScenes.count];
    for (TVKActionWaterMarkScene *scene in _actionWaterMarkModel.actionWaterMarkScenes) {
        TVKActionWaterMarkSceneDisplay *sceneDisplay = [[TVKActionWaterMarkSceneDisplay alloc] initWithWaterMarkScene:scene
                                                                                                            container:container
                                                                                                            videoSize:videoSize
                                                                                                                   rw:_actionWaterMarkModel.rw];
        sceneDisplay.factory = self.waterMarkFactory;
        [array addObject:sceneDisplay];
    }

    _sceneDisplayArray = array;
}

@end
