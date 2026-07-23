/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetVideoInfo.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/1/6
 Description : 后台返回的媒体信息，
 History     : 17/1/6 初始版本
 ***********************************************************/

#import "SPPlayerDefine.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/* --------------------后台返回的清晰度信息-------------------- */
@interface SPNetMediaDefinitionInfo : NSObject

/**
 * @brief 视频清晰度名字，比如sd，hd等，见SPPlayerDefine.h
 */
@property (nonatomic, copy, nonnull) NSString *definition;

/**
 * @brief 清晰度显示完整文案，以分号和括号作为分隔符将清晰度名字和分辨率等隔开，调用方可根据自己需要处理。格式如下：
 * 标清;(270P)
 * 高清;(480P)
 * 超清;(720P)
 * 蓝光;(1080P VIP尊享)
 * HDR臻彩视界;(VIP尊享)
 * 杜比视听;(VIP尊享)
 */
@property (nonatomic, copy) NSString *fullText;

/**
 * @brief 分辨率文本，比如：270P、480P、720P、1080P、HDR
 */
@property (nonatomic, copy) NSString *resolutionText;

/**
 * @brief 清晰度显示的短格式名称
 */
@property (nonatomic, copy, nonnull) NSString *definitionShowShortName;

/**
 @brief 当前清晰度格式是否需要会员才能观看
 */
@property (nonatomic, assign) BOOL isNeedVip;

/**
 * @brief 当前清晰度格式对应的整体文件大小，单位字节
 */
@property (nonatomic, assign) UInt64 fileSize;

/**
 * @brief 当前清晰度视频编码格式，1:H264, 2:H265, 3:HDR10, 4:DolbyVision
 */
@property (nonatomic, assign) int videoCodec;

/**
 * @brief 当前清晰度音频编码格式，1:AAC, 2:Dolby Surround, 3:Dolby Atmos, 4:Dobly 2.0
 */
@property (nonatomic, assign) int audioCodec;

/**
 * @brief 当前清晰度会进行哪些后处理，bitset形式，0x1:超分
 */
@property (nonatomic, assign) int postProcess;

@end

/* --------------------seek的预览截图信息-------------------- */
// 请参考wiki：http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481005866113
@interface SPNetThumbInfo : NSObject

/**
 * @brief 截图的宽度
 */
@property (nonatomic, assign) CGFloat width;

/**
 * @brief 截图的高度
 */
@property (nonatomic, assign) CGFloat height;

/**
 * @brief 一张物理截图每行有几张小图
 */
@property (nonatomic, assign) NSInteger column;

/**
 * @brief 一张物理截图有多少行
 */
@property (nonatomic, assign) NSInteger row;

/**
 * @brief 截图与截图之间的时间间隔，单位秒
 */
@property (nonatomic, assign) NSTimeInterval interval;

/**
 * @brief 截图下载地址的前缀
 */
@property (nonatomic, copy, nonnull) NSString *urlPrefix;

/**
 * @brief 截图格式对应的（部分）文件名
 */
@property (nonatomic, copy, nonnull) NSString *fileName;

@end

/* --------------------后台返回的直播回看信息 -------------------- */
@interface SPNetLiveSeebackInfo : NSObject

/**
 @brief 回看开始时间
 */
@property (nonatomic, assign) NSUInteger seebackStartTime;

/**
 @brief 最大回看时长
 */
@property (nonatomic, assign) NSUInteger maxSeebackTime;

/**
 @brief 服务器当前时间
 */
@property (nonatomic, assign) NSUInteger serverTime;

/**
 @brief 当前这次播放是否是回看
 */
@property (nonatomic, assign) BOOL isSeebackState;

@end

/* --------------------直播试看信息 -------------------- */
@interface SPNetLivePreviewInfo : NSObject

/**
 * @brief 直播流的播放限时，单位秒，0为不限制
 */
@property (nonatomic, assign) NSTimeInterval playTime;

/**
 * @brief 直播流的每次试看时长，单位秒，0为不支持
 */
@property (nonatomic, assign) NSTimeInterval previewTime;

/**
 * @brief 当天可以试看的总次数
 */
@property (nonatomic, assign) NSInteger previewCount;

/**
 * @brief 当天剩余可试看次数
 */
@property (nonatomic, assign) NSInteger restPreviewCount;

@end

/* --------------------后台返回的直播排队信息 -------------------- */
@interface SPLiveQueueInfo : NSObject

/**
 * @brief 是否排队，0:未排队, 1.排队中, 2.已出队
 */
@property (nonatomic, assign) int queue_status;

/**
 * @brief 排队的排名信息
 */
@property (nonatomic, assign) int queue_rank;

/**
 * @brief 开通会员是否可以插队, 0:NO, 1:YES
 */
@property (nonatomic, assign) int queue_vip_jump;

/**
 * @brief 排队的会员key， 轮询时使用
 */
@property (nonatomic, copy, nonnull) NSString *queue_session_key;

@end

/* --------------------后台返回的正片视频信息，包括当前清晰度，清晰度列表等信息-------------------- */
@interface SPNetVideoInfo : NSObject

/**
 *  @brief 视频id.用于某些场景下，比如秒播，外面没有videoId的情况，此时需要使用这里返回的videoId
 */
@property (nonatomic, copy, nonnull) NSString *videoId;

/**
 *  @brief 播放类型，详见SPPlayType定义
 */
@property (nonatomic, assign) SPPlayType playType;

/**
 *  @brief 视频时长，单位为秒
 */
@property (nonatomic, assign) NSTimeInterval duration;

/**
 *  @brief 当前视频播放的清晰度信息
 */
@property (nonatomic, strong, nonnull) SPNetMediaDefinitionInfo *currentDefinition;

/**
 *  @brief 当前视频的清晰度列表信息
 */
@property (nonatomic, strong, nonnull) NSArray<SPNetMediaDefinitionInfo *> *definitionList;

/**
 * @brief 付费状态
 * -2 用户没有登录
 * -1 视频状态非法
 * 0  无需付费/检查没付费
 * 1  单片已付费
 * 2  会员（包月）已付费
 */
@property (nonatomic, assign) NSInteger chargeState;

/**
 * @brief 视频状态
 * 跟用户付费状态有关，表示该用户对该视频的播放权限
 * 取值0, 1, 2, 3, 5, 6, 130, 131, 133, 134
 * 注意：当视频状态为2时, 视频可以播放, 其他状态都不可播放；视频状态8为收费状态
 */
@property (nonatomic, assign) int state;

/**
 * @brief 媒资付费状态(仅在秒播时使用)
 */
@property (nonatomic, assign) int mediaState;

/**
 * @brief 视频“链接”（即两个视频内容相同但是vid不一样, 当没有链接时, link的值为vid本身）
 */
@property (nonatomic, copy, nonnull) NSString *lnk;

/**
 * @brief 点播试看时长
 */
@property (nonatomic, assign) NSTimeInterval vodPreviewTime;

/**
 * @brief 点播试看起点（起点加上面的时长等于试看终点）
 */
@property (nonatomic, assign) NSTimeInterval vodPreviewStart;

/**
 * @brief 点播试看终点
 */
@property (nonatomic, assign, readonly) NSTimeInterval vodPreViewEnd;

/**
 * @brief 当前视频是否需要付费
 */
@property (nonatomic, assign) BOOL needPay;

/**
 * @brief 用户是否已经付过费
 */
@property (nonatomic, assign) BOOL isPay;

/**
 * @brief 视频宽高比，
 */
@property (nonatomic, assign) float aspectRation;

/**
 * @brief 后台返回的媒体格式
 */
@property (nonatomic, assign) SPMediaFormat mediaFormat;

/**
 * @brief 是否是VR视频
 */
@property (nonatomic, assign) BOOL isVR;

/**
 * @brief 是否是快发版本视频，快发版本视频是非完整版视频
 */
@property (nonatomic, assign) BOOL fVideo;

/**
 * @brief 直播排队
 */
@property (nonatomic, strong, nullable) SPLiveQueueInfo *liveQueueInfo;

/**
 * @brief 直播试看信息
 */
@property (nonatomic, strong, nullable) SPNetLivePreviewInfo *livePreviewInfo;

/**
 * @brief 直播回看信息
 */
@property (nonatomic, strong, nullable) SPNetLiveSeebackInfo *seebackInfo;

/**
 @brief 用户主动切换的清晰度
 */
@property (nonatomic, assign) BOOL isUserSwithDefition;

/**
 截屏/录屏方式
 0: 使用 app 逻辑判断
 1: 不可截屏/录屏
 2: 系统不可但 app 可截屏/录屏
 3: 系统和 app 均可截屏/录屏
*/
@property (nonatomic, assign) NSInteger sshot;

/**
 截图方式 0: 播放器截图，1: 后台截图
*/
@property (nonatomic, assign) NSInteger mshot;


/**
 @brief 视频地址列表
 */
@property (nonatomic, copy) NSArray<NSURL *> *videoUrlArray;

/**
 @brief 分片时长列表，和videoUrlArray中的地址一一对应
 */
@property (nonatomic, copy) NSArray<NSNumber *> *videoTimeArray;

/**
 @brief flowId唯一标识一次播放，主要用于数据上报
 */
@property (nonatomic, copy) NSString *flowId;

/**
 @brief 当前播放所用的cdn url
 */
@property (nonatomic, copy) NSString *cdnPlayUrl;

/**
 @brief 当前播放所用的cdnId
 */
@property (nonatomic, copy) NSString *cdnId;

/**
 @brief 是否走了代理组件
 */
@property (nonatomic, assign) BOOL isP2PPlayMode;

/**
 @brief 是否走了代理组件的离线播放
 */
@property (nonatomic, assign) BOOL isP2POfflinePlay;

/**
 drm类型，详情见TVKDrmEncryptionType
 */
@property (nonatomic, assign) int drm;


@end

NS_ASSUME_NONNULL_END
