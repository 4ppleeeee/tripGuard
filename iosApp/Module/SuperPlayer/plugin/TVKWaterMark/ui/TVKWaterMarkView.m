/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : QLWaterMark.m
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/

#import "TVKWaterMarkView.h"
#import "TVKWaterMarkPosition.h"
#import "TVKWaterMarkImageLoader.h"
#import "TVKThreadUtils.h"

@interface TVKWaterMarkView () {
    TVKWaterMarkImageLoader *_imageLoader;
}

@property (atomic, strong) UIImageView *waterMark;

@property (nonatomic, assign) BOOL canShow;  // 可以展示水印了，但可能要等待图片下载完成

@property (nonatomic, assign) BOOL imageLoaded;  //图片已加载

@property (nonatomic, strong) TVKWaterMarkPosition *position;

@end

@implementation TVKWaterMarkView

- (instancetype)initWithWaterMarkInfo:(TVKWaterMarkInfo *)waterMarkInfo {
    self = [self init];
    SPLOGI(SP_WATER_MARK_LOG_FILTER, @"%@,%p", NSStringFromSelector(_cmd), self);
    if (self) {
        self.waterMarkInfo = waterMarkInfo;
        tvk_dispatch_main_async_safe(^{
          self.waterMark = [[UIImageView alloc] init];
        });

        self.position                = [[TVKWaterMarkPosition alloc] init];
        self.position.originPosition = waterMarkInfo.originPosition;
        self.position.rw             = waterMarkInfo.rw;

        _imageLoader = [[TVKWaterMarkImageLoader alloc] init];
        [self loadImage];
    }
    return self;
}

- (void)dealloc {
    SPLOGI(SP_WATER_MARK_LOG_FILTER, @"%@,%p", NSStringFromSelector(_cmd), self);
    if (self.waterMark) {
        UIView *waterMark = self.waterMark;
        tvk_dispatch_main_async_safe(^{
          [waterMark removeFromSuperview];
        });
    }
}

- (void)show {
    tvk_dispatch_main_async_safe(^{
      self.canShow          = YES;
      self.waterMark.hidden = NO;
      if (self.imageLoaded && self.waterMarkInfo.isShow) {
          [self showWithAnimation];
      }
    });
}

- (void)hide {
    tvk_dispatch_main_async_safe(^{
      self.canShow          = NO;
      self.waterMark.hidden = YES;
    });
}

- (void)showWithAnimation {
    [self.container addSubview:self.waterMark];
    self.waterMark.frame = self.position.waterMarkPosition;
    [self.container layoutSubviews];
    [self doAnimate];
}

- (void)doAnimate {
    self.waterMark.alpha = 0;
    [UIView animateWithDuration:0.3
        animations:^{
          self.waterMark.alpha = self.waterMarkInfo.alpha;
        }
        completion:^(BOOL finished){
        }];
}

- (void)setVideoViewSize:(CGSize)videoViewSize {
    SPLOGI(SP_WATER_MARK_LOG_FILTER,
            @"setVideoViewSize,view size width=(%f),height=(%f)",
            videoViewSize.width,
            videoViewSize.height);
    self.position.viewSize = videoViewSize;
}

- (void)setVideoSize:(CGSize)videoSize {
    SPLOGI(SP_WATER_MARK_LOG_FILTER,
            @"setVideoSize,video size width=(%f),height=(%f)",
            videoSize.width,
            videoSize.height);
    self.position.videoSize = videoSize;
}

- (void)setStretchMode:(SPVideoStretchMode)mode {
    SPLOGI(SP_WATER_MARK_LOG_FILTER,
            @"setStretchMode,mode=(%d)",
            mode);
    self.position.stretchMode = mode;
}

- (void)requestLayout {
    tvk_dispatch_main_async_safe(^{
        self.waterMark.frame = self.position.waterMarkPosition;
        SPLOGI(SP_WATER_MARK_LOG_FILTER,
                @"requestLayout,wmf=(%f,%f,%f,%f)",
                self.waterMark.frame.origin.x,
                self.waterMark.frame.origin.y,
                self.waterMark.frame.size.width,
                self.waterMark.frame.size.height);
    });
}


- (void)destroy {
    if (self.waterMark) {
        tvk_dispatch_main_async_safe(^{
          [self.waterMark removeFromSuperview];
          self.waterMark = nil;
        });
    }
}
- (void)loadImage {
    __weak typeof(self) weakSelf = self;
    [_imageLoader loadWithUrl:self.waterMarkInfo.imageUrl
                          MD5:self.waterMarkInfo.MD5
                   completion:^(UIImage *image, NSError *error) {
                     if (image) {
                         weakSelf.imageLoaded     = YES;
                         weakSelf.waterMark.image = image;
                         if (self.canShow) {
                             [self showWithAnimation];
                         }
                     } else {
                         [self loadImageHttpsUrl];
                     }
                   }];
}

- (void)loadImageHttpsUrl {
    __weak typeof(self) weakSelf = self;
    [_imageLoader loadWithUrl:self.waterMarkInfo.imageHttpsUrl
                          MD5:self.waterMarkInfo.MD5
                   completion:^(UIImage *image, NSError *error) {
                       if (image) {
                           weakSelf.imageLoaded     = YES;
                           weakSelf.waterMark.image = image;
                           if (self.canShow) {
                               [self showWithAnimation];
                           }
                       }
                   }];
}

@end
