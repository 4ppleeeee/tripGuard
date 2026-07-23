/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     TVKPlayerLogContext.m
 * @brief    生成统一的日志打印规范Tag
 * @author   andygao
 * @version  1.0.0
 * @date     2019/10/28
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "TVKPlayerLogContext.h"

NSString *const gTVKPlayerDefaultTagPrefix = @"TVKPlayer";
NSString *const gTVKPlayerDefaultModuleName = @"TVKPlayerManager";
NSString *const gTVKPlayerModeNameWrapper = @"TVKPlayerWrapper";
NSString *const gTVKPlayerModeNameCGI = @"TVKPlayerWrapper_CGI";
NSString *const gTVKPlayerModeNameRichMedia = @"TVKRichMedia";
NSString *const gTVKPlayerReportModeName = @"TVKReport";
NSString *const gTVKPlayerEventsPluginName = @"TVKPlayerEvents";
NSString *const gTVKPlayerModelNameRichMedia = @"TVKRichMedia";

/**life id 基准*/
int const gTVKPlayerDefaultLifeId = 1000;

/**play id 基准*/
int const gTVKPlayerDefaultPlayId = 10000;

@interface TVKPlayerLogContext ()

/**对象类id，再次声明，修改内部读写权限*/
@property (assign, nonatomic) int lifeId;

/**播放任务id，再次声明，修改内部读写权限*/
@property (assign, nonatomic) int playId;

/**对应完整的TAG，再次声明，修改内部读写权限*/
@property (strong, nonatomic, nonnull) NSString *fullTag;

@end

/**全局静态递增的基准lifeId*/
static int gTVKPlayerBaseLifeId = gTVKPlayerDefaultLifeId;


@implementation TVKPlayerLogContext

+ (instancetype)logContextWithDefaults {
    return [TVKPlayerLogContext logContextWithModuleName:gTVKPlayerDefaultModuleName];
}

+ (instancetype)logContextWithModuleName:(NSString *)moduleName {
    TVKPlayerLogContext *context = [[TVKPlayerLogContext alloc] init];
    context.lifeId = gTVKPlayerBaseLifeId ++;
    context.playId = gTVKPlayerDefaultPlayId;
    context.prefix = gTVKPlayerDefaultTagPrefix;
    context.modelName = moduleName;
    context.fullTag = [context rebuildFullTag];
    return context;
}

+ (instancetype)logContextWithPrefix:(NSString *)prefix lifeId:(int)lifeId playId:(int)playId moduleName:(NSString *)moduleName {
    TVKPlayerLogContext *context = [[TVKPlayerLogContext alloc] init];
    context.prefix = prefix;
    context.lifeId = lifeId;
    context.playId = playId;
    context.modelName = moduleName;
    context.fullTag = [context rebuildFullTag];
    return context;
}

+ (NSString *)commonTagWithPrefix:(NSString *)prefix lifeId:(int)lifeId playId:(int)playId moduleName:(NSString *)moduleName {
    return [NSString stringWithFormat:@"%@_C%d_T%d_%@", prefix, lifeId, playId, moduleName];
}

- (void)increasePlayId {
    _playId ++;
    self.fullTag = [self rebuildFullTag];
}

- (void)setPrefix:(NSString *)prefix {
    _prefix = prefix;
    self.fullTag = [self rebuildFullTag];
}

- (void)setModelName:(NSString *)modelName {
    _modelName = modelName;
    self.fullTag = [self rebuildFullTag];
}

- (NSString *)rebuildFullTag {
    return [NSString stringWithFormat:@"%@_C%d_T%d_%@", self.prefix, self.lifeId, self.playId, self.modelName];
}

- (id)copyWithZone:(NSZone *)zone {
    TVKPlayerLogContext *context = [[TVKPlayerLogContext allocWithZone:zone] init];
    context.prefix = [self.prefix mutableCopyWithZone:zone];
    context.modelName = [self.modelName mutableCopyWithZone:zone];
    context.lifeId = self.lifeId;
    context.playId = self.playId;
    context.fullTag = [self.fullTag mutableCopyWithZone:zone];
    return context;
}

@end

