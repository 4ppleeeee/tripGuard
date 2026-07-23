//
//  QNKuiklyRenderBridge.m
//  iosApp
//
//  Created by tingdongli on 2025/1/7.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "QNKuiklyRenderBridge.h"
#import "QNKuiklyRender.h"
#import <KuiklyIOSRender/KuiklyRenderBridge.h>
#import "KBFontAwesome.h"
#import "KRRouterHandler.h"
#import <RaftKit/RaftKit.h>


@implementation QNKuiklyRenderBridge
/**
 * 初始化并设置Kuikly渲染桥接组件
 * 创建QNKuiklyRender实例并注册为组件扩展处理器
 */
- (void)setup {
    // 创建Kuikly渲染组件实例
    QNKuiklyRender *kuiklyRender = [[QNKuiklyRender alloc] init];
    // 注册组件扩展处理器
    [KuiklyRenderBridge registerComponentExpandHandler:kuiklyRender];
    // 注册 Kuikly 路由处理器
    [KRRouterHandler registerIfNeeded];
    // 加载iconfont字体
    [KBFontLoader loadFont];
}

- (void)becomeActive {
    if (![NSProcessInfo.processInfo.environment[@"KMM_SHOW_RAFTKIT"] isEqualToString:@"1"]) {
        return;
    }
    // 显示工具的悬浮球入口（悬浮球可以在RaftKit面板->设置中隐藏）
    [RFKTManager install];
    // 注意：为避免用户手动关闭悬浮球后无法再次打开，可在适当固定入口调用-[RFKTManager showFloatingBall]接口来重新显示。
    // 或使用下述手势触发方式来显示悬浮球。

    //【可选】设置工具的其他触发方式，包括摇一摇，双指双击，双指长按。
    // 你也可以通过RFKTTapTrigger、RFKTLongPressTrigger的初始化方法设置点击次数、长按时间。
    [RFKTManager.sharedInstance setupWithTriggers: @[[RFKTShakeTrigger trigger], [RFKTTapTrigger trigger], [RFKTLongPressTrigger trigger]]];
}

@end
