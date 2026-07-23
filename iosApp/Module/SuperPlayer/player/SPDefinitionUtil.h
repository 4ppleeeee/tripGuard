//
//  SPDefinitionUtil.h
//  SPPlayer
//
//  Created by ethanyxliu on 2019/11/4.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPDefinitionUtil : NSObject

+ (SPHEVCLevel)hevcLevelFromLumaSamples:(NSInteger)lumaSamples;

+ (NSInteger)lumaSamplesFromDefinitionName:(NSString *)defnName;

+ (CGSize)resolutionForDefinitionName:(NSString *)defnName;

@end

NS_ASSUME_NONNULL_END
