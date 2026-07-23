//
//  SPLiveInfoParser.m
//  SPPlayer
//
//  Created by hemanli on 2019/10/7.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPLiveInfoParser.h"
#import "SPJSONResponse.h"

@implementation SPLiveInfoParser

+ (SPLiveInfoData *)parseLiveInfoJson:(NSString *)json {
    SPLiveInfoData *liveInfoData = [[SPLiveInfoData alloc] init];
    NSDictionary *root = [SPJSONResponse parseJSON:json];
    if (root == nil) {
        liveInfoData.parseResult = SPJsonErrorCodeParseFail;
        return liveInfoData;
    }

    SPLivePlayInfo *livePlayInfo = [[SPLivePlayInfo alloc] init];
    liveInfoData.livePlayInfo = livePlayInfo;

    SPLiveCGIErrorModel *errorModel = [[SPLiveCGIErrorModel alloc] init];
    liveInfoData.cgiErrorModel = errorModel;
    errorModel.retCode = [[root spNumberForKeySafeModel:@"iretcode"] intValue];
    errorModel.type = [[root spNumberForKeySafeModel:@"type"] intValue];
    errorModel.errInfo = [root spStringForKeySafeModel:@"errinfo"];
    errorModel.curSeverTime = [[root spNumberForKeySafeModel:@"svrtick"] longLongValue];
    errorModel.randFlag = [root spStringForKeySafeModel:@"rand"];

    livePlayInfo.vid = [root spStringForKeySafeModel:@"livesid"];   // 流ID，和请求传入的cnlid不一定不一致，所以使用后台返回的
    NSString *livePID = [root spStringForKeySafeModel:@"livepid"];  // 节目ID
    if (livePID.length > 0) {                                     // PID不一定会有
        livePlayInfo.coverID = livePID;
    }

    livePlayInfo.needPay = [root spBoolForKeySafeModel:@"ispay"];
    livePlayInfo.isUserPay = [root spBoolForKeySafeModel:@"isuserpay"];
    livePlayInfo.livePlayTime = [[root spNumberForKeySafeModel:@"playtime"] doubleValue];
    livePlayInfo.livePreviewTime = [[root spNumberForKeySafeModel:@"totalplaytime"] doubleValue];
    livePlayInfo.livePreviewCount = [[root spNumberForKeySafeModel:@"previewcnt"] intValue];
    livePlayInfo.liveRestPreviewCount = [[root spNumberForKeySafeModel:@"restpreviewcnt"] intValue];

    int stream = [[root spNumberForKeySafeModel:@"stream"] intValue];
    livePlayInfo.mediaType = (stream == 1 ? SPMediaFormatFLV : SPMediaFormatHLS);
    livePlayInfo.stream = stream;
    livePlayInfo.acode = [[root spNumberForKeySafeModel:@"acode"] intValue];
    livePlayInfo.vcode = [[root spNumberForKeySafeModel:@"vcode"] intValue];
    livePlayInfo.hlsp2p = [[root spNumberForKeySafeModel:@"hlsp2p"] intValue];
    livePlayInfo.live360 = [[root spNumberForKeySafeModel:@"live360"] intValue];
    livePlayInfo.isHevc = livePlayInfo.vcode == 2 ? YES : NO;
    livePlayInfo.cdnName = [root spStringForKeySafeModel:@"cdn_name"];
    livePlayInfo.defn = [root spStringForKeySafeModel:@"defn"];
    livePlayInfo.sshot = [[root spNumberForKeySafeModel:@"sshot"] intValue];
    livePlayInfo.mshot = [[root spNumberForKeySafeModel:@"mshot"] intValue];

    //直播排队相关
    livePlayInfo.queueStatus = [[root spNumberForKeySafeModel:@"queue_status"] intValue];
    livePlayInfo.queueVipJump = [[root spNumberForKeySafeModel:@"queue_vip_jump"] intValue];
    livePlayInfo.queueRank = [[root spNumberForKeySafeModel:@"queue_rank"] longLongValue];
    livePlayInfo.queueSessionKey = [root spStringForKeySafeModel:@"queue_session_key"];

    NSDictionary *seeBackDict = [root spDictionaryForKeySafeModel:@"playback"];
    livePlayInfo.seeBackBaseInfo = [SPLiveSeeBackBaseInfo seeBackInfoWithDict:seeBackDict];
    
    // 解析清晰度列表
    [self parseDefinitionArray:root livePlayInfo:livePlayInfo];

    // 解析播放地址
    [self parsePlayUrl:root livePlayInfo:livePlayInfo];

    return liveInfoData;
}

+ (void)parseDefinitionArray:(NSDictionary *)root livePlayInfo:(SPLivePlayInfo *)livePlayInfo {
    //当前清晰度
    NSString *currentDefn = [root spStringForKeySafeModel:@"defn"];

    //清晰度列表
    NSArray *definitionArray = [root spArrayForKeySafeModel:@"formats"];
    NSMutableArray<SPDefinitionModel *> *defnModelArray = [[NSMutableArray alloc] init];
    for (NSDictionary *formatDic in definitionArray) {
        SPDefinitionModel *defnModel = [SPDefinitionModel definitionModelFromLiveDict:formatDic];

        if ([defnModel.fileName isEqualToString:currentDefn]) {
            livePlayInfo.currentDefinition = defnModel;  // 当前清晰度
            defnModel.audio = livePlayInfo.acode;
        }

        [defnModelArray addObject:defnModel];
    }

    livePlayInfo.defnModelList = defnModelArray;
}

+ (void)parsePlayUrl:(NSDictionary *)root livePlayInfo:(SPLivePlayInfo *)livePlayInfo {
    //流地址
    NSString *playUrl = [root spStringForKeySafeModel:@"playurl"];
    NSArray<NSString *> *backUrlArray = [root objectForKey:@"backurl_list"];
    SPSection *section = [[SPSection alloc] init];
    section.url = playUrl;
    section.index = 0;
    // 因为前面取的时候保证了playUrl不会为nil，所以这里不判空了
    NSMutableArray<NSString *> *urlList = [[NSMutableArray alloc] initWithObjects:playUrl, nil];

    for (NSDictionary *dict in backUrlArray) {
        NSString *urlStr = [dict objectForKey:@"url"];
        if (urlStr.length != 0) {
            [urlList addObject:urlStr];
        }
    }

    section.urlList = urlList;
    livePlayInfo.sectionArray = @[ section ];
}

@end
