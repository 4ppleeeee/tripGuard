//
//  KuiklyPageView.h
//  iosApp
//
//  iOS 端 KuiklyPageView 自定义组件，承载子 Pager。
//  与 DSL 侧 com.tencent.weishi.compose.main.welfare.KuiklyPageView 对应，
//  类名必须保持为 "KuiklyPageView"（Kuikly 框架按类名查找原生组件）。
//

#import <UIKit/UIKit.h>
#import <KuiklyIOSRender/KuiklyRenderViewExportProtocol.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * iOS 端 Kuikly 子页面容器组件。
 *
 * 职责：作为一个 UIView 嵌入在外层 Kuikly Pager 中，内部持有一个独立的
 * `KuiklyView`（子 Pager），根据 DSL 侧传来的 `pageName` / `pageData`
 * 加载子页面；与外层宿主 Pager 完全独立。
 *
 * 与 Android 端 `KuiklyPageView`（FrameLayout）等价。
 */
@interface KuiklyPageView : UIView <KuiklyRenderViewExportProtocol>

@end

NS_ASSUME_NONNULL_END
