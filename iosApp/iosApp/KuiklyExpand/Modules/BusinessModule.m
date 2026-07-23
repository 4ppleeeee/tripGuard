//
//  BusinessModule.m
//  QQNewsBaseLib
//
//  Created by 李庭栋 on 2025/9/28.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "BusinessModule.h"

// CP选择通知常量
NSString * const kQNCPSelectedNotification = @"kQNCPSelectedNotification";

@implementation BusinessModule
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
    return nil;
}


- (void)onCPSelected:(NSDictionary *)cpDict {
 
}

@end
