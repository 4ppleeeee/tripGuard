//
//  SPMediaPlayInfo+waterMark.m
//  SuperPlayer
//
//  Created by jamieling on 2021/4/15.
//

#import "SPMediaPlayInfo+waterMark.h"
#import <objc/runtime.h>

@implementation SPMediaPlayInfo (waterMark)

- (TVKWaterMarkModel *)waterMarkModel {
    return objc_getAssociatedObject(self, @selector(waterMarkModel));
}

- (void)setWaterMarkModel:(TVKWaterMarkModel *)waterMarkModel {
    objc_setAssociatedObject(self, @selector(waterMarkModel), waterMarkModel, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}


@end
