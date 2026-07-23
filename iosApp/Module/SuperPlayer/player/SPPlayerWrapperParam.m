//
//  SPPlayerWrapperParam.m
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperParam.h"
#import "SPPlayerUtils.h"

@implementation SPPlayerWrapperParam

- (instancetype)init {
    self = [super init];
    if (self) {
        _flowId = @"";
        _playSeq = 0;
        _playerSeq = 0;
        _volume = 1.0F;
        _stretchMode = SPVideoStretchModeAspectFit;
        _playRate = 1.0;
        _loopback = NO;
        _loopbackStartPosMs = 0;
        _loopbackEndPosMs = 0;
        _liveBackTime = 0;
        _enableResourceLoader = NO;
    }
    return self;
}

- (void)resetAllParams {
    _flowId = @"";
    _playSeq = 0;
    _playerSeq = 0;
    _volume = 1.0F;
    _stretchMode = SPVideoStretchModeAspectFit;
    _playRate = 1.0;
    _loopback = NO;
    _loopbackStartPosMs = 0;
    _loopbackEndPosMs = 0;
    _liveBackTime = 0;
    _enableResourceLoader = NO;
}

- (BOOL)isAirPlay {
    return (self.mediaInfo && [SPPlayerUtils isAirPlayWithMediaInfo:self.mediaInfo]);
}

- (void)clear:(RestType)resetType {
    if (resetType == RestWhenRelease) {
        [self resetAllParams];
    } else if (resetType == ResetWhenStop) {
        // stop目前保留之前的设置
    }
}

@end
