//
//  SPCGIQueryBuilder.m
//  SPPlayer
//
//  Created by liyukuan on 2019/11/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIQueryBuilder.h"

@implementation SPCGIQueryBuilder

+ (void)buildCommonParam:(NSMutableDictionary *)paramDict requestCommonParam:(SPCGIRequestCommonParam *)requestCommonParam {
    if (paramDict == nil) {
        return;
    }
    [paramDict spSetString:requestCommonParam.platform forKey:@"platform"];
    [paramDict spSetString:[NSString stringWithFormat:@"ios%@", requestCommonParam.sysVer] forKey:@"sysver"];
    NSString *device = [requestCommonParam.deviceModel stringByReplacingOccurrencesOfString:@" " withString:@"_"];
    [paramDict spSetString:device forKey:@"device"];

    NSString *netTypeStr = [NSString stringWithFormat:@"%d", (int)requestCommonParam.netType];
    [paramDict spSetString:netTypeStr forKey:@"nettype"];
    [paramDict spSetString:netTypeStr forKey:@"newnettype"];
    [paramDict spSetString:requestCommonParam.uin forKey:@"qq"];  // TOCHECK:是否需要, hemanli
    [paramDict spSetString:requestCommonParam.uin forKey:@"qqlog"];
    [paramDict spSetString:requestCommonParam.guid forKey:@"guid"];
//    [paramDict spSetString:requestCommonParam.qimei forKey:@"qimei"];
    [paramDict spSetString:requestCommonParam.userID forKey:@"userid"];
    [paramDict spSetString:requestCommonParam.wxOpenID forKey:@"openid"];
    [paramDict spSetString:requestCommonParam.localeIdentifier forKey:@"lang"];
}

@end
