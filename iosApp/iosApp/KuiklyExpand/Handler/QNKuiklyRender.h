//
//  QNKuiklyRender.h
//  iosApp
//
//  Created by tingdongli on 2025/1/6.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <KuiklyIOSRender/KuiklyRenderBridge.h>

NS_ASSUME_NONNULL_BEGIN
/**
 * QNKuiklyRender: Kuikly渲染组件扩展类
 * 实现KuiklyRenderComponentExpandProtocol协议，提供图片加载等功能
 */
@interface QNKuiklyRender : NSObject <KuiklyRenderComponentExpandProtocol>

@end

NS_ASSUME_NONNULL_END
