// The MIT License (MIT)
//
// Copyright (c) 2015-2016 forkingdog ( https://github.com/forkingdog )
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#import "UINavigationController+FDFullscreenPopGesture.h"
#import <objc/runtime.h>

static IMP qn_originalKRScrollViewGestureRecognizerShouldBeginImp = nil;

static BOOL qn_isKRScrollView(UIView *view)
{
    return [view isKindOfClass:[UIScrollView class]] &&
        [NSStringFromClass(view.class) isEqualToString:@"KRScrollView"];
}

static BOOL qn_scrollViewCanScrollToLeading(UIScrollView *scrollView)
{
    UIEdgeInsets contentInset = scrollView.adjustedContentInset;

    CGFloat visibleWidth = scrollView.bounds.size.width;
    CGFloat scrollableWidth = scrollView.contentSize.width + contentInset.left + contentInset.right;
    if (scrollableWidth <= visibleWidth + 0.5) {
        return NO;
    }

    CGFloat leadingOffsetX = -contentInset.left;
    return scrollView.contentOffset.x > leadingOffsetX + 0.5;
}

static BOOL qn_viewPrefersLeadingEdgeSwipeBackPriority(UIView *view)
{
    return [objc_getAssociatedObject(view, @selector(setCss_iosLeadingEdgeSwipeBackPriority:)) boolValue];
}

static BOOL qn_viewIsInLeadingEdgeSwipeBackPriorityScope(UIView *view)
{
    UIView *currentView = view;
    while (currentView) {
        if (qn_viewPrefersLeadingEdgeSwipeBackPriority(currentView)) {
            return YES;
        }
        currentView = currentView.superview;
    }
    return NO;
}

static UIScrollView *qn_priorityKRScrollViewCanScrollToLeadingFromView(UIView *view)
{
    UIScrollView *scrollViewCanScrollToLeading = nil;
    UIView *currentView = view;
    while (currentView) {
        if (qn_isKRScrollView(currentView) &&
            qn_scrollViewCanScrollToLeading((UIScrollView *)currentView)) {
            scrollViewCanScrollToLeading = (UIScrollView *)currentView;
        }

        if (qn_viewPrefersLeadingEdgeSwipeBackPriority(currentView) && scrollViewCanScrollToLeading) {
            return scrollViewCanScrollToLeading;
        }

        currentView = currentView.superview;
    }
    return nil;
}

static BOOL qn_shouldBlockInteractivePopForScrollView(UIGestureRecognizer *gestureRecognizer)
{
    CGPoint location = [gestureRecognizer locationInView:gestureRecognizer.view];
    UIView *hitView = [gestureRecognizer.view hitTest:location withEvent:nil];
    return qn_priorityKRScrollViewCanScrollToLeadingFromView(hitView) != nil;
}

static BOOL qn_shouldYieldScrollPanToInteractivePop(UIScrollView *scrollView, UIPanGestureRecognizer *panGestureRecognizer)
{
    if (!qn_viewIsInLeadingEdgeSwipeBackPriorityScope(scrollView)) {
        return NO;
    }

    CGPoint velocity = [panGestureRecognizer velocityInView:panGestureRecognizer.view];
    BOOL isLeftToRight = [UIApplication sharedApplication].userInterfaceLayoutDirection == UIUserInterfaceLayoutDirectionLeftToRight;
    CGFloat multiplier = isLeftToRight ? 1 : -1;
    if ((velocity.x * multiplier) <= fabs(velocity.y)) {
        return NO;
    }

    UIScrollView *priorityScrollView = qn_priorityKRScrollViewCanScrollToLeadingFromView(scrollView);
    if (priorityScrollView) {
        return priorityScrollView != scrollView;
    }

    return YES;
}

static BOOL qn_krScrollViewGestureRecognizerShouldBegin(id self, SEL selector, UIGestureRecognizer *gestureRecognizer)
{
    if ([self isKindOfClass:[UIScrollView class]]) {
        UIScrollView *scrollView = (UIScrollView *)self;
        if (gestureRecognizer == scrollView.panGestureRecognizer &&
            [gestureRecognizer isKindOfClass:[UIPanGestureRecognizer class]] &&
            qn_shouldYieldScrollPanToInteractivePop(scrollView, (UIPanGestureRecognizer *)gestureRecognizer)) {
            return NO;
        }
    }

    if (qn_originalKRScrollViewGestureRecognizerShouldBeginImp) {
        BOOL (*originalImp)(id, SEL, UIGestureRecognizer *) =
            (BOOL (*)(id, SEL, UIGestureRecognizer *))qn_originalKRScrollViewGestureRecognizerShouldBeginImp;
        return originalImp(self, selector, gestureRecognizer);
    }
    return YES;
}

static void qn_installKRScrollViewSwipeBackPatchIfNeeded(void)
{
    static BOOL isInstalled = NO;
    if (isInstalled) {
        return;
    }

    Class scrollViewClass = NSClassFromString(@"KRScrollView");
    SEL selector = @selector(gestureRecognizerShouldBegin:);
    Method method = class_getInstanceMethod(scrollViewClass, selector);
    if (!scrollViewClass || !method) {
        return;
    }

    qn_originalKRScrollViewGestureRecognizerShouldBeginImp = method_getImplementation(method);
    method_setImplementation(method, (IMP)qn_krScrollViewGestureRecognizerShouldBegin);
    isInstalled = YES;
}

@interface _FDFullscreenPopGestureRecognizerDelegate : NSObject <UIGestureRecognizerDelegate>

@property (nonatomic, weak) UINavigationController *navigationController;

@end

@implementation _FDFullscreenPopGestureRecognizerDelegate

- (BOOL)gestureRecognizerShouldBegin:(UIPanGestureRecognizer *)gestureRecognizer
{
    // Ignore when no view controller is pushed into the navigation stack.
    if (self.navigationController.viewControllers.count <= 1) {
        return NO;
    }

    // Ignore when the active view controller doesn't allow interactive pop.
    UIViewController *topViewController = self.navigationController.viewControllers.lastObject;
    if (topViewController.fd_interactivePopDisabled) {
        return NO;
    }

    // Ignore when the beginning location is beyond max allowed initial distance to left edge.
    CGPoint beginningLocation = [gestureRecognizer locationInView:gestureRecognizer.view];
    CGFloat maxAllowedInitialDistance = topViewController.fd_interactivePopMaxAllowedInitialDistanceToLeftEdge;
    if (maxAllowedInitialDistance > 0 && beginningLocation.x > maxAllowedInitialDistance) {
        return NO;
    }

    // Ignore pan gesture when the navigation controller is currently in transition.
    if ([[self.navigationController valueForKey:@"_isTransitioning"] boolValue]) {
        return NO;
    }

    // Prevent calling the handler when the gesture begins in an opposite direction.
    CGPoint translation = [gestureRecognizer translationInView:gestureRecognizer.view];
    BOOL isLeftToRight = [UIApplication sharedApplication].userInterfaceLayoutDirection == UIUserInterfaceLayoutDirectionLeftToRight;
    CGFloat multiplier = isLeftToRight ? 1 : - 1;
    if ((translation.x * multiplier) <= 0) {
        return NO;
    }

    if (qn_shouldBlockInteractivePopForScrollView(gestureRecognizer)) {
        return NO;
    }

    return YES;
}

@end

typedef void (^_FDViewControllerWillAppearInjectBlock)(UIViewController *viewController, BOOL animated);

@interface UIViewController (FDFullscreenPopGesturePrivate)

@property (nonatomic, copy) _FDViewControllerWillAppearInjectBlock fd_willAppearInjectBlock;

@end

@implementation UIViewController (FDFullscreenPopGesturePrivate)

+ (void)load
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Method viewWillAppear_originalMethod = class_getInstanceMethod(self, @selector(viewWillAppear:));
        Method viewWillAppear_swizzledMethod = class_getInstanceMethod(self, @selector(fd_viewWillAppear:));
        method_exchangeImplementations(viewWillAppear_originalMethod, viewWillAppear_swizzledMethod);

        Method viewWillDisappear_originalMethod = class_getInstanceMethod(self, @selector(viewWillDisappear:));
        Method viewWillDisappear_swizzledMethod = class_getInstanceMethod(self, @selector(fd_viewWillDisappear:));
        method_exchangeImplementations(viewWillDisappear_originalMethod, viewWillDisappear_swizzledMethod);
    });
}

- (void)fd_viewWillAppear:(BOOL)animated
{
    // Forward to primary implementation.
    [self fd_viewWillAppear:animated];

    if (self.fd_willAppearInjectBlock) {
        self.fd_willAppearInjectBlock(self, animated);
    }
}

- (void)fd_viewWillDisappear:(BOOL)animated
{
    // Forward to primary implementation.
    [self fd_viewWillDisappear:animated];

//    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//        UIViewController *viewController = self.navigationController.viewControllers.lastObject;
//        if (viewController && !viewController.fd_prefersNavigationBarHidden) {
//            [self.navigationController setNavigationBarHidden:NO animated:NO];
//        }
//    });
}

- (_FDViewControllerWillAppearInjectBlock)fd_willAppearInjectBlock
{
    return objc_getAssociatedObject(self, _cmd);
}

- (void)setFd_willAppearInjectBlock:(_FDViewControllerWillAppearInjectBlock)block
{
    objc_setAssociatedObject(self, @selector(fd_willAppearInjectBlock), block, OBJC_ASSOCIATION_COPY_NONATOMIC);
}

@end

@implementation UIView (QnSwipeBackControlCSS)

- (void)setCss_disableSwipeBack:(NSNumber *)css_disableSwipeBack
{
    UIViewController *viewController = [self qn_nearestViewControllerForSwipeBackControl];
    viewController.fd_interactivePopDisabled = css_disableSwipeBack.boolValue;
}

- (void)setCss_iosLeadingEdgeSwipeBackPriority:(NSNumber *)css_iosLeadingEdgeSwipeBackPriority
{
    qn_installKRScrollViewSwipeBackPatchIfNeeded();
    objc_setAssociatedObject(
        self,
        @selector(setCss_iosLeadingEdgeSwipeBackPriority:),
        css_iosLeadingEdgeSwipeBackPriority,
        OBJC_ASSOCIATION_RETAIN_NONATOMIC
    );
}

- (UIViewController *)qn_nearestViewControllerForSwipeBackControl
{
    UIResponder *responder = self;
    while (responder) {
        responder = responder.nextResponder;
        if ([responder isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)responder;
        }
    }
    return nil;
}

@end

@implementation UINavigationController (FDFullscreenPopGesture)

+ (void)load
{
    // Inject "-pushViewController:animated:"
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class class = [self class];

        SEL originalSelector = @selector(pushViewController:animated:);
        SEL swizzledSelector = @selector(fd_pushViewController:animated:);

        Method originalMethod = class_getInstanceMethod(class, originalSelector);
        Method swizzledMethod = class_getInstanceMethod(class, swizzledSelector);

        BOOL success = class_addMethod(class, originalSelector, method_getImplementation(swizzledMethod), method_getTypeEncoding(swizzledMethod));
        if (success) {
            class_replaceMethod(class, swizzledSelector, method_getImplementation(originalMethod), method_getTypeEncoding(originalMethod));
        } else {
            method_exchangeImplementations(originalMethod, swizzledMethod);
        }
    });
}

- (void)fd_pushViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if (![self.interactivePopGestureRecognizer.view.gestureRecognizers containsObject:self.fd_fullscreenPopGestureRecognizer]) {

        // Add our own gesture recognizer to where the onboard screen edge pan gesture recognizer is attached to.
        [self.interactivePopGestureRecognizer.view addGestureRecognizer:self.fd_fullscreenPopGestureRecognizer];

        // Forward the gesture events to the private handler of the onboard gesture recognizer.
        NSArray *internalTargets = [self.interactivePopGestureRecognizer valueForKey:@"targets"];
        id internalTarget = [internalTargets.firstObject valueForKey:@"target"];
        SEL internalAction = NSSelectorFromString(@"handleNavigationTransition:");
        self.fd_fullscreenPopGestureRecognizer.delegate = self.fd_popGestureRecognizerDelegate;
        [self.fd_fullscreenPopGestureRecognizer addTarget:internalTarget action:internalAction];

        // Disable the onboard gesture recognizer.
        self.interactivePopGestureRecognizer.enabled = NO;
    }

    // Handle perferred navigation bar appearance.
    [self fd_setupViewControllerBasedNavigationBarAppearanceIfNeeded:viewController];

    // Forward to primary implementation.
    if (![self.viewControllers containsObject:viewController]) {
        [self fd_pushViewController:viewController animated:animated];
    }
}

- (void)fd_setupViewControllerBasedNavigationBarAppearanceIfNeeded:(UIViewController *)appearingViewController
{
    if (!self.fd_viewControllerBasedNavigationBarAppearanceEnabled) {
        return;
    }

    __weak typeof(self) weakSelf = self;
    _FDViewControllerWillAppearInjectBlock block = ^(UIViewController *viewController, BOOL animated) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf) {
            [strongSelf setNavigationBarHidden:viewController.fd_prefersNavigationBarHidden animated:animated];
        }
    };

    // Setup will appear inject block to appearing view controller.
    // Setup disappearing view controller as well, because not every view controller is added into
    // stack by pushing, maybe by "-setViewControllers:".
    appearingViewController.fd_willAppearInjectBlock = block;
    UIViewController *disappearingViewController = self.viewControllers.lastObject;
    if (disappearingViewController && !disappearingViewController.fd_willAppearInjectBlock) {
        disappearingViewController.fd_willAppearInjectBlock = block;
    }
}

- (_FDFullscreenPopGestureRecognizerDelegate *)fd_popGestureRecognizerDelegate
{
    _FDFullscreenPopGestureRecognizerDelegate *delegate = objc_getAssociatedObject(self, _cmd);

    if (!delegate) {
        delegate = [[_FDFullscreenPopGestureRecognizerDelegate alloc] init];
        delegate.navigationController = self;

        objc_setAssociatedObject(self, _cmd, delegate, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
    return delegate;
}

- (UIPanGestureRecognizer *)fd_fullscreenPopGestureRecognizer
{
    UIPanGestureRecognizer *panGestureRecognizer = objc_getAssociatedObject(self, _cmd);

    if (!panGestureRecognizer) {
        panGestureRecognizer = [[UIPanGestureRecognizer alloc] init];
        panGestureRecognizer.maximumNumberOfTouches = 1;

        objc_setAssociatedObject(self, _cmd, panGestureRecognizer, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
    return panGestureRecognizer;
}

- (BOOL)fd_viewControllerBasedNavigationBarAppearanceEnabled
{
    NSNumber *number = objc_getAssociatedObject(self, _cmd);
    if (number) {
        return number.boolValue;
    }
    self.fd_viewControllerBasedNavigationBarAppearanceEnabled = YES;
    return YES;
}

- (void)setFd_viewControllerBasedNavigationBarAppearanceEnabled:(BOOL)enabled
{
    SEL key = @selector(fd_viewControllerBasedNavigationBarAppearanceEnabled);
    objc_setAssociatedObject(self, key, @(enabled), OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

#pragma mark - 方向转发（将方向控制代理给 topViewController）

- (BOOL)shouldAutorotate {
    if (self.topViewController) {
        return self.topViewController.shouldAutorotate;
    }
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    if (self.topViewController) {
        return self.topViewController.supportedInterfaceOrientations;
    }
    return UIInterfaceOrientationMaskPortrait;
}

@end

@implementation UIViewController (FDFullscreenPopGesture)

- (BOOL)fd_interactivePopDisabled
{
    return [objc_getAssociatedObject(self, _cmd) boolValue];
}

- (void)setFd_interactivePopDisabled:(BOOL)disabled
{
    objc_setAssociatedObject(self, @selector(fd_interactivePopDisabled), @(disabled), OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (BOOL)fd_prefersNavigationBarHidden
{
    return [objc_getAssociatedObject(self, _cmd) boolValue];
}

- (void)setFd_prefersNavigationBarHidden:(BOOL)hidden
{
    objc_setAssociatedObject(self, @selector(fd_prefersNavigationBarHidden), @(hidden), OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (CGFloat)fd_interactivePopMaxAllowedInitialDistanceToLeftEdge
{
#if CGFLOAT_IS_DOUBLE
    return [objc_getAssociatedObject(self, _cmd) doubleValue];
#else
    return [objc_getAssociatedObject(self, _cmd) floatValue];
#endif
}

- (void)setFd_interactivePopMaxAllowedInitialDistanceToLeftEdge:(CGFloat)distance
{
    SEL key = @selector(fd_interactivePopMaxAllowedInitialDistanceToLeftEdge);
    objc_setAssociatedObject(self, key, @(MAX(0, distance)), OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

@end