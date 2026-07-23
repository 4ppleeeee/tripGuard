/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPPlayerUtils.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/2/22
 Description :
 History     : 17/2/22 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPMediaPlayerDefine.h"
#import "SPPlayerDefine.h"
#import "SPPlayerWrapperDefine.h"
//#import "SPCGIDefines.h"
#import "SPMediaPlayInfo.h"

typedef NS_ENUM(NSInteger, SPDefLevel) {
    SPDefLevel_None = 0,
    SPDefLevel_Sd = 11,
    SPDefLevel_Hd = 16,
    SPDefLevel_Shd = 21,
    SPDefLevel_Fhd = 26
};

@class SPNetMediaDefinitionInfo;
@class SPDefinitionModel;

@class SPNetVideoInfo;
@class SPMediaInfo;

@interface SPPlayerUtils : NSObject

+ (SPNetVideoInfo *)netVideoInfoFromPlayInfo:(SPMediaPlayInfo *)playInfo;

+ (SPNetMediaDefinitionInfo *)definitionInfoFromDefnModel:(SPDefinitionModel *)fileInfo;

+ (NSString *)stringForMediaPlayerState:(SPMediaPlayerState)state;
+ (NSString *)stringForMediaPlayerEvent:(SPMediaPlayerEvent)event;
+ (NSString *)stringOfMediaFormat:(SPMediaFormat)mediaType;
+ (NSString *)stringForPlayerWrapperState:(SPPlayerWrapperState)state;
+ (NSString *)stringForPlayerWrapperEvent:(SPPlayerWrapperEvent)event;

+ (BOOL)isEnableQuickPlayWithMediaInfo:(SPMediaInfo *)mediaInfo;
+ (BOOL)isQuickPlayWithMediaInfo:(SPMediaInfo *)mediaInfo;
+ (void)removeQuickPlayInfoOfMediaInfo:(SPMediaInfo *)mediaInfo;
+ (NSString *)previdFromMediaInfo:(SPMediaInfo *)mediaInfo;
+ (NSString *)historyVidFromMediaInfo:(SPMediaInfo *)mediaInfo;

+ (NSComparisonResult)compareDefinition:(NSString *)defn1 second:(NSString *)defn2;

+ (int)nettypeForGetVInfo;

+ (BOOL)allowAutoReduceDefinitionWithMediaInfo:(SPMediaInfo *)mediaInfo;

+ (NSString *)stringForPlayType:(SPPlayType)playType;

+ (BOOL)needSkipStartAndEndWithMediaInfo:(SPMediaInfo *)mediaInfo;

+ (SPHEVCLevel)hevcLevel;
//+ (int)supportH264Level;

// 是否是答题类直播
+ (BOOL)isLiveQAFromMediaInfo:(SPMediaInfo *)mediaInfo;

+ (BOOL)isForceOnlineWithMediaInfo:(SPMediaInfo *)mediaInfo;

+ (int64_t)seeBackTimeWithMediaInfo:(SPMediaInfo *)mediaInfo;

+ (NSString *)adaptiveTypeFromMediaInfo:(SPMediaInfo *)mediaInfo;

+ (NSString *)logTagWithPlayerSeq:(int)playerSeq playSeq:(int)playSeq;

+ (BOOL)isAirPlayWithMediaInfo:(SPMediaInfo *)mediaInfo;

+ (void)getVt:(NSString **)vt urlIndex:(NSUInteger *)urlIndex byUrl:(NSString *)urlString mediaPlayInfo:(SPMediaPlayInfo *)mediaPlayInfo;
@end
