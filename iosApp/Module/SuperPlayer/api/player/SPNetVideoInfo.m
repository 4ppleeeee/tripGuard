/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetVideoInfo.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/1/6
 Description :
 History     : 17/1/6 初始版本
 ***********************************************************/

#import "SPNetVideoInfo.h"
#import <objc/runtime.h>

@implementation SPNetMediaDefinitionInfo

- (NSString *)description {
    return [NSString stringWithFormat:@"definition:%@, fullText:%@, definitionShowShortName:%@, "
                                      @"isNeedVip:%d, fileSize:%llu, videoCodec:%d",
                                      self.definition, self.fullText, self.definitionShowShortName,
                                      self.isNeedVip, self.fileSize, self.videoCodec];
}

@end

@implementation SPNetThumbInfo

@end

@implementation SPNetVideoInfo

- (NSString *)description {
    return [NSString stringWithFormat:@"duration:%lf, vodPreviewTime:%lf, vodPreviewStart:%lf",
                                          self.duration, self.vodPreviewTime, self.vodPreviewStart];
}

- (NSTimeInterval)vodPreViewEnd {
    return self.vodPreviewStart + self.vodPreviewTime;
}

@end

@implementation SPLiveQueueInfo
- (NSString *)description {
    return [NSString stringWithFormat:@"queue_status:%d, queue_rank:%d, queue_vip_jump:%d, queue_session_key:%@",
                                      self.queue_status, self.queue_rank, self.queue_vip_jump, self.queue_session_key];
}
@end

@implementation SPNetLiveSeebackInfo

- (NSString *)description {
    return [NSString stringWithFormat:@"seebackStartTime:%ld, maxSeebackTime:%ld, serverTime:%ld, isSeebackState:%d",
                                      self.seebackStartTime, self.maxSeebackTime, self.serverTime, self.isSeebackState];
}

@end

@implementation SPNetLivePreviewInfo

- (NSString *)description {
  return [NSString stringWithFormat:@"playTime:%lf, previewTime:%lf, "
                                    @"previewCount:%ld, restPreviewCount:%ld",
                                    self.playTime, self.previewTime,
                                    (long)self.previewCount,
                                    (long)self.restPreviewCount];
}

@end
