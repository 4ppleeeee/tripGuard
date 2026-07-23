//
//  SPVODPlayInfo.m
//  SPPlayer
//
//  Created by ethanyxliu on 2019/9/13.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPVODPlayInfo.h"
#import "SPCGIDefines.h"

@implementation SPVODPlayInfo

- (NSTimeInterval)vodPreViewEnd {
    return self.vodPreviewStart + self.vodPreViewTime;
}

- (BOOL)isPreWatch {
    return (self.exem == SPLimitTypeDefnPreview || self.videoState == SPVODVideoStateNeedCharge);
}

@end
