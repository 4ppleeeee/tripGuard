//
//  SPCGIManagerPrinter.h
//  SPPlayer
//
//  Created by liyukuan on 2019/11/28.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayParam.h"
#import "SPMediaPlayInfo.h"
#import "SPCGIRequestParam.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPCGIManagerPrinter : NSObject

+ (void)printPlayParam:(SPPlayParam *)playParam logTag:(NSString *)logTag;

+ (void)printRequestParam:(SPCGIRequestParam *)requestParam logTag:(NSString *)logTag;

+ (void)printResponse:(SPMediaPlayInfo *)playInfo logTag:(NSString *)logTag;

@end

NS_ASSUME_NONNULL_END
