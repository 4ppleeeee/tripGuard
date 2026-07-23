/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPPlayerLogContext.h
 * @brief    生成统一的日志打印规范Tag
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/10/28
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXPORT NSString *const gSPPlayerDefaultTagPrefix;
FOUNDATION_EXPORT NSString *const gSPPlayerDefaultModuleName;
FOUNDATION_EXPORT NSString *const gSPPlayerModeNameWrapper;
FOUNDATION_EXPORT NSString *const gSPPlayerReportModeName;

FOUNDATION_EXPORT int const gSPPlayerDefaultLifeId;
FOUNDATION_EXPORT int const gSPPlayerDefaultPlayId;

@interface SPPlayerLogContext : NSObject

/**播放器分层实现类的对象ID*/
@property (assign, nonatomic) int lifeId;

/**播放器播放任务的ID，以open为准叠加*/
@property (assign, nonatomic) int playId;

/**统一的日志前缀*/
@property (strong, nonatomic) NSString *tagPrefix;

/**自己从属的model名称，模块的自己生成*/
@property (copy, nonatomic) NSString *modelName;

@end

/**
*  统一的日志规范，格式prefix_Cid_Tid_model , 前缀_对象ID_播放任务ID_模块名称
*  范例 ：SP_C101_T1002_SPPlayerWrapper
*  本方法用于模块子节点的构造，因为model需要来源于模块父节点，，例如SPPlayerWrapperCGIModel，他是 wrapper 模块的子节点
*/
@interface SPPlayerLogContextUtil : NSObject

+ (NSString *)commonPlayerTag:(SPPlayerLogContext *)logContext;

+ (NSString *)commonPlayerTagWithTagPrefix:(NSString *)tagPrefix lifeId:(int)lifeId playId:(int)playId modelName:(NSString *)modelName;

@end

NS_ASSUME_NONNULL_END
