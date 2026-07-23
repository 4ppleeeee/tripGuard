//
//  SPCGIBase.m
//  SPPlayer
//
//  Created by liyukuan on 2019/11/4.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIBase.h"

@implementation SPCGIBase

- (instancetype)initWithParam:(SPCGIInitParam *)param {
    if ((self = [super init])) {
        _cgiInitParam = param;
    }

    return self;
}
@end
