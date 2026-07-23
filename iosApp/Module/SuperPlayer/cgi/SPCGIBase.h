//
//  SPCGIBase.h
//  SPPlayer
//
//  Created by liyukuan on 2019/11/4.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ISPPlayInfoGetter.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPCGIBase : NSObject

@property (nonatomic, strong) SPCGIInitParam *cgiInitParam;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithParam:(SPCGIInitParam *)param NS_DESIGNATED_INITIALIZER;

@end

NS_ASSUME_NONNULL_END
