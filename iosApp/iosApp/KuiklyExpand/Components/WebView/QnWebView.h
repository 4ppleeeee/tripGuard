#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>
#import <KuiklyIOSRender/KuiklyRenderViewExportProtocol.h>

@class QnWebViewJSBridge;

NS_ASSUME_NONNULL_BEGIN

/**
 * QnWebView - iOS 端 WebView 渲染组件
 * 遵循 KuiklyRenderViewExportProtocol 协议，将 WKWebView 暴露给 Kuikly 框架
 *
 * 类名 "QnWebView" 与 Kotlin 侧 viewName() 返回值一致
 * iOS 端通过运行时自动发现，无需手动注册
 */
@interface QnWebView : UIView <KuiklyRenderViewExportProtocol, WKNavigationDelegate, WKUIDelegate>

/** 内部 WKWebView 实例 */
@property (nonatomic, strong, readonly) WKWebView *webView;

/** JSBridge 实例 */
@property (nonatomic, strong, readonly) QnWebViewJSBridge *jsBridge;

@end

NS_ASSUME_NONNULL_END
