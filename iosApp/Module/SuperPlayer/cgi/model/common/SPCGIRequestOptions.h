//
//  SPCGIRequestOptions.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/11.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPCGIRequestOptions : NSObject

@property (nonatomic, assign) SPCGIIPStack ipStack;

@property (nonatomic, assign) BOOL preferIPV6;  // 在双栈环境下是否优先使用IPV6

@property (nonatomic, assign) BOOL useHttps;

@property (nonatomic, assign) BOOL useCache;

@property (nonatomic, assign) int maxRetryTimes;  // 最大重试次数，如果不设置，默认为6次，主备份域名各3次

@end

NS_ASSUME_NONNULL_END
