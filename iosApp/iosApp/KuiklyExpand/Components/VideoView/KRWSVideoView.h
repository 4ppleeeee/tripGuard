#import <UIKit/UIKit.h>
#import <KuiklyIOSRender/KuiklyRenderViewExportProtocol.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * iOS 端 Kuikly 自定义视频播放组件
 *
 * 职责：提供一个 UIView 作为 ThumbPlayer 的渲染目标（playerView），
 * 当 view 准备好时通过 Kuikly 事件回调通知 DSL 层。
 * DSL 层拿到回调后通过 WSVideoPlayer.playVideo(surface, videoInfo)
 * 将此 view 作为 surface 传递给 ThumbPlayer（iOS 端 setSurface 接受 UIView）。
 */
@interface KRWSVideoView : UIView <KuiklyRenderViewExportProtocol>

@end

NS_ASSUME_NONNULL_END
