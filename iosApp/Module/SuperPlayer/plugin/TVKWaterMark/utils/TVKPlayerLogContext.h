/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     TVKPlayerLogContext.h
 * @brief    生成统一的日志打印规范Tag
 * @author   andygao
 * @version  1.0.0
 * @date     2019/10/28
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXPORT NSString *const gTVKPlayerDefaultTagPrefix;
FOUNDATION_EXPORT NSString *const gTVKPlayerDefaultModuleName;
FOUNDATION_EXPORT NSString *const gTVKPlayerModeNameWrapper;
FOUNDATION_EXPORT NSString *const gTVKPlayerModeNameCGI;
FOUNDATION_EXPORT NSString *const gTVKPlayerReportModeName;
FOUNDATION_EXPORT NSString *const gTVKPlayerModeNameRichMedia;
FOUNDATION_EXPORT NSString *const gTVKPlayerEventsPluginName;

FOUNDATION_EXPORT int const gTVKPlayerDefaultLifeId;
FOUNDATION_EXPORT int const gTVKPlayerDefaultPlayId;

@interface TVKPlayerLogContext : NSObject<NSCopying>

/**播放器分层实现类的对象ID，外部只读*/
@property (assign, nonatomic, readonly) int lifeId;

/**播放器播放任务的ID，以open为准叠加，外部只读*/
@property (assign, nonatomic, readonly) int playId;

/**统一的日志前缀*/
@property (strong, nonatomic, nonnull) NSString *prefix;

/**自己从属的model名称，模块的自己生成*/
@property (strong, nonatomic, nonnull) NSString *modelName;

/**对应的完整Tag，内部生成，外部只读*/
@property (strong, nonatomic, readonly, nonnull) NSString *fullTag;

/**
 * 默认的构造方法，屏蔽调用，因为已经提供静态初始化方法
 */
- (instancetype)init NS_UNAVAILABLE;

/**
 * 静态实例方法
 * 每次调用，lifeId递增
 * 每次调用，playId采用最后一次值
 * 每次调用，prefix采用default prefix
 * 每次调用，moduleName采用defaultModuleName
 */
+ (instancetype)logContextWithDefaults;

/**
 * 静态实例方法，传入moduleName的构造方法
 * 每次调用，lifeId递增
 * 每次调用，playId采用最后一次值
 * 每次调用，prefix采用default prefix
*/
+ (instancetype)logContextWithModuleName:(nonnull NSString *)moduleName;
/**
 * 静态实例方法，外部传递所有参数
 * @param prefix  前缀
 * @param lifeId  实例Id
 * @param playId  播放序列Id
 * @param moduleName 模块名称
 */
+ (instancetype)logContextWithPrefix:(nonnull NSString *)prefix lifeId:(int)lifeId playId:(int)playId moduleName:(nonnull NSString *)moduleName;

/**
 * 工具类方法，直接不构造对象，生成TAG
 * @param prefix 前缀
 * @param lifeId 实例Id
 * @param playId 播放序列ID
 * @param moduleName 模块名称
 */
+ (nonnull NSString *)commonTagWithPrefix:(nonnull NSString *)prefix lifeId:(int)lifeId playId:(int)playId moduleName:(nonnull NSString *)moduleName;

/**
 * playId 递增，简化外部playId ++ 的操作
 */
- (void)increasePlayId;

@end

NS_ASSUME_NONNULL_END
