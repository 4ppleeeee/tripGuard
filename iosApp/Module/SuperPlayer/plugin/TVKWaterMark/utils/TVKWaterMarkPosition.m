/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkPosition.m
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/

#import "TVKWaterMarkPosition.h"

@implementation TVKWaterMarkPosition

- (CGRect)waterMarkPosition {
    return [self calculatePosition];
}

- (CGRect)calculatePosition {
    CGFloat videoWidth  = self.videoSize.width;
    CGFloat videoHeight = self.videoSize.height;

    if (videoWidth == 0 || videoHeight == 0) {
        return CGRectZero;
    }

    CGFloat viewWidth  = self.viewSize.width;
    CGFloat viewHeight = self.viewSize.height;

    CGFloat ratioWidth            = viewWidth / videoWidth;
    CGFloat ratioHeight           = viewHeight / videoHeight;
    CGRect scaledOriginalPosition = self.originPosition;
    float scale                   = 1;
    if (self.rw > 0) {
        //rw的使用请见http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481006415665
        scale                  = (videoWidth > videoHeight ? videoHeight : videoWidth) / self.rw;
        scaledOriginalPosition = CGRectMake(self.originPosition.origin.x * scale,
                                            self.originPosition.origin.y * scale,
                                            self.originPosition.size.width * scale,
                                            self.originPosition.size.height * scale);
    }

    CGRect position = CGRectZero;
    CGFloat x       = 0;
    CGFloat y       = 0;
    CGFloat ratio   = 0;
    if (self.stretchMode == SPVideoStretchModeAspectFit) {
        ratio = MIN(ratioWidth, ratioHeight);
        x     = ratio * scaledOriginalPosition.origin.x + (viewWidth - ratio * videoWidth) / 2.0;
        y     = ratio * scaledOriginalPosition.origin.y + (viewHeight - ratio * videoHeight) / 2.0;
    } else if (self.stretchMode == SPVideoStretchModeAspectFill) {
        if (ratioWidth > ratioHeight) {
            ratio = MAX(ratioWidth, ratioHeight);
            x     = ratio * scaledOriginalPosition.origin.x;
            y     = ratio * scaledOriginalPosition.origin.y + (viewHeight - ratio * videoHeight) / 2.0;;
        } else {
            ratio = MIN(ratioWidth, ratioHeight);
            x     = ratio * scaledOriginalPosition.origin.x + (viewWidth - ratio * videoWidth) / 2.0;
            y     = ratio * scaledOriginalPosition.origin.y;
        }
    } else if (self.stretchMode == SPVideoStretchModeFullScreen) {
        ratio = MIN(ratioWidth, ratioHeight);
        x     = ratioWidth * scaledOriginalPosition.origin.x;
        y     = ratioHeight * scaledOriginalPosition.origin.y;
    }

    CGFloat width  = ratio * scaledOriginalPosition.size.width;
    CGFloat height = ratio * scaledOriginalPosition.size.height;
    position       = CGRectMake(viewWidth - x - width, y, width, height);

    return position;
}
@end
