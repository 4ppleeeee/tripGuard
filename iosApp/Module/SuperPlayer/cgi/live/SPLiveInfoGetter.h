//
//  SPLiveInfoGetter.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/21.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ISPPlayInfoGetter.h"
#import "SPCGIBase.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPLiveInfoGetter : SPCGIBase <ISPPlayInfoGetter>

- (instancetype)init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
