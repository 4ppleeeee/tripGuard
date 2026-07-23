//
//  SPPlayerWrapperPlayerStrategy.h
//  SPPlayer
//
//  Created by 郭力 on 2019/10/19.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPMediaPlayInfo.h"
#import "SPPlayerWrapperDefine.h"
#import <Foundation/Foundation.h>
#import <ThumbPlayer/TPOptionalID.h>
#import <ThumbPlayer/TPPlayerCoreType.h>

@class SPPlayerWrapperInfo;

NS_ASSUME_NONNULL_BEGIN

@interface SPPlayerWrapperPlayerStrategy : NSObject

/**
 * 播放器的选择策略 - （根据多媒体信息来选择)
 * @param playInfo 媒体信息
 */
+ (NSArray<NSNumber *> *)playerTypeListForPlayInfo:(SPMediaPlayInfo *)playInfo withTag:(NSString *)tag;

/**
 * 播放器的选择策略 - (根据wrapper层的播放场景来选择)
 * @param scene 播放场景
 */
+ (NSArray<NSNumber *> *)playerTypeListForPlayScene:(SPPlayerWrapperScene)scene withTag:(NSString *)tag;



/**
 *  切换清晰度的策略选择
 */
+ (SPVideoSwitchDefinitionType)switchTypeStrategyForMediaInfo:(nullable SPMediaInfo *)mediaInfo
                                                 andExtralInfo:(nullable NSDictionary *)extralInfo
                                            andCurrentPlayInfo:(nullable SPMediaPlayInfo *)playInfo
                                               andCurrentState:(SPPlayerWrapperState)state
                                                       withTag:(nullable NSString *)tag;

/**
 * 开启下载组件的策略
*/
+ (BOOL)proxyEnableStrategyForWrapperInfo:(SPPlayerWrapperInfo *)wrapperInfo;

@end

NS_ASSUME_NONNULL_END
