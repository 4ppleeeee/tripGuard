/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKConfigDefines.h
 Author      : SPSDKconfig
 Version     : 1.0
 Date        : 14/7/15
 Description : 配置字段定义
 History     : 14/7/15 初始版本
 ***********************************************************/

#ifndef SPSDKConfigDefines_h
#define SPSDKConfigDefines_h

#pragma - mark 配置key定义

/** 单位时间跳过帧的次数，来判断是否有跳帧的现象，字段值为int类型 */
#define SPSDKCONFKEY_UNIT_OF_TIME_TO_JUDGE_SKIP_FRAMES @"unit_of_time_to_judge_skip_frames"
/** 是否允许使用软解杜比音频，字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_DOLBY_AUDIO_SOFT_DECODE @"enable_dolby_audio_soft_decode"
/** 直播排队开关*/
#define SPSDKCONFKEY_LIVEQUEUEENABLE @"liveQueueEnable"
/** 多音轨是否走代理组件 */
#define SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK_USE_PROXY @"enable_multi_audio_track_use_proxy"
/** 启动上传日志的延迟时间*/
#define SPSDKCONFKEY_ON_LAUNCH_UPLOAD_LOG_DELAY_MS @"on_launch_upload_log_delay_ms"
/** 是否允许启动后，上传日志，字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_ON_LAUNCH_UPLOAD_LOG @"enable_on_launch_upload_log"
/** 使用P2P 设备黑名单，iPad低端设备使用*/
#define SPSDKCONFKEY_P2P_DEVICE_BLACKLIST @"p2p_device_blackList"
/** 外部url,选择z系统或者自研 播放策略  */
#define SPSDKCONFKEY_EXTERNAL_URL_PLAY_STRATEGY @"external_url_play_strategy"
/**url 列表 ,getvinfo 的域名等*/
#define SPSDKCONFKEY_URLS_LIST @"spsdk_urls_list"
/**Vid黑名单，在黑名单内的视频不能启用后处理增强(兜底逻辑)*/
#define SPSDKCONFKEY_VIDEO_ENHANCE_VID_BLACKLIST @"video_enhance_vid_blacklist"
/** 支持m3u8直出 */
#define SPSDKCONFKEY_ENABLE_GETVINFO_CARRY_M3U8 @"enable_getvinfo_carry_m3u8"
/** 是否支持多音轨 */
#define SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK @"enable_multi_audio_track"
/** 当播放器跳帧次数日志打开(report_log&15)，并且跳帧次数大于此值则上传。默认值是5。字段值为int类型 */
#define SPSDKCONFKEY_REPORT_LOG_PLAYER_BIG_JUMP_TIMES_MAX @"report_log_player_big_jump_times_max"
/** 是否允许打印上报的字段日志。字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_MTA_PRINT_LOG @"enable_MTA_print_log"
// 是否允许使用杜比公司提供的软解库
#define  SPSDKCONFKEY_USE_DOLBY_AUDIO_SOFT_LIBRARY    @"use_dolby_audio_soft_library"
/** 控制播放位置的日志打印频率.默认值10，表示10*400ms的时间打印一次，值越大，打印频率越小。 */
#define  SPSDKCONFKEY_UPDATE_POSITION_LOG_PRINT_FREQUENCE    @"update_position_log_print_frequence"

/****************************水印 ******************************/
/** 水印开关 */
#define SPSDKCONFKEY_ENABLE_WATERMARK @"enable_watermark"
/** 直播水印开关 */
#define SPSDKCONFKEY_ENABLE_LIVE_WATERMARK @"enable_live_watermark"

/**********************CGI 相关 *************************/
/** ckey版本 */
#define SPSDKCONFKEY_CKEY_VERSION @"ckey_version"
/** 点播是否使用https */
#define SPSDKCONFKEY_GET_VINFO_CAN_USE_HTTPS @"get_vinfo_can_use_https"
/** 直播请求是否可以使用https。字段值为BOOL类型 */
#define SPSDKCONFKEY_LIVE_INFO_CAN_USE_HTTPS @"live_info_can_use_https"
/** cgi是否使用https */
#define SPSDKCONFKEY_CGI_USE_HTTPS @"cgi_use_https"
/** CGI 重试次数 */
#define SPSDKCONFKEY_CGI_RETRY_MAX_TIMES @"cgi_retry_max_times"
/** CGI请求是否使用缓存 */
#define SPSDKCONFKEY_CGI_USE_CACHE @"cgi_use_cache"
/** 超时时间间隔 */
#define SPSDKCONFKEY_NETWORK_TIME_OUT_INTERVAL @"network_time_out_interval"
/** 点播自研加密开关 */
#define SPSDKCONFKEY_ENABLE_SELF_ENCRYPTION @"enable_self_encryption"
/** 直播自研加密开关 */
#define SPSDKCONFKEY_LIVE_ENABLE_SELF_ENCRYPTION @"live_enable_self_encryption"
/** 双栈环境是否优先使用IPV6 */
#define SPSDKCONFKEY_PREFER_IPV6_IN_IP_STACK_DUAL @"prefer_ipv6_in_ip_stack_dual"

/******************************getvinfo 请求参数 ***********************/
/** 是否开启直播排队，字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_LIVE_QUEUE @"enable_live_queue"
/** 0:auto 1:http 3:hls 其他:auto，默认http */
#define SPSDKCONFKEY_GET_VINFO_DLTYPE @"get_vinfo_dltype"
/** getvinfo请求的格式类型，1:auto,  2: 5分钟分片，4:整片，默认auto。字段值为int类型 */
#define SPSDKCONFKEY_GET_VINFO_CLIP @"get_vinfo_clip"
/** 是否允许支持杜比播放，字段值为BOOL类型 */
#define SPSDKCONFKEY_SUPPORT_DOLBY_AUDIO_PLAY @"support_dolby_audio_play"
/** dolby audio 设备列表 */
#define SPSDKCONFKEY_SUPPORT_DOLBY_AUDIO_DEVICE_LIST @"support_dolby_audio_device_list"
/** dolby vision 开关 */
#define SPSDKCONFKEY_ENABLE_DOLBY_VISION @"enable_dolby_vision"
/** dolby vision 黑名单 */
#define SPSDKCONFKEY_DOLBY_VISION_BLACKLIST @"dolby_vision_blacklist"
/** dolby vision 设备列表 */
#define SPSDKCONFKEY_SUPPORT_DOLBY_VISION_DEVICE_LIST @"support_dolby_vision_device_list"
/** 直播dolby audio 开关 */
#define SPSDKCONFKEY_ENABLE_LIVE_DOLBY_AUDIO @"enable_live_dolby_audio"
/** 直播dolby vision 开关 */
#define SPSDKCONFKEY_ENABLE_LIVE_DOLBY_VISION @"enable_live_dolby_vision"
/** 直播能力值总开关，如果NO，则spvideo、spaudio不生效 */
#define SPSDKCONFKEY_LIVE_ACTIVE_SP @"live_active_sp"
/** hdr10 开关 */
#define SPSDKCONFKEY_ENABLE_HDR10 @"enable_hdr10"
/** hdr10 支持的设备列表 */
#define SPSDKCONFKEY_SUPPORT_HDR10_DEVICE_LIST @"support_hdr10_device_list"
/** 是否支持纯音频播放，字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_AUDIO_PLAY @"enable_audio_play"

/*****************************P2P *************************/
/** 点播p2p开关 */
#define SPSDKCONFKEY_ENABLE_ONLINE_VOD_P2P @"enable_online_vod_P2P"
/** 直播hls 使用p2p 开关 */
#define SPSDKCONFKEY_ENABLE_LIVE_HLS_P2P @"enable_live_hls_P2P"
/** 直播p2p flv格式开关，默认0，关闭p2p*/
#define SPSDKCONFKEY_ENABLE_LIVE_FLV_P2P @"enable_live_flv_P2P"
/** 移动网络使用 p2p */
#define SPSDKCONFKEY_ENABLE_WWAN_P2P @"enable_wwan_p2p"
/** 免流使用p2p */
#define SPSDKCONFKEY_ENABLE_FREE_FLOW_P2P_PLAY @"enable_free_flow_P2P_play"
/** 是否允许付费视频走p2p。字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_NEED_CHARGE_P2P @"enable_need_charge_P2P"
/** 设置给p2p组件的配置项 */
#define SPSDKCONFKEY_P2PHTTPPROXYCONFIG @"P2PHttpproxyConfig"
/** 是否允许预加载下个视频vid数据，字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_PRELOAD_NEXT_VID @"enable_preload_next_vid"

/****************************上报 **************************/
/** 检查网速的周期时间，单位毫秒，默认为1000ms */
#define SPSDKCONFKEY_CHECK_NETWORK_SPEED_PEROID_MS @"check_network_speed_peroid_ms"
/** 重置网速(主要是指平均网速、最大网速)的周期次数，达到周期次数，则重置网速，默认为60次，和check_network_speed_peroid_ms在一起，就是一分钟重置一次 */
#define SPSDKCONFKEY_RESET_NETWORK_SPEED_PEROID @"reset_network_speed_peroid"
/** 请求播放器配置的最小时间间隔 */
#define SPSDKCONFKEY_REQUEST_CONFIG_MIN_INTERVAL @"request_config_min_interval"
/** 启动后，延迟delay_get_config_ms去请求播放器配置 */
#define SPSDKCONFKEY_DELAY_GET_CONFIG_MS @"delay_get_config_ms"
/** 缓冲的最小间隔，report_cache_min_interval_ms以上的缓冲才认为是缓冲，才进行上报 */
#define SPSDKCONFKEY_REPORT_CACHE_MIN_INTERVAL_MS @"report_cache_min_interval_ms"
/** seek详细信息上报允许的上报最大个数 */
#define SPSDKCONFKEY_REPORT_SEEK_INFO_MAX @"report_seek_info_max"
/** 二次缓冲详细信息上报允许的上报最大个数 */
#define SPSDKCONFKEY_REPORT_SECOND_BUFFER_INFO_MAX @"report_second_buffer_info_max"
/** 启动后，延迟delay_launch_report_ms再对保存在本地的数据进行上报，单位毫秒 */
#define SPSDKCONFKEY_DELAY_LAUNCH_REPORT_MS @"delay_launch_report_ms"
/** 正片位置更新的间隔，用于通知广告、上报。上报用于统计用户实际播放时长。单位秒。时间为0.5 */
#define SPSDKCONFKEY_POSITION_UPDATE_INTERVAL @"position_update_interval"
/** 是否允许上报日志打印 */
#define SPSDKCONFKEY_REPORT_LOG_PRINT_ENABLE @"report_log_print_enable"
/** 本地日志上传开关
 *  位数    16            |15        |14    |   13   12  11  10  9   8   | 7 6   5   4   | 3  2   1   0
 *  开关     离线下载错误   |OMX       |P2P   |   下载类型                   |播放模式        |   日志类型
 *  错误、首次缓冲、二次缓冲的日志上传开关，按位进行控制 */
#define SPSDKCONFKEY_REPORT_LOG @"report_log"
/** 当首次缓冲日志打开(report_log&2)，并且首次缓冲时长大于此值则上传，单位毫秒。默认是30000 */
#define SPSDKCONFKEY_REPORT_LOG_FIRST_LOADING_TIME_MAX_MS @"report_log_first_loading_time_max_ms"
/** 当二次缓冲日志打开(report_log&4)，并且二次缓冲次数大于此值则上传。默认值是1 */
#define SPSDKCONFKEY_REPORT_LOG_SECOND_BUFFERING_TIMES_MAX @"report_log_second_buffering_times_max"
/** 根据配置进行概率性下发,后台控制概率，前端只决定是否上传。默认后台不下发 */
#define SPSDKCONFKEY_NEED_TAKE_SAMPLE @"need_take_sample"
/** 日志上传的抽样率log_auto_upload_sample/10000;取值介于0和10000之间;0为不上报,10000为全部上报;默认为0 */
#define SPSDKCONFKEY_LOG_AUTO_UPLOAD_SAMPLE @"log_auto_upload_sample"
/** 当错误日志打开(report_log&1)，错误码是log_auto_update_error_code则上传。为空时，表示所有的错误码。默认是空 */
#define SPSDKCONFKEY_LOG_AUTO_UPDATE_ERROR_CODE @"log_auto_update_error_code"
/** 是否允许MTA上报，字段值为BOOL类型 */
#define SPSDKCONFKEY_SUPPORTMTA @"supportMTA"
/** 直播周期打点上报的周期。单位ms。默认值为60000 */
#define SPSDKCONFKEY_LIVE_PERIOD_INTERVAL_MS @"live_period_interval_ms"
/**  播放器私有飞天上报的采样率。分母是10000，feitian_player_report_sample是分子。字段值为int类型 */
#define SPSDKCONFKEY_FEITIAN_PLAYER_REPORT_SAMPLE @"feitian_player_report_sample"
/** 播放结束时，是否允许上传特殊用户id的本地日志 */
#define SPSDKCONFKEY_ENABLE_UPLOAD_SPECIAL_UID_LOG @"enable_upload_special_uid_log"
/** 指定用户列表，便于根据用户id捞取日志。字段值为NSArray列表 */
#define SPSDKCONFKEY_SPECIAL_UID_ARRAY @"special_uid_array"
/** 特殊用户的uid范围的最小值（含），用于qq范围。字段值为int类型 */
#define SPSDKCONFKEY_UID_RANGE_LOW @"uid_range_low"
/** 特殊用户的uid范围的最大值（含），用于qq范围 */
#define SPSDKCONFKEY_UID_RANGE_HIGH @"uid_range_high"
/** odk上报开关 */
#define SPSDKCONFKEY_ENABLE_MTA_REPORT @"enable_mta_report"
/**  灯塔上报开关 */
#define SPSDKCONFKEY_ENABLE_BEACON_REPORT @"enable_beacon_report"

/********************播放逻辑 *********************/
/** 是否允许使用自研播放器。字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_SELF_PLAYER @"enable_self_player"
/** hevc 开关 */
#define SPSDKCONFKEY_ENABLE_HEVC @"enable_hevc"
/** 离线Hevc开关*/
#define SPSDKCONFKEY_ENABLE_HEVC_OFFLINE @"enable_hevc_offline"
/** HEVC得分 */
#define SPSDKCONFKEY_HEVC_LEVEL @"hevc_level"
/** h264得分*/
#define SPSDKCONFKEY_H264_LEVEL @"h264_level"
/** 是否允许系统播放器缓冲超时则转到自研播放器。字段值为BOOL类型 */
#define SPSDKCONFKEY_SYS_PLAYER_SWITCH_TO_SELF_WHEN_TIME_OUT @"sys_player_switch_to_self_when_time_out"
/** 播放器重试次数*/
#define SPSDKCONFKEY_PLAYER_RETRY_COUNT @"player_retry_count"
/** 是否允许无缝切换清晰度，字段值为BOOL类型 */
#define SPSDKCONFKEY_ENABLE_SEAMLESS_SWITCH_DEFINITION @"enable_seamless_switch_definition"
/** 播放器特殊错误码列表， 字段值为NSArray列表 */
#define SPSDKCONFKEY_PLAYER_SPECIAL_ERROR_LIST @"player_special_error_list"
/** 输出特殊格式*/
#define SPSDKCONFKEY_VTB_OUTPUT_PIC_TYPE @"vtb_output_pic_type"
/** 是否支持sdr+。字段值为BOOL类型 */
#define SPSDKCONFKEY_SUPPORT_SDR_PLUS @"support_sdr_plus"
/** start buffer延迟发送时间，过滤时间很短的缓冲 */
#define SPSDKCONFKEY_START_BUFFER_DELAY_TIME_MS @"start_buffer_delay_time_ms"
/** start buffer 延迟发送开关 */
#define SPSDKCONFKEY_ENABLE_START_BUFFER_DELAY @"enable_start_buffer_delay"
/** timer 是否在子线程 */
#define SPSDKCONFKEY_PLAY_IN_SUB_THREAD @"play_in_sub_thread"

/*****************************播放器内核 （TODO）*********************************/
/** 直播buffer size*/
#define SPSDKCONFKEY_LIVE_PACKET_BUFFER_TIME_MS @"live_packet_buffer_time_ms"
/** 渲染类型*/
#define SPSDKCONFKEY_PLAYER_RENDER_TYPE @"player_render_type"
/** 精确seek 阀值*/
#define SPSDKCONFKEY_ACCURATE_SEEK_DURATION_THRESHOLD @"accurate_seek_duration_threshold"
/** 精确seek 开始位置阀值*/
#define SPSDKCONFKEY_ACCURATE_SEEK_START_POSITION_THRESHOLD @"accurate_seek_start_position_threshold"
/** 视频解码模式，0: 硬解优先，1: 仅硬解，2:仅软解 */
#define SPSDKCONFKEY_VIDEO_DECODER_MODE @"video_decoder_mode"
/** 预加载buffer 大小*/
#define SPSDKCONFKEY_PRELOAD_BUFFER_TIME_MS @"preload_buffer_time_ms"
/** 在线播放buffer超时*/
#define SPSDKCONFKEY_PLAYER_ONLINE_BUFFERING_TIME_OUT @"player_online_buffering_time_out"
/** 外部地址的buffer 超时*/
#define SPSDKCONFKEY_PLAYER_EXTERNAL_URL_BUFFERING_TIME_OUT @"player_external_url_buffering_time_out"
/** 播放器离线播放缓冲超时时间。字段值为int类型 */
#define SPSDKCONFKEY_PLAYER_OFFLINE_BUFFERING_TIME_OUT @"player_offline_buffering_time_out"
/** 点播mp4的buffersize*/
#define SPSDKCONFKEY_VOD_MP4_PACKET_BUFFER_TIME_MS @"vod_mp4_packet_buffer_time_ms"
/** tcp 超时*/
#define SPSDKCONFKEY_TCP_TIME_OUT_MS @"tcp_time_out_ms"
/** seek 后的最小buffer*/
#define SPSDKCONFKEY_MIN_BUFFER_SIZE_AFTER_SEEK @"min_buffer_size_after_seek"
/** 音轨的渲染模式*/
#define SPSDKCONFKEY_AUDIO_RENDER_MODE @"audio_render_mode"
/** 蓝光的buffer 超时*/
#define SPSDKCONFKEY_BLU_RAY_BUFFER_TIME_OUT_MS @"blu_ray_buffer_time_out_ms"
/** 非蓝光的buffer 超时*/
#define SPSDKCONFKEY_NON_BLU_RAY_BUFFER_TIME_OUT_MS @"non_blu_ray_buffer_time_out_ms"
/** 点播hls的buffer 大小*/
#define SPSDKCONFKEY_VOD_HLS_PACKET_BUFFER_TIME_MS @"vod_hls_packet_buffer_time_ms"
/** 离线hls的buffer size*/
#define SPSDKCONFKEY_VOD_OFFLINE_HLS_PACKET_BUFFER_TIME_MS @"vod_offline_hls_packet_buffer_time_ms"
/** TCP重试次数，字段值为int类型 */
#define SPSDKCONFKEY_TCP_RETRY_TIMES @"tcp_retry_times"
/** 音频解码类型。字段值枚举和SPPlayerAudioDecoderMode保持一致。字段值为int类型 */
#define SPSDKCONFKEY_AUDIO_DECODER_MODE @"audio_decoder_mode"
/**解码错误最大次数*/
#define SPSDKCONFKEY_MAX_CODEC_ERROR_COUNT @"max_codec_error_count"
/** hevc 优化开关*/
#define SPSDKCONFKEY_HEVC_OPTIMIZATION @"hevc_optimization_enable"
/** 杜比输出音量 */
#define SPSDKCONFKEY_DOLBY_OUTPUT_REFERENCE_LEVEL @"dolby_output_reference_level"

#define SPSDKCONFKEY_ENABLE_READ_PACKET_WHEN_PAUSED @"enable_read_packet_when_paused"
/*****************************本配置在线更新能力*********************************/
/** 本配置是否允许在线更新*/
#define SPSDKCONFKEY_CONFIG_ENABLE_UPDATE @"config_enable_update"
/*****************************其他场景*********************************/
/** fairplay 开关 */
#define SPSDKCONFKEY_ENABLE_FAIRPLAY @"enable_fairplay"
/** 测试环境 ，APP 彩蛋*/
#define SPSDKCONFKEY_GETVINFO_ENV @"getvinfo_env"
/** 使用metal的最小系统版本（包含） */
#define SPSDKCONFKEY_USE_METAL_MIN_SYS_VER @"use_metal_min_sys_ver"

/***********************************水印 ************************************/
/** 水印功能开关.默认打开 */
#define SPSDKCONF_ENABLE_WATERMARK SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_WATERMARK)
/** 直播水印开关*/
#define SPSDKCONF_ENABLE_LIVE_WATERMARK SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_LIVE_WATERMARK)

/*************************CGI*****************************/
/** CKEY VERSION.腾讯视频为IOS为5.2，SDK为4.X */
#define SPSDKCONF_CKEY_VERSION SPSDKCONF_STRING(SPSDKCONFKEY_CKEY_VERSION)
/** GETVINFO是否允许走HTTPS */
#define SPSDKCONF_GET_VINFO_CAN_USE_HTTPS SPSDKCONF_BOOL(SPSDKCONFKEY_GET_VINFO_CAN_USE_HTTPS)
/** LIVEINFO是否允许走HTTPS */
#define SPSDKCONF_LIVE_INFO_CAN_USE_HTTPS SPSDKCONF_BOOL(SPSDKCONFKEY_LIVE_INFO_CAN_USE_HTTPS)
/** CGI 是否使用HTTPS */
#define SPSDKCONF_CGI_USE_HTTPS SPSDKCONF_BOOL(SPSDKCONFKEY_CGI_USE_HTTPS)
/** GETVINFO/LIVEINFO 错误重试次数 */
#define SPSDKCONF_CGI_RETRY_MAX_TIMES SPSDKCONF_INT(SPSDKCONFKEY_CGI_RETRY_MAX_TIMES)
/**CGI请求是否使用CACHE*/
#define SPSDKCONF_CGI_USE_CACHE SPSDKCONF_BOOL(SPSDKCONFKEY_CGI_USE_CACHE)
/** HTTP请求网络超时时间 */
#define SPSDKCONF_NETWORK_TIME_OUT_INTERVAL SPSDKCONF_INT(SPSDKCONFKEY_NETWORK_TIME_OUT_INTERVAL)
/** 点播自研加密开关 */
#define SPSDKCONF_ENABLE_SELF_ENCRYPTION SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_SELF_ENCRYPTION)
/** 直播自研加密开关 */
#define SPSDKCONF_LIVE_ENABLE_SELF_ENCRYPTION SPSDKCONF_BOOL(SPSDKCONFKEY_LIVE_ENABLE_SELF_ENCRYPTION)
/** 双栈环境是否优先使用IPV6 */
#define SPSDKCONF_PREFER_IPV6_IN_IP_STACK_DUAL SPSDKCONF_BOOL(SPSDKCONFKEY_PREFER_IPV6_IN_IP_STACK_DUAL)

/*****************************GETVINFO相关****************************/
/** 是否开启直播排队 */
#define SPSDKCONF_ENABLE_LIVE_QUEUE SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_LIVE_QUEUE)
/** 0:AUTO 1:HTTP 3:HLS 其他:AUTO，默认HTTP */
#define SPSDKCONF_GET_VINFO_DLTYPE SPSDKCONF_INT(SPSDKCONFKEY_GET_VINFO_DLTYPE)
/** 1:AUTO 2: 5分钟分片，4:整片，默认AUTO */
#define SPSDKCONF_GET_VINFO_CLIP SPSDKCONF_INT(SPSDKCONFKEY_GET_VINFO_CLIP)
/** 是否支持杜比音效果 */
#define SPSDKCONF_SUPPORT_DOLBY_AUDIO_PLAY SPSDKCONF_BOOL(SPSDKCONFKEY_SUPPORT_DOLBY_AUDIO_PLAY)
/** 支持杜比音效的设备列表 */
#define SPSDKCONF_SUPPORT_DOLBY_AUDIO_DEVICE_LIST SPSDKCONF_ARRAY(SPSDKCONFKEY_SUPPORT_DOLBY_AUDIO_DEVICE_LIST)
/** 杜比VISION总开关 */
#define SPSDKCONF_ENABLE_DOLBY_VISION SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_DOLBY_VISION)
/** 杜比VISION黑名单 */
#define SPSDKCONF_DOLBY_VISION_BLACKLIST SPSDKCONF_ARRAY(SPSDKCONFKEY_DOLBY_VISION_BLACKLIST)
/** 支持杜比VISION的设备列表 */
#define SPSDKCONF_SUPPORT_DOLBY_VISION_DEVICE_LIST SPSDKCONF_ARRAY(SPSDKCONFKEY_SUPPORT_DOLBY_VISION_DEVICE_LIST)
/** 直播杜比AUDIO开关 */
#define SPSDKCONF_ENABLE_LIVE_DOLBY_AUDIO SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_LIVE_DOLBY_AUDIO)
/** 直播杜比VISION开关 */
#define SPSDKCONF_ENABLE_LIVE_DOLBY_VISION SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_LIVE_DOLBY_VISION)
/** 直播能力值总开关，如果NO，则SPVIDEO、SPAUDIO不生效 */
#define SPSDKCONF_LIVE_ACTIVE_SP SPSDKCONF_BOOL(SPSDKCONFKEY_LIVE_ACTIVE_SP)
/** HDR10开关 */
#define SPSDKCONF_ENABLE_HDR10 SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_HDR10)
/** 之后HDR10的设备列表 */
#define SPSDKCONF_SUPPORT_HDR10_DEVICE_LIST SPSDKCONF_ARRAY(SPSDKCONFKEY_SUPPORT_HDR10_DEVICE_LIST)
/** 是否支持纯音频播放 */
#define SPSDKCONF_ENABLE_AUDIO_PLAY SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_AUDIO_PLAY)

/*****************************P2P相关*********************************/
/** 点播P2P总开关 */
#define SPSDKCONF_ENABLE_ONLINE_VOD_P2P SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_ONLINE_VOD_P2P)
/** 直播P2P总开关 */
#define SPSDKCONF_ENABLE_LIVE_HLS_P2P SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_LIVE_HLS_P2P)
/** 直播p2p flv格式开关，默认0，关闭p2p*/
#define SPSDKCONF_ENABLE_LIVE_FLV_P2P SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_LIVE_FLV_P2P)

/** 运营商网络是否走P2P */
#define SPSDKCONF_ENABLE_WWAN_P2P SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_WWAN_P2P)
/** 免流是否走P2P */
#define SPSDKCONF_ENABLE_FREE_FLOW_P2P_PLAY SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_FREE_FLOW_P2P_PLAY)
/** 付费视频是否走P2P */
#define SPSDKCONF_ENABLE_NEED_CHARGE_P2P SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_NEED_CHARGE_P2P)
/** 设置给P2P组件的配置项 */
#define SPSDKCONF_P2P_HTTP_PROXY_CONFIG SPSDKCONF_OBJECT(SPSDKCONFKEY_P2PHTTPPROXYCONFIG)
/** 是否允许预加载下一集(走下载组件时才生效) */
#define SPSDKCONF_ENABLE_PRELOAD_NEXT_VID SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_PRELOAD_NEXT_VID)

/*****************************上报相关*********************************/
/** 检查网速的周期时间，单位毫秒，默认为1000MS */
#define SPSDKCONF_CHECK_NETWORK_SPEED_PEROID_MS SPSDKCONF_FLOAT(SPSDKCONFKEY_CHECK_NETWORK_SPEED_PEROID_MS)
/** 重置网速(主要是指平均网速、最大网速)的周期次数，达到周期次数，则重置网速，默认为60次，和CHECK_NETWORK_SPEED_PEROID_MS在一起，就是一分钟重置一次 */
#define SPSDKCONF_RESET_NETWORK_SPEED_PEROID SPSDKCONF_INT(SPSDKCONFKEY_RESET_NETWORK_SPEED_PEROID)
/** 请求播放器配置的最小时间间隔 */
#define SPSDKCONF_REQUEST_CONFIG_MIN_INTERVAL SPSDKCONF_DOUBLE(SPSDKCONFKEY_REQUEST_CONFIG_MIN_INTERVAL)
/** 启动后，延迟DELAY_GET_CONFIG_MS去请求播放器配置 */
#define SPSDKCONF_DELAY_GET_CONFIG_MS SPSDKCONF_INT(SPSDKCONFKEY_DELAY_GET_CONFIG_MS)
/** 缓冲的最小间隔，REPORT_CACHE_MIN_INTERVAL_MS以上的缓冲才认为是缓冲，才进行上报 */
#define SPSDKCONF_REPORT_CACHE_MIN_INTERVAL_MS SPSDKCONF_INT(SPSDKCONFKEY_REPORT_CACHE_MIN_INTERVAL_MS)
/** SEEK详细信息上报允许的上报最大个数 */
#define SPSDKCONF_REPORT_SEEK_INFO_MAX SPSDKCONF_INT(SPSDKCONFKEY_REPORT_SEEK_INFO_MAX)
/** 二次缓冲详细信息上报允许的上报最大个数 */
#define SPSDKCONF_REPORT_SECOND_BUFFER_INFO_MAX SPSDKCONF_INT(SPSDKCONFKEY_REPORT_SECOND_BUFFER_INFO_MAX)
/** 启动后，延迟DELAY_LAUNCH_REPORT_MS再对保存在本地的数据进行上报，单位毫秒 */
#define SPSDKCONF_DELAY_LAUNCH_REPORT_MS SPSDKCONF_INT(SPSDKCONFKEY_DELAY_LAUNCH_REPORT_MS)
/** 正片位置更新的间隔，用于通知广告、上报。上报用于统计用户实际播放时长。单位秒。时间为0.4 */
#define SPSDKCONF_POSITION_UPDATE_INTERVAL SPSDKCONF_FLOAT(SPSDKCONFKEY_POSITION_UPDATE_INTERVAL)
/** 是否允许上报日志打印 */
#define SPSDKCONF_REPORT_LOG_PRINT_ENABLE SPSDKCONF_BOOL(SPSDKCONFKEY_REPORT_LOG_PRINT_ENABLE)
/** 本地日志上传开关
 * 位数      17          16            |15        |14    |   13   12  11  10  9   8   | 7 6   5   4   | 3  2   1   0
 * 开关   硬解失败      离线下载错误       |跳帧次数       |P2P   |   下载类型                   |播放模式        |   日志类型
 * 错误、首次缓冲、二次缓冲的日志上传开关，按位进行控制 */
#define SPSDKCONF_REPORT_LOG SPSDKCONF_INT(SPSDKCONFKEY_REPORT_LOG)
/** 当首次缓冲日志打开(REPORT_LOG&2)，并且首次缓冲时长大于此值则上传，单位毫秒。默认是30000 */
#define SPSDKCONF_REPORT_LOG_FIRST_LOADING_TIME_MAX_MS SPSDKCONF_INT(SPSDKCONFKEY_REPORT_LOG_FIRST_LOADING_TIME_MAX_MS)
/** 当二次缓冲日志打开(REPORT_LOG&4)，并且二次缓冲次数大于此值则上传。默认值是1 */
#define SPSDKCONF_REPORT_LOG_SECOND_BUFFERING_TIMES_MAX SPSDKCONF_INT(SPSDKCONFKEY_REPORT_LOG_SECOND_BUFFERING_TIMES_MAX)
/** 根据配置进行概率性下发,后台控制概率，前端只决定是否上传。默认后台不下发 */
#define SPSDKCONF_NEED_TAKE_SAMPLE SPSDKCONF_INT(SPSDKCONFKEY_NEED_TAKE_SAMPLE)
/** 日志上传的抽样率LOG_AUTO_UPLOAD_SAMPLE/10000;取值介于0和10000之间;0为不上报,10000为全部上报;默认为0 */
#define SPSDKCONF_LOG_AUTO_UPLOAD_SAMPLE SPSDKCONF_INT(SPSDKCONFKEY_LOG_AUTO_UPLOAD_SAMPLE)
/** 当错误日志打开(REPORT_LOG&1)，错误码是LOG_AUTO_UPDATE_ERROR_CODE则上传。为空时，表示所有的错误码。默认是空 */
#define SPSDKCONF_LOG_AUTO_UPDATE_ERROR_CODE SPSDKCONF_STRING(SPSDKCONFKEY_LOG_AUTO_UPDATE_ERROR_CODE)
/** MTA上报开关。默认为TRUE。如果关闭，则连ODK初始化都不会*/
#define SPSDKCONF_SUPPORTMTA SPSDKCONF_BOOL(SPSDKCONFKEY_SUPPORTMTA)
/** 直播周期打点上报的周期。单位MS。默认值为60000 */
#define SPSDKCONF_LIVE_PERIOD_INTERVAL_MS SPSDKCONF_INT(SPSDKCONFKEY_LIVE_PERIOD_INTERVAL_MS)
/** 播放器FEITIAN_PLAYER上报的抽样率;取值介于0和10000之间;0为不上报,10000为全部上报,其他值N表示上报上报的抽样率为N/10000; */
#define SPSDKCONF_FEITIAN_PLAYER_REPORT_SAMPLE SPSDKCONF_INT(SPSDKCONFKEY_FEITIAN_PLAYER_REPORT_SAMPLE)
/** 播放结束时，是否允许上传特殊用户ID的本地日志 */
#define SPSDKCONF_ENABLE_UPLOAD_SPECIAL_UID_LOG SPSDKCONF_INT(SPSDKCONFKEY_ENABLE_UPLOAD_SPECIAL_UID_LOG)
/** 特殊用户的UID列表 */
#define SPSDKCONF_SPECIAL_UID_ARRAY SPSDKCONF_ARRAY(SPSDKCONFKEY_SPECIAL_UID_ARRAY)
/** 特殊用户的UID范围的最小值（含），用于QQ范围 */
#define SPSDKCONF_UID_RANGE_LOW SPSDKCONF_INT(SPSDKCONFKEY_UID_RANGE_LOW)
/** 特殊用户的UID范围的最大值（含），用于QQ范围 */
#define SPSDKCONF_UID_RANGE_HIGH SPSDKCONF_INT(SPSDKCONFKEY_UID_RANGE_HIGH)
/** ODK上报开关 */
#define SPSDKCONF_ENABLE_MTA_REPORT SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_MTA_REPORT)
/**  灯塔上报开关 */
#define SPSDKCONF_ENABLE_BEACON_REPORT SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_BEACON_REPORT)

/*****************************播放器逻辑 *********************************/
/** 自研开关 */
#define SPSDKCONF_ENABLE_SELF_PLAYER SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_SELF_PLAYER)
/** HEVC开关  */
#define SPSDKCONF_ENABLE_HEVC SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_HEVC)
/** 离线HEVC开关  */
#define SPSDKCONF_ENABLE_HEVC_OFFLINE SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_HEVC_OFFLINE)
/** 后台配置HEVC得分 */
#define SPSDKCONF_HEVC_LEVEL SPSDKCONF_INT(SPSDKCONFKEY_HEVC_LEVEL)
/** 后台配置H264得分  */
#define SPSDKCONF_H264_LEVEL SPSDKCONF_INT(SPSDKCONFKEY_H264_LEVEL)
/** 系统播放器超时是否切到自研 */
#define SPSDKCONF_SYS_PLAYER_SWITCH_TO_SELF_WHEN_TIME_OUT SPSDKCONF_BOOL(SPSDKCONFKEY_SYS_PLAYER_SWITCH_TO_SELF_WHEN_TIME_OUT)
/** 播放器重试次数 */
#define SPSDKCONF_PLAYER_RETRY_COUNT SPSDKCONF_INT(SPSDKCONFKEY_PLAYER_RETRY_COUNT)
/** 播放器特殊错误码列表(系统播放器特殊错误需要特殊处理，目前只有11800，11839 ) */
#define SPSDKCONF_PLAYER_SPECIAL_ERROR_LIST SPSDKCONF_ARRAY(SPSDKCONFKEY_PLAYER_SPECIAL_ERROR_LIST)
/** 输出数据格式 */
#define SPSDKCONF_VTB_OUTPUT_PIC_TYPE SPSDKCONF_INT(SPSDKCONFKEY_VTB_OUTPUT_PIC_TYPE)
/** 是否支持SDR+ */
#define SPSDKCONF_SUPPORT_SDR_PLUS SPSDKCONF_BOOL(SPSDKCONFKEY_SUPPORT_SDR_PLUS)
/** START BUFFER延迟发送时间，过滤时间很短的缓冲 */
#define SPSDKCONF_START_BUFFER_DELAY_TIME_MS SPSDKCONF_INT(SPSDKCONFKEY_START_BUFFER_DELAY_TIME_MS)
/** START BUFFER 延迟发送开关 */
#define SPSDKCONF_ENABLE_START_BUFFER_DELAY SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_START_BUFFER_DELAY)
/** 是否允许缓冲延迟 */
#define SPSDKCONF_PLAY_IN_SUB_THREAD SPSDKCONF_BOOL(SPSDKCONFKEY_PLAY_IN_SUB_THREAD)

/*****************************播放器内核 （TODO）*********************************/
/** 直播缓冲BUFFER大小的KEY */
#define SPSDKCONF_LIVE_PACKET_BUFFER_TIME_MS SPSDKCONF_INT(SPSDKCONFKEY_LIVE_PACKET_BUFFER_TIME_MS)
/** 视频渲染方式 0：OPENGL，1：METAL, 2:基于UIVIEW的METAL */
#define SPSDKCONF_PLAYER_RENDER_TYPE SPSDKCONF_INT(SPSDKCONFKEY_PLAYER_RENDER_TYPE)
/** 后台切前台走精确SEEK的阈值 */
#define SPSDKCONF_ACCURATE_SEEK_DURATION_THRESHOLD SPSDKCONF_INT(SPSDKCONFKEY_ACCURATE_SEEK_DURATION_THRESHOLD)
/** 起播时走精确SEEK的阈值 */
#define SPSDKCONF_ACCURATE_SEEK_START_POSITION_THRESHOLD_MS SPSDKCONF_INT(SPSDKCONFKEY_ACCURATE_SEEK_START_POSITION_THRESHOLD)
/** VIDEO 解码*/
#define SPSDKCONF_VIDEO_DECODER_MODE SPSDKCONF_INT(SPSDKCONFKEY_VIDEO_DECODER_MODE)
/** 自研播放器首次缓冲加载的帧数 */
#define SPSDKCONF_PRELOAD_BUFFER_TIME_MS SPSDKCONF_INT(SPSDKCONFKEY_PRELOAD_BUFFER_TIME_MS)
/** 播放器在线播放缓冲超时时间 */
#define SPSDKCONF_PLAYER_ONLINE_BUFFERING_TIME_OUT SPSDKCONF_INT(SPSDKCONFKEY_PLAYER_ONLINE_BUFFERING_TIME_OUT)
/** 播放器外链地址的缓冲超时时间 */
#define SPSDKCONF_PLAYER_EXTERNAL_URL_BUFFERING_TIME_OUT SPSDKCONF_INT(SPSDKCONFKEY_PLAYER_EXTERNAL_URL_BUFFERING_TIME_OUT)
/** 播放器离线播放缓冲超时时间 */
#define SPSDKCONF_PLAYER_OFFLINE_BUFFERING_TIME_OUT SPSDKCONF_INT(SPSDKCONFKEY_PLAYER_OFFLINE_BUFFERING_TIME_OUT)
/** 点播MP4 BUFFER SIZE */
#define SPSDKCONF_VOD_MP4_PACKET_BUFFER_TIME_MS SPSDKCONF_INT(SPSDKCONFKEY_VOD_MP4_PACKET_BUFFER_TIME_MS)
/** TCP 超时 */
#define SPSDKCONF_TCP_TIME_OUT_MS SPSDKCONF_INT(SPSDKCONFKEY_TCP_TIME_OUT_MS)
/** SEEK 后最小BUFFERSIZE*/
#define SPSDKCONF_MIN_BUFFER_SIZE_AFTER_SEEK SPSDKCONF_INT(SPSDKCONFKEY_MIN_BUFFER_SIZE_AFTER_SEEK)
/** 音频渲染模式*/
#define SPSDKCONF_AUDIO_RENDER_MODE SPSDKCONF_INT(SPSDKCONFKEY_AUDIO_RENDER_MODE)
/**蓝光超时*/
#define SPSDKCONF_FHD_BUFFER_TIME_OUT_MS SPSDKCONF_INT(SPSDKCONFKEY_BLU_RAY_BUFFER_TIME_OUT_MS)
/** 非蓝光超时*/
#define SPSDKCONF_BELOW_FHD_BUFFER_TIME_OUT_MS SPSDKCONF_INT(SPSDKCONFKEY_NON_BLU_RAY_BUFFER_TIME_OUT_MS)
/**点播HLS超时*/
#define SPSDKCONF_VOD_HLS_PACKET_BUFFER_TIME_MS SPSDKCONF_INT(SPSDKCONFKEY_VOD_HLS_PACKET_BUFFER_TIME_MS)
/**离线HLS超时*/
#define SPSDKCONF_VOD_OFFLINE_HLS_PACKET_BUFFER_TIME_MS SPSDKCONF_INT(SPSDKCONFKEY_VOD_OFFLINE_HLS_PACKET_BUFFER_TIME_MS)
/** TCP重试次数，字段值为INT类型 */
#define SPSDKCONF_TCP_RETRY_TIMES SPSDKCONF_INT(SPSDKCONFKEY_TCP_RETRY_TIMES)
/**音频解码类型。字段值枚举和SPPLAYERAUDIODECODERMODE保持一致。字段值为INT类型*/
#define SPSDKCONF_AUDIO_DECODER_MODE SPSDKCONF_INT(SPSDKCONFKEY_AUDIO_DECODER_MODE)
/**解码错误*/
#define SPSDKCONF_MAX_CODEC_ERROR_COUNT SPSDKCONF_INT(SPSDKCONFKEY_MAX_CODEC_ERROR_COUNT)
/** HEVC 优化开关*/
#define SPSDKCONF_HEVC_OPTIMIZATION_ENABLE SPSDKCONF_BOOL(SPSDKCONFKEY_HEVC_OPTIMIZATION)
/** 杜比输出音量 */
#define SPSDKCONF_DOLBY_OUTPUT_REFERENCE_LEVEL  SPSDKCONF_FLOAT(SPSDKCONFKEY_DOLBY_OUTPUT_REFERENCE_LEVEL)
/** PAUSE 状态下是否继续缓存数据，正常情况不用配置，底层默认为TRUE，部分场景比如移动网络下需要暂停*/
#define SPSDKCONF_ENABLE_READ_PACKET_WHEN_PAUSED SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_READ_PACKET_WHEN_PAUSED)
/*****************************本配置在线更新能力*********************************/
/** 本配置是否允许在线更新*/
#define SPSDKCONF_CONFIG_ENABLE_UPDATE SPSDKCONF_BOOL(SPSDKCONFKEY_CONFIG_ENABLE_UPDATE)
/*****************************其他场景*********************************/
/** FAIRPLAY 开关 */
#define SPSDKCONF_ENABLE_FAIRPLAY SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_FAIRPLAY)
/** 0:正式环境，1:测试环境 ，APP 彩蛋 */
#define SPSDKCONF_GETVINFO_ENV SPSDKCONF_INT(SPSDKCONFKEY_GETVINFO_ENV)
/** 使用METAL的最小系统版本（包含） */
#define SPSDKCONF_USE_METAL_MIN_SYS_VER SPSDKCONF_FLOAT(SPSDKCONFKEY_USE_METAL_MIN_SYS_VER)
/** 超分开关*/
#define SPSDKCONF_ENABLE_SUPER_RESOLUTION SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_SUPER_RESOLUTION)
/** 超分分屏模式 */
#define SPSDKCONF_ENABLE_SR_SPLIT_MODE SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_SR_SPLIT_MODE)
/** 增强模式 */
#define SPSDKCONF_ENABLE_VIDEO_ENHANCEMENT SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_VIDEO_ENHANCE)
/** 色盲开关*/
#define SPSDKCONF_ENABLE_COLOR_BLIND SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_COLOR_BLIND)
/** 单位时间跳过帧的次数，来判断是否有跳帧的现象，字段值为INT类型 */
#define SPSDKCONF_UNIT_OF_TIME_TO_JUDGE_SKIP_FRAMES SPSDKCONF_INT(SPSDKCONFKEY_UNIT_OF_TIME_TO_JUDGE_SKIP_FRAMES)
/** 是否允许使用软解杜比音频，字段值为BOOL类型 */
#define SPSDKCONF_ENABLE_DOLBY_AUDIO_SOFT_DECODE  SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_DOLBY_AUDIO_SOFT_DECODE)
/** 直播排队开关*/
#define SPSDKCONF_LIVEQUEUEENABLE  SPSDKCONF_BOOL(SPSDKCONFKEY_LIVEQUEUEENABLE)
/** 多音轨是否走代理组件 */
#define SPSDKCONF_ENABLE_MULTI_AUDIO_TRACK_USE_PROXY  SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK_USE_PROXY)
/** 启动上传日志的延迟时间*/
#define SPSDKCONF_ON_LAUNCH_UPLOAD_LOG_DELAY_MS  SPSDKCONF_INT(SPSDKCONFKEY_ON_LAUNCH_UPLOAD_LOG_DELAY_MS)
/** 是否允许启动后，上传日志，字段值为BOOL类型 */
#define SPSDKCONF_ENABLE_ON_LAUNCH_UPLOAD_LOG  SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_ON_LAUNCH_UPLOAD_LOG)
/** 使用P2P 设备黑名单，IPAD低端设备使用*/
#define SPSDKCONF_P2P_DEVICE_BLACKLIST  SPSDKCONF_ARRAY(SPSDKCONFKEY_P2P_DEVICE_BLACKLIST)
/** 外部URL,选择Z系统或者自研 播放策略  */
#define SPSDKCONF_EXTERNAL_URL_PLAY_STRATEGY SPSDKCONF_INT(SPSDKCONFKEY_EXTERNAL_URL_PLAY_STRATEGY)
/**URL 列表 ,GETVINFO 的域名等*/
#define SPSDKCONF_URLS_LIST  SPSDKCONF_OBJECT(SPSDKCONFKEY_URLS_LIST)
/**VID黑名单，在黑名单内的视频不能启用后处理增强(兜底逻辑)*/
#define SPSDKCONF_VIDEO_ENHANCE_VID_BLACKLIST  SPSDKCONF_ARRAY(SPSDKCONFKEY_VIDEO_ENHANCE_VID_BLACKLIST)
/** 支持M3U8直出 */
#define SPSDKCONF_ENABLE_GETVINFO_CARRY_M3U8  SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_GETVINFO_CARRY_M3U8)
/** 是否支持多音轨 */
#define SPSDKCONF_ENABLE_MULTI_AUDIO_TRACK SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_MULTI_AUDIO_TRACK)
/** 当播放器跳帧次数日志打开(REPORT_LOG&15)，并且跳帧次数大于此值则上传。默认值是5。字段值为INT类型 */
#define SPSDKCONF_REPORT_LOG_PLAYER_BIG_JUMP_TIMES_MAX  SPSDKCONF_INT(SPSDKCONFKEY_REPORT_LOG_PLAYER_BIG_JUMP_TIMES_MAX)
/** 是否允许打印上报的字段日志。字段值为BOOL类型 */
#define SPSDKCONF_ENABLE_MTA_PRINT_LOG  SPSDKCONF_BOOL(SPSDKCONFKEY_ENABLE_MTA_PRINT_LOG)
/** 是否允许使用杜比公司提供的软解库 */
#define SPSDKCONF_USE_DOLBY_AUDIO_SOFT_LIBRARY  SPSDKCONF_BOOL(SPSDKCONFKEY_USE_DOLBY_AUDIO_SOFT_LIBRARY)
/** 控制播放位置的日志打印频率.默认值10，表示10*500MS的时间打印一次，值越大，打印频率越小。 */
#define SPSDKCONF_UPDATE_POSITION_LOG_PRINT_FREQUENCE  SPSDKCONF_INT(SPSDKCONFKEY_UPDATE_POSITION_LOG_PRINT_FREQUENCE)

#endif
