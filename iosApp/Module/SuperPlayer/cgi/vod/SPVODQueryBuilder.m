//
//  SPVODQueryBuilder.m
//  SPPlayer
//
//  Created by liyukuan on 2019/11/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPVODQueryBuilder.h"

@implementation SPVODQueryBuilder

+ (void)buildVODQuery:(NSMutableDictionary *)queryDict vodRequestParam:(SPVODRequestParam *)vodRequestParam {
    [self buildBasicParam:queryDict requestParam:vodRequestParam];
    [self buildVODCommonParam:queryDict requestCommonParam:vodRequestParam.commonParams];
    [self buildVODCapabilityParam:queryDict capabilityParam:vodRequestParam.capabilityParam];
    [self buildExtraParam:queryDict requestParam:vodRequestParam];
}

+ (void)buildBasicParam:(NSMutableDictionary *)paramDic requestParam:(SPVODRequestParam *)requestParam {
    [paramDic spSetString:requestParam.vid forKey:@"vid"];
    [paramDic spSetString:requestParam.cid forKey:@"cid"];
    [paramDic spSetString:requestParam.srccontenid forKey:@"srccontenid"];
    NSString *defn = requestParam.definition.length > 0 ? requestParam.definition : @"auto";
    [paramDic spSetString:defn forKey:@"defn"];
    [paramDic spSetString:[self dTypeFromMediaFormat:requestParam.mediaFormat] forKey:@"dtype"];
    [paramDic spSetString:[self clipFromMediaFormat:requestParam.mediaFormat] forKey:@"clip"];
    [paramDic spSetString:(requestParam.needCharge ? @"1" : @"0") forKey:@"charge"];
    [paramDic spSetString:requestParam.flowID forKey:@"flowid"];

    [paramDic spSetString:requestParam.track forKey:@"track"];
    [paramDic spSetString:[NSString stringWithFormat:@"%d", (int)requestParam.startPosition] forKey:@"atime"];
    [self buildFreeFlowParam:paramDic freeflowParam:requestParam.freeFlowParam];
}

+ (void)buildVODCommonParam:(NSMutableDictionary *)paramDict requestCommonParam:(SPCGIRequestCommonParam *)requestCommonParam {
    [self buildCommonParam:paramDict requestCommonParam:requestCommonParam];
}

/**
 * 构建点播能力字段的query，比如hevc level等
 *  @param paramDict 用来存储query的字典
 *  @param capabilityParam 一个SPVODCapabilityParam的实例
 */
+ (void)buildVODCapabilityParam:(NSMutableDictionary *)paramDict capabilityParam:(SPVODCapabilityParam *)capabilityParam {
    if (paramDict == nil || capabilityParam == nil) {
        return;
    }
    [paramDict spSetString:[NSString stringWithFormat:@"%d", (int)capabilityParam.hevcLevel] forKey:@"hevclv"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.spvideo] forKey:@"spvideo"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.spaudio] forKey:@"spaudio"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", (int)capabilityParam.spwm] forKey:@"spwm"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", (int)capabilityParam.spsrt] forKey:@"spsrt"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", (int)capabilityParam.drm] forKey:@"drm"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.defnPayVer] forKey:@"defnpayver"];
    [paramDict spSetString:[NSString stringWithFormat:@"%@", capabilityParam.spptype] forKey:@"spptype"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.sphls] forKey:@"sphls"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.spgzip] forKey:@"spgzip"];
}

+ (void)buildFreeFlowParam:(NSMutableDictionary *)paramDict freeflowParam:(NSDictionary<NSString *, NSString *> *)freeflowParam {
    [paramDict addEntriesFromDictionary:freeflowParam];
}

+ (void)buildExtraParam:(NSMutableDictionary *)paramDic requestParam:(SPVODRequestParam *)requestParam {
    [paramDic addEntriesFromDictionary:requestParam.extraParams];
}

+ (NSString *)dTypeFromMediaFormat:(SPMediaFormat)mediaFormat {
    NSString *dType = @"3";  // HLS
    switch (mediaFormat) {
        case SPMediaFormatAuto:
            dType = @"3";
            break;
        case SPMediaFormatMultiMp4:
        case SPMediaFormatOneMp4:
            dType = @"1";
            break;
        case SPMediaFormatHLS:
            dType = @"3";
            break;
        default:
            dType = @"3";
            break;
    }

    return dType;
}

//NSString *const TVKCGIClipDefault = @"0";   //默认
//NSString *const TVKCGIClipAuto = @"1";      //后台指定
//NSString *const TVKCGIClipMulti = @"2";     //5分钟分片
//NSString *const TVKCGIClipOne = @"4";       //整段Mp4

+ (NSString *)clipFromMediaFormat:(SPMediaFormat)mediaFormat {
    NSString *clip = @"0";
    switch (mediaFormat) {
        case SPMediaFormatAuto:
            clip = @"1";
            break;
        case SPMediaFormatMultiMp4:
            clip = @"2";
            break;
        case SPMediaFormatOneMp4:
            clip = @"4";
            break;
        default:
            break;
    }

    return clip;
}
@end
