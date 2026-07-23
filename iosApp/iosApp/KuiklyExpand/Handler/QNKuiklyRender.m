//
//  QNKuiklyRender.m
//  iosApp
//
//  Created by tingdongli on 2025/1/6.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "QNKuiklyRender.h"
#import "UIImageView+WebCache.h"
#import <Foundation/Foundation.h>
#import "KBFontAwesome.h"
#import <CoreText/CTFontManager.h>
// 引入 OC 实现的 KRTextPostProcessorAdapter（评论输入框表情后置处理）
#import "KRTextPostProcessorAdapter.h"

/**
 * QNKuiklyRender: Kuikly渲染组件扩展类
 * 实现KuiklyRenderComponentExpandProtocol协议，提供图片加载等功能
 */
@implementation QNKuiklyRender
/**
 * 为ImageView设置网络图片
 * @param url 图片URL地址
 * @param imageView 目标ImageView控件
 * @return 设置是否成功
 */
- (BOOL)hr_setImageWithUrl:(nonnull NSString *)url
              forImageView:(nonnull UIImageView *)imageView {
    return [self hr_setImageWithUrl:url
                       forImageView:imageView
                           complete:^(UIImage * _Nullable image,
                                      NSError * _Nullable error,
                                      NSURL * _Nullable imageURL) {
        
    }];
}


- (BOOL)hr_setImageWithUrl:(NSString *)url
              forImageView:(UIImageView *)imageView
                  complete:(ImageCompletionBlock)completeBlock {
    // 参数校验
    if (url.length <= 0 || !imageView) {
        return NO;
    }
    
    // 判断是否为本地图片路径
    if ([url hasPrefix:@"file://"] || [url hasPrefix:@"/"]) {
        // 加载本地图片
        UIImage *localImage = [self loadLocalImageWithUrl:url];
        if (localImage) {
            imageView.image = localImage;
            if (completeBlock) {
                completeBlock(localImage, nil, [NSURL URLWithString:url]);
            }
            return YES;
        }
        NSError *loadImageError = [NSError errorWithDomain:@"QNKuiklyRender Local Image Not Exist" code:-1 userInfo:nil];
        if (completeBlock) {
            completeBlock(nil, loadImageError, [NSURL URLWithString:url]);
        }
        return NO;
    } else {
        // 使用SDWebImage加载网络图片
        [[SDWebImageManager sharedManager] loadImageWithURL:[NSURL URLWithString:url]
                                                    options:SDWebImageHandleCookies
                                                    context:nil
                                                   progress:^(NSInteger receivedSize, NSInteger expectedSize, NSURL * _Nullable targetURL) {
        } completed:^(UIImage * _Nullable image, NSData * _Nullable data,
                      NSError * _Nullable error, SDImageCacheType cacheType, BOOL finished, NSURL * _Nullable imageURL) {
            if (error) {
                NSLog(@"kuikly load image failed:%@", error);
            }
            NSString *currentUrl = [imageView valueForKey:@"css_src"];
            if ([currentUrl isEqualToString:url]) {
                [imageView setImage:image];
            } else {
                NSLog(@"pic changed, old:%@, new:%@", url, currentUrl);
            }
            if (completeBlock) {
                completeBlock(image, error, [NSURL URLWithString:url]);
            }
        }];
        return YES;
    }
}

/**
 *  加载本地图片，支持日夜间图片自动降级
 *  日间格式: file:///path/drawable/image.webp
 *  夜间格式: file:///path/dark-drawable/image.webp
 *  如果夜间图片不存在，自动加载日间图片
 */
- (UIImage *)loadLocalImageWithUrl:(NSString *)url {
    if (url.length <= 0) {
        return nil;
    }
    
    // 移除 file:// 前缀
    NSString *filePath = [url stringByReplacingOccurrencesOfString:@"file://" withString:@""];
    
    // 尝试加载指定路径的图片
    UIImage *image = [UIImage imageWithContentsOfFile:filePath];
    if (image) {
        return image;
    }
    
    // 如果是夜间图片路径且加载失败，尝试加载日间图片
    if ([filePath containsString:@"dark-drawable"]) {
        NSString *dayFilePath = [filePath stringByReplacingOccurrencesOfString:@"dark-drawable" withString:@"drawable"];
        image = [UIImage imageWithContentsOfFile:dayFilePath];
        if (image) {
            return image;
        }
    }
    
    return nil;
}

- (void)loadFont {
    NSBundle *bundle = [NSBundle mainBundle];
    NSArray *resources = @[@"lanting.otf", @"TTTGB-Medium.otf", @"DreamHanSerifCN-W7.ttf", @"DINMittelschriftLTW1G.ttf"];
    for (NSString *resource in resources) {
        NSString *fontPath = [bundle pathForResource:resource ofType:nil];
        
        if (fontPath) {
            CGDataProviderRef providerRef = CGDataProviderCreateWithFilename([fontPath UTF8String]);
            CGFontRef fontRef = CGFontCreateWithDataProvider(providerRef);
            CFErrorRef errorRef = NULL;
            if (!CTFontManagerRegisterGraphicsFont(fontRef, &errorRef)) {
                CFStringRef errorDescription = CFErrorCopyDescription(errorRef);
                NSLog(@"Failed to register font %@: %@", resource, (__bridge NSString *)errorDescription);
                CFRelease(errorDescription);
                CFRelease(errorRef);
            } else {
                NSLog(@"Successfully registered font: %@", resource);
            }
            CFRelease(fontRef);
            CFRelease(providerRef);
        } else {
            NSLog(@"Font file not found: %@", resource);
        }
    }
}

/*
 * 自定义字体创建
 * @param fontfamily 字体名
 * @param fontSize 字体大小
 * @return 返回自定义字体 （注：若返回nil，则走sdk自身默认创建字体逻辑）
 */
- (UIFont *)hr_fontWithFontFamily:(NSString *)fontfamily
                         fontSize:(CGFloat)fontSize {
    NSLog(@"fontfamily:%@", fontfamily);
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [self loadFont];
    });
    
    if ([fontfamily isEqualToString:@"iconfont"]) {
        return [UIFont fontAwesome:fontSize];
    }
    
    if ([fontfamily isEqualToString:@"FZLanTingHeiS-H-GB"] ||
        [fontfamily isEqualToString:@"TTTGBMedium"]) {
        return [UIFont fontWithName:fontfamily size:fontSize];
    }
    
    // 支持 DreamHanSerifCN-W7 字体
    if ([fontfamily isEqualToString:@"DreamHanSerifCN-W7"] ||
        [fontfamily isEqualToString:@"DreamHanSerifCN"] ||
        [fontfamily isEqualToString:@"qqnews_font_sysong"]) {
        UIFont *customFont = [UIFont fontWithName:@"DreamHanSerifCN-W7" size:fontSize];
        if (customFont) {
            NSLog(@"Successfully created DreamHanSerifCN-W7 font with size: %.1f", fontSize);
            return customFont;
        } else {
            NSLog(@"Failed to create DreamHanSerifCN-W7 font, fallback to system font");
        }
    }
    
    return nil;
}

/*
 * 扩展 Kotlin Text / 富文本组件的后置处理（用于评论展示、底部草稿等只读富文本场景）。
 * 注：若插入 NSTextAttachment，请其实现 KRTextAttachmentStringProtocol 协议。
 * @param attributedString 源文本对象
 * @param font 字体
 * @param textPostProcessor 后置处理标记（由 kotlin 侧 text 组件属性设置 textPostProcessor() 而来）
 * @return 返回新的文本对象
 */
- (NSMutableAttributedString *)kr_customTextWithAttributedString:(NSAttributedString *)attributedString
                                                            font:(UIFont *)font textPostProcessor:(NSString *)textPostProcessor {
    // 评论文本 / 草稿展示场景：把 [/xxx] 短码替换为表情图片附件。
    // 其他 processor 透传原文本，避免污染其他业务的文本渲染。
    return [[KRTextPostProcessorAdapter shared] processWithAttributedString:attributedString
                                                                       font:font
                                                                  processor:textPostProcessor ?: @""];
}

/*
 * 扩展 Kotlin BasicTextField / 输入框（KRTextAreaView）的富文本后置处理。
 *
 * 注意：与上面 `kr_customTextWithAttributedString:font:textPostProcessor:` 是两个独立的回调。
 * - `kr_` 版本由 KuiklyTextView 等只读富文本组件调用（见 `KuiklyTextView` / `KRRichText`）。
 * - `hr_` 版本由 KRTextAreaView（输入框）的 `-p_applyTextPostProcessorIfNeed` 调用，
 *   SDK 内部用 `respondsToSelector:` 探测是否实现，未实现则直接跳过表情后处理，
 *   表现就是「评论展示能显示表情，但输入框中只显示 [/xxx] 短码」。
 *
 * 由于 SDK 此处不传 font 参数，font 传 nil；KRTextPostProcessorAdapter 内部
 * 对 font==nil 已有兜底（默认字号、兜底插入 NSFontAttributeName 等）。
 *
 * @param attributedString 输入框当前富文本
 * @param textPostProcessor 后置处理标记（由 kotlin 侧 Modifier.textPostProcessor("comment_input") 而来）
 * @return 处理后的富文本
 */
- (NSMutableAttributedString *)hr_customTextWithAttributedString:(NSAttributedString *)attributedString
                                               textPostProcessor:(NSString *)textPostProcessor {
    return [[KRTextPostProcessorAdapter shared] processWithAttributedString:attributedString
                                                                       font:nil
                                                                  processor:textPostProcessor ?: @""];
}


/*
 * 扩展Kotlin文本组件的text属性-后置处理
 * @param text 源文本
 * @param textPostProcessor 后置处理标记（由kotlin侧text组件属性设置textPostProcessor()而来）
 * @return 返回新的文本对象
 */
- (NSString *)kr_customTextWithText:(NSString *)text textPostProcessor:(NSString *)textPostProcessor {
    return text;
}

@end
