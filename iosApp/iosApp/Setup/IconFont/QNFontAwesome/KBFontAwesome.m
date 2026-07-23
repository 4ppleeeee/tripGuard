 //
//  KBFontAwesome.m
//  KuaiBao
//
//  Created by Rayce Lee on 15/08/2017.
//
//

#import "KBFontAwesome.h"
#import <SDWebImage/SDImageCache.h>
#import <CoreText/CTFontManager.h>

/*
 * font characters map tool
 * http://bluejamesbond.github.io/
 */

@implementation KBFontLoader
+ (void)loadFont {
    NSBundle *bundle = [NSBundle mainBundle];
    NSString *fontPath = [bundle pathForResource:@"KBFontAwesome.ttf" ofType:nil];
    
    if (fontPath) {
        CGDataProviderRef providerRef = CGDataProviderCreateWithFilename([fontPath UTF8String]);
        CGFontRef fontRef = CGFontCreateWithDataProvider(providerRef);
        CFErrorRef errorRef = NULL;
        if (!CTFontManagerRegisterGraphicsFont(fontRef, &errorRef)) {
            CFStringRef errorDescription = CFErrorCopyDescription(errorRef);
            CFRelease(errorDescription);
        }
        CFRelease(fontRef);
        CFRelease(providerRef);
    }
}
@end

@implementation UIFont (KBFontAwesome)
+ (UIFont *)fontAwesome:(CGFloat)fontSize {
    NSString *const fontName = @"xwiconfont";
    
    // 1.这里是一个兜底逻辑，不一定执行，字体在info.plist中Fonts provided by application设置。
    // 2.fontName变量被编译器优化掉，断点为nil(<optimized out>)
    if ([UIFont fontNamesForFamilyName:fontName].count == 0) {        
        [KBFontLoader loadFont];
    }
    return [UIFont fontWithName:fontName size:fontSize];
}
@end
