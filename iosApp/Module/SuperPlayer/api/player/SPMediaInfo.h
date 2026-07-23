/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPMediaInfo.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/1/6
 Description :视频信息描述
 History     : 17/1/6 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

/**
 * SPMediaInfo由外部传入，包含播放必须的字段。
 */
@interface SPMediaInfo : NSObject

/**
 * @brief 视频id.点播时为视频vid,直播时为视频流id(sid, channelid), 必填字断
 */
@property (nonatomic, copy) NSString *videoId;

/**
 * @brief 专辑ID.点播时为视频专辑id(cid),直播时为pid，必填字断，如果没有则用videoId填充
 */
@property (nonatomic, copy) NSString *coverId;

/**
 * @brief 栏目ID(lid).非必填字断，如果没有则无须设置
 */
@property (nonatomic, copy) NSString *columnId;

/**
 * @brief 播放类型，定义见SPPlayType
 */
@property (nonatomic, assign) SPPlayType playType;

/**
 * @brief 腾讯视频源换链时使用，视频播放清晰度。定义见SPPlayerDefine.h，清晰度由后台控制
 */
@property (nonatomic, copy) NSString *definition;

/**
 * @brief
 * 视频流类型类型，定义见SPMediaFormat，腾讯视频源换链类型或指定视频下载格式
 */
@property (nonatomic, assign) SPMediaFormat mediaFormat;

/**
 * @brief 平台号（业务号），用于区分业务，同一个业务区分安卓 和 IOS
 */
@property (nonatomic, copy) NSString *platform;

/**
 * @brief 平台号对应的dtfrom，播放来源，运维统计流量用
*/
@property (nonatomic, copy) NSString *sdtfrom;

/**
 * @brief 下载组件业务号（QQ专用，默认不赋值，对下载SDK单独设置的业务建议使用(SUPERPLAYER_SDK_PLATFROM * 1000 + scenesId)）
 */
@property (nonatomic, assign) int serviceType;

/**
 * @brief 业务场景ID，区分同一个平台号不同场景，需在100～200内
 */
@property (nonatomic, assign) NSInteger scenesId;

/**
 * @brief 播放的起始点，单位s.默认值为0
 */
@property (nonatomic, assign) NSTimeInterval startPosition;

/**
 * @brief 校准标记(内部使用)，是否校准过startPosition（因为中间起播后，startPosition在非seek或自然播放下，要强制校准到vodPreviewStart）
 */
@property (nonatomic, assign) NSTimeInterval haveResetStartPosition;

/**
 * @brief 跳过结尾skipEndPosition时长，单位s.例如电视剧尾部有2分钟需要跳过，则值为2*60.默认值为0
 */
@property (nonatomic, assign) NSTimeInterval skipEndPosition;

/**
 * @brief 视频时长，预加载时使用，影响下载sdk计算视频码率
*/
@property (nonatomic, assign) NSTimeInterval duration;

/**
 * @brief 是否预加载过，用于上报
 */
@property (nonatomic, assign) BOOL preloaded;

/**
 * @brief 是否是付费视频
 */
@property (nonatomic, assign) BOOL isNeedCharge;

/**
 * @brief 是否打开音频帧回调，默认否
 */
@property (nonatomic, assign) BOOL enableAudioFrameCallback;

/**
 * @brief 是否打开视频帧回调，默认否
*/
@property (nonatomic, assign) BOOL enableVideoFrameCallback;

/**
 * @brief 设置输出的音频采样率
 */
@property (nonatomic, assign) NSInteger audioFrameOutSampleRate;

/**
 * @brief 设置输出的音频声道布局
 */
@property (nonatomic, assign) NSInteger audioFrameOutChannelLayout;

/**
 * @brief 播放过程中缓存的数据时长，单位ms
 */
@property (nonatomic, assign) NSInteger bufferPacketMinTotalDurationMs;

/**
 * @brief 首次播放需要加载的数据时长，单位ms
 */
@property (nonatomic, assign) NSInteger preloadPacketTotalDurationMs;

/**
 * @brief 缓冲时（缓冲数据不够引起的二次缓冲，或seek引起的拖动缓冲）最少要缓存多长时间的数据才能结束缓冲，单位ms
 */
@property (nonatomic, assign) NSInteger minBufferingPacketDurationMs;

/**
 * @brief 是否是Drm，默认NO
 */
@property (nonatomic, assign) BOOL isDrm;

/**
 * @brief 源内容ID，短带长的短视频ID(feedid或短vid)
 */
@property (nonatomic, copy) NSString *srccontenid;
/**
 * @brief 数据上报使用.
 * @discussion（注:飞天上报的进入详情页上报的key，请设置为feiTianDetailPageInfo,value为NSDictionary,value的格式如下
 *----------------
 *key      | value
 *----------------
 *stime    | （进入详情页时间点(单位:ms, 格林威冶时间)）
 *----------
 *code     | （错误码）
 *----------------
 * 注意：由于内部使用的上报组件（默认是odk）,对上报格式和类型有一定的要求，所以要确保上报的格式和字段类型等要符合上报的要求，否则可能会导致上报丢失。
 * 详见odk的上报要求：http://tapd.oa.com/webboss/markdown_wikis/#1010019311007970485
 * 本类的isValidReportInfoMap是对上报的基本检验。如果校验失败，则肯定不会设置给odk，这部分数据就会丢失。校验通过，则设置给odk，由odk进行上报。
 * odk本身没有提供校验函数，isValidReportInfoMap只是做了基本的校验，是否能被odk正确处理，请尽量参考odk的上报要求。
 */
@property (nonatomic, strong) NSDictionary<NSString *, NSObject *> *reportInfoMap;

/**
 * @brief 视频请求的额外信息，用来适配一些小渠道的额外信息以及其他需要携带的扩展字断
 * extraRequestParamsMap 一个包含j扩展参数的字典，key和value都是NSString类型!!!
 *        key                :  value
 *        ----------------------------------
 *        defnsrc            : 清晰度来源
 *        incver             : app版本号
 *        previd             : previd，秒播时传入
 *        fhdswitch          : 付费清晰度降档开关，若为0，表示非用户指定付费清晰度，后台会做降档逻辑，若为1,表示用户主动选择了付费清晰度
 */
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *extraRequestParamsMap;

/**
 * @brief 一些额外配置字段
 * configMap 包含扩展配置的字典，key和value都是NSString类型!!!
 *        key                 :  value
 *        -------------------------------------------------
 *        airplay_min_defn    : airplay最低清晰度
 *        history_vid         : 历史记录videoId，秒播时传入
 *        enable_quick_play   : 秒播开关, 1:开启，0:关闭
 *        auto_reduce_definition: 自动降清晰度开关，1:打开，0:关闭。如果不传，默认打开
 *        skip_start_and_end  : 是否跳过片头, 1:跳过，0:不跳过(仅秒播用)
 *        live_type           : 1:直播答题
 *        force_online        : 用于离线下载的视频，0:不强制走在线，1:强制走在线
 *        see_back_time       : 直播回看时间，仅直播用, -1:正常直播，> 0:直播回看时间
 *        adaptive_type       : 自适应码率开关,若为1,则会根据网络情况自动切换清晰度
 *        is_airplay          : 本次播放是否为airplay,播放器内部会选择合适的播放器，1: 当前为airplay播放
 *        is_offline_airplay  : 表示离线视频是否走有线投射等场景，会使用系统AVQueuePlayer。1: 是，0:不是
 */
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *configMap;

/**
 * @brief 离线下载播放信息
 * @discussion 仅仅playType为SPPlayTypeDidDownLoadVod(腾讯视频原生完整下载后播放)和SPPlayTypeWillDownLoadVod(腾讯视频原生边下载边播放)设置生效.
 *   downloadVodInfo 格式如下
 *        ----------------
 *        key      | value
 *        ----------------
 *        duration | （总时长）
 *        ----------
 *        count    | （总的分片地址数量）
 *        ----------------
 *        index(分片索引)| （具体分片信息字典（clipInfo））
 *        ---------------
 *
 *        具体分片信息字典(clipInfo)格式：
 *        ----------------
 *        key     | value
 *        ----------------
 *        url     | 此分片地址
 *        ----------------
 *        clipDuration | 此分片时长
 *        -------------------
 */
@property (nonatomic, copy) NSDictionary *downloadVodInfo;

/**
 * @brief 用于联通大王卡、移动、电信免流参数的传递.各免流参数传递说明如下：
 * 联通免流
 * 参数：
 * 键    ：  值
 * unicom：联通免流参数（字符串）
 * unicomtype：联通免流类型（数字）
 * 联通名流类型unicomtype：
 * 值    含义
 * 0    普通免流订购业务(默认)
 * 1    小王卡免流订购业务
 * 2    大王卡免流订购业务
 *
 * 电信免流
 * 键    :   值
 * telcom：电信免流参数 (字符串）
 *
 * 移动免流
 * 键  :   值
 * cmcc：移动免流参数(字符串）
 */
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *freeFlowParam;

/**
 * @brief 免流状态
 */
@property (nonatomic, assign) SPFreeFlowType freeFlowType;

/**
 * @brief 免流状态是否已经同步到服务器的免流状态. 0为未同步, 1为已同步
 */
@property (nonatomic, assign) int freeFlowSynBackEndState;

/**
 * @brief 是否时feeds流视频
 */
@property (nonatomic, assign) BOOL isFeedVideo;

/**
 * @brief 需要播放的外部播放地址，仅仅在playType为SPPlayTypeLocalFile或者SPPlayTypeExternalUrl时设置有效.
 */
@property (nonatomic, copy) NSString *url;

/**
 * @brief 播放缓存文件id，仅仅在playType为SPPlayTypeExternalUrl时设置有效.
 */
@property (nonatomic, copy) NSString *fileId;

/**
 * @brief 业务层可以指定单个视频的存储路径，注意，必须是绝对路径并且目录必须保证存在.
 */
@property (nonatomic, copy) NSString *savePath;

/**
 * @brief optional. 数据上报中使用，用于区分同一个平台号下不同业务场景
 * 0：默认 1：腾讯视频app内游戏场景（后续如果新增依次递增）
 */
@property (nonatomic, assign) int bizId;

/**
 * @brief 指定播放器类型，数值参考TPPlayerDefines中TPPlayerType
*/
@property (nonatomic, copy) NSArray<NSNumber *> *playerTypeList;

/**
 * @brief 是否使用下载代理，YES：使用，NO：不使用，不设置时走默认逻辑
 * 以下情况例外，否则功能异常
 * 1.air play不使用
 * 2.rtmp不使用
 * 3.直播回看不使用下载组件
 * 4.离线播放，不管任何场景，都得开启下载组件
 * 5.边下边播，不管任何场景，都得开启下载组件
 */
@property (nonatomic, strong) NSNumber *useDownloadProxy;

/**
 * @brief 是否使用防盗链缓存，只对playType为SPPlayTypeOnlineVod和SPPlayTypeOnlineLive时生效，默认为YES
 */
@property (nonatomic, assign) BOOL useVInfoGetterCache;

/**
 * @brief 扩展参数，后续新增参数可添加到此，key可直接与p2p组件key值对应，参考TPDownloadProxyDLParamKey
 */
@property(nonatomic, copy) NSDictionary<NSString *, NSString *> *downloadExtInfoMap;

/**
 * 对设置给reportInfoMap的上报数据进行校验。isValidReportInfoMap是对上报的基本检验。
 * 如果校验失败，则肯定不会设置给odk，这部分数据就会丢失。校验通过，则设置给odk，由odk进行上报。
 * odk本身没有提供校验函数，isValidReportInfoMap只是做了基本的校验，是否能被odk正确处理，请尽量参考odk的上报要求。
 */
+ (BOOL)isValidReportInfoMap:(NSDictionary *)reportInfoMap;

@end
