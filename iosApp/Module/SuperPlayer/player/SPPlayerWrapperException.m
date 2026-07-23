//
//  SPPlayerWrapperException.m
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayerWrapperException.h"

@implementation CommonInfo
@end

@implementation ErrorInfo
@end

@implementation RetryInfo
@end

@implementation SPPlayerWrapperException

- (instancetype)init {
    if (self = [super init]) {
        self.commonInfo = [CommonInfo alloc];
        self.errorInfo = [ErrorInfo alloc];
        self.retryInfo = [RetryInfo alloc];

        self.commonInfo.level = LevelWarning;
        self.commonInfo.position = 0;
        self.commonInfo.logMode = LogModeImmediate;

        self.retryInfo.retryMode = RetryModeSource;
    }
    return self;
}
@end
