/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPLiveSeeBackBaseInfo.m
 Author      : charli
 Version     : 1.0
 Date        : 16/11/17
 Description :
 History     : 16/11/17 初始版本
 ***********************************************************/

#import "SPLiveSeeBackBaseInfo.h"

@implementation SPLiveSeeBackBaseInfo

+ (SPLiveSeeBackBaseInfo *)seeBackInfoWithDict:(NSDictionary *)dict {
    if (!dict || dict.count <= 0) {
        return nil;
    }
    SPLiveSeeBackBaseInfo *seeBackBaseInfo = [[SPLiveSeeBackBaseInfo alloc] init];
    seeBackBaseInfo.seeBackstartTime        = [[dict spNumberForKeySafeModel:@"playbackstart"] longLongValue];
    seeBackBaseInfo.maxSeeBacktime          = [[dict spNumberForKeySafeModel:@"playbacktime"] longLongValue];
    seeBackBaseInfo.serverTime              = [[dict spNumberForKeySafeModel:@"svrtick"] longLongValue];
    seeBackBaseInfo.hasSeeBack              = YES;
    return seeBackBaseInfo;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"seeBackStartTime=%lld, maxSeeBackTime=%lld, serverTime=%lld",
            self.seeBackstartTime, self.maxSeeBacktime, self.serverTime];
}
@end
