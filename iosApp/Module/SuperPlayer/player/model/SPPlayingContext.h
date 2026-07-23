/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPPlayingContext.h
 * @brief    播放上下文，用来指定在请求cgi时满足当前播放的一些参数
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

/*
 * 播放过程中对CGI请求的参数要求。比如播放发生错误，发生重试的时候，可能需要关掉某些功能，比如不请求HEVC。
 */
@interface SPPlayingContext : NSObject <NSCopying>

@property (nonatomic, assign) BOOL enableHEVC;  // 是否请求HEVC，默认为YES

@property (nonatomic, assign) BOOL enableFairPlay;  // 是否请求Fairplay，默认为YES

@property (nonatomic, copy) NSString *requiredDefinition;  // 当前这次播放所要求的清晰度，默认为nil

@property (nonatomic, assign) NSTimeInterval currentPlayPosition;  // 当前播放的时间点

@property (nonatomic, assign) SPMediaFormat requiredMediaFormat;  // 当前这次播放所要求的格式，默认SPMediaFormatAuto

@property (nonatomic, assign) int64_t liveSeebackTime;  // 直播回看的时间点，仅当请求类型为直播回看时填充该字段

@property (nonatomic, strong) id extraInfo;  // 扩展字段，外面传递，内部不理解。

@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *extraRequestParams;  // 当前这次播放的cgi请求希望携带的参数

/**
* 一些额外配置字段
* extraConfig 包含扩展配置的字典，key和value都是NSString类型!!!
*        key                          :  value
*        -------------------------------------------------
*        offline_sdtfrom        : 离线下载时的sdtfrom，离线下载合播放时的sdtfrom不同
*        is_dlna                    : 是否是DLNA，@"0"表示NO，@"1"则为YES
*        is_live_get_preview     : 是否是查询直播信息，@"0"表示NO，@"1"则为YES
*/
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *extraConfig;

- (void)copyFrom:(SPPlayingContext *)context;

@end
