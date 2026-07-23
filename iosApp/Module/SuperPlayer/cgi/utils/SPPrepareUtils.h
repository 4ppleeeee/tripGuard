/************************************************************
 Copyright (C), 1998-2019年, Tencent Tech. Co., Ltd.
 FileName   : SPPrepareUtils.h
 Author     : andygao
 Version    : 1.0
 Date       : 2019/3/15
 Description:
 History    : 2019/3/15 初始版本
 ************************************************************/

#import <Foundation/Foundation.h>
#import "SPCGIDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPPrepareUtils : NSObject

/** bitset形式：
 0x1：支持纯音频（只播音频）
 0x2：Dolby Surround
 0x4：Dolby Atmos
 */
+ (int)supportAudioBitSet;

/**
 是否支持音频播放
 0x0:不支持
 0x1：支持纯音频（只播音频）
 */
+ (int)supportAudioPlayBitSet;

/**
 bitset形式：
 0x1：支持1080P付费
 0x2：支持4K付费
 0x4：支持杜比付费
 (支持4k付费/杜比付费的app必须支持1080P付费)
 */
+ (int)supportDefnPayVerBitSet;

/**
 0：不支持
 1：支持静态水印
 2：支持动态水印
 */
+ (TVKWaterMarkCapability)supportWaterMarkCapablity;

@end

NS_ASSUME_NONNULL_END
