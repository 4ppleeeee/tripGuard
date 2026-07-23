/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPVODPlayInfo.h
 * @brief    点播播放信息
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPMediaPlayInfo.h"
#import "SPVODExtraInfo.h"
#import "SPDrmModel.h"

@interface SPVODPlayInfo : SPMediaPlayInfo

@property (nonatomic, assign) int exem;                                     // 用于判断是限播还是杜比试看。(历史原因，丑陋的实现)

@property (nonatomic, strong) SPDrmModel *drmModel;                        // drm加密的信息

@property (nonatomic, assign) NSTimeInterval startPosition;                 // 播放的起始时间点

@property (nonatomic, assign) NSTimeInterval skipEndPosition;               // 播放跳过片尾时间点

@property (nonatomic, assign) NSTimeInterval duration;                      // 视频长度，节点root.vl.vi.td

@property (nonatomic, assign) int chargeState;                              // 付费状态, 0:未检查, 1:单片付费, 2:包月付费, -1:未付费, -2:未登录，节点root.vl.vi.ch

@property (nonatomic, assign) NSTimeInterval vodPreviewStart;               // 点播试看开始时间(中间试看开始时长，存在且不为0，则为中间试看，可以认为只能播startpreview：startpreview+preview这个范围的内容)

@property (nonatomic, assign) NSTimeInterval vodPreViewTime;                // 点播试看时长

@property (nonatomic, assign, readonly) NSTimeInterval vodPreViewEnd;       // 点播试看结束时长（终点）

/**
 * 取值0, 1, 2, 3, 5, 6, 130, 131, 133, 134
 * 注意：当视频状态为2时, 视频可以播放, 其他状态都不可播放；视频状态8为收费状态
 */
@property (nonatomic, assign) int videoState;

@property (atomic, assign) NSInteger dltype;                                // 视频下载类型，1：http，2：p2p 3:HLS 8:m3u8直出

@property (nonatomic, copy) NSString *link;                                 // 节点root.vl.vi.lnk

@property (nonatomic, assign) int clipCount;                                // 分片数，节点root.vl.vi.cl.fc

@property (nonatomic, assign) int rate;                                     // 首次加载对应的音视频码率(单位:kByte/s) 用作上报, 节点root.vl.vi.br

@property (nonatomic, assign) int fp2p;                                     // 客户端是否开启p2p，空或0不开启，2开启

@property (nonatomic, assign) int mediaState;                               // 媒资付费状态，仅在秒播时使用

@property (atomic, assign) int videoType;                                   // 仅用于数据上报

@property (nonatomic, assign) float aspectRatio;                            // 宽高比

@property (nonatomic, copy) NSString *m3u8;                                 // m3u8字符串，用于m3u8直出

@property (nonatomic, assign) int64_t fileSize;                             // 视频文件大小

@property (nonatomic, assign) BOOL fVideo;  //是否是快发版本视频，快发版本视频是非完整版视频

@property (nonatomic, strong) SPVODExtraInfo *extraInfo;                   // 一些扩展信息，主要用于数据上报等

@end

