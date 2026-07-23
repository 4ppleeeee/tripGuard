#import "QnLottieView.h"
#import "QnLottieDownloader.h"
#import "iosApp-Swift.h"
#import <KuiklyIOSRender/KuiklyRenderViewExportProtocol.h>
@import Lottie;

// ─────────────────────────────────────────────
// Prop Keys（与 KMP 侧保持一致）
// ─────────────────────────────────────────────
static NSString *const kPropData             = @"data";
static NSString *const kPropApplyTheme       = @"applyTheme";
static NSString *const kPropSetProgress      = @"setProgress";
static NSString *const kPropDownloadListener = @"setLottieDownloadStatusListener";

// ─────────────────────────────────────────────
// 下载状态 JSON（与 KMP 侧 LottieDownloadStatus.toJsonString 保持一致）
// ─────────────────────────────────────────────
static NSString *downloadStatusJSON(NSString *status) {
    return [NSString stringWithFormat:@"{\"DOWNLOAD\":\"%@\"}", status];
}

// ─────────────────────────────────────────────
// QnLottieDataModel（镜像 KMP 侧 QnLottieData，仅 iOS 使用字段）
// ─────────────────────────────────────────────
@interface QnLottieDataModel : NSObject
@property (nonatomic, copy)   NSString *name;
@property (nonatomic, copy)   NSString *status;
@property (nonatomic, assign) BOOL      autoPlay;
@property (nonatomic, assign) BOOL      loop;
@property (nonatomic, copy)   NSString *tintColor;
@property (nonatomic, copy)   NSString *tintColorKey;
@property (nonatomic, assign) NSInteger scaleType;      // -1 表示未设置
@property (nonatomic, assign) float     scale;          // 缩放比例，默认 1.0
@property (nonatomic, assign) float     cornerInDp;
@property (nonatomic, assign) BOOL      enableInteraction;
@property (nonatomic, assign) float     progressRangeStart; // -1 表示未设置
@property (nonatomic, assign) float     progressRangeEnd;
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *textDelegate;
@end

@implementation QnLottieDataModel
- (instancetype)init {
    self = [super init];
    if (self) {
        _scaleType = -1;
        _scale = 1.0f;
        _enableInteraction = YES;
        _progressRangeStart = -1.0f;
        _progressRangeEnd   = -1.0f;
    }
    return self;
}
@end

// ─────────────────────────────────────────────
// QnLottieView
// ─────────────────────────────────────────────
// 使用 QnLottieAnimationView（Swift 桥接，见 QnLottieViewFactory.swift）作为渲染视图：
// - 支持带 images/ 子目录的 lottie zip（FilepathImageProvider）
// - 对于 bundle 资源，使用 CompatibleAnimationView（ObjC 兼容）
// ─────────────────────────────────────────────
@interface QnLottieView ()

/// 当前动画视图（QnLottieAnimationView 或 CompatibleAnimationView，均遵循 QnLottieAnimatable）
@property (nonatomic, strong, nullable) UIView<QnLottieAnimatable> *animationView;
@property (nonatomic, strong, nullable) QnLottieDataModel *currentData;
@property (nonatomic, assign) float pendingProgress;
@property (nonatomic, copy, nullable) KuiklyRenderCallback downloadCallback;

@end

@implementation QnLottieView

- (instancetype)init {
    self = [super init];
    if (self) {
        self.backgroundColor = UIColor.clearColor;
        self.clipsToBounds = YES;
        _pendingProgress = NAN;
    }
    return self;
}

// ─────────────────────────────────────────────
// KuiklyRenderViewExportProtocol
// ─────────────────────────────────────────────
- (void)hrv_setPropWithKey:(NSString *)propKey propValue:(id)propValue {
    if ([propKey isEqualToString:@"frame"]) {
        if ([propValue isKindOfClass:[NSValue class]]) {
            self.frame = [(NSValue *)propValue CGRectValue];
            if (self.animationView) {
                float scale = self.currentData ? self.currentData.scale : 1.0f;
                if (fabsf(scale - 1.0f) < 1e-5f) {
                    self.animationView.frame = self.bounds;
                } else {
                    [self applyScaleTransform:scale toView:self.animationView];
                }
            }
        }
        return;
    }
    if ([propKey isEqualToString:kPropData]) {
        [self applyData:propValue];
    } else if ([propKey isEqualToString:kPropApplyTheme]) {
        // 主题切换预留
    } else if ([propKey isEqualToString:kPropSetProgress]) {
        [self applyProgress:[propValue floatValue]];
    } else if ([propKey isEqualToString:kPropDownloadListener]) {
        self.downloadCallback = [propValue copy];
    }
}

// ─────────────────────────────────────────────
// 数据处理
// ─────────────────────────────────────────────
- (void)applyData:(id)propValue {
    QnLottieDataModel *data = [self parseDataModel:propValue];
    if (!data || data.name.length == 0) return;

    QnLottieDataModel *oldData = self.currentData;
    BOOL nameChanged   = ![data.name isEqualToString:oldData.name];
    BOOL statusChanged = ![data.status isEqualToString:oldData.status];
    if (nameChanged || statusChanged) {
        [self stopAndResetAnimation];
        self.pendingProgress = NAN;
    }

    self.currentData = data;
    self.userInteractionEnabled = data.enableInteraction;

    if (data.cornerInDp > 0) {
        self.layer.cornerRadius = data.cornerInDp;
        self.layer.masksToBounds = YES;
    } else {
        self.layer.cornerRadius = 0;
    }

    [self loadAnimationWithData:data];

    // 动画视图已存在时，处理属性变化触发的播放逻辑
    if (self.animationView && !nameChanged && !statusChanged) {
        [self handlePlayLogicChangeFromOldData:oldData toNewData:data];
    }
}

- (nullable QnLottieDataModel *)parseDataModel:(id)propValue {
    if (!propValue) return nil;
    QnLottieDataModel *model = [[QnLottieDataModel alloc] init];
    @try {
        model.name              = [[propValue valueForKey:@"name"] description] ?: @"";
        model.status            = [[propValue valueForKey:@"status"] description] ?: @"";
        model.autoPlay          = [[propValue valueForKey:@"autoPlay"] boolValue];
        model.loop              = [[propValue valueForKey:@"loop"] boolValue];
        model.tintColor         = [[propValue valueForKey:@"tintColor"] description] ?: @"";
        model.tintColorKey      = [[propValue valueForKey:@"tintColorKey"] description] ?: @"";
        model.enableInteraction = [[propValue valueForKey:@"enableInteraction"] boolValue];

        id scaleTypeObj = [propValue valueForKey:@"scaleType"];
        if (scaleTypeObj && scaleTypeObj != [NSNull null]) {
            @try { model.scaleType = [[scaleTypeObj valueForKey:@"nativeInt"] integerValue]; }
            @catch (__unused NSException *e) { model.scaleType = -1; }
        }

        id cornerObj = [propValue valueForKey:@"cornerInDp"];
        if (cornerObj && cornerObj != [NSNull null]) {
            model.cornerInDp = [cornerObj floatValue];
        }

        id progressRangeObj = [propValue valueForKey:@"progressRange"];
        if (progressRangeObj && progressRangeObj != [NSNull null]) {
            @try {
                model.progressRangeStart = [[progressRangeObj valueForKey:@"startProgress"] floatValue];
                model.progressRangeEnd   = [[progressRangeObj valueForKey:@"endProgress"] floatValue];
            } @catch (__unused NSException *e) {}
        }

        id scaleObj = [propValue valueForKey:@"scale"];
        if (scaleObj && scaleObj != [NSNull null]) {
            float scaleVal = [scaleObj floatValue];
            model.scale = (scaleVal > 0 && !isnan(scaleVal) && !isinf(scaleVal)) ? scaleVal : 1.0f;
        }

        id textDelegateObj = [propValue valueForKey:@"textDelegate"];
        if ([textDelegateObj isKindOfClass:[NSDictionary class]]) {
            model.textDelegate = textDelegateObj;
        }
    } @catch (NSException *e) {
        NSLog(@"[QnLottieView] parseDataModel exception: %@", e);
        return nil;
    }
    return model;
}

// ─────────────────────────────────────────────
// 动画加载
// ─────────────────────────────────────────────
- (void)loadAnimationWithData:(QnLottieDataModel *)data {
    NSString *name = data.name;
    if ([name hasPrefix:@"http://"] || [name hasPrefix:@"https://"]) {
        [self notifyDownloadStatus:@"DOWNLOADING"];
        __weak typeof(self) weakSelf = self;
        [[QnLottieDownloader shared] downloadLottieWithURL:name completion:^(NSString *localPath, NSError *error) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (!strongSelf) return;
            if (![strongSelf.currentData.name isEqualToString:name]) return;
            if (error || !localPath) {
                NSLog(@"[QnLottieView] 下载失败: %@, error: %@", name, error);
                [strongSelf notifyDownloadStatus:@"FAILED"];
                return;
            }
            [strongSelf notifyDownloadStatus:@"COMPLETED"];
            [strongSelf loadAnimationFromLocalPath:localPath data:data];
        }];
    } else {
        [self loadAnimationFromAssetsName:name data:data];
    }
}

/// 从本地文件路径加载动画（下载完成后调用）
/// - .zip/.lottie → 后台线程解析 DotLottieFile，主线程创建视图（避免主线程阻塞）
/// - .json        → 主线程直接创建（LottieAnimation + FilepathImageProvider）
- (void)loadAnimationFromLocalPath:(NSString *)localPath data:(QnLottieDataModel *)data {
    NSString *name = data.name;
    __weak typeof(self) weakSelf = self;
    [QnLottieAnimationView loadAsyncWithFilePath:localPath completion:^(QnLottieAnimationView *animView) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (!strongSelf) return;
        // 如果 URL 已切换，丢弃旧结果
        if (![strongSelf.currentData.name isEqualToString:name]) return;
        [strongSelf setupAnimationView:animView data:data];
    }];
}

/// 从 bundle assets 加载动画（本地资源名称）
- (void)loadAnimationFromAssetsName:(NSString *)name data:(QnLottieDataModel *)data {
    NSString *animName = name;
    if ([animName hasSuffix:@".json"] || [animName hasSuffix:@".lottie"]) {
        animName = animName.stringByDeletingPathExtension;
    }
    // bundle 资源使用 CompatibleAnimationView（ObjC 兼容，无需 imageProvider）
    CompatibleAnimation *animation = [[CompatibleAnimation alloc] initWithName:animName
                                                                  subdirectory:nil
                                                                        bundle:NSBundle.mainBundle];
    CompatibleAnimationView *animView = [[CompatibleAnimationView alloc] initWithCompatibleAnimation:animation];
    [self setupAnimationView:animView data:data];
}

/// 配置并挂载动画视图（支持 QnLottieAnimationView 和 CompatibleAnimationView）
- (void)setupAnimationView:(UIView<QnLottieAnimatable> *)animView data:(QnLottieDataModel *)data {
    [self stopAndResetAnimation];

    animView.frame = self.bounds;
    animView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    animView.userInteractionEnabled = NO;
    animView.contentMode = [self contentModeForScaleType:data.scaleType];
    animView.loopAnimationCount = data.loop ? -1 : 0;

    // tintColor 染色
    if (data.tintColor.length > 0) {
        UIColor *color = [self colorFromHexString:data.tintColor];
        if (color) {
            NSString *keypathStr = (data.tintColorKey.length > 0)
                ? [NSString stringWithFormat:@"%@.Color", data.tintColorKey]
                : @"**.Color";
            CompatibleAnimationKeypath *keypath = [[CompatibleAnimationKeypath alloc] initWithKeypath:keypathStr];
            [animView setColorValue:color forKeypath:keypath];
        }
    }

    // 文本替换
    if (data.textDelegate.count > 0) {
        [animView setTextProvider:data.textDelegate];
    }

    // 应用 scale 缩放
    [self applyScaleTransform:data.scale toView:animView];

    [self addSubview:animView];
    self.animationView = animView;

    // 播放区间 progressRange
    [self playAnimationView:animView withData:data];

    if (!isnan(self.pendingProgress)) {
        animView.currentProgress = self.pendingProgress;
        self.pendingProgress = NAN;
    }
}

/// 根据 data 执行播放（统一入口）
- (void)playAnimationView:(UIView<QnLottieAnimatable> *)animView withData:(QnLottieDataModel *)data {
    BOOL hasRange = (data.progressRangeStart >= 0 && data.progressRangeEnd > data.progressRangeStart);
    if (hasRange) {
        if (data.autoPlay) {
            [animView playFromProgress:data.progressRangeStart
                            toProgress:data.progressRangeEnd
                            completion:nil];
        } else {
            animView.currentProgress = data.progressRangeStart;
        }
    } else if (data.autoPlay) {
        [animView play];
    }
}

/**
 * 处理属性变化（name/status 未变）时的播放状态机，对齐 QQNews 逻辑：
 * 1. autoPlay 从 NO→YES：触发播放
 * 2. loop 从 NO→YES：触发播放
 * 3. progressRange 发生变化：触发播放
 */
- (void)handlePlayLogicChangeFromOldData:(QnLottieDataModel *)oldData
                               toNewData:(QnLottieDataModel *)newData {
    UIView<QnLottieAnimatable> *animView = self.animationView;
    if (!animView) return;

    // 更新 loop 模式
    animView.loopAnimationCount = newData.loop ? -1 : 0;

    // 1. autoPlay 从 NO→YES
    if (!oldData.autoPlay && newData.autoPlay) {
        [self playAnimationView:animView withData:newData];
        return;
    }

    // 2. loop 从 NO→YES
    if (!oldData.loop && newData.loop) {
        [self playAnimationView:animView withData:newData];
        return;
    }

    // 3. progressRange 发生变化
    BOOL oldHasRange = (oldData.progressRangeStart >= 0 && oldData.progressRangeEnd > oldData.progressRangeStart);
    BOOL newHasRange = (newData.progressRangeStart >= 0 && newData.progressRangeEnd > newData.progressRangeStart);
    if (oldHasRange || newHasRange) {
        BOOL startChanged = fabsf(oldData.progressRangeStart - newData.progressRangeStart) > 1e-5f;
        BOOL endChanged   = fabsf(oldData.progressRangeEnd   - newData.progressRangeEnd)   > 1e-5f;
        if (startChanged || endChanged) {
            [self playAnimationView:animView withData:newData];
            return;
        }
    }

    // 4. scale 变化：重新应用缩放
    if (fabsf(oldData.scale - newData.scale) > 1e-5f) {
        [self applyScaleTransform:newData.scale toView:animView];
    }
}

/**
 * 对动画视图应用 scale 缩放变换
 * scale == 1.0 时不做任何变换，保持原始大小
 */
- (void)applyScaleTransform:(float)scale toView:(UIView *)view {
    if (!view) return;
    if (fabsf(scale - 1.0f) < 1e-5f) {
        view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        view.transform = CGAffineTransformIdentity;
        view.frame = self.bounds;
        return;
    }
    // scale != 1.0 时，关闭 autoresizing，手动设置 bounds/center/transform
    view.autoresizingMask = UIViewAutoresizingNone;
    view.transform = CGAffineTransformIdentity;
    view.bounds = self.bounds;
    view.center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
    view.transform = CGAffineTransformMakeScale(scale, scale);
}

// ─────────────────────────────────────────────
// 进度控制
// ─────────────────────────────────────────────
- (void)applyProgress:(float)progress {
    if (isnan(progress)) return;
    if (self.animationView) {
        self.animationView.currentProgress = progress;
    } else {
        self.pendingProgress = progress;
    }
}

// ─────────────────────────────────────────────
// 停止 & 重置
// ─────────────────────────────────────────────
- (void)stopAndResetAnimation {
    if (self.animationView) {
        [self.animationView stop];
        [self.animationView removeFromSuperview];
        self.animationView = nil;
    }
}

// ─────────────────────────────────────────────
// 下载状态回调
// ─────────────────────────────────────────────
- (void)notifyDownloadStatus:(NSString *)status {
    if (self.downloadCallback) {
        self.downloadCallback(downloadStatusJSON(status));
    }
}

// ─────────────────────────────────────────────
// 辅助：ScaleType → UIViewContentMode
// ─────────────────────────────────────────────
- (UIViewContentMode)contentModeForScaleType:(NSInteger)scaleType {
    switch (scaleType) {
        case 1:  return UIViewContentModeScaleToFill;
        case 2:  return UIViewContentModeScaleAspectFit;
        case 3:  return UIViewContentModeScaleAspectFit;
        case 4:  return UIViewContentModeScaleAspectFit;
        case 5:  return UIViewContentModeCenter;
        case 6:  return UIViewContentModeScaleAspectFill;
        case 7:  return UIViewContentModeScaleAspectFit;
        default: return UIViewContentModeScaleAspectFit;
    }
}

/// 解析 hex 颜色字符串（支持 6 位 RGB 和 8 位 ARGB，带或不带 #）
- (nullable UIColor *)colorFromHexString:(NSString *)hexString {
    NSString *hex = [hexString stringByReplacingOccurrencesOfString:@"#" withString:@""];
    if (hex.length == 6) hex = [@"ff" stringByAppendingString:hex];
    if (hex.length != 8) return nil;

    unsigned int argb = 0;
    [[NSScanner scannerWithString:hex] scanHexInt:&argb];
    CGFloat a = ((argb >> 24) & 0xFF) / 255.0;
    CGFloat r = ((argb >> 16) & 0xFF) / 255.0;
    CGFloat g = ((argb >>  8) & 0xFF) / 255.0;
    CGFloat b = ((argb      ) & 0xFF) / 255.0;
    return [UIColor colorWithRed:r green:g blue:b alpha:a];
}

// ─────────────────────────────────────────────
// 生命周期
// ─────────────────────────────────────────────
- (void)removeFromSuperview {
    [self stopAndResetAnimation];
    [super removeFromSuperview];
}

- (void)dealloc {
    [self stopAndResetAnimation];
    self.downloadCallback = nil;
}

@end
