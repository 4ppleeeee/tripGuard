/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPVODInfoGetter.m
 * @brief    点播cgi请求实现
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/21
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPVODInfoGetter.h"
#import "SPGetVInfoData.h"
#import "SPGetVBKeyData.h"
#import "SPVODRequestParam.h"
#import "SPGetVInfoRequest.h"
#import "SPVBKeyRequest.h"
#import "SPVODURLBuilder.h"

@interface SPVODInfoGetter () <SPGetVInfoRequestDelegate, SPVBKeyRequestDelegate>

@property (nonatomic, strong) SPGetVInfoRequest *getVInfoRequest;

@property (nonatomic, strong) SPVBKeyRequest *vkeyRequest;

@property (nonatomic, assign) int requestID;

@property (nonatomic, strong) SPVODRequestParam *requestParam;

@property (nonatomic, strong) SPGetVInfoData *getvinfoData;

@property (nonatomic, assign) BOOL stopped;

@property (nonatomic, strong) NSRecursiveLock *lock;

@end

@implementation SPVODInfoGetter
@synthesize delegate = _delegate;

- (instancetype)initWithParam:(SPCGIInitParam *)param {
    if ((self = [super initWithParam:param])) {
        _getVInfoRequest = [[SPGetVInfoRequest alloc] initWithParam:param];
        _getVInfoRequest.delegate = self;
        _lock = [[NSRecursiveLock alloc] init];
    }

    return self;
}

- (int)requestWithParam:(SPCGIRequestParam *)param {
    SPLOGS(self.cgiInitParam.logTag, @"SPVODInfoGetter requestWithParam");
    [self.lock lock];
    [self stopWithPlayID:self.requestID];
    self.stopped = NO;
    self.requestParam = (SPVODRequestParam *)param;
    [self.lock unlock];

    // 请注意这行代码为什么没有加锁，因为SPGetVInfoRequest里面有自己的锁，在[SPGetVInfoRequest
    // requestWithParam:]里面也会加锁锁，而SPGetVInfoRequest的回调是在另一个线程回调的，也会加锁，
    // 如果这里加锁的话，会造成一个典型的死锁问题：线程A持有了锁A，并且尝试获取锁B，线程B持有了锁B，并且尝试获取锁A。总之，在调用下一层接口的时候，这一层最好不要拿着锁。
    int requestID = [self.getVInfoRequest requestWithParam:param];

    [self.lock lock];
    self.requestID = requestID;
    SPLOGS(self.cgiInitParam.logTag, @"requestID=%d", self.requestID);
    [self.lock unlock];
    return self.requestID;
}

- (void)stopWithPlayID:(int)playID {
    [self.lock lock];
    if (self.stopped) {
        SPLOGS(self.cgiInitParam.logTag, @"already stopped, requestID=%d", playID);
        [self.lock unlock];
        return;
    }

    SPLOGS(self.cgiInitParam.logTag, @"SPVODInfoGetter stop, requestID=%d", playID);

    // vkeyRequest是在中间过程创建的，要放在锁里面，防止使用的时候被其他线程修改
    SPVBKeyRequest *vbkeyRequest = self.vkeyRequest;
    [self.lock unlock];

    // 这里为什么不能加锁，原因请看requestWithParam:里的注释
    [self.getVInfoRequest stopWithRequestID:playID];
    [vbkeyRequest stop];

    [self.lock lock];
    self.stopped = YES;
    [self.lock unlock];
}

#pragma mark -internal method

- (void)processGetVInfoData:(SPGetVInfoData *)getvinfoData {
    if (SPGetVInfoRequestTypeOfflinePlay == self.requestParam.getvinfoReqType) {
        [self notifyPlayInfo:getvinfoData.vodPlayInfo requestID:self.requestID];
        return;
    }

    SPVODPlayInfo *vodPlayInfo = getvinfoData.vodPlayInfo;
    [self fillVODPlayInfo:vodPlayInfo fromRequestParam:self.requestParam];
    if (SPMediaDLTypeHttp == vodPlayInfo.dltype) {
        [self processMP4:getvinfoData];
    } else if (SPMediaDLTypeHLS == vodPlayInfo.dltype || SPMediaDLTypeHLSM3U8 == vodPlayInfo.dltype) {
        [self processHLS:getvinfoData];
    }
}

- (void)processHLS:(SPGetVInfoData *)getvinfoData {
    SPLOGS(self.cgiInitParam.logTag, @"process HLS, requestID=%d", self.requestID);
    SPSection *section = [SPVODURLBuilder buildHLSURLWithGetVInfoData:getvinfoData
                                                                sdtFrom:self.requestParam.commonParams.sdtFrom
                                                               freeFlow:(self.requestParam.freeFlowParam.count > 0)];

    if (section) {
        getvinfoData.vodPlayInfo.sectionArray = @[ section ];
    }
    [self notifyPlayInfo:getvinfoData.vodPlayInfo requestID:self.requestID];
}

- (void)processMP4:(SPGetVInfoData *)getvinfoData {
    if (getvinfoData.vodPlayInfo.clipCount == 0) {
        [self processWholeMP4:getvinfoData];  // 整片MP4
    } else {
        [self processMultiMP4:getvinfoData];  // 分片MP4
    }
}

/**
 * 整片MP4
 */
- (void)processWholeMP4:(SPGetVInfoData *)getvinfoData {
    SPLOGS(self.cgiInitParam.logTag, @"process whole MP4, requestID=%d", self.requestID);
    SPSection *section = [SPVODURLBuilder buildWholeMP4URLWithGetVInfoData:getvinfoData
                                                                     sdtFrom:self.requestParam.commonParams.sdtFrom
                                                                    freeFlow:(self.requestParam.freeFlowParam.count > 0)];

    if (section) {
        getvinfoData.vodPlayInfo.sectionArray = @[ section ];
    }

    [self notifyPlayInfo:getvinfoData.vodPlayInfo requestID:self.requestID];
}

- (void)processMultiMP4:(SPGetVInfoData *)getvinfoData {
    SPLOGS(self.cgiInitParam.logTag, @"process multi MP4, requestID=%d", self.requestID);
    if (getvinfoData.vodPlayInfo.clipCount == 1) {
        [self processOnlyOneClip:getvinfoData];
        return;
    }
    
    int endIndex = getvinfoData.vodPlayInfo.clipCount;
    int beginIndex = 0;
    if (getvinfoData.vodPlayInfo.isPreWatch) {  // 处理试看的情况
        endIndex = [self endIndexOfPreview:getvinfoData.vodPlayInfo.vodPreViewEnd clipInfoArray:getvinfoData.clipInfoArray];
        if (endIndex <= 1) {
            [self processOnlyOneClip:getvinfoData];
            return;
        }
        //正常后台方案表示：有中间试看时，不会返回分片MP4格式
        if (getvinfoData.vodPlayInfo.vodPreviewStart > 0) {
            beginIndex = [self beginIndexOfPreview:getvinfoData.vodPlayInfo.vodPreviewStart clipInfoArray:getvinfoData.clipInfoArray];
        }
    }
    
    //确保beginIndex小于等于endIndex(endIndex不会小于2, 历史当没有beginIndex时，此处写死为2)
    beginIndex = MAX(beginIndex, 2);
    // 走到这里endIndex一定是大于1的
    [self requesSPeyWithGetVInfoData:getvinfoData beginIndex:beginIndex endIndex:endIndex];
}

- (void)processOnlyOneClip:(SPGetVInfoData *)getvinfoData {
    SPLOGS(self.cgiInitParam.logTag, @"process only one clip MP4, requestID=%d", self.requestID);
    SPSection *section = nil;
    if (self.requestParam.freeFlowParam.count > 0) {
        SPClipInfo *clipInfo = getvinfoData.clipInfoArray.firstObject;
        section = [SPVODURLBuilder buildURLDirectlyFromUIInfoArray:getvinfoData.uiInfoArray
                                                           duration:clipInfo.clipDuration
                                                           fileSize:clipInfo.clipSize
                                                            fileMD5:clipInfo.md5
                                                              keyID:clipInfo.keyID];
    } else {
        section = [SPVODURLBuilder buildMP4ClipURLWith:getvinfoData.clipInfoArray.firstObject
                                            uiInfoArray:getvinfoData.uiInfoArray
                                               fileName:getvinfoData.fileName
                                                   vkey:getvinfoData.fvKey
                                                   rate:getvinfoData.vodPlayInfo.rate
                                                   defn:getvinfoData.vodPlayInfo.currentDefinition.fileName
                                                sdtFrom:self.requestParam.commonParams.sdtFrom];
    }

    if (section) {
        getvinfoData.vodPlayInfo.sectionArray = @[ section ];
    }
    [self notifyPlayInfo:getvinfoData.vodPlayInfo requestID:self.requestID];
}

- (int)endIndexOfPreview:(NSTimeInterval)previewEndTime clipInfoArray:(NSArray<SPClipInfo *> *)clipInfoArray {
    int endIndex = -1;
    NSTimeInterval accumulatedDuration = 0;
    for (int i = 0; i < clipInfoArray.count; i++) {
        SPClipInfo *clipInfo = [clipInfoArray objectAtIndex:i];
        accumulatedDuration += clipInfo.clipDuration;
        if (previewEndTime < accumulatedDuration + FLT_EPSILON) {
            endIndex = i + 1;
            SPLOGS(self.cgiInitParam.logTag, @"prewatch only use %d end clips, requestID=%d", endIndex, self.requestID);
            break;
        }
    }

    return endIndex;
}

- (int)beginIndexOfPreview:(NSTimeInterval)previewStartTime clipInfoArray:(NSArray<SPClipInfo *> *)clipInfoArray {
    int beginIndex = 0;
    NSTimeInterval accumulatedDuration = 0;
    for (int i = 0; i < clipInfoArray.count; i++) {
        SPClipInfo *clipInfo = [clipInfoArray objectAtIndex:i];
        accumulatedDuration += clipInfo.clipDuration;
        if (accumulatedDuration >= previewStartTime + FLT_EPSILON) {
            beginIndex = MAX(0, i - 1);
            SPLOGS(self.cgiInitParam.logTag, @"prewatch only use %d start clips, requestID=%d", beginIndex, self.requestID);
            break;
        }
    }

    return beginIndex;
}



/**
 * 发起vbkey请求
 * @param getvinfoData getvinfo解析出的信息
 * @param beginIndex 要请求的开始分片号
 * @param endIndex 要请求的结束分片号
 **/
- (void)requesSPeyWithGetVInfoData:(SPGetVInfoData *)getvinfoData beginIndex:(int)beginIndex endIndex:(int)endIndex {
    self.getvinfoData = getvinfoData;
    // 把fvkey填充到第一个分片，fvkey就是第一个分片的vkey
    SPClipInfo *firstClipInfo = getvinfoData.clipInfoArray.firstObject;
    firstClipInfo.vkey = getvinfoData.fvKey;

    self.vkeyRequest = [[SPVBKeyRequest alloc] initWithParam:self.cgiInitParam];
    SPVBKeyRequestParam *requestParam = [[SPVBKeyRequestParam alloc] init];
    requestParam.vodReqParam = self.requestParam;
    requestParam.getvinfoData = getvinfoData;
    requestParam.beginIndex = beginIndex;
    requestParam.endIndex = endIndex;
    self.vkeyRequest.delegate = self;
    [self.vkeyRequest requestWithParam:requestParam];
}

- (void)notifyPlayInfo:(SPMediaPlayInfo *)playInfo requestID:(int)requestID {
    if ([self.delegate respondsToSelector:@selector(playInfoGetter:onGetPlayInfo:playID:)]) {
        [self.delegate playInfoGetter:self onGetPlayInfo:playInfo playID:requestID];
    }

    self.requestParam = nil;
}

- (void)notifyError:(NSError *)error requestID:(int)requestID {
    if ([self.delegate respondsToSelector:@selector(playInfoGetter:onGetPlayInfoFailedWithError:playID:)]) {
        [self.delegate playInfoGetter:self onGetPlayInfoFailedWithError:error playID:requestID];
    }

    self.requestParam = nil;
}

- (void)fillVODPlayInfo:(SPVODPlayInfo *)vodPlayInfo fromRequestParam:(SPCGIRequestParam *)requestParam {
    vodPlayInfo.coverID = requestParam.cid;
}

#pragma mark -SPGetVInfoRequestDelegate
- (void)request:(SPGetVInfoRequest *)request onGetVInfoData:(SPGetVInfoData *)getvinfoData requestID:(int)requestID {
    SPLOGS(self.cgiInitParam.logTag, @"getvinfo success, requestID=%d", requestID);
    [self.lock lock];
    if (self.requestID != requestID) {
        [self.lock unlock];
        return;
    }

    [self processGetVInfoData:getvinfoData];
    [self.lock unlock];
}

- (void)request:(SPGetVInfoRequest *)request onGetVInfoFailed:(NSError *)error requestID:(int)requestID {
    SPLOGS(self.cgiInitParam.logTag, @"getvinfo failed, requestID=%d", requestID);
    [self.lock lock];
    if (self.requestID != requestID) {
        [self.lock unlock];
        return;
    }

    [self notifyError:error requestID:self.requestID];
    [self.lock unlock];
}

#pragma mark -SPVBKeyRequestDelegate
- (void)request:(SPVBKeyRequest *)request onGeSPeyData:(SPGetVBKeyData *)geSPeyData requestID:(int)requestID {
    SPLOGS(self.cgiInitParam.logTag, @"getvbkey success, requestID=%d", requestID);
    [self.lock lock];
    if (self.vkeyRequest != request || self.stopped) {
        [self.lock unlock];
        return;
    }

    // 从getvbkey返回的clipInfo补充vkey和urlList
    NSArray<SPClipInfo *> *clipInfoArray = self.getvinfoData.clipInfoArray;
    for (SPClipInfo *clipInfo in clipInfoArray) {
        SPClipInfo *newClipInfo = [geSPeyData.clipInfoDict objectForKey:@(clipInfo.index)];
        if (newClipInfo) {
            clipInfo.vkey = newClipInfo.vkey;
            clipInfo.urlList = newClipInfo.urlList;
        }
    }

    NSMutableArray<SPSection *> *sectionArray = [[NSMutableArray alloc] init];
    // 处理免流情况
    if (self.requestParam.freeFlowParam.count > 0) {
        // 第一个分片是getvinfo返回的，先构建第一个分片的，TODO:这里调试的时候注意一下，在免流情况下，getvinfo的cl.ci节点下面有没有url,hemanli
        SPClipInfo *clipInfo = clipInfoArray.firstObject;
        SPSection *firstSection = [SPVODURLBuilder buildURLDirectlyFromUIInfoArray:self.getvinfoData.uiInfoArray
                                                                            duration:clipInfo.clipDuration
                                                                            fileSize:clipInfo.clipSize
                                                                             fileMD5:clipInfo.md5
                                                                               keyID:clipInfo.keyID];
        [sectionArray addObject:firstSection];
        // 其余分片的地址是getvbkey返回的，构建其余分片的地址
        for (int i = 1; i < clipInfoArray.count; i++) {
            SPClipInfo *clipInfo = [clipInfoArray objectAtIndex:i];
            SPSection *section = [[SPSection alloc] init];
            section.url = clipInfo.urlList.firstObject;
            section.index = 0;
            section.urlList = clipInfo.urlList;
            section.duration = clipInfo.clipDuration;
            section.clipSize = clipInfo.clipSize;
            section.clipMD5 = clipInfo.md5;
            section.keyID = clipInfo.keyID;
            section.vtList = firstSection.vtList;  // VT list其实是一样的
            [sectionArray addObject:section];
        }

    } else {
        for (SPClipInfo *clipInfo in clipInfoArray) {
            SPSection *section = [SPVODURLBuilder buildMP4ClipURLWith:clipInfo
                                                            uiInfoArray:self.getvinfoData.uiInfoArray
                                                               fileName:geSPeyData.fileName
                                                                   vkey:clipInfo.vkey
                                                                   rate:self.getvinfoData.vodPlayInfo.rate
                                                                   defn:self.getvinfoData.vodPlayInfo.currentDefinition.fileName
                                                                sdtFrom:self.requestParam.commonParams.sdtFrom];
            [sectionArray addObject:section];
        }
    }

    self.getvinfoData.vodPlayInfo.sectionArray = sectionArray;
    [self notifyPlayInfo:self.getvinfoData.vodPlayInfo requestID:self.requestID];
    [self.lock unlock];
}

- (void)request:(SPVBKeyRequest *)request onGeSPeyFailed:(NSError *)error requestID:(int)requestID {
    SPLOGS(self.cgiInitParam.logTag, @"getvbkey failed, requestID=%d", requestID);
    [self.lock lock];
    [self notifyError:error requestID:self.requestID];
    [self.lock unlock];
}

@end
