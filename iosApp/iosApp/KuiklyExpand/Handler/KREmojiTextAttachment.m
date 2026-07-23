//
//  KREmojiTextAttachment.m
//  iosApp
//
//  KRTextAttachmentStringProtocol 由 KuiklyIOSRender 定义，含一个方法
//  -kr_originlTextBeforeTextAttachment 返回该附件在原始文本中的字符串。
//

#import "KREmojiTextAttachment.h"
#import <KuiklyIOSRender/KuiklyRenderBridge.h>

@interface KREmojiTextAttachment () <KRTextAttachmentStringProtocol>
@end

@implementation KREmojiTextAttachment

- (instancetype)initWithImage:(UIImage *)image originalText:(NSString *)originalText {
    self = [super init];
    if (self) {
        self.image = image;
        _originalText = [originalText copy] ?: @"";
    }
    return self;
}

#pragma mark - KRTextAttachmentStringProtocol

- (NSString *)kr_originlTextBeforeTextAttachment {
    return self.originalText ?: @"";
}

@end
