//
//  SPWeakProxy.h
//  SuperPlayer
//
//  Created by ethanyxliu on 2020/8/18.
//  Copyright © 2020 tencent. All rights reserved.
//

#import <Foundation/NSProxy.h>

NS_ASSUME_NONNULL_BEGIN

@interface SPWeakProxy : NSProxy

@property (nullable, nonatomic, weak, readonly) id target;

+ (instancetype)proxyWithTarget:(id)target;

@end

NS_ASSUME_NONNULL_END
