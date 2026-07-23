/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName: SPVInfoModels.m
 Author: liyukuan
 Version : 1.0
 Date: 2018/1/13
 Description:
 
 History: 2018/1/13 初始版本
 ***********************************************************/

#import "SPDrmModel.h"

@implementation SPDrmModel

+ (SPDrmModel *)drmModelByParseCKCField:(NSString *)ckc drm:(int)drm {
    NSArray *array = [ckc componentsSeparatedByString:@"|"];
    if (array.count < 2) {
        return nil;
    }

    SPDrmModel *model = [[SPDrmModel alloc] init];
    model.ckcUrl       = [array firstObject];
    model.cerUrl       = [array objectAtIndex:1];
    model.drm          = drm;
    return model;
}
@end
