/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkInfo.m
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/
//

#import "TVKWaterMarkInfo.h"

@interface TVKWaterMarkInfo ()

@property (nonatomic, strong) TVKWaterMarkPosition *position;

@property (nonatomic, strong) UIView *markView;

@property (nonatomic, assign) BOOL clickAble;

@end

@implementation TVKWaterMarkInfo

- (id)initWithWaterMarkMD5:(NSString *)MD5
                  imageUrl:(NSString *)imageUrl
             imageHttpsUrl:(NSString *)imageHttpsUrl
            originPosition:(CGRect)originPosition
                     alpha:(CGFloat)alpha
                    isShow:(BOOL)isShow
                        rw:(int)rw {
    self = [self init];
    if (self) {
        self.MD5            = MD5;
        self.imageUrl       = imageUrl;
        self.imageHttpsUrl  = imageHttpsUrl;
        self.originPosition = originPosition;
        self.position       = [[TVKWaterMarkPosition alloc] init];
        self.alpha          = alpha;
        self.isShow         = isShow;
        self.rw             = rw;
    }

    return self;
}
@end

@implementation TVKWaterMarkCGIInfo
@end

@implementation TVKWaterMarkExtraInfo
@end
