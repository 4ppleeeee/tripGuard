//
//  KBFontAwesome.h
//  KuaiBao
//
//  Created by Rayce Lee on 15/08/2017.
//
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
/*
 * http://bluejamesbond.github.io/CharacterMap/
 */

typedef unichar KBFontAwesomeCode;

@interface KBFontLoader : NSObject
+ (void)loadFont;
@end

@interface UIFont (KBFontAwesome)
+ (UIFont *)fontAwesome:(CGFloat)fontSize;
@end

@interface NSString(KBFontAwesome)
+ (NSString *)fontAwesome:(KBFontAwesomeCode)value;
@end
