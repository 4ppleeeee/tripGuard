/************************************************************
 Copyright (C), 1998-2019年, Tencent Tech. Co., Ltd.
 FileName   : SPPrepareUtils.m
 Author     : andygao
 Version    : 1.0
 Date       : 2019/3/15
 Description:
 History    : 2019/3/15 初始版本
 ************************************************************/

#import "SPPrepareUtils.h"
#import "SPPlayerUtils.h"

@implementation SPPrepareUtils

#pragma mark - audio

+ (int)supportAudioBitSet {
    int spaudio = [SPPrepareUtils supportAudioPlayBitSet];
    return spaudio;
}

+ (int)supportAudioPlayBitSet {
    int spaudio = 0;
    if (SPSDKCONF_ENABLE_AUDIO_PLAY) {
        spaudio += 0x1;
    }
    return spaudio;
}

#pragma mark - 支持清晰度付费能力

+ (int)supportDefnPayVerBitSet {
    // defnpayver,1:支持1080P付费,2:支持4K付费,4:支持杜比付费
    int defnpayver = SPDefnPayVer1080P;
    return defnpayver;
}

#pragma mark - 水印

+ (TVKWaterMarkCapability)supportWaterMarkCapablity {
    return TVKWaterMarkCapabilityAction;  // 支持动态水印；如果终端不设置，后台返回的清晰度格式会比较少.
}

@end
