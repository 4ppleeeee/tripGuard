#import "KRRouterHandler.h"
#import "KuiklyRenderViewController.h"

@interface IOSNativeRouter ()

+ (KuiklyRenderViewController *)p_renderViewControllerWithPageName:(NSString *)pageName
                                              navigationController:(UINavigationController *)navigationController;
+ (KuiklyRenderViewController *)p_renderViewControllerWithPageName:(NSString *)pageName
                                                preferredController:(UIViewController *)controller;
+ (BOOL)p_activateSingleInstanceViewController:(KuiklyRenderViewController *)targetViewController
                                      pageData:(NSDictionary<NSString *, id> *)pageData
                                      animated:(BOOL)animated;

@end

@implementation IOSNativeRouter

+ (UIViewController *)viewControllerForContext:(id<UmbrellaIKmmContext>)context {
    id rawContext = context;
    if ([rawContext isKindOfClass:[UIViewController class]]) {
        return (UIViewController *)rawContext;
    }
    return nil;
}

+ (UINavigationController *)navigationControllerForContext:(id<UmbrellaIKmmContext>)context {
    UIViewController *controller = [self viewControllerForContext:context];
    if ([controller isKindOfClass:[UINavigationController class]]) {
        return (UINavigationController *)controller;
    }
    if (controller.navigationController != nil) {
        return controller.navigationController;
    }
    UIViewController *topController = [self p_topVisibleController];
    if ([topController isKindOfClass:[UINavigationController class]]) {
        return (UINavigationController *)topController;
    }
    return topController.navigationController;
}

+ (id<UmbrellaIKmmContext>)topVisibleKmmContext {
    UIViewController *controller = [self p_topVisibleController];
    if ([controller conformsToProtocol:@protocol(UmbrellaIKmmContext)]) {
        return (id<UmbrellaIKmmContext>)controller;
    }
    if ([controller isKindOfClass:[UINavigationController class]]) {
        UIViewController *topViewController = ((UINavigationController *)controller).topViewController;
        if ([topViewController conformsToProtocol:@protocol(UmbrellaIKmmContext)]) {
            return (id<UmbrellaIKmmContext>)topViewController;
        }
    }
    return nil;
}

+ (BOOL)openPageWithName:(NSString *)pageName
               pageData:(NSDictionary<NSString *, id> *)pageData
                context:(id<UmbrellaIKmmContext>)context
               animated:(BOOL)animated {
    UIViewController *controller = [self viewControllerForContext:context];
    return [self openPageWithName:pageName pageData:pageData controller:controller animated:animated];
}

+ (BOOL)openSingleInstancePageWithName:(NSString *)pageName
                              pageData:(NSDictionary<NSString *, id> *)pageData
                               context:(id<UmbrellaIKmmContext>)context
                              animated:(BOOL)animated {
    if (pageName.length == 0) {
        return NO;
    }

    UIViewController *controller = [self viewControllerForContext:context];
    KuiklyRenderViewController *targetViewController = [self p_renderViewControllerWithPageName:pageName
                                                                            preferredController:controller];
    if (targetViewController != nil) {
        return [self p_activateSingleInstanceViewController:targetViewController pageData:pageData animated:animated];
    }

    return [self openPageWithName:pageName pageData:pageData controller:controller animated:animated];
}

+ (BOOL)openPageWithName:(NSString *)pageName
               pageData:(NSDictionary<NSString *, id> *)pageData
             controller:(UIViewController *)controller
               animated:(BOOL)animated {
    if (pageName.length == 0) {
        return NO;
    }

    KuiklyRenderViewController *renderViewController =
        [[KuiklyRenderViewController alloc] initWithPageName:pageName pageData:pageData ?: @{}];
    UINavigationController *navigationController = nil;
    if ([controller isKindOfClass:[UINavigationController class]]) {
        navigationController = (UINavigationController *)controller;
    } else {
        navigationController = controller.navigationController;
    }
    if (navigationController == nil) {
        navigationController = [self navigationControllerForContext:(id<UmbrellaIKmmContext>)controller];
    }
    if (navigationController != nil) {
        [navigationController pushViewController:renderViewController animated:animated];
        return YES;
    }

    UIViewController *presentController = controller ?: [self p_topVisibleController];
    UINavigationController *wrapperNavigationController = [[UINavigationController alloc] initWithRootViewController:renderViewController];
    if (presentController != nil) {
        [presentController presentViewController:wrapperNavigationController animated:animated completion:nil];
        return YES;
    }

    UIWindow *window = [self p_activeWindow];
    if (window == nil) {
        return NO;
    }
    window.rootViewController = wrapperNavigationController;
    [window makeKeyAndVisible];
    return YES;
}

+ (BOOL)presentPageWithName:(NSString *)pageName
                  pageData:(NSDictionary<NSString *, id> *)pageData
                   context:(id<UmbrellaIKmmContext>)context
                  animated:(BOOL)animated {
    if (pageName.length == 0) {
        return NO;
    }

    KuiklyRenderViewController *renderViewController = [[KuiklyRenderViewController alloc] initWithPageName:pageName pageData:pageData ?: @{}];
    UINavigationController *wrapperNavigationController = [[UINavigationController alloc] initWithRootViewController:renderViewController];
    // 用 `coverVertical`（默认）得到"从底部向上覆盖全屏"的效果，配合 `fullScreen` 盖住 status bar 一整屏。
    // 注意不要改成 `.automatic` 的 sheet 样式，那会给外边框、留顶部留白。
    wrapperNavigationController.modalPresentationStyle = UIModalPresentationFullScreen;
    wrapperNavigationController.modalTransitionStyle = UIModalTransitionStyleCoverVertical;

    UIViewController *presenter = [self viewControllerForContext:context] ?: [self p_topVisibleController];
    if (presenter == nil) {
        return NO;
    }
    [presenter presentViewController:wrapperNavigationController animated:animated completion:nil];
    return YES;
}

+ (BOOL)presentDialogPageWithName:(NSString *)pageName
                          pageData:(NSDictionary<NSString *, id> *)pageData
                           context:(id<UmbrellaIKmmContext>)context
                          animated:(BOOL)animated {
    if (pageName.length == 0) {
        return NO;
    }

    KuiklyRenderViewController *renderViewController = [[KuiklyRenderViewController alloc] initWithPageName:pageName pageData:pageData ?: @{}];
    UINavigationController *wrapperNavigationController = [[UINavigationController alloc] initWithRootViewController:renderViewController];
    wrapperNavigationController.modalPresentationStyle = UIModalPresentationOverFullScreen;
    wrapperNavigationController.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;
    wrapperNavigationController.view.backgroundColor = [UIColor clearColor];
    renderViewController.view.backgroundColor = [UIColor clearColor];

    UIViewController *presenter = [self viewControllerForContext:context] ?: [self p_topVisibleController];
    if (presenter == nil) {
        return NO;
    }
    [presenter presentViewController:wrapperNavigationController animated:animated completion:nil];
    return YES;
}

+ (BOOL)openRoute:(NSString *)route
          context:(id<UmbrellaIKmmContext>)context
         animated:(BOOL)animated {
    NSString *trimmedRoute = [route stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (trimmedRoute.length == 0) {
        return NO;
    }
    if ([trimmedRoute hasPrefix:@"/page/"]) {
        return [self openPageWithName:trimmedRoute pageData:nil context:context animated:animated];
    }

    NSURL *url = [NSURL URLWithString:trimmedRoute];
    if (url == nil) {
        return NO;
    }
    UIApplication *application = UIApplication.sharedApplication;
    if (@available(iOS 10.0, *)) {
        [application openURL:url options:@{} completionHandler:nil];
    } else {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
        [application openURL:url];
#pragma clang diagnostic pop
    }
    return YES;
}

+ (BOOL)goBackWithContext:(id<UmbrellaIKmmContext>)context animated:(BOOL)animated {
    UIViewController *controller = [self viewControllerForContext:context] ?: [self p_topVisibleController];
    if (controller == nil) {
        return NO;
    }

    if ([controller isKindOfClass:[UINavigationController class]]) {
        UINavigationController *navigationController = (UINavigationController *)controller;
        if (navigationController.viewControllers.count > 1) {
            [navigationController popViewControllerAnimated:animated];
            return YES;
        }
        if (navigationController.presentingViewController != nil) {
            [navigationController dismissViewControllerAnimated:animated completion:nil];
            return YES;
        }
    }

    UINavigationController *navigationController = controller.navigationController;
    if (navigationController != nil && navigationController.viewControllers.firstObject != controller) {
        [navigationController popViewControllerAnimated:animated];
        return YES;
    }
    if (controller.presentingViewController != nil) {
        [controller dismissViewControllerAnimated:animated completion:nil];
        return YES;
    }
    if (navigationController.presentingViewController != nil && navigationController.viewControllers.firstObject == controller) {
        [navigationController dismissViewControllerAnimated:animated completion:nil];
        return YES;
    }
    return NO;
}

+ (BOOL)quitWithContext:(id<UmbrellaIKmmContext>)context animated:(BOOL)animated {
    return [self goBackWithContext:context animated:animated];
}

+ (UIWindow *)p_activeWindow {
    UIApplication *application = UIApplication.sharedApplication;
    UIWindow *fallbackWindow = nil;
    for (UIScene *scene in application.connectedScenes) {
        if (![scene isKindOfClass:[UIWindowScene class]]) {
            continue;
        }
        UIWindowScene *windowScene = (UIWindowScene *)scene;
        if (windowScene.activationState != UISceneActivationStateForegroundActive &&
            windowScene.activationState != UISceneActivationStateForegroundInactive) {
            continue;
        }
        for (UIWindow *window in windowScene.windows) {
            if (window.isKeyWindow) {
                return window;
            }
            if (fallbackWindow == nil) {
                fallbackWindow = window;
            }
        }
    }
    return fallbackWindow ?: application.windows.firstObject;
}

+ (UIViewController *)p_topVisibleController {
    UIWindow *window = [self p_activeWindow];
    UIViewController *rootViewController = window.rootViewController;
    return [self p_topVisibleControllerFrom:rootViewController];
}

+ (KuiklyRenderViewController *)p_renderViewControllerWithPageName:(NSString *)pageName
                                              navigationController:(UINavigationController *)navigationController {
    if (pageName.length == 0 || navigationController == nil) {
        return nil;
    }
    for (UIViewController *viewController in navigationController.viewControllers.reverseObjectEnumerator) {
        if (![viewController isKindOfClass:[KuiklyRenderViewController class]]) {
            continue;
        }
        KuiklyRenderViewController *renderViewController = (KuiklyRenderViewController *)viewController;
        if ([renderViewController.pageName isEqualToString:pageName]) {
            return renderViewController;
        }
    }
    return nil;
}

+ (KuiklyRenderViewController *)p_renderViewControllerWithPageName:(NSString *)pageName
                                                preferredController:(UIViewController *)controller {
    if (pageName.length == 0) {
        return nil;
    }

    UINavigationController *preferredNavigationController = nil;
    if ([controller isKindOfClass:[UINavigationController class]]) {
        preferredNavigationController = (UINavigationController *)controller;
    } else {
        preferredNavigationController = controller.navigationController;
    }
    KuiklyRenderViewController *targetViewController = [self p_renderViewControllerWithPageName:pageName
                                                                           navigationController:preferredNavigationController];
    if (targetViewController != nil) {
        return targetViewController;
    }

    for (id<UmbrellaIKmmContext> context in IOSPageStackStore.allPages.reverseObjectEnumerator) {
        UIViewController *pageController = [self viewControllerForContext:context];
        if ([pageController isKindOfClass:[KuiklyRenderViewController class]]) {
            KuiklyRenderViewController *renderViewController = (KuiklyRenderViewController *)pageController;
            if ([renderViewController.pageName isEqualToString:pageName]) {
                return renderViewController;
            }
        }

        UINavigationController *navigationController = nil;
        if ([pageController isKindOfClass:[UINavigationController class]]) {
            navigationController = (UINavigationController *)pageController;
        } else {
            navigationController = pageController.navigationController;
        }
        targetViewController = [self p_renderViewControllerWithPageName:pageName navigationController:navigationController];
        if (targetViewController != nil) {
            return targetViewController;
        }
    }
    return nil;
}

+ (BOOL)p_activateSingleInstanceViewController:(KuiklyRenderViewController *)targetViewController
                                      pageData:(NSDictionary<NSString *, id> *)pageData
                                      animated:(BOOL)animated {
    if (targetViewController == nil) {
        return NO;
    }

    NSDictionary<NSString *, id> *intentData = pageData ?: @{};
    UINavigationController *navigationController = targetViewController.navigationController;
    void (^dispatchNewIntent)(void) = ^{
        [targetViewController dispatchPageNewIntentWithPageData:intentData];
    };
    if (navigationController == nil) {
        dispatchNewIntent();
        return YES;
    }

    void (^popAndDispatch)(void) = ^{
        if ([navigationController.viewControllers containsObject:targetViewController] &&
            navigationController.topViewController != targetViewController) {
            [navigationController popToViewController:targetViewController animated:animated];
        }
        dispatchNewIntent();
    };

    UIViewController *presentedViewController = navigationController.presentedViewController ?: targetViewController.presentedViewController;
    if (presentedViewController != nil) {
        [presentedViewController dismissViewControllerAnimated:animated completion:popAndDispatch];
    } else {
        popAndDispatch();
    }
    return YES;
}

+ (UIViewController *)p_topVisibleControllerFrom:(UIViewController *)controller {
    if (controller == nil) {
        return nil;
    }
    if (controller.presentedViewController != nil) {
        return [self p_topVisibleControllerFrom:controller.presentedViewController];
    }
    if ([controller isKindOfClass:[UINavigationController class]]) {
        UINavigationController *navigationController = (UINavigationController *)controller;
        return [self p_topVisibleControllerFrom:navigationController.topViewController ?: navigationController.visibleViewController];
    }
    if ([controller isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tabBarController = (UITabBarController *)controller;
        return [self p_topVisibleControllerFrom:tabBarController.selectedViewController];
    }
    return controller;
}

@end

@implementation KRRouterHandler

+ (void)registerIfNeeded {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [KRRouterModule registerRouterHandler:[self new]];
    });
}

- (void)openPageWithName:(NSString *)pageName
                pageData:(NSDictionary *)pageData
             hotReloadIp:(NSString *)ip
              controller:(UIViewController *)controller {
    (void)ip;
    [IOSNativeRouter openPageWithName:pageName pageData:pageData controller:controller animated:YES];
}

- (void)closePage:(UIViewController *)controller {
    id<UmbrellaIKmmContext> context = [controller conformsToProtocol:@protocol(UmbrellaIKmmContext)] ? (id<UmbrellaIKmmContext>)controller : nil;
    [IOSNativeRouter goBackWithContext:context animated:YES];
}

@end
