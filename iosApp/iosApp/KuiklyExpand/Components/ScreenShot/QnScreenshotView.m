//
//  QnScreenshotView.m
//  iosApp
//
//  Created by tingdongli on 2025/1/14.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "QnScreenshotView.h"

@implementation QnScreenshotView
@synthesize hr_rootView;

- (void)hrv_setPropWithKey:(NSString * _Nonnull)propKey
                 propValue:(id _Nonnull)propValue {
    // 处理框架尺寸属性
    if ([propKey isEqualToString:@"frame"]) {
        // 检查并转换NSRect值
        if ([propValue isKindOfClass:[NSValue class]]) {
            // 设置视图框架
            CGRect frame = [(NSValue *)propValue CGRectValue];
            self.frame = frame;
        }
    }
    KUIKLY_SET_CSS_COMMON_PROP;
}

- (void)hrv_callWithMethod:(NSString *)method
                    params:(NSString *)params
                  callback:(KuiklyRenderCallback)callback {
    NSLog(@"QnScreenshotView hrv_callWithMethod:%@, params:%@", method, params);
    
    if ([method isEqualToString:@"take"]) {
        // 创建截图
        UIGraphicsImageRenderer *render = [[UIGraphicsImageRenderer alloc] initWithBounds:self.bounds];
        UIImage *screenshot = [render imageWithActions:^(UIGraphicsImageRendererContext * _Nonnull rendererContext) {
            [self.layer.presentationLayer renderInContext:rendererContext.CGContext];
        }];
        
        // 保存到本地
        NSString *documentsPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
        NSString *fileName = [NSString stringWithFormat:@"screenshot_%ld.png", (long)[[NSDate date] timeIntervalSince1970]];
        NSString *filePath = [documentsPath stringByAppendingPathComponent:fileName];
        NSString *finalPath = filePath;
        if (filePath.length <= 0) {
            finalPath = @"";
        }
        
        NSData *imageData = UIImagePNGRepresentation(screenshot);
        [imageData writeToFile:filePath atomically:YES];
        
        // 回调文件路径
        if (callback) {
            callback(@{@"path" : finalPath});
        }
    }
}

@end
