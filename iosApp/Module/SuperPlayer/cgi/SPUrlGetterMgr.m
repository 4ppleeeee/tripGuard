//
//  SPUrlGetterMgr.m
//  SPPlayer
//
//  Created by haitend on 2019/10/14.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPUrlGetterMgr.h"
#import "ISPPlayInfoGetter.h"
#import "SPVODInfoGetter.h"
#import "SPCGIManagerHelper.h"
#import "SPLiveInfoGetter.h"
#import "ISPPlayInfoGetter.h"
#import "SPPlayerUtils.h"
#import "SPVODRequestParam.h"
#import "SPLiveRequestParam.h"

#define LOGTAG @"SPUrlGetterMgr"

@interface SPUrlGetterMgr () <ISPPlayInfoGetterDelegate>
/** 多线程重入锁 */
@property (nonatomic, strong) NSRecursiveLock *lock;

/** 记录requestId，用于stop */
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, id<ISPPlayInfoGetter>> *playInfoGetterMap;

@end

@implementation SPUrlGetterParam

@end

@implementation SPUrlGetterMgr

- (instancetype)init {
    if (self = [super init]) {
        _playInfoGetterMap = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (int)startRequestWithParam:(SPUrlGetterParam *)param {
    [self.lock lock];

    id<ISPPlayInfoGetter> vInfoGetter = [self buildInfoGetterWithPlayParam:param];
    SPPlayParam *playParam = [[SPPlayParam alloc] init];
    playParam.mediaInfo = param.mediaInfo;

    playParam.playContext = [[SPPlayingContext alloc] init];
    playParam.playContext.requiredMediaFormat = param.requestMediaFormat;
    playParam.requestType = SPCGIRequestTypeURLGetter;
    NSMutableDictionary<NSString *, NSString *> *extraConfig = [[NSMutableDictionary alloc] init];
    if (param.getVinfoType == SPGetVInfoTypeForDlna) {
        [extraConfig setValue:@"1" forKey:@"is_dlna"];
    } else if (param.getVinfoType == SPGetVInfoTypePreviewInfo) {
        [extraConfig setValue:@"1" forKey:@"is_live_get_preview"];
    }
    playParam.playContext.extraConfig = extraConfig;

    SPCGIRequestParam *requestParam = [SPCGIManagerHelper buildCGIRequestParamWithPlayParam:playParam];

    int requestId = [vInfoGetter requestWithParam:requestParam];
    SPLOGI(LOGTAG, @"vodUrlGetter! vodRequestId=%d", requestId);
    [self.playInfoGetterMap setObject:vInfoGetter forKey:@(requestId)];
    [self.lock unlock];
    return requestId;
}

- (void)stop:(int)requestId {
    [self.lock lock];
    SPLOGI(LOGTAG, @"stop! vodRequestId=%d", requestId);
    if (requestId > 0) {
        id<ISPPlayInfoGetter> vInfoGetter = [self.playInfoGetterMap objectForKey:@(requestId)];
        [vInfoGetter stopWithPlayID:requestId];
        [self.playInfoGetterMap removeObjectForKey:@(requestId)];
    }
    [self.lock unlock];
}

#pragma mark -internal method

- (id<ISPPlayInfoGetter>)buildInfoGetterWithPlayParam:(SPUrlGetterParam *)playParam {
    id<ISPPlayInfoGetter> infoGetter = nil;
    SPCGIInitParam *cgiInitParam = [[SPCGIInitParam alloc] init];
    cgiInitParam.logTag = LOGTAG;
    if (SPPlayTypeOnlineLive == playParam.mediaInfo.playType) {
        infoGetter = [[SPLiveInfoGetter alloc] initWithParam:cgiInitParam];
    } else {
        infoGetter = [[SPVODInfoGetter alloc] initWithParam:cgiInitParam];
    }
    infoGetter.delegate = self;
    return infoGetter;
}

/** 每次请求如果没有stop，就会返回 */
- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onGetPlayInfo:(SPMediaPlayInfo *)playInfo playID:(int)playID {
    SPLOGI(LOGTAG, @"playInfoGetter onGetPlayInfo!");

    if (![self.playInfoGetterMap.allKeys containsObject:@(playID)]) {
        /** map 里面不存在，则是被stop了 */
        SPLOGI(LOGTAG, @"playInfoGetter onGetPlayInfo stoped,return!");
        [self.lock unlock];
        return;
    }
    if ([self.delegate respondsToSelector:@selector(onPlayInfoSuccess:requestId:)]) {
        SPNetVideoInfo *netVideoInfo = [SPPlayerUtils netVideoInfoFromPlayInfo:playInfo];
        [self.delegate onPlayInfoSuccess:netVideoInfo requestId:playID];
    }
    [self.playInfoGetterMap removeObjectForKey:@(playID)];
}

- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onPlayInfoUpDate:(SPMediaPlayInfo *)playInfo playID:(int)playID {
    SPLOGI(LOGTAG, @"playInfoGetter onPlayInfoUpDate!");
    // Nothing
}

- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onGetPlayInfoFailedWithError:(NSError *)error playID:(int)playID {
    SPLOGI(LOGTAG, @"playInfoGetter onGetPlayInfoFailedWithError!");
    if (![self.playInfoGetterMap.allKeys containsObject:@(playID)]) {
        /** map 里面不存在，则是被stop了 */
        SPLOGI(LOGTAG, @"playInfoGetter onGetPlayInfo stoped,return!");
        [self.lock unlock];
        return;
    }
    if ([self.delegate respondsToSelector:@selector(onPlayInfoFailed:requestId:)]) {
        [self.delegate onPlayInfoFailed:error requestId:playID];
    }
    [self.playInfoGetterMap removeObjectForKey:@(playID)];
}
@end
