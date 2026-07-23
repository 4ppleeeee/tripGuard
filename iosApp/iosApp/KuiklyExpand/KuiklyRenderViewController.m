#import "KuiklyRenderViewController.h"
#import "UINavigationController+FDFullscreenPopGesture.h"
#import <KuiklyIOSRender/KuiklyRenderContextProtocol.h>
#import "Handler/KRRouterHandler.h"
#import <umbrella/umbrella.h>
#import <KuiklyIOSRender/KuiklyRenderViewControllerDelegator.h>

#define HRWeakSelf __weak typeof(self) weakSelf = self;
static NSString *const kComposeEventOnPageNewIntent = @"OnPageNewIntent";

@interface IOSPageStackEntry : NSObject
@property (nonatomic, weak) id<UmbrellaIKmmContext> context;
@property (nonatomic, weak) id stack;
@property (nonatomic, strong) UmbrellaPageLifecycleState *state;
@property (nonatomic, assign) NSUInteger sequence;
@property (nonatomic, assign) BOOL active;
@end

@implementation IOSPageStackEntry
@end

@interface IOSPageStackStore ()
@property (nonatomic, strong) NSMutableArray<IOSPageStackEntry *> *entries;
@property (nonatomic, assign) NSUInteger sequence;
@property (nonatomic, assign) BOOL applicationActive;
@end

@implementation IOSPageStackStore

+ (instancetype)p_sharedStore {
    static IOSPageStackStore *store;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        store = [[IOSPageStackStore alloc] initPrivate];
    });
    return store;
}

+ (void)onPageCreatedWithStack:(id)stack context:(id<UmbrellaIKmmContext>)context {
    [[self p_sharedStore] p_onPageCreatedWithStack:stack context:context];
}

+ (void)onPageWillAppear:(id<UmbrellaIKmmContext>)context {
    [[self p_sharedStore] p_onPageWillAppear:context];
}

+ (void)onPageDidAppear:(id<UmbrellaIKmmContext>)context {
    [[self p_sharedStore] p_onPageDidAppear:context];
}

+ (void)onPageWillDisappear:(id<UmbrellaIKmmContext>)context {
    [[self p_sharedStore] p_onPageWillDisappear:context];
}

+ (void)onPageDidDisappear:(id<UmbrellaIKmmContext>)context {
    [[self p_sharedStore] p_onPageDidDisappear:context];
}

+ (void)onPageDestroyedWithStack:(id)stack context:(id<UmbrellaIKmmContext>)context {
    [[self p_sharedStore] p_onPageDestroyedWithStack:stack context:context];
}

+ (NSArray<id<UmbrellaIKmmContext>> *)allPages {
    return [[self p_sharedStore] p_allPages];
}

+ (NSArray<id<UmbrellaIKmmContext>> *)activePages {
    return [[self p_sharedStore] p_activePages];
}

+ (id<UmbrellaIKmmContext>)topValidPage {
    return [[self p_sharedStore] p_topValidPage];
}

+ (BOOL)isPageActive:(id<UmbrellaIKmmContext>)context {
    return [[self p_sharedStore] p_isPageActive:context];
}

+ (UmbrellaPageLifecycleState *)pageLifecycleStateForContext:(id<UmbrellaIKmmContext>)context {
    return [[self p_sharedStore] p_pageLifecycleStateForContext:context];
}

+ (BOOL)applicationStateActive {
    return [self p_sharedStore].applicationActive;
}

- (instancetype)init {
    return [IOSPageStackStore p_sharedStore];
}

- (instancetype)initPrivate {
    self = [super init];
    if (self) {
        _entries = [NSMutableArray array];
        UIApplicationState state = UIApplication.sharedApplication.applicationState;
        _applicationActive = state == UIApplicationStateActive;
        NSNotificationCenter *center = NSNotificationCenter.defaultCenter;
        [center addObserver:self selector:@selector(p_applicationDidBecomeActive) name:UIApplicationDidBecomeActiveNotification object:nil];
        [center addObserver:self selector:@selector(p_applicationWillResignActive) name:UIApplicationWillResignActiveNotification object:nil];
        [center addObserver:self selector:@selector(p_applicationDidEnterBackground) name:UIApplicationDidEnterBackgroundNotification object:nil];
        [center addObserver:self selector:@selector(p_applicationWillEnterForeground) name:UIApplicationWillEnterForegroundNotification object:nil];
    }
    return self;
}

- (void)p_applicationDidBecomeActive {
    self.applicationActive = YES;
}

- (void)p_applicationWillResignActive {
    self.applicationActive = NO;
}

- (void)p_applicationDidEnterBackground {
    self.applicationActive = NO;
}

- (void)p_applicationWillEnterForeground {
    self.applicationActive = YES;
}

- (void)p_onPageCreatedWithStack:(id)stack context:(id<UmbrellaIKmmContext>)context {
    if (context == nil) {
        return;
    }
    [self p_cleanupInvalidEntries];
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:YES];
    entry.stack = stack ?: [IOSNativeRouter navigationControllerForContext:context];
    entry.state = [UmbrellaPageLifecycleState create];
    entry.active = NO;
    entry.sequence = ++self.sequence;
}

- (void)p_onPageWillAppear:(id<UmbrellaIKmmContext>)context {
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:YES];
    entry.stack = entry.stack ?: [IOSNativeRouter navigationControllerForContext:context];
    entry.state = [UmbrellaPageLifecycleState start];
    entry.sequence = ++self.sequence;
}

- (void)p_onPageDidAppear:(id<UmbrellaIKmmContext>)context {
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:YES];
    id stack = entry.stack ?: [IOSNativeRouter navigationControllerForContext:context];
    entry.stack = stack;
    for (IOSPageStackEntry *item in self.entries) {
        if (item == entry) {
            continue;
        }
        if ([self p_isSameStack:item.stack rhs:stack]) {
            item.active = NO;
        }
    }
    entry.active = YES;
    entry.state = [UmbrellaPageLifecycleState resume];
    entry.sequence = ++self.sequence;
}

- (void)p_onPageWillDisappear:(id<UmbrellaIKmmContext>)context {
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:NO];
    if (entry == nil) {
        return;
    }
    entry.active = NO;
    entry.state = [UmbrellaPageLifecycleState pause];
    entry.sequence = ++self.sequence;
}

- (void)p_onPageDidDisappear:(id<UmbrellaIKmmContext>)context {
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:NO];
    if (entry == nil) {
        return;
    }
    entry.active = NO;
    entry.state = [UmbrellaPageLifecycleState stop];
    entry.sequence = ++self.sequence;
}

- (void)p_onPageDestroyedWithStack:(id)stack context:(id<UmbrellaIKmmContext>)context {
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:NO];
    if (entry == nil) {
        return;
    }
    entry.stack = stack ?: entry.stack;
    entry.active = NO;
    entry.state = [UmbrellaPageLifecycleState destroy];
    entry.sequence = ++self.sequence;
    [self.entries removeObject:entry];
    [self p_cleanupInvalidEntries];
}

- (NSArray<id<UmbrellaIKmmContext>> *)p_allPages {
    [self p_cleanupInvalidEntries];
    NSMutableArray<id<UmbrellaIKmmContext>> *result = [NSMutableArray array];
    for (IOSPageStackEntry *entry in self.entries) {
        if (entry.context != nil) {
            [result addObject:entry.context];
        }
    }
    id<UmbrellaIKmmContext> visibleContext = [IOSNativeRouter topVisibleKmmContext];
    if (visibleContext != nil && ![self p_result:result containsContext:visibleContext]) {
        [result addObject:visibleContext];
    }
    return result;
}

- (NSArray<id<UmbrellaIKmmContext>> *)p_activePages {
    [self p_cleanupInvalidEntries];
    if (!self.applicationActive) {
        return @[];
    }
    NSMutableArray<id<UmbrellaIKmmContext>> *result = [NSMutableArray array];
    for (IOSPageStackEntry *entry in self.entries) {
        if (entry.active && entry.context != nil) {
            [result addObject:entry.context];
        }
    }
    if (result.count == 0) {
        id<UmbrellaIKmmContext> visibleContext = [IOSNativeRouter topVisibleKmmContext];
        if (visibleContext != nil) {
            [result addObject:visibleContext];
        }
    }
    return result;
}

- (id<UmbrellaIKmmContext>)p_topValidPage {
    [self p_cleanupInvalidEntries];
    NSArray<id<UmbrellaIKmmContext>> *activePages = [self p_activePages];
    if (activePages.count > 0) {
        return activePages.lastObject;
    }
    IOSPageStackEntry *latestEntry = nil;
    for (IOSPageStackEntry *entry in self.entries) {
        if (entry.context == nil) {
            continue;
        }
        if (latestEntry == nil || latestEntry.sequence < entry.sequence) {
            latestEntry = entry;
        }
    }
    if (latestEntry.context != nil) {
        return latestEntry.context;
    }
    return [IOSNativeRouter topVisibleKmmContext];
}

- (BOOL)p_isPageActive:(id<UmbrellaIKmmContext>)context {
    if (context == nil || !self.applicationActive) {
        return NO;
    }
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:NO];
    if (entry != nil && entry.active) {
        return YES;
    }
    return [IOSNativeRouter topVisibleKmmContext] == context;
}

- (UmbrellaPageLifecycleState *)p_pageLifecycleStateForContext:(id<UmbrellaIKmmContext>)context {
    IOSPageStackEntry *entry = [self p_entryForContext:context createIfNeeded:NO];
    if (entry.state != nil) {
        return entry.state;
    }
    if ([IOSNativeRouter topVisibleKmmContext] == context && self.applicationActive) {
        return [UmbrellaPageLifecycleState resume];
    }
    return [UmbrellaPageLifecycleState unknown];
}

- (IOSPageStackEntry *)p_entryForContext:(id<UmbrellaIKmmContext>)context createIfNeeded:(BOOL)createIfNeeded {
    if (context == nil) {
        return nil;
    }
    [self p_cleanupInvalidEntries];
    for (IOSPageStackEntry *entry in self.entries) {
        if (entry.context == context) {
            return entry;
        }
    }
    if (!createIfNeeded) {
        return nil;
    }
    IOSPageStackEntry *entry = [[IOSPageStackEntry alloc] init];
    entry.context = context;
    entry.state = [UmbrellaPageLifecycleState unknown];
    [self.entries addObject:entry];
    return entry;
}

- (void)p_cleanupInvalidEntries {
    NSMutableArray<IOSPageStackEntry *> *invalidEntries = [NSMutableArray array];
    for (IOSPageStackEntry *entry in self.entries) {
        if (entry.context == nil) {
            [invalidEntries addObject:entry];
        }
    }
    [self.entries removeObjectsInArray:invalidEntries];
}

- (BOOL)p_result:(NSArray<id<UmbrellaIKmmContext>> *)result containsContext:(id<UmbrellaIKmmContext>)context {
    for (id<UmbrellaIKmmContext> item in result) {
        if (item == context) {
            return YES;
        }
    }
    return NO;
}

- (BOOL)p_isSameStack:(id)lhs rhs:(id)rhs {
    if (lhs == nil || rhs == nil) {
        return NO;
    }
    return lhs == rhs;
}

@end

@interface KuiklyRenderViewController()<KuiklyRenderViewControllerDelegatorDelegate, UmbrellaIKmmContext, WSAppWindowOrientationControl>

@property (nonatomic, strong) KuiklyRenderViewControllerDelegator *delegator;
@property (nonatomic, assign) UIInterfaceOrientationMask currentOrientationMask;
@property (nonatomic, assign) BOOL wsStatusBarHidden;
@property (nonatomic, assign) BOOL wsStatusBarLightContent;

@end

@implementation KuiklyRenderViewController {
    NSDictionary *_pageData;
    NSString *_pageName;
}

- (instancetype)initWithPageName:(NSString *)pageName pageData:(NSDictionary *)pageData {
    if (self = [super init]) {
        pageData = [self p_mergeExtParamsWithOriditalParam:pageData];
        _pageData = pageData;
        _pageName = [pageName copy];
    _currentOrientationMask = UIInterfaceOrientationMaskPortrait;
        _wsStatusBarHidden = NO;
        _wsStatusBarLightContent = NO;
        _delegator = [[KuiklyRenderViewControllerDelegator alloc] initWithPageName:pageName pageData:pageData];
        _delegator.delegate = self;
    }
    return self;
}

- (NSString *)pageName {
    return _pageName;
}

- (void)dispatchPageNewIntentWithPageData:(NSDictionary<NSString *, id> *)pageData {
    NSDictionary *newPageData = [self p_mergeExtParamsWithOriditalParam:pageData ?: @{}];
    _pageData = newPageData;
    [self.delegator sendWithEvent:kComposeEventOnPageNewIntent data:newPageData ?: @{}];
}

#pragma mark - 方向控制

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return self.currentOrientationMask;
}

- (BOOL)shouldAutorotate {
    return YES;
}

#pragma mark - 状态栏控制

- (BOOL)prefersStatusBarHidden {
    return self.wsStatusBarHidden;
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    if (self.wsStatusBarLightContent) {
        return UIStatusBarStyleLightContent;
    }
    if (@available(iOS 13.0, *)) {
        return UIStatusBarStyleDarkContent;
    }
    return UIStatusBarStyleDefault;
}

- (UIStatusBarAnimation)preferredStatusBarUpdateAnimation {
    return UIStatusBarAnimationFade;
}

#pragma mark - WSAppWindowOrientationControl

- (void)wsSetSupportedOrientationMask:(UIInterfaceOrientationMask)mask {
    self.currentOrientationMask = mask;
}

- (void)wsSetStatusBarHidden:(BOOL)hidden {
    self.wsStatusBarHidden = hidden;
    [self setNeedsStatusBarAppearanceUpdate];
}

- (void)wsSetStatusBarLightContent:(BOOL)lightContent {
    self.wsStatusBarLightContent = lightContent;
    [self setNeedsStatusBarAppearanceUpdate];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.fd_prefersNavigationBarHidden = YES;
    self.view.backgroundColor = [UIColor whiteColor];
    [_delegator viewDidLoadWithView:self.view];
    [self.navigationController setNavigationBarHidden:YES animated:NO];

    [IOSPageStackStore onPageCreatedWithStack:self.navigationController context:self];
    [UmbrellaQnPlatformLogic.shared.appPageStack onPageCreatedStack:self.navigationController context:self];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    [_delegator viewDidLayoutSubviews];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [_delegator viewWillAppear];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    [IOSPageStackStore onPageWillAppear:self];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [_delegator viewDidAppear];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    [IOSPageStackStore onPageDidAppear:self];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_delegator viewWillDisappear];
    [IOSPageStackStore onPageWillDisappear:self];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [_delegator viewDidDisappear];
    [IOSPageStackStore onPageDidDisappear:self];
}

- (NSURL *)resourceFolderUrlForKuikly:(NSString *)pageName {
    return [[NSBundle mainBundle] bundleURL];
}

#pragma mark - private

- (NSDictionary *)p_mergeExtParamsWithOriditalParam:(NSDictionary *)pageParam {
    NSMutableDictionary *mParam = [(pageParam ?: @{}) mutableCopy];

    return mParam;
}

#pragma mark - KuiklyRenderViewControllerDelegatorDelegate

- (UIView *)createLoadingView {
    UIView *loadingView = [[UIView alloc] init];
    loadingView.backgroundColor = [UIColor whiteColor];
    return loadingView;
}

- (UIView *)createErrorView {
    UIView *errorView = [[UIView alloc] init];
    errorView.backgroundColor = [UIColor whiteColor];
    return errorView;
}

- (void)fetchContextCodeWithPageName:(NSString *)pageName resultCallback:(KuiklyContextCodeCallback)callback {
    if (callback) {
        callback(@"umbrella", nil);
    }
}

- (void)dealloc {
    [UmbrellaQnPlatformLogic.shared.appPageStack onPageDestroyedStack:self.navigationController context:self];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
