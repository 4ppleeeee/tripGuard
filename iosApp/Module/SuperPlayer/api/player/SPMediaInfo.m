/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPMediaInfo.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/1/6
 Description :
 History     : 17/1/6 初始版本
 ***********************************************************/

#import "SPMediaInfo.h"
#import "SPPlayerUtils.h"
#import <objc/runtime.h>

@implementation SPMediaInfo

- (instancetype)init {
    self = [super init];
    if (self) {
        _useVInfoGetterCache = YES;
    }
    return self;
}

- (NSString *)description {
    NSMutableString *desciptionString = [NSMutableString string];
    [desciptionString appendFormat:@"vid=%@, cid=%@, srccontenid=%@, playType=%@\n", self.videoId,
                                    self.coverId, self.srccontenid, [SPPlayerUtils stringForPlayType:self.playType]];
    [desciptionString appendFormat:@"definition=%@, startPosition=%lf, skipEndPosition:%lf\n",
                                    self.definition, self.startPosition, self.skipEndPosition];
    [desciptionString appendFormat:@"url=%@, columnId=%@, reportInfoMap=%@\n",
                                    self.url, self.columnId, self.reportInfoMap];
    [desciptionString appendFormat:@"extraParam=%@, configMap=%@\n", self.extraRequestParamsMap, self.configMap];

    return [desciptionString copy];
}

+ (BOOL)isValidReportInfoMap:(NSDictionary *)reportMap {
    if (reportMap.count > 0) {
        return [NSJSONSerialization isValidJSONObject:reportMap];
    }
    return NO;
}

@end
