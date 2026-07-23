//
//  SPPlayerWrapperParam.h
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPMediaInfo.h"
#import "SPPlayerMediaSource.h"
#import "SPUserInfo.h"
#import "SPMediaInfo.h"
//#import "SPVideoView.h"
//#import "SPVideoView+Private.h"
NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, RestType) {
    ResetWhenStop = 0x01,    //清理时机：wrapper层stop的时候重置参数
    RestWhenRelease = 0x02,  //清理时机：wrapper层release的时候重置参数
};

@interface SPPlayerWrapperParam : NSObject

@property (nonatomic, copy) NSString *flowId;

@property (nonatomic, assign) int playSeq;

@property (nonatomic, assign) int playerSeq;

@property (nonatomic, strong) dispatch_queue_t queue;

//@property (nonatomic, strong, nullable) SPVideoView *videoView;

@property (nonatomic, strong) SPMediaInfo *mediaInfo;

@property (nonatomic, strong) SPUserInfo *userInfo;

@property (nonatomic, assign) NSTimeInterval progressInterval;

@property (nonatomic, assign) BOOL enableResourceLoader;

@property (nonatomic, assign) BOOL allowsExternalPlayback;

@property (nonatomic, assign) BOOL usesExternalPlaybackWhileExternalScreenIsActive;

@property (nonatomic, assign) float volume;

@property (nonatomic, assign) SPVideoStretchMode stretchMode;

@property (nonatomic, assign) CGFloat playRate;

@property (nonatomic, assign) BOOL loopback;

@property (nonatomic, assign) int64_t loopbackStartPosMs;

@property (nonatomic, assign) int64_t loopbackEndPosMs;

@property (nonatomic, assign) float viewAngleX;

@property (nonatomic, assign) float viewAngleY;

@property (nonatomic, assign) float viewAngleZ;

@property (nonatomic, assign) BOOL allowExternalPlayback;

@property (nonatomic, assign) int64_t liveBackTime;

@property (nonatomic, assign) BOOL preload;

- (BOOL)isAirPlay;

- (void)clear:(RestType)resetType;

@end

NS_ASSUME_NONNULL_END
