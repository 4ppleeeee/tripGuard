//
//  SPCGIManagerHelper.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/4.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayParam.h"
#import "SPCGIRequestParam.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPCGIManagerHelper : NSObject

+ (SPCGIRequestParam *)buildCGIRequestParamWithPlayParam:(SPPlayParam *)playParam;

+ (NSString *)stringOfRequestType:(SPCGIRequestType)requestType;

+ (NSString *)stringOfGetVInfoRequestType:(SPGetVInfoRequestType)getvinfoRequestType;

+ (SPCGINetType)cgiNetType;

@end

NS_ASSUME_NONNULL_END
