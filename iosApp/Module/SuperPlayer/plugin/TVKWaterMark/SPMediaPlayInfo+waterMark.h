//
//  SPMediaPlayInfo+waterMark.h
//  SuperPlayer
//
//  Created by jamieling on 2021/4/15.
//

#import "SPMediaPlayInfo.h"

@class TVKWaterMarkModel;

NS_ASSUME_NONNULL_BEGIN

@interface SPMediaPlayInfo (waterMark)

/**
 水印信息
 */
@property (nonatomic, strong) TVKWaterMarkModel *waterMarkModel;

@end

NS_ASSUME_NONNULL_END
