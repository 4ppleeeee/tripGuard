#import <UIKit/UIKit.h>
#import <KuiklyIOSRender/KuiklyRenderViewExportProtocol.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * iOS 端 Kuikly 自定义 Lottie 组件
 *
 * 对应 commonMain 中的 QnLottieView，viewName 为 "QnLottieView"。
 *
 * 支持属性（与 KMP 侧 QnLottieData / QnLottieView 保持一致）：
 * - data        : QnLottieData 对象（KMP 直接传递，非 JSON 字符串）
 * - applyTheme  : BOOL，日夜间主题切换
 * - setProgress : NSNumber (float 0-1)，手动控制动画进度
 * - setLottieDownloadStatusListener : KuiklyRenderCallback，下载状态回调
 *
 * 使用 lottie-ios（Airbnb）渲染动画，通过 QnLottieDownloader 完成
 * URL 下载与磁盘缓存（支持 .json 和 .lottie/.zip 格式）。
 */
@interface QnLottieView : UIView <KuiklyRenderViewExportProtocol>

@end

NS_ASSUME_NONNULL_END
