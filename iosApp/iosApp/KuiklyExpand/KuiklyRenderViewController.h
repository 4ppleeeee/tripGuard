#import <UIKit/UIKit.h>
#import <umbrella/umbrella.h>

NS_ASSUME_NONNULL_BEGIN

/// 动态方向控制协议，由 IOSAppWindowHelper 调用
@protocol WSAppWindowOrientationControl <NSObject>
@optional
- (void)wsSetSupportedOrientationMask:(UIInterfaceOrientationMask)mask;
- (void)wsSetStatusBarHidden:(BOOL)hidden;
@end

@interface IOSPageStackStore : NSObject

+ (void)onPageCreatedWithStack:(id _Nullable)stack context:(id<UmbrellaIKmmContext>)context;
+ (void)onPageWillAppear:(id<UmbrellaIKmmContext>)context;
+ (void)onPageDidAppear:(id<UmbrellaIKmmContext>)context;
+ (void)onPageWillDisappear:(id<UmbrellaIKmmContext>)context;
+ (void)onPageDidDisappear:(id<UmbrellaIKmmContext>)context;
+ (void)onPageDestroyedWithStack:(id _Nullable)stack context:(id<UmbrellaIKmmContext>)context;
+ (NSArray<id<UmbrellaIKmmContext>> *)allPages;
+ (NSArray<id<UmbrellaIKmmContext>> *)activePages;
+ (id<UmbrellaIKmmContext> _Nullable)topValidPage;
+ (BOOL)isPageActive:(id<UmbrellaIKmmContext>)context;
+ (UmbrellaPageLifecycleState *)pageLifecycleStateForContext:(id<UmbrellaIKmmContext>)context;
+ (BOOL)applicationStateActive;

@end

@interface KuiklyRenderViewController : UIViewController <WSAppWindowOrientationControl>

@property (nonatomic, copy, readonly) NSString *pageName;

/*
 * @brief 创建实例对应的初始化方法.
 * @param pageName 页面名 （对应的值为kotlin侧页面注解 @Page("xxxx")中的xxx名）
 * @param params 页面对应的参数（kotlin侧可通过pageData.params获取）
 * @return 返回KuiklyRenderViewController实例
 */
- (instancetype)initWithPageName:(NSString *)pageName pageData:(NSDictionary *)pageData;

/// 动态设置支持的屏幕方向
- (void)wsSetSupportedOrientationMask:(UIInterfaceOrientationMask)mask;

/// 动态设置状态栏隐藏
- (void)wsSetStatusBarHidden:(BOOL)hidden;

/// 动态设置状态栏样式（YES = 深色图标/黑字，适合浅色背景；NO = 浅色图标/白字，适合深色背景）
- (void)wsSetStatusBarLightContent:(BOOL)lightContent;


- (void)dispatchPageNewIntentWithPageData:(NSDictionary<NSString *, id> *)pageData;
@end

NS_ASSUME_NONNULL_END
