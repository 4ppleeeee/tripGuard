#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <umbrella/umbrella.h>
#import <KuiklyIOSRender/KRRouterModule.h>

NS_ASSUME_NONNULL_BEGIN

@interface IOSNativeRouter : NSObject

+ (BOOL)openPageWithName:(NSString *)pageName
               pageData:(NSDictionary<NSString *, id> * _Nullable)pageData
                context:(id<UmbrellaIKmmContext> _Nullable)context
               animated:(BOOL)animated;

+ (BOOL)openSingleInstancePageWithName:(NSString *)pageName
                              pageData:(NSDictionary<NSString *, id> * _Nullable)pageData
                               context:(id<UmbrellaIKmmContext> _Nullable)context
                              animated:(BOOL)animated;

+ (BOOL)openPageWithName:(NSString *)pageName
               pageData:(NSDictionary<NSString *, id> * _Nullable)pageData
             controller:(UIViewController * _Nullable)controller
               animated:(BOOL)animated;

/// 以模态方式（`presentViewController`）弹出页面：
/// `modalPresentationStyle = .fullScreen` + `modalTransitionStyle = .coverVertical`，
/// 效果为从底部向上覆盖全屏。
/// 退出由 `goBackWithContext:` 现有 `dismissViewControllerAnimated:` 分支处理。
+ (BOOL)presentPageWithName:(NSString *)pageName
                  pageData:(NSDictionary<NSString *, id> * _Nullable)pageData
                   context:(id<UmbrellaIKmmContext> _Nullable)context
                  animated:(BOOL)animated;

/// 以弹层方式覆盖当前页面：
/// `modalPresentationStyle = .overFullScreen`，保留底层页面可见，适用于 ComposeDialog。
/// 退出由 `goBackWithContext:` 现有 `dismissViewControllerAnimated:` 分支处理。
+ (BOOL)presentDialogPageWithName:(NSString *)pageName
                          pageData:(NSDictionary<NSString *, id> * _Nullable)pageData
                           context:(id<UmbrellaIKmmContext> _Nullable)context
                          animated:(BOOL)animated;

+ (BOOL)openRoute:(NSString *)route
          context:(id<UmbrellaIKmmContext> _Nullable)context
         animated:(BOOL)animated;

+ (BOOL)goBackWithContext:(id<UmbrellaIKmmContext> _Nullable)context animated:(BOOL)animated;
+ (BOOL)quitWithContext:(id<UmbrellaIKmmContext> _Nullable)context animated:(BOOL)animated;
+ (id<UmbrellaIKmmContext> _Nullable)topVisibleKmmContext;
+ (UINavigationController * _Nullable)navigationControllerForContext:(id<UmbrellaIKmmContext> _Nullable)context;
+ (UIViewController * _Nullable)viewControllerForContext:(id<UmbrellaIKmmContext> _Nullable)context;
@end

@interface KRRouterHandler : NSObject<KRRouterProtocol>

+ (void)registerIfNeeded;

@end

NS_ASSUME_NONNULL_END
