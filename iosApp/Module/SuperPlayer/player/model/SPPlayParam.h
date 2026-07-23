/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPPlayParam.h
 * @brief    播放参数，主要用来请求cgi
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "SPMediaInfo.h"
#import "SPPlayingContext.h"

NS_ASSUME_NONNULL_BEGIN

/** cgi请求类型 */
typedef NS_ENUM(NSUInteger, SPCGIRequestType) {
    SPCGIRequestTypeNormal                 = 0,  // 正常播放cgi请求
    SPCGIRequestTypeSwitchDefnSeamless     = 1,  // 无缝切换清晰度
    SPCGIRequestTypeSwitchDefnReOpen       = 2,  // 非无缝切换清晰度
    SPCGIRequestTypeErrorRetry             = 5,  // 错误
    SPCGIRequestTypeLiveSeekBack           = 6,  // 直播回看
    SPCGIRequestTypeVKeyExpire             = 7,  // VKEY过期
    SPCGIRequestTypePIP                    = 8,  // 画中画
    SPCGIRequestTypeOfflineDownload        = 9,  // 离线下载
    SPCGIRequestTypeNoMoreData             = 11, // NoMoreData，CGI不关心
    SPCGIRequestTypeRefreshPlayer          = 12, // 刷新播放器
    SPCGIRequestTypeURLGetter              = 13, // URL直出
};

/** 播放所需参数 */
@interface SPPlayParam : NSObject

@property (nonatomic, assign) int playSeq;  //当前这次播放的sequcence

@property (nonatomic, strong) NSString *flowID;

@property (nonatomic, assign) SPCGIRequestType requestType;  // 请求类型

@property (nonatomic, strong) SPMediaInfo *mediaInfo;  // 外面传入的媒体信息

@property (nonatomic, strong) SPPlayingContext *playContext; // 播放过程中要求的一些参数，非必须，可以为nil


@end

NS_ASSUME_NONNULL_END
