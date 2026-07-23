//
//  SPPlayingContextHelper.m
//  SPPlayer
//
//  Created by ethanyxliu on 2019/11/25.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPPlayingContextHelper.h"

@implementation SPPlayingContextHelper

+ (BOOL)isDLNAWithPlayContext:(SPPlayingContext *)playContext {
    id value = [playContext.extraConfig objectForKey:@"is_dlna"];
    if (value == nil || ![value isKindOfClass:[NSString class]]) {
        return NO;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

+ (BOOL)isLiveGetPreviewWithPlayContext:(SPPlayingContext *)playContext {
    id value = [playContext.extraConfig objectForKey:@"is_live_get_preview"];
    if (value == nil || ![value isKindOfClass:[NSString class]]) {
        return NO;
    }

    return [(NSString *)value isEqualToString:@"1"];
}

@end
