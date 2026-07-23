//
//  SPPlayerBase.h
//  SPPlayer
//
//  Created by ethanyxliu on 2019/10/30.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayerLogContextUtil.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPPlayerBase : NSObject

@property (strong, nonatomic) SPPlayerLogContext *playerLogContext;

@end

NS_ASSUME_NONNULL_END
