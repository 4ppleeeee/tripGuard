//
//  SPCGIQueryBuilder.h
//  SPPlayer
//
//  Created by liyukuan on 2019/11/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIRequestParam.h"
#import "SPCGICapabilityParam.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * 构建CGI请求URL中的query，以key-value的形式放在字典里
 */
@interface SPCGIQueryBuilder : NSObject

+ (void)buildCommonParam:(NSMutableDictionary *)paramDict requestCommonParam:(SPCGIRequestCommonParam *)requestCommonParam;

@end

NS_ASSUME_NONNULL_END
