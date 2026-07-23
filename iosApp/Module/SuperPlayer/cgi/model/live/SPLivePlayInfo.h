/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPLivePlayInfo.h
 * @brief    直播播放所需信息，主要是由CGI返回的信息
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPMediaPlayInfo.h"
#import "SPLiveSeeBackBaseInfo.h"

@interface SPLivePlayInfo : SPMediaPlayInfo

@property (nonatomic, assign) NSInteger liveErroCode;                      // 直播错误码

// 直播相关
@property (nonatomic, assign) BOOL needPay;                                // 当前频道是否需要付费，0为不付费，1为付费

@property (nonatomic, assign) BOOL isUserPay;                              // 请求用户是否对频道付费，1表示已付费，0为未付费，该字段仅在needPlay字段为1时有效

/** 直播试看begin */
@property (nonatomic, assign) CGFloat livePlayTime;                        // 直播流的播放限时，单位秒，0为不限制

@property (nonatomic, assign) int livePreviewCount;                  // 如果节目支持试看，返回请求用户当天可试看总次数

@property (nonatomic, assign) int liveRestPreviewCount;              // 如果节目支持试看，返回请求用户当天剩余可试看次数

@property (nonatomic, assign) CGFloat livePreviewTime;                     // 直播流的每次可试看时长，单位秒，0为不支持
/** 直播试看end */

@property (nonatomic, strong) SPLiveSeeBackBaseInfo *seeBackBaseInfo;     // 直播回看信息

/** 直播排队begin */
@property (nonatomic, assign) int queueStatus;                        // 0:未排队，1:排队中，2:已出队

@property (nonatomic, assign) int64_t queueRank;                          // 如果排队中，返回排队的排名

@property (nonatomic, assign) int queueVipJump;                       // 开通会员后能否插队 0否 1是

@property (nonatomic, copy) NSString *queueSessionKey;                      // 排队的会员key,返回给app,轮询时使用
/** 直播排队end */

@property (nonatomic, assign) int stream;                                   // 1:flv, 2:hls

@property (nonatomic, assign) int live360;                                  // 是否为全景视频,0为非全景，1为全景

@property (nonatomic, assign) int acode;                                    // 1=aac，2=dolby（新增字段）

@property (nonatomic, assign) int vcode;                                    // 1=h264，2=hevc（新增字段）

@property (nonatomic, assign) int hlsp2p;                                   // 直播流是否开启p2p播放，0=未开启，1=开启

@property (nonatomic, copy) NSString *cdnName;                                   // cdn名称

@property (nonatomic, copy) NSString *defn;                                   // 清晰度名称（sd/hd等）

@end

