//
//  SPLiveInfoGetter.m
//  SPPlayer
//
//  Created by liyukuan on 2019/9/21.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPLiveInfoGetter.h"
#import "SPLiveRequestParam.h"
#import "SPLiveInfoRequest.h"

@interface SPLiveInfoGetter () <SPLiveInfoRequestDelegate>

@property (nonatomic, assign) int requestID;

@property (nonatomic, strong) SPLiveInfoRequest *liveInfoRequest;

@property (nonatomic, strong) NSRecursiveLock *lock;

@end

@implementation SPLiveInfoGetter
@synthesize delegate = _delegate;

- (instancetype)initWithParam:(SPCGIInitParam *)param {
    if ((self = [super initWithParam:param])) {
        _liveInfoRequest = [[SPLiveInfoRequest alloc] initWithParam:param];
        _liveInfoRequest.delegate = self;
        _lock = [[NSRecursiveLock alloc] init];
    }

    return self;
}

- (int)requestWithParam:(SPCGIRequestParam *)param {
    SPLOGS(self.cgiInitParam.logTag, @"SPLiveInfoGetter requestWithParam");

    // 请注意这行代码为什么没有加锁，因为SPLiveInfoRequest里面有自己的锁，在[SPLiveInfoRequest
    // requestWithParam:]里面也会加锁锁，而SPLiveInfoRequest的回调是在另一个线程回调的，也会加锁，
    // 如果这里加锁的话，会造成一个典型的死锁问题：线程A持有了锁A，并且尝试获取锁B，线程B持有了锁B，并且尝试获取锁A。总之，在调用下一层接口的时候，这一层最好不要拿着锁。
    int requestID = [self.liveInfoRequest requestWithParam:param];

    [self.lock lock];
    self.requestID = requestID;
    SPLOGS(self.cgiInitParam.logTag, @"SPLiveInfoGetter requestID=%d", self.requestID);
    [self.lock unlock];

    return self.requestID;
}

- (void)stopWithPlayID:(int)playID {
    [self.lock lock];
    SPLOGS(self.cgiInitParam.logTag, @"stop, requestID=%d", self.requestID);
    [self.lock unlock];

    [self.liveInfoRequest stopWithRequestID:playID];
}

#pragma mark -SPLiveInfoRequestDelegate
- (void)request:(SPLiveInfoRequest *)request onGetLiveInfo:(SPLivePlayInfo *)playInfo requestID:(int)requestID {
    SPLOGS(self.cgiInitParam.logTag, @"live cgi success, requestID=%d", requestID);
    [self.lock lock];
    if (self.requestID != requestID) {
        SPLOGS(self.cgiInitParam.logTag, @"requestID not match, %d:%d", self.requestID, requestID);
        [self.lock unlock];
        return;
    }

    if ([self.delegate respondsToSelector:@selector(playInfoGetter:onGetPlayInfo:playID:)]) {
        [self.delegate playInfoGetter:self onGetPlayInfo:playInfo playID:self.requestID];
    }
    [self.lock unlock];
}

- (void)request:(SPLiveInfoRequest *)request onGetLiveInfoFailed:(NSError *)error requestID:(int)requestID {
    SPLOGS(self.cgiInitParam.logTag, @"live cgi failed, requestID=%d", requestID);
    [self.lock lock];
    if (self.requestID != requestID) {
        SPLOGS(self.cgiInitParam.logTag, @"requestID not match, %d:%d", self.requestID, requestID);
        [self.lock unlock];
        return;
    }

    if ([self.delegate respondsToSelector:@selector(playInfoGetter:onGetPlayInfoFailedWithError:playID:)]) {
        [self.delegate playInfoGetter:self onGetPlayInfoFailedWithError:error playID:self.requestID];
    }
    [self.lock unlock];
}

@end
