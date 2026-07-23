//
//  SPCGIRequestIDMgr.m
//  SPPlayer
//
//  Created by hemanli on 2019/10/20.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIRequestIDMgr.h"

static int gVodRequestID = 20000;

static int gVbkeyRequestID = 30000;

static int gLiveRequestID = 40000;

@interface SPCGIRequestIDMgr ()

@property (nonatomic, strong) NSRecursiveLock *getVInfoIDLock;

@property (nonatomic, strong) NSRecursiveLock *getVBKeyIDLock;

@property (nonatomic, strong) NSRecursiveLock *LiveIDLock;

@end

@implementation SPCGIRequestIDMgr

+ (instancetype)sharedInstance {
    static SPCGIRequestIDMgr *s_globalLock = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_globalLock = [[SPCGIRequestIDMgr alloc] init];
    });

    return s_globalLock;
}

- (instancetype)init {
    if ((self = [super init])) {
        _getVBKeyIDLock = [[NSRecursiveLock alloc] init];
        _getVBKeyIDLock = [[NSRecursiveLock alloc] init];
        _LiveIDLock = [[NSRecursiveLock alloc] init];
    }

    return self;
}

- (int)generateGetVInfoRequestID {
    [self.getVInfoIDLock lock];
    int requestID = ++gVodRequestID;
    [self.getVInfoIDLock unlock];
    return requestID;
}

- (int)generateGetVBKeyRequestID {
    [self.getVBKeyIDLock lock];
    int requestID = ++gVbkeyRequestID;
    [self.getVBKeyIDLock unlock];
    return requestID;
}

- (int)generateGetLiveRequestID {
    [self.LiveIDLock lock];
    int requestID = ++gLiveRequestID;
    [self.LiveIDLock unlock];
    return requestID;
}

@end
