/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPCGIManager.m
 * @brief    CGI请求管理类，统一管理点播和直播cgi的请求
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPCGIManager.h"
#import "ISPPlayInfoGetter.h"
#import "SPCGIManagerHelper.h"
#import "SPPlayerUtils.h"
#import "SPVODPlayInfo.h"
#import "SPLivePlayInfo.h"
#import "SPCGIFactory.h"
#import "SPCGIManagerPrinter.h"

@interface SPCGIManager () <ISPPlayInfoGetterDelegate>

@property (nonatomic, strong) SPPlayCommonParam *commonParam;

@property (nonatomic, copy) NSString *logTag;

@property (nonatomic, strong) id<ISPPlayInfoGetter> playInfoGetter;

@property (nonatomic, assign) int requestID;

@property (nonatomic, strong) SPPlayParam *playParam;

@property (nonatomic, assign) BOOL cancelled;

@end

@implementation SPCGIManager

- (instancetype)initWithParam:(SPPlayCommonParam *)param {
    if ((self = [super init])) {
        _commonParam = param;
        _logTag = [NSString stringWithFormat:@"SPCGI#%d", param.playerSeq];
    }
    
    SPLOGS(self.logTag, @"SPCGIManager init:%p", self);
    return self;
}

- (void)dealloc {
    SPLOGS(self.logTag, @"SPCGIManager dealloc:%p", self);
}

- (void)requestWithPlayParam:(SPPlayParam *)playParam {
    NSString *logTag = [self.playerLogContext.tagPrefix stringByAppendingString:@"_CGI"];
    if (logTag != nil) {
        self.logTag = logTag;
    }
    
    SPLOGS(self.logTag, @"SPCGIManager requestWithPlayParam, flowID=%@", playParam.flowID);
    
    [self cancel];
    
    self.cancelled = NO; // 一次新的请求开始，要把cancelled置为NO
    [SPCGIManagerPrinter printPlayParam:playParam logTag:self.logTag];
    
    self.playInfoGetter = [self buildInfoGetterWithPlayParam:playParam];
    SPCGIRequestParam *requestParam = [self buildCGIRequestParamWithPlayParam:playParam];
    [SPCGIManagerPrinter printRequestParam:requestParam logTag:self.logTag];
    
    self.requestID = [self.playInfoGetter requestWithParam:requestParam];
    SPLOGS(self.logTag, @"SPCGIManager start request, requestID=%d", self.requestID);
    self.playParam = playParam;
}

- (void)cancel {
    [self.playInfoGetter stopWithPlayID:self.requestID];
    self.playInfoGetter = nil;
    self.cancelled = YES;
}

#pragma mark-internal method
- (id<ISPPlayInfoGetter>)buildInfoGetterWithPlayParam:(SPPlayParam *)playParam {
    SPCGIInitParam *cgiInitParam = [[SPCGIInitParam alloc] init];
    cgiInitParam.logTag = self.logTag;
    id<ISPPlayInfoGetter> infoGetter = [SPCGIFactory createPlayInfoGetterWithPlayParam:playParam
                                                                             cgiInitParam:cgiInitParam];
    
    infoGetter.delegate = self;
    
    return infoGetter;
}

- (SPCGIRequestParam *)buildCGIRequestParamWithPlayParam:(SPPlayParam *)playParam {
    return [SPCGIManagerHelper buildCGIRequestParamWithPlayParam:playParam];
}

- (void)processQuickPlayWithPlayInfo:(SPMediaPlayInfo *)playInfo {
    if ([SPPlayerUtils isQuickPlayWithMediaInfo:self.playParam.mediaInfo]) {
        SPLOGS(self.logTag, @"process quickplay, requestID=%d", self.requestID);
        SPVODPlayInfo *vodPlayInfo = (SPVODPlayInfo *)playInfo;
        self.playParam.mediaInfo.videoId = playInfo.vid;
        
        NSString *historyVid = [SPPlayerUtils historyVidFromMediaInfo:self.playParam.mediaInfo];
        BOOL skipStartAndEnd = [SPPlayerUtils needSkipStartAndEndWithMediaInfo:self.playParam.mediaInfo];
        if (historyVid == nil) {
            self.playParam.mediaInfo.startPosition = skipStartAndEnd ? vodPlayInfo.startPosition : 0;
        } else {
            if (![historyVid isEqualToString:vodPlayInfo.vid]) {
                // 如果有历史记录，startPosition传的时历史记录position。但历史记录vid和getVInfo返回的vid不一致，则使用getVInfo返回的跳过片头时间
                SPLOGS(self.logTag, @"history vid not match, use skipStart returned by getVInfo");
                self.playParam.mediaInfo.startPosition = skipStartAndEnd ? vodPlayInfo.startPosition : 0;
            } else {
                if (vodPlayInfo.videoState == SPVODVideoStateNeedCharge) { //试看
                    self.playParam.mediaInfo.startPosition = skipStartAndEnd ? vodPlayInfo.startPosition : 0;
                }
            }
        }
        
        // 试看不设置跳过片尾
        if (vodPlayInfo.videoState != SPVODVideoStateNeedCharge && vodPlayInfo.exem != SPLimitTypeDefnPreview) {
            self.playParam.mediaInfo.skipEndPosition = skipStartAndEnd ? vodPlayInfo.skipEndPosition : 0;
        }
        
        // 清楚秒播标记
        [SPPlayerUtils removeQuickPlayInfoOfMediaInfo:self.playParam.mediaInfo];
    }
}

- (void)processPrewatchWithPlayInfo:(SPMediaPlayInfo *)playInfo {
    if (!playInfo.isPreWatch) {
        return;
    }
    if (![playInfo isKindOfClass:[SPVODPlayInfo class]]) {
        return;
    }
    
    //br1 : 如果是试看视频，去掉跳过片尾的设置
    self.playParam.mediaInfo.skipEndPosition = 0;
    
    self.playParam.mediaInfo.haveResetStartPosition = NO;
    //br2 : 如果起播时间大于试看结束时长，将startpos标记为0
    SPVODPlayInfo *vodPlayInfo = ((SPVODPlayInfo *)playInfo);
    if (self.playParam.mediaInfo.startPosition >= vodPlayInfo.vodPreViewEnd) {
        SPLOGS(self.logTag, @"processPrewatchWithPlayInfo, startPosition=%f, vodPreViewEnd=%f",
               self.playParam.mediaInfo.startPosition,
               vodPlayInfo.vodPreViewEnd);
        self.playParam.mediaInfo.startPosition = 0;
        self.playParam.mediaInfo.haveResetStartPosition = YES;
    }
    
    //br3 : 如果起播时间小于试看开始时间，将startpos标记为开始时间
    if (self.playParam.mediaInfo.startPosition < vodPlayInfo.vodPreviewStart) {
        SPLOGS(self.logTag, @"processPrewatchWithPlayInfo, startPosition=%f, vodPreviewStart=%f",
               self.playParam.mediaInfo.startPosition,
               vodPlayInfo.vodPreviewStart);
        self.playParam.mediaInfo.startPosition = MAX(0, vodPlayInfo.vodPreviewStart);
        self.playParam.mediaInfo.haveResetStartPosition = YES;
    }
}

#pragma mark-ISPPlayInfoGetterDelegate
- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onGetPlayInfo:(SPMediaPlayInfo *)playInfo playID:(int)playID {
    SPLOGS(self.logTag, @"cgi success, requestID=%d", playID);
    @weakify(self)
    dispatch_async(self.commonParam.playerQueue, ^{
        @strongify(self)
        if (self.cancelled) {
            SPLOGS(self.logTag, @"already cancelled, requestID=%d", self.requestID);
            return;
        }

        if (self.requestID != playID) {
            SPLOGS(self.logTag, @"requestID not match, %d,%d", self.requestID, playID);
            return;
        }

        [self processQuickPlayWithPlayInfo:playInfo];
        [self processPrewatchWithPlayInfo:playInfo];
        playInfo.mediaInfo = self.playParam.mediaInfo;

        [SPCGIManagerPrinter printResponse:playInfo logTag:self.logTag];

        if ([self.delegate respondsToSelector:@selector(cgiManagerOnGetPlayInfo:requestParam:)]) {
            [self.delegate cgiManagerOnGetPlayInfo:playInfo requestParam:self.playParam];
        }

        self.playParam = nil;
    });
}

- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onGetPlayInfoFailedWithError:(NSError *)error playID:(int)playID {
    SPLOGS(self.logTag, @"cgi failed, error=%@, requestID=%d", error, playID);
    @weakify(self)
    dispatch_async(self.commonParam.playerQueue, ^{
        @strongify(self)

        if (self.cancelled) {
            SPLOGS(self.logTag, @"already cancelled, requestID=%d, ", self.requestID);
            return;
        }

        if (self.requestID != playID) {
            return;
        }

        if ([self.delegate respondsToSelector:@selector(cgiManagerOnError:requestParam:)]) {
            [self.delegate cgiManagerOnError:error requestParam:self.playParam];
        }
        self.playParam = nil;
    });
}



@end
