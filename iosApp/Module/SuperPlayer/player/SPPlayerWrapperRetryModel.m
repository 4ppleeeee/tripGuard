//
//  SPPlayerWrapperRetryModel.m
//  SPPlayer
//
//  Created by 郭力 on 2019/10/8.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperRetryModel.h"

@implementation SPPlayerWrapperRetryModel

+ (SPPlayerRetryMode)retryModeForPlayerError:(TPErrorType)error
                                withMediaInfo:(nonnull SPMediaInfo *)mediaInfo
                                  andPlayInfo:(nonnull SPMediaPlayInfo *)playInfo{
// #lizard forgives 忽略圈复杂度的检测
    if (!mediaInfo) {
        return SPPlayerRetryModeCallError;
    }
    
    if (!playInfo) {
        return SPPlayerRetryModeCallError;
    }
    
    if (mediaInfo.playType == SPPlayTypeLocalFile ||
        mediaInfo.playType == SPPlayTypeExternalUrl) {
        return SPPlayerRetryModeCallError;
    }
    
    SPPlayerRetryMode mode = SPPlayerRetryModeCallError;

    BOOL isH265  = playInfo.isHevc;
    BOOL isDRM   = playInfo.currentDefinition.drm == SPDrmTypeFairPlay;
    
    switch (error) {
        //以下错误没有重试的必要，直接报错
        case TPErrorTypeSelfDevPlayerGeneral:
        case TPErrorTypeSelfDevPlayerRenderGeneral:
        case TPErrorTypeSelfDevPlayerVideoPostProcessGeneral:
        case TPErrorTypeSelfDevPlayerAudioPostProcessGeneral:
        case TPErrorTypeSystemAVPlayerGeneral:
        case TPErrorTypeiOSPlatformGeneral:
        case TPErrorTypeSystemAVPlayerNetwork:
            mode = SPPlayerRetryModeCallError;
            break;
                    
        //数据错误，分别关闭不同的特性源
        case TPErrorTypeSelfDevPlayerDemuxerGeneral:
        case TPErrorTypeSelfDevPlayerDemuxerNetwork:
        case TPErrorTypeSelfDevPlayerDemuxerStream:
            if (isDRM)   {mode = SPPlayerRetryModeDisableDRM; break;}
            if (isH265)  {mode = SPPlayerRetryModeDisableH265; break;}
            mode = SPPlayerRetryModeCallError;
            break;
            
        //数据缓冲错误，直接报错，不做任何重试
        case TPErrorTypeSelfDevPlayerDemuxerBufferingTimeout:
            mode = SPPlayerRetryModeCallError;
            break;
        
        //解码错误，分辨关闭不同的特性源
        case TPErrorTypeSelfDevPlayerDecoderGeneral:
        case TPErrorTypeSelfDevPlayerDecoderAudioNotSupport:
        case TPErrorTypeSelfDevPlayerDecoderAudioStream:
        case TPErrorTypeSelfDevPlayerDecoderVideoNotSupport:
        case TPErrorTypeSelfDevPlayerDecoderVideoStream:
        case TPErrorTypeSelfDevPlayerDecoderSubtitleNotSupport:
        case TPErrorTypeSelfDevPlayerDecoderSubtitleStream:
            if (isDRM)   {mode = SPPlayerRetryModeDisableDRM; break;}
            if (isH265)  {mode = SPPlayerRetryModeDisableH265; break;}
            mode = SPPlayerRetryModeCallError;
            break;
        
        //下载组件错误，不做重试，直接报错
        case TPErrorTypeDownloadProxyGeneral:
            mode = SPPlayerRetryModeCallError;
            break;
        
        //以下case不会走到，为了去除编译warning
        case TPErrorTypeUnknown:
            mode = SPPlayerRetryModeCallError;
            break;
    }
    
    return mode;
}

@end
