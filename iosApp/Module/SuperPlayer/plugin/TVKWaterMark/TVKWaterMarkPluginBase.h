//
//  TVKWaterMarkPluginBase.h
//  SPPlayer
//
//  Created by haitend on 2019/10/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPMediaPlayInfo.h"
#import "TVKMediaPlayerPlugin.h"

NS_ASSUME_NONNULL_BEGIN

@interface TVKWaterMarkPluginBase : NSObject
/** 插件回调 */
@property (nonatomic, weak) id<TVKMediaPlayerPluginDelegate> delegate;
/** pluginId. 用于唯一标识此plugin */
@property (nonatomic, assign) TVKMediaPlayerPluginViewId pluginId; /** 插件加载，插件可以在这个方法做一些初始化的事情 */

- (void)load;
/** 插件卸载，插件可以在这个方法做一些反初始化的事情 */
- (void)unLoad;
/** 拉伸模式改变 */
- (void)onStretchModeChanged:(SPVideoStretchMode)stretchMode;
/** view 宽高改变 */
- (void)onViewSizeChanged:(CGSize)viewSize;
/** 播放器位置信息,动态水印使用 */
- (void)onPlayerPositionUpdated:(NSTimeInterval)playerPosition;
/** 视频开始播放 动态水印需要开始播放的绝对时间 */
- (void)onPlayStart:(NSTimeInterval)startTime;

@end

NS_ASSUME_NONNULL_END
