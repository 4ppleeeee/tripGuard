package com.tencent.news.core.audio.model

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.trace.NewsAudioLog


// 音频播放状态
enum class AudioPlayStatus {
    NONE,       // 未播放状态
    LOADING,    // 正在缓冲
    READY,      // 准备完成
    PLAYING,    // 正在播放
    PAUSE,      // 暂停（可以恢复续播）
    FINISH,     // 当前媒体播放完（必须完整播完才有）
    SKIP,       // 当前媒体播放中被打断（各种业务原因的打断都算，比如用户点击‘下一首’、设置了其他拨单）
    INTERRUPT,  // 当前媒体播放中被打断（特指系统抢占音频焦点导致的，重获焦点后可以恢复播放）
    STOP,       // 播放器销毁（不可恢复、不能再起播了，资源已回收）
    ERROR       // 播放错误（例如：断网、音频流异常、系统播放器异常）
}


// 音频播放类型：URL 或者 TTS 类型
enum class AudioPlayType {
    NONE,           // 默认
    URL,            // URL播放类型
    TTS,            // TTS播放类型
    STREAM_PCM,     // 流式PCM播放类型
}


// 原生层注入的播放器类型
enum class AudioPlatformPlayerType {
    NORMAL,     // 默认类型：支持TTS 以及 URL播放
    STREAM      // 支持流式播放
}


// 音频播放Url类型
enum class AudioParamPlayUrlType {
    URL,        // 普通MP3 URL播放
    STREAM,     // stream UL播放
}


enum class AudioPlayScene(val sceneValue: String) {
    ARTICLE_DETAIL("article_detail"),                              // 图文底层
    ARTICLE_DETAIL_RELATED("article_detail_related"),              // 图文底层相关推荐
    RELATE("relate"),                                              // 头部焦点模块
    AI_POD_CAST("ai_pod_cast"),                                    // AI播客
    QUESTION_ANSWER("question_answer"),                            // 问答
    NewsList("news_list"),                                         // 要闻所有子频道入口
    PODCAST_FOLLOW("podcast_follow"),                              // 播客关注列表
    LAUNCH_MORNING_POST("launch_morning_post"),                    // 冷启悬浮球早晚报
    LAUNCH_DEFAULT_POD("launch_default_pod"),                      // 启动默认悬浮-热点精选
    PLAYER("audio_intro_pod_player"),                              // 播客精选
    RADIO_TAB("radio_tab"),                                        // 电台Tab
    RADIO_ALBUM("radio_album"),                                    // 电台合集
    RADIO_DETAIL_INSERT("radio_detail_insert"),                    // 电台底层页插入音频
    FAVOR_LIST("favor_list"),                                      // 收藏列表
    AUDIO_HISTORY("audio_history"),                                // 音频历史
    TODAY_RECOMMEND("today_recommend"),                            // 今日推荐
    HOT_INTERPRETATION("hot_module"),                              // 最新播客模块
    PLUG_IN("plug_in"),                                            // Plugin 模块
    HOT_EVENT("hot_event"),                                        // 专题
    CARE_VIDEO("care_video")                                       // Care 视频
}


// 音频播放速率档位
enum class AudioPlayRateType(val rateValue: Float) {
    RATE_TYPE_1X(1.0f),     // 默认 1.0倍速
    RATE_TYPE_0_7X(0.7f),   // 0.7倍速
    RATE_TYPE_1_2X(1.2f),   // 1.2倍速
    RATE_TYPE_1_5X(1.5f),   // 1.5倍速
    RATE_TYPE_2X(2.0f),     // 2.0倍速
    RATE_TYPE_3X(3.0f)      // 3.0倍速
}


// TTS下载状态
enum class AudioTTSDownloadStatus {
    NONE,                // 初始状态
    DOWNLOADING,         // 正在下载
    PAUSE_DOWNLOADING,   // 暂停下载
    FINISHED,            // 下载完成
    FAILED               // 下载失败
}


// 音频播放数据绑定优先级
enum class AudioPlayInfoPriorityType {
    NORMAL,            // 默认未绑定
    HIGH,              // 高优先级
    LOW                // 低优先级
}


// 音频dataLoader请求状态
enum class AudioDataLoaderRequestStatus {
    START,                               // 开始请求
    SUCCESS,                             // 请求成功
    ERROR                                // 请求失败
}


// 音频播放场景
enum class AudioPlayMonitorPlayScene(val value: String) {
    UNKNOWN("unknown"),                                          // 未知
    ARTICLE_DETAIL_RELATED("article_detail_related"),            // 图文底层相关推荐
    AI_BANNER("ai_banner"),                                      // 爱听头部banner
    AI_MORNING_POST("ai_morning_post"),                          // 爱听早晚报
    AI_724_POST("ai_724_post"),                                  // 爱听724
    AI_INTRODUCE("ai_introduce"),                                // 爱听今日推荐
    AI_LISTEN_LATER("ai_listen_later"),                          // 爱听稍后听
    AI_HOT("ai_hot"),                                            // 爱听热点解读
    AI_BOARD_CAST("ai_board_cast"),                              // 爱听播客
    HOME_PAGE_MORNING_POST("home_page_morning_post"),            // 首页早晚报
    HOME_PAGE_724_POST("home_page_724_post"),                    // 首页724
    HOME_PAGE_724_DETAIL_POST("home_page_724_detail_post"),      // 首页724详情页
    RADIO_DETAIL("radio_detail"),                                // 图文底层页正文音频
    NEWS_LIST("audio_btn"),                                      // 要闻所有频道的列表入口
    RADIO_DETAIL_INSERT("radio_detail_insert"),                  // 图文底层页正文内嵌音频
    PERSONAL_PAGE("personal_page"),                              // 个人主页音频
    HI_CAR("hi_car"),                                            // 华为HICar
    MEDIA_CENTER("media_center"),                                // 媒体中心
    HI_CAR_INTRODUCE("hi_car_introduce"),                        // HICar 推荐
    HI_CAR_PICK("hi_car_pick"),                                  // HICar 精选
    AIGC_DETAIL("aigc_detail"),                                  // 新闻妹页面
    POD_CAST("pod_cast"),                                        // 播客
    AI_POD_CAST("ai_pod_cast"),                                  // AI播客
    QUESTION_ANSWER("question_answer_detail"),                   // 问答详情
    POD_CAST_FOLLOW("pod_cast_follow"),                          // 播客关注
    RADIO_TAB("radio_tab"),                                      // 电台Tab
    RADIO_ALBUM("radio_album"),                                  // 电台合集
    AUDIO_HISTORY("audio_history"),                              // 音频历史
    PLUG_IN("plug_in"),                                          // Plugin 模块
    HOT_EVENT("hot_event"),                                      // 专题
    CARE_VIDEO("care_video")                                     // Care 视频
}

// 音频播放器URL卡顿时间事件Id
const val AUDIO_PLAY_STUCK_ID = "kmm_audio_player_stuck"

// 音频播放器TTS卡顿时间事件Id
const val AUDIO_PLAY_STUCK_TTS_ID = "kmm_audio_player_stuck_tts"

// 音频播放器URL播放错误事件Id
const val AUDIO_PLAY_URL_ERROR_ID = "kmm_audio_player_error"

// 音频播放器TTS播放错误事件Id
const val AUDIO_PLAY_TTS_ERROR_ID = "kmm_audio_player_error_tts"

// TTS转换错误上报事件Id
const val AUDIO_CONVERT_TTS_ERROR_ID = "kmm_audio_convert_tts_error"

// TTS分片转换耗时上报事件Id
const val AUDIO_CONVERT_TTS_TIME_ID = "kmm_audio_convert_tts_time"

// 占位的播放器errorCode
const val AUDIO_PLAY_ERROR_CODE_UNKNOWN = -9999

// 无网络自定义错误码
const val AUDIO_NO_NET_WORK_CODE = -9998


data class AudioProgressInfo(
    val currentPlayItem: IKmmFeedsItem? = null,
    val progress: Float = 0f,
    val duration: Float = 0f,
)

// TTS默认音色
const val AUDIO_DEFAULT_TIMBRE = "male0"

// 虚拟播放器日志Tag
const val AUDIO_PLAYER_LOG_TAG = "virtualPlayer"

// 虚拟播放器日志Tag
const val AUDIO_SERVICE_LOG_TAG = "audioService"

// 当前播放的TTS句子索引
const val AUDIO_SENTENCE_INDEX_NOT_FOUND = -1

// 当前播放的TTS句子的ID
const val AUDIO_SENTENCE_ID_NOT_FOUND = -1


// 记录虚拟播放器日志
fun virtualPlayerLog(log: String) = NewsAudioLog.fileLog(subTag = AUDIO_PLAYER_LOG_TAG, log)

// miniBar状态
enum class MiniBarState(val value: String) {
    OPEN("open"),
    CLOSE("close"),
    EXPAND("expand"),
    COLLAPSE("collapse"),
    HIDE("hide"),
    SHOW("show"),
    WIDEN("widen"),
    RESTORE_WIDTH("restore_width")
}

enum class AudioPlayOperation(val value: String) {
    PLAY_NEXT("play_next"),
    JUMP_TO_AUDIO_DETAIL("jump_to_audio_detail"),
    RELEASE("release"),
    ADD_TO_LIST("add_to_list"),
    PLAY_TARGET("play_target"),
    FORBID_LOAD_LIST("forbid_load_list")
}
