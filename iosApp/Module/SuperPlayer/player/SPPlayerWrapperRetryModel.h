//
//  SPPlayerWrapperRetryModel.h
//  SPPlayer
//
//  Created by 郭力 on 2019/10/8.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPMediaInfo.h"
#import "SPMediaPlayInfo.h"
#import <Foundation/Foundation.h>
#import <ThumbPlayer/TPErrorType.h>
NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, SPPlayerRetryMode) {
    SPPlayerRetryModeCallError = 0,
    SPPlayerRetryModeDisableH265,
    SPPlayerRetryModeDisableDolby,
    SPPlayerRetryModeDisableDRM,
    SPPlayerRetryModeDisableHDR,
};

@interface SPPlayerWrapperRetryModel : NSObject

+ (SPPlayerRetryMode)retryModeForPlayerError:(TPErrorType)error
                                withMediaInfo:(SPMediaInfo *)mediaInfo
                                  andPlayInfo:(SPMediaPlayInfo *)playInfo;

@end

NS_ASSUME_NONNULL_END
