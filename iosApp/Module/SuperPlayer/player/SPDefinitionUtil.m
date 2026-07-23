//
//  SPDefinitionUtil.m
//  SPPlayer
//
//  Created by ethanyxliu on 2019/11/4.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPDefinitionUtil.h"
#import "SPPlayerDefine.h"

@implementation SPDefinitionUtil

+ (SPHEVCLevel)hevcLevelFromLumaSamples:(NSInteger)lumaSamples {
    if (lumaSamples < [self lumaSamplesFromDefinitionName:kSPMediaDefinitionSD]) {
        return SPHEVCLevelNone;
    } else if (lumaSamples < [self lumaSamplesFromDefinitionName:kSPMediaDefinitionHD]) {
        return SPHEVCLevelSD;
    } else if (lumaSamples < [self lumaSamplesFromDefinitionName:kSPMediaDefinitionSHD]) {
        return SPHEVCLevelHD;
    } else if (lumaSamples < [self lumaSamplesFromDefinitionName:kSPMediaDefinitionFHD]) {
        return SPHEVCLevelSHD;
    } else if (lumaSamples < [self lumaSamplesFromDefinitionName:kSPMediaDefinitionUHD]) {
        return SPHEVCLevelFHD;
    } else {
        return SPHEVCLevelUHD;
    }
}

+ (NSInteger)lumaSamplesFromDefinitionName:(NSString *)defnName {
    if (defnName == nil) {
        return 0;
    }

    static NSMutableDictionary *s_definitionSamplesMap = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_definitionSamplesMap = [[NSMutableDictionary alloc] init];
        [s_definitionSamplesMap setObject:@(480 * 270) forKey:kSPMediaDefinitionSD];
        [s_definitionSamplesMap setObject:@(848 * 480) forKey:kSPMediaDefinitionHD];
        [s_definitionSamplesMap setObject:@(1280 * 720) forKey:kSPMediaDefinitionSHD];
        [s_definitionSamplesMap setObject:@(1920 * 1080) forKey:kSPMediaDefinitionFHD];
        [s_definitionSamplesMap setObject:@(3840 * 2160) forKey:kSPMediaDefinitionUHD];
    });

    NSNumber *number = [s_definitionSamplesMap objectForKey:defnName];
    return number.integerValue;
}

+ (CGSize)resolutionForDefinitionName:(NSString *)defnName {
    if ([defnName isEqualToString:kSPMediaDefinitionSD]) {
        return CGSizeMake(480, 270);
    } else if ([defnName isEqualToString:kSPMediaDefinitionHD]) {
        return CGSizeMake(848, 480);
    } else if ([defnName isEqualToString:kSPMediaDefinitionSHD]) {
        return CGSizeMake(1280, 720);
    } else if ([defnName isEqualToString:kSPMediaDefinitionFHD]) {
        return CGSizeMake(1920, 1080);
    } else if ([defnName isEqualToString:kSPMediaDefinitionUHD]) {
        return CGSizeMake(3840, 2160);
    } else {
        return CGSizeZero;
    }
}
@end
