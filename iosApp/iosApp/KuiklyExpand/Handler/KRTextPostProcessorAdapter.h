//
//  KRTextPostProcessorAdapter.h
//  iosApp
//
//  评论输入框表情后置处理适配器（iOS 端，OC 版）。
//
//  当 Kuikly 文本/输入框组件设置了 `Modifier.textPostProcessor("comment_input")`
//  + `outputTransformation = TextPostProcessorOutputTransformation("comment_input")`
//  时，渲染层会回调到 `QNKuiklyRender.kr_customTextWithAttributedString:`，
//  我们在那里转发到本类的 `processWithAttributedString:font:processor:`，
//  由它把 `[/xxx]` 形式的表情短码替换为对应表情图片的 NSTextAttachment，
//  从而在原生输入框 / 文本中渲染出表情图标。
//
//  仅处理 processor 名称为 "comment_input" 的请求，其他场景透传原文本，
//  避免污染其他业务的文本渲染。
//
//  与三端一致性约定：
//  - processor 名称必须与 Kotlin 侧 `EMOJI_TEXT_POST_PROCESSOR = "comment_input"` 保持一致
//    （见 wsCompose/CommentInputEditSection.kt）。
//  - 表情短码与 position 的映射是 wsFeeds/CommentEmojiLoader.kt 中
//    `ALL_EMO_FAST_SYMBOL` / `ALL_EMO_FAST_POSITION` 的 1:1 复制；
//    源端新增 / 修改表情时，须同步更新本文件。
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// 评论输入框 processor 名称（与 Kotlin 侧 `textPostProcessor("comment_input")` 保持一致）。
FOUNDATION_EXPORT NSString *const KRTextPostProcessorCommentInput;

@interface KRTextPostProcessorAdapter : NSObject

/// 全局单例。
@property (class, nonatomic, readonly) KRTextPostProcessorAdapter *shared;

/// 处理评论输入框文本：扫描 [/xxx] 短码并替换为表情图片附件。
/// @param attributedString 原始富文本（来自 Kuikly 渲染层）。
/// @param font 当前文字字体，用于推算表情图标的视觉尺寸。
/// @param processor 后置处理标记。仅当为 `KRTextPostProcessorCommentInput` 时生效。
/// @return 替换完成的富文本；非目标 processor 或无短码命中时返回原文本拷贝。
- (NSMutableAttributedString *)processWithAttributedString:(NSAttributedString *)attributedString
                                                      font:(nullable UIFont *)font
                                                 processor:(NSString *)processor;

@end

NS_ASSUME_NONNULL_END
