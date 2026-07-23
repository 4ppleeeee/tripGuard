//
//  QNKuiklyRenderBridge.h
//  iosApp
//
//  Created by tingdongli on 2025/1/7.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/**
 * QNKuiklyRenderBridge: Kuikly渲染桥接类
 * 负责初始化和设置Kuikly渲染组件
 */
@interface QNKuiklyRenderBridge : NSObject

/**
 * 初始化并设置Kuikly渲染桥接组件
 * 创建QNKuiklyRender实例并注册为组件扩展处理器
 */
- (void)setup;

- (void)becomeActive;

@end

NS_ASSUME_NONNULL_END
