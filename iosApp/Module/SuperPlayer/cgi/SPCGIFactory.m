//
//  SPCGIFactory.m
//  SPPlayer
//
//  Created by liyukuan on 2019/10/15.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIFactory.h"
#import "SPVODInfoGetter.h"
#import "SPLiveInfoGetter.h"
#import "SPPlayParam.h"

@implementation SPCGIFactory

+ (id<ISPPlayInfoGetter>)createPlayInfoGetterWithPlayParam:(SPPlayParam *)playParam
                                               cgiInitParam:(SPCGIInitParam *)cgiInitParam {
    id<ISPPlayInfoGetter> infoGetter = nil;
    SPPlayInfoGetterType type = SPPlayInfoGetterTypeNone;
    if (SPPlayTypeOnlineLive == playParam.mediaInfo.playType) {
        type = SPPlayInfoGetterTypeLive;
    } else {
        type = SPPlayInfoGetterTypeVOD;
    }
    
    infoGetter = [self createPlayInfoGetterWithType:type cgiInitParam:cgiInitParam];
    return infoGetter;
}

+ (id<ISPPlayInfoGetter>)createPlayInfoGetterWithType:(SPPlayInfoGetterType)type
                                          cgiInitParam:(SPCGIInitParam *)cgiInitParam {
    id<ISPPlayInfoGetter> infoGetter = nil;
    switch (type) {
        case SPPlayInfoGetterTypeVOD:
            infoGetter = [[SPVODInfoGetter alloc] initWithParam:cgiInitParam];
            break;
        case SPPlayInfoGetterTypeLive:
            infoGetter = [[SPLiveInfoGetter alloc] initWithParam:cgiInitParam];
            break;
        default:
            break;
    }
    
    return infoGetter;
}

@end
