//
//  PerformanceModule.h
//  iosApp
//
//  Created by tingdongli on 2025/3/5.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import <KuiklyIOSRender/KRBaseModule.h>

NS_ASSUME_NONNULL_BEGIN

@class KuiklyRenderView;

/// 页面首帧完成通知，`object` 为当前 `hr_rootView`，`userInfo` 透传 Kotlin 下发的 `param`
extern NSString * const kQNPerformanceModuleFirstFrameNotification;

/**
 * PerformanceModule
 *
 * 负责承接 Kotlin 侧的性能相关模块调用。
 * 当前提供：
 * - `onPageFirstFrameRendered`：发出 iOS 首帧完成通知
 * - `firePreClick`：预留空实现
 */
@interface PerformanceModule : KRBaseModule

@property (nonatomic, assign) KuiklyRenderView *hr_rootView;

@end

NS_ASSUME_NONNULL_END
