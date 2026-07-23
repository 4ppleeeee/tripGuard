//
//  DialogModule.m
//  iosApp
//
//  Created by smart on 2025/4/6.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "DialogModule.h"
#import "KRRouterHandler.h"

@implementation DialogModule

/**
 * 根据提供的方法名动态调用模块中的方法
 *
 * @param method 要调用的方法名（不包含冒号）
 * @param params 传递给方法的参数
 * @param callback 处理结果的回调函数
 * @return 方法调用的结果，如果方法不存在则返回 nil
 */
- (id _Nullable)hrv_callWithMethod:(NSString *)method
                            params:(id _Nullable)params
                          callback:(KuiklyRenderCallback)callback {
    if ([method isEqualToString:@"onCloseDialog"] ||
        [method isEqualToString:KMM_DIALOG_MODULE_ON_CLOSE]) {
        BOOL animated = YES;
        if ([params isKindOfClass:[NSDictionary class]]) {
            id animation = ((NSDictionary *)params)[@"animation"];
            if ([animation respondsToSelector:@selector(boolValue)]) {
                animated = [animation boolValue];
            }
        }
        [IOSNativeRouter goBackWithContext:nil animated:animated];
        return nil;
    }

    return nil;
}

@end
