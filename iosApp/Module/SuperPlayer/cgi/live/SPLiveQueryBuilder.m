//
//  SPLiveQueryBuilder.m
//  SPPlayer
//
//  Created by liyukuan on 2019/11/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPLiveQueryBuilder.h"

#define LIVE_SVR_TICK @"SP_live_svrtick"

@implementation SPLiveQueryBuilder

+ (void)buildLiveQuery:(NSMutableDictionary *)queryDict liveRequestParam:(SPLiveRequestParam *)liveRequestParam {
    [self buildBasicParam:queryDict requestParam:liveRequestParam];
    [self buildLiveCommonParam:queryDict requestCommonParam:liveRequestParam.commonParams];
    [self buildLiveCapabilityParam:queryDict capabilityParam:liveRequestParam.capabilityParam];
    [self buildExtraParam:queryDict extraParams:liveRequestParam.extraParams];
}

+ (void)buildBasicParam:(NSMutableDictionary *)paramDict requestParam:(SPLiveRequestParam *)requestParam {
    [paramDict spSetString:requestParam.vid forKey:@"cnlid"];
    [paramDict spSetString:requestParam.cid forKey:@"livepid"];
    [paramDict spSetString:@"1" forKey:@"system"];  //0:win, 1:iOS, 2:android, 3:mac
    NSString *defn = requestParam.definition.length > 0 ? requestParam.definition : @"auto";
    [paramDict spSetString:defn forKey:@"defn"];
    
    int fntick = [self lastServerTick];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", fntick] forKey:@"fntick"];
    
    //1为FLV，2为HLS
    [paramDict spSetString:((requestParam.mediaFormat == SPMediaFormatFLV) ? @"1" : @"2")
                     forKey:@"stream"];
    
    [paramDict spSetString:requestParam.flowID forKey:@"flowid"];
    [paramDict spSetObject:((requestParam.requestType == SPLiveRequestTypePreview) ? @"1" : @"0")
                     forKey:@"getpreviewinfo"];
    
    [paramDict spSetString:[NSString stringWithFormat:@"%lld", requestParam.userLiveSeeBackTime] forKey:@"playbacktime"];
    
    [paramDict spSetString:requestParam.p2pVersion forKey:@"p2pver"];
    
    [self buildFreeFlowParam:paramDict freeFlowParam:requestParam.freeFlowParam];
}

+ (void)buildFreeFlowParam:(NSMutableDictionary *)paramDict freeFlowParam:(NSDictionary<NSString *, NSString *> *)freeFlowParam {
    [paramDict addEntriesFromDictionary:freeFlowParam];
}

/**
 * 构建直播播通用的query，比如平台号、系统版本号、网络类型，大部分字段跟点播相同，也是接收一个SPCGIRequestCommonParam的实例为参数，只是点播和直播各取所需。
 * @param paramDict 用来存储query的字典
 *  @param requestCommonParam 一个SPCGIRequestCommonParam的实例
 */
+ (void)buildLiveCommonParam:(NSMutableDictionary *)paramDict requestCommonParam:(SPCGIRequestCommonParam *)requestCommonParam {
    [self buildCommonParam:paramDict requestCommonParam:requestCommonParam];
    [paramDict spSetString:[self loginTypeForLive:requestCommonParam.loginType] forKey:@"logintype"];
}

/**
 * 构建直播能力字段的query，比如hevc level等
 * @param paramDict 用来存储query的字典
 * @param capabilityParam 一个SPLiveCapabilityParam的实例
 */
+ (void)buildLiveCapabilityParam:(NSMutableDictionary *)paramDict capabilityParam:(SPLiveCapabilityParam *)capabilityParam {
    [paramDict spSetString:[NSString stringWithFormat:@"%d", (int)capabilityParam.hevcLevel] forKey:@"hevclv"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.spvideo] forKey:@"spvideo"];
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.spaudio] forKey:@"spaudio"];
    // 直播能力值总开关，如果NO，则spvideo、spaudio不生效
    [paramDict spSetString:[NSString stringWithFormat:@"%d", capabilityParam.active_sp] forKey:@"active_sp"];
    [paramDict spSetString:(capabilityParam.enableLiveQueue ? @"1" : @"0") forKey:@"livequeue"];
}

+ (void)buildExtraParam:(NSMutableDictionary *)paramDict extraParams:(NSDictionary<NSString *, NSString *> *)extraParams {
    [paramDict addEntriesFromDictionary:extraParams];
}

+ (NSString *)loginTypeForLive:(SPCGILoginType)loginType {
    NSString *liveLoginType;
    switch (loginType) {
        case SPCGILoginTypeNone:
            liveLoginType = @"3";
            break;
        case SPCGILoginTypeQQ:
            liveLoginType = @"1";
            break;
        case SPCGILoginTypeWx:
            liveLoginType = @"2";
        default:
            break;
    }
    
    return liveLoginType;
}

/**
 * 上一次存储选择的清晰度的时间戳，单位为秒
 */
+ (int)lastServerTick {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    int fntick = [[userDefaults objectForKey:LIVE_SVR_TICK] intValue];
    if (fntick == 0) {
        fntick = [[NSDate date] timeIntervalSince1970];
    }
    return fntick;
}

+ (void)storeServerTick:(int64_t)serverTick {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:[NSNumber numberWithLongLong:serverTick] forKey:LIVE_SVR_TICK];
    [userDefaults synchronize];
}
@end
