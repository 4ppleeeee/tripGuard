//
//  SPPlayingContextHelper.h
//  SPPlayer
//
//  Created by ethanyxliu on 2019/11/25.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayingContext.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPPlayingContextHelper : NSObject

+ (BOOL)isDLNAWithPlayContext:(SPPlayingContext *)playContext;

+ (BOOL)isLiveGetPreviewWithPlayContext:(SPPlayingContext *)playContext;

@end

NS_ASSUME_NONNULL_END
