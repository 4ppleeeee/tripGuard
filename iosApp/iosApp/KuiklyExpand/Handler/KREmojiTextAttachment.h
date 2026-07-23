//
//  KREmojiTextAttachment.h
//  iosApp
//
//  评论输入框表情图片附件。
//
//  继承 NSTextAttachment 并实现 KRTextAttachmentStringProtocol 协议（由 KuiklyIOSRender SDK 定义），
//  用于在 PostProcessor 把 [/xxx] 短码替换为表情图片时，告知 SDK 该附件对应的"原始文本"，
//  使 SDK 能正确处理光标定位、字符删除、文本反向同步给 Kotlin BasicTextField 状态等操作。
//
//  仅实现这个协议后，渲染层才能在评论输入框中真正显示出表情图片；
//  否则 SDK 会丢弃裸 NSTextAttachment 导致输入框看不到表情。
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface KREmojiTextAttachment : NSTextAttachment

/// 该附件在原始文本中对应的短码，例如 "[/微笑]"。
/// 由创建方在 init 时传入，SDK 通过 kr_originlTextBeforeTextAttachment 反查。
@property (nonatomic, copy) NSString *originalText;

/// 创建表情附件。
/// @param image 表情图片
/// @param originalText 该图标对应的原始短码（如 "[/微笑]"）
- (instancetype)initWithImage:(UIImage *)image originalText:(NSString *)originalText;

@end

NS_ASSUME_NONNULL_END
