//
//  PerformanceModule.m
//  iosApp
//
//  Created by tingdongli on 2025/3/5.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "PerformanceModule.h"
#import <KuiklyIOSRender/NSObject+KR.h>

NSString * const kQNPerformanceModuleFirstFrameNotification = @"kQNPerformanceModuleFirstFrameNotification";

@implementation PerformanceModule

@synthesize hr_rootView;

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
    SEL selector = NSSelectorFromString([NSString stringWithFormat:@"%@:", method]);
    if ([self respondsToSelector:selector]) {
        NSMutableDictionary *args = [@{
            KR_PARAM_KEY: params ?: @"",
        } mutableCopy];
        if (callback) {
            args[KR_CALLBACK_KEY] = callback;
        }
        return [self kr_invokeWithSelector:selector args:args];
    }

    NSString *reason = [NSString stringWithFormat:@"module方法不存在: %@:(NSDictionary *)args）在Module中未实现，请补充该方法", method];
    NSLog(@"PerformanceModule:%@", reason);
    if (callback) {
        callback(@{
            @"code": @(-1),
            @"message": @"method does not exist",
        });
    }
    return nil;
}

#pragma mark Method

/**
 * 当页面的第一帧渲染完成时调用。
 * 当前通过通知把事件抛给 iOS 侧，便于后续接入性能埋点或页面级监听。
 */
- (void)onPageFirstFrameRendered:(NSDictionary *)args {
    id param = [args isKindOfClass:[NSDictionary class]] ? args[KR_PARAM_KEY] : args;
    dispatch_async(dispatch_get_main_queue(), ^{
        NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
        if ([param isKindOfClass:[NSDictionary class]]) {
            [userInfo addEntriesFromDictionary:param];
        } else if (param && !([param isKindOfClass:[NSString class]] && [((NSString *)param) length] == 0)) {
            userInfo[KR_PARAM_KEY] = param;
        }

        [[NSNotificationCenter defaultCenter] postNotificationName:kQNPerformanceModuleFirstFrameNotification
                                                            object:self.hr_rootView
                                                          userInfo:userInfo.count > 0 ? userInfo : nil];
    });
}

/**
 * 预点击能力预留空实现，后续再按需对齐 Android。
 */
- (void)firePreClick:(NSDictionary *)viewRefProp {
    (void)viewRefProp;
}

@end
