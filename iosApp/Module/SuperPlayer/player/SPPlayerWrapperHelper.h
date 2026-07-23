//
//  SPPlayerWrapperHelper.h
//  SPPlayer
//
//  Created by 郭力 on 2019/10/10.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayParam.h"
#import "SPPlayerWrapperException.h"
#import "SPPlayerWrapperInfo.h"
#import "SPPlayerWrapperParam.h"
#import "SPPlayerWrapperStateManager.h"
#import "SPVODPlayInfo.h"
//#import "SPVideoView.h"
#import <Foundation/Foundation.h>
#import <ThumbPlayer/ITPMediaAsset.h>
#import <ThumbPlayer/TPPlayerCoreType.h>
#import <ThumbPlayer/TPTrackInfo.h>
#import <ThumbPlayer/TPPlayerState.h>

NS_ASSUME_NONNULL_BEGIN

@interface SPPlayerWrapperHelper : NSObject

+ (NSArray<SPSection *> *)buildSectionArrayWithUrl:(NSString *)url;

+ (NSString *)buildVideoErrorMessageWithType:(SPCGIRequestType)type module:(NSUInteger)module errCode:(NSUInteger)erroCode;

+ (BOOL)isValidMediaPlayInfo:(SPMediaPlayInfo *)playInfo;

+ (void)fillStreamInfo:(SPStreamInfo *)streamInfo fromInfoString:(NSString *)infoString;

//+ (TPVideoInfo *)buildTPVideoInfoWithMediaInfo:(SPMediaInfo *)mediaInfo andPlayInfo:(SPMediaPlayInfo *)playInfo;

///lowryhe 代码注释
//+ (id<ITPMediaAsset>)buildTPMediaAssetWithMediaInfo:(SPMediaInfo *)mediaInfo andPlayInfo:(SPMediaPlayInfo *)playInfo;

+ (NSString *)buildUrlForLiveBackPlayWithUrl:(NSString *)url andPosition:(int64_t)position;

+ (BOOL)isAllowSeamlessSwitchDefinitionTypeForPlayInfo:(SPMediaPlayInfo *)playInfo
                                               withTag:(NSString *)tag;

+ (void)printWrapperApiCallWithTag:(NSString *)tag api:(SPPlayerWrapperAPI)api params:(nullable id)params;

//+ (void)printWrapperVideoViewAndDataPointWithTag:(NSString *)tag andVideoView:(nullable SPVideoView *)view andInfo:(SPPlayerWrapperInfo *)info;

+ (void)printWrapperCallBackWithTag:(NSString *)tag andCallBack:(SPPlayerWrapperCB)callback;

//+ (void)printWrapperPlayerInfoWithTag:(NSString *)tag andInfo:(TPPlayerCoreType)info;
//
//+ (void)printWrapperPlayerInfoWithTag:(NSString *)tag andPlayerInfo:(SPWrapperPlayerInfo *)info;

+ (void)printWrapperStreamInfoWithTag:(NSString *)tag andStreamInfo:(SPStreamInfo *)streamInfo;

+ (void)printWrapperException:(SPPlayerWrapperException *)exception withTag:(NSString *)tag;

+ (void)printSeekLiveBackInfo:(NSString *)tag position:(int64_t)position info:(SPPlayerWrapperInfo *)info;

+ (NSString *)stringValueForWrapperState:(SPPlayerWrapperState)state;

+ (NSString *)stringValueForWrapperStage:(SPPlayerWrapperStage)stage;

+ (void)printWrapperParams:(SPPlayerWrapperParam *)param withTag:(NSString *)tag;

+ (NSString*)buildTPDownloadFileIdWithPlayInfo:(SPMediaPlayInfo *)playInfo;

+ (SPPlayerWrapperState)convertWrapperStateFromTPPlayerState: (TPPlayerState)tPPlayerState;

+ (BOOL)isCorrectPlayInfo:(SPMediaPlayInfo *)playInfo withLastRequestInfo:(SPPlayingContext *)requestInfo withTag:(NSString *)tag;

/**
 是否到达了试看时间。到达后，不允许用户再继续观看
 */
+ (BOOL)isReachPreivewTimeWithCurrentPosition:(NSTimeInterval)currentPositon playInfo:(SPMediaPlayInfo *)playInfo;

@end

NS_ASSUME_NONNULL_END
