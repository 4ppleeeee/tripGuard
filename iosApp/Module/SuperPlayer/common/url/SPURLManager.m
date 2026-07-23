//
//  SPURLManager.m
//  SPPlayer
//
//  Created by liyukuan on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPURLManager.h"

static NSString *const kSPVInfoTestHost = @"testvv.video.qq.com";
static NSString *const kSPLInfoTestHost = @"test.zb.video.qq.com";

@implementation SPURLManager

+ (NSString *)getvinfoHost {
    return SPSDKCONF_GETVINFO_ENV == 0 ? @"vv.video.qq.com" : kSPVInfoTestHost;
}

+ (NSString *)getVInfoBackHost {
    return SPSDKCONF_GETVINFO_ENV == 0 ? @"bkvv.video.qq.com" : kSPVInfoTestHost;
}

+ (NSString *)getVInfoIPV6Host {
    return SPSDKCONF_GETVINFO_ENV == 0 ? @"vv6.video.qq.com" : kSPVInfoTestHost;
}

+ (NSString *)liveInfoHost {
    return SPSDKCONF_GETVINFO_ENV == 0 ? @"info.zb.video.qq.com" : kSPLInfoTestHost;
}

+ (NSString *)liveInfoBackHost {
    return SPSDKCONF_GETVINFO_ENV == 0 ? @"bk.info.zb.video.qq.com" : kSPLInfoTestHost;
}

@end
