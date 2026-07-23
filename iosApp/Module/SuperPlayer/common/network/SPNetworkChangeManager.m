/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetworkChangeManager.m
 Author      : deronhuang
 Version     : 1.0
 Date        : 2017/1/3
 Description :
 History     : 2017/1/3 初始版本
 ***********************************************************/

#import "SPNetworkChangeManager.h"
#import "SPReachability.h"

@interface SPNetworkChangeManager ()

@property (nonatomic, strong) SPReachability *internetReach;

@end

@implementation SPNetworkChangeManager

+ (instancetype)sharedInstance {
    static id sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (id)init {
    if (self = [super init]) {
        self.internetReach = [SPReachability reachabilityForInternetConnection];
        [self.internetReach startNotifier];
    }
    return self;
}

@end
