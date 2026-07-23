/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPPlayingContext.m
 * @brief    播放上下文，用来指定在请求cgi时满足当前播放的一些参数
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPPlayingContext.h"

@implementation SPPlayingContext

- (instancetype)init {
    if ((self = [super init])) {
        _enableHEVC = YES;
        _requiredMediaFormat = SPMediaFormatAuto;
        _enableFairPlay = YES;
    }

    SPLOGS(@"SPPlayingContext", @"init:%@", self);

    return self;
}

- (id)copyWithZone:(NSZone *)zone {
    SPPlayingContext *context = [[self class] allocWithZone:zone];
    context.enableHEVC = self.enableHEVC;
    context.enableFairPlay = self.enableFairPlay;
    context.requiredDefinition = self.requiredDefinition;
    context.currentPlayPosition = self.currentPlayPosition;
    context.requiredMediaFormat = self.requiredMediaFormat;
    context.liveSeebackTime = self.liveSeebackTime;
    context.extraInfo = self.extraInfo;  // TODO: ethanyxliu 需要考虑自定义类，需要实现NSCopying
    context.extraRequestParams = [[NSDictionary alloc] initWithDictionary:self.extraRequestParams copyItems:YES];
    return context;
}

- (void)copyFrom:(SPPlayingContext *)context {
    self.enableHEVC = context.enableHEVC;
    self.enableFairPlay = context.enableFairPlay;
    self.requiredDefinition = context.requiredDefinition;
    self.currentPlayPosition = context.currentPlayPosition;
    self.requiredMediaFormat = context.requiredMediaFormat;
    self.liveSeebackTime = context.liveSeebackTime;
    self.extraInfo = [context.extraInfo copy];
    self.extraRequestParams = [[NSDictionary alloc] initWithDictionary:context.extraRequestParams copyItems:YES];
}
@end
