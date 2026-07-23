//
//  KuiklyPageView.m
//  iosApp
//
//  iOS 端 KuiklyPageView 实现：在外层 Kuikly Pager 中以 View 粒度承载一个独立的子 Pager。
//

#import "KuiklyPageView.h"
#import <KuiklyIOSRender/KuiklyView.h>
#import <KuiklyIOSRender/KuiklyRenderModuleExportProtocol.h>

// 与 DSL 侧 KuiklyPageViewAttr 中的属性名一致
static NSString *const kPropPageName = @"pageName";
static NSString *const kPropPageData = @"pageData";

// KMP 主 framework 名（参见 KuiklyRenderViewController.m 中 fetchContextCode 回调）
static NSString *const kFrameworkName = @"umbrella";

@interface KuiklyPageView () <KuiklyViewDelegate>

@property (nonatomic, copy) NSString *pageName;
@property (nonatomic, copy) NSDictionary *pageData;

/// 内嵌的子 Pager
@property (nonatomic, strong) KuiklyView *innerKuiklyView;

/// 当前 view 是否已可见 (resume 状态)
@property (nonatomic, assign) BOOL appeared;

@end

@implementation KuiklyPageView

#pragma mark - Init

- (instancetype)init {
    if (self = [super init]) {
        _pageName = @"";
        _pageData = @{};
        _appeared = NO;
    }
    return self;
}

#pragma mark - KuiklyRenderViewExportProtocol

- (void)hrv_callWithMethod:(NSString *)method
                    params:(NSString *)params
                  callback:(KuiklyRenderCallback)callback {
    KUIKLY_CALL_CSS_METHOD;
}

- (void)hrv_setPropWithKey:(NSString *)propKey propValue:(id)propValue {
    if ([propKey isEqualToString:@"frame"]) {
        // 默认 frame 由 Kuikly 框架透传，KuiklyView 内部根据宿主 frame.size 布局
        if ([propValue isKindOfClass:[NSValue class]]) {
            self.frame = [(NSValue *)propValue CGRectValue];
        }
        return;
    }

    if ([propKey isEqualToString:kPropPageName]) {
        NSString *newName = [propValue isKindOfClass:[NSString class]] ? (NSString *)propValue : nil;
        if (newName.length > 0 && ![newName isEqualToString:self.pageName]) {
            self.pageName = newName;
            [self tryAttach];
        }
        return;
    }

    if ([propKey isEqualToString:kPropPageData]) {
        self.pageData = [self parsePageData:propValue];
        [self tryAttach];
        return;
    }
}

- (NSDictionary *)parsePageData:(id)value {
    if ([value isKindOfClass:[NSDictionary class]]) {
        return value;
    }
    if ([value isKindOfClass:[NSString class]]) {
        NSString *str = (NSString *)value;
        if (str.length == 0) return @{};
        NSError *error = nil;
        NSData *data = [str dataUsingEncoding:NSUTF8StringEncoding];
        id obj = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
        if (!error && [obj isKindOfClass:[NSDictionary class]]) {
            return obj;
        }
    }
    return @{};
}

#pragma mark - Lifecycle

- (void)layoutSubviews {
    [super layoutSubviews];
    self.innerKuiklyView.frame = self.bounds;
    [self tryAttach];
}

- (void)didMoveToWindow {
    [super didMoveToWindow];
    if (self.window) {
        [self tryAttach];
        [self viewDidAppearIfNeeded];
    } else {
        [self viewDidDisappearIfNeeded];
    }
}

- (void)removeFromSuperview {
    [self viewDidDisappearIfNeeded];
    [super removeFromSuperview];
}

- (void)dealloc {
    self.innerKuiklyView = nil;
}

#pragma mark - Attach / Resume

- (void)tryAttach {
    if (self.innerKuiklyView) return;
    if (self.pageName.length == 0) return;
    if (CGRectIsEmpty(self.bounds)) return;
    if (!self.window) return;

    NSLog(@"[KuiklyPageView] attach pageName=%@ frame=%@", self.pageName, NSStringFromCGRect(self.bounds));
    KuiklyView *kView = [[KuiklyView alloc] initWithFrame:self.bounds
                                                 pageName:self.pageName
                                                 pageData:self.pageData ?: @{}
                                                 delegate:self
                                            frameworkName:kFrameworkName];
    kView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self addSubview:kView];
    self.innerKuiklyView = kView;
}

- (void)viewDidAppearIfNeeded {
    if (!self.innerKuiklyView || self.appeared) return;
    self.appeared = YES;
    [self.innerKuiklyView viewWillAppear];
    [self.innerKuiklyView viewDidAppear];
}

- (void)viewDidDisappearIfNeeded {
    if (!self.innerKuiklyView || !self.appeared) return;
    self.appeared = NO;
    [self.innerKuiklyView viewWillDisappear];
    [self.innerKuiklyView viewDidDisappear];
}

#pragma mark - KuiklyViewDelegate

- (void)fetchContextCodeWithPageName:(NSString *)pageName resultCallback:(KuiklyContextCodeCallback)callback {
    if (callback) {
        callback(kFrameworkName, nil);
    }
}

@end
