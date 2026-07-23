//
//  SPCGIFactory.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/15.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ISPPlayInfoGetter.h"
#import "SPPlayParam.h"

typedef NS_ENUM(NSUInteger, SPPlayInfoGetterType) {
    SPPlayInfoGetterTypeNone,
    SPPlayInfoGetterTypeVOD,         // 正常getvinfo
    SPPlayInfoGetterTypeDirectInfo,  // 外部直接传递getvinfo
    SPPlayInfoGetterTypeLive,        // 直播
};

@interface SPCGIFactory : NSObject

+ (id<ISPPlayInfoGetter>)createPlayInfoGetterWithPlayParam:(SPPlayParam *)playParam
                                               cgiInitParam:(SPCGIInitParam *)cgiInitParam;

+ (id<ISPPlayInfoGetter>)createPlayInfoGetterWithType:(SPPlayInfoGetterType)type
                                          cgiInitParam:(SPCGIInitParam *)cgiInitParam;

@end
