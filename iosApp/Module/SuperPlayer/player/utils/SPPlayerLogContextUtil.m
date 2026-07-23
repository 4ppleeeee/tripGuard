/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPPlayerLogContext.m
 * @brief    生成统一的日志打印规范Tag
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/10/28
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPPlayerLogContextUtil.h"

NSString *const gSPPlayerDefaultTagPrefix = @"SPPlayer";
NSString *const gSPPlayerDefaultModuleName = @"SPMediaPlayer";
NSString *const gSPPlayerModeNameWrapper = @"SPPlayerWrapper";
NSString *const gSPPlayerReportModeName = @"SPReport";

int const gSPPlayerDefaultLifeId = 0;
int const gSPPlayerDefaultPlayId = 0;

@implementation SPPlayerLogContext

@end

@implementation SPPlayerLogContextUtil

+ (NSString *)commonPlayerTag:(SPPlayerLogContext *)logContext {
    if (!logContext) {
        return [NSString stringWithFormat:@"%@_C%d_T%d_%@", gSPPlayerDefaultTagPrefix, gSPPlayerDefaultLifeId,
                                                            gSPPlayerDefaultPlayId, gSPPlayerDefaultModuleName];
    }
    return [NSString stringWithFormat:@"%@_C%d_T%d_%@", logContext.tagPrefix, logContext.lifeId,
                                                        logContext.playId, logContext.modelName];
}

+ (NSString *)commonPlayerTagWithTagPrefix:(NSString *)tagPrefix
                                    lifeId:(int)lifeId
                                    playId:(int)playId
                                 modelName:(NSString *)modelName {
    return [NSString stringWithFormat:@"%@_C%d_T%d_%@", tagPrefix, lifeId, playId, modelName];
}

@end
