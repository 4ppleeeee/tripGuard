package com.tencent.news.core.audio.model

import com.tencent.news.core.channel.constants.NewsChannel

enum class PageScene {
    ARTICLE,                   // 图文
    EVENT,                     // 专题
    POD_PAGE_SOURCE,           // 播客页面来源
    HOT_MODULE,                // 热点精选页
    HOT_CARD,                  // 热点精选大卡
}

object AudioEntryScene {
    const val NONE = ""
    const val AUDIO_BTN = "audio_btn"
}

data class RadioScene(
    val scene: RadioSceneType,
    val subSceneType: RadioSubSceneType,
    val channelId: String = NewsChannel.NEWS_AUDIO,
    val pageScene: PageScene? = null,
    val contextInfo: AudioExtendContextInfo = AudioExtendContextInfo()     // 音频上下文扩展信息
) {
    fun getKey(): String {
        return "${scene.type}_${subSceneType.subType}"
    }

    override fun equals(other: Any?): Boolean {
        if (other is RadioScene) {
            return this.getKey() == other.getKey()
        }
        return false
    }
}

data class AudioExtendContextInfo(
    val podcastTotalScene: PodcastTotalScene = PodcastTotalScene.NONE,      // 播客主场景
    val podcastScene: PodcastScene = PodcastScene.NONE,                     // 播客具体模块
) {
    var podcastTargetId: String? = null
    var podAlbumDto: AudioExtendPodAlbumDto? = null   // 播客专辑dto
    var radioStationDto: AudioExtendRadioStationDto? = null
    var audioEntryScene: String = AudioEntryScene.NONE
    var reportArticleTotalScene: String? = null       // 上报用音频主场景，不影响播放场景
    var reportArticleScene: String? = null            // 上报用音频子场景，不影响播放场景
    var showPodcastEntrance: Boolean = true           // 跳转到播客详情页后右上角是否展示播客二级入口
}

// 合集dto
data class AudioExtendPodAlbumDto(val albumTargetId: String?, val albumChannelId: String?)

// 电台dto
data class AudioExtendRadioStationDto(val radioPlayFrom: String?)


/**
 * 将播单层二级scene转为一级scene
 * 方便虚拟播放器内部上报埋点scene
 */
fun RadioScene.convertToAudioPlayScene(): AudioPlayMonitorPlayScene {
    return when (this) {
        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.RELATE
        ) -> AudioPlayMonitorPlayScene.AI_BANNER

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.MORNING_POST
        ) -> AudioPlayMonitorPlayScene.AI_MORNING_POST

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.LIVE_POST
        ) -> AudioPlayMonitorPlayScene.AI_724_POST

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.TODAY_RECOMMEND
        ) -> AudioPlayMonitorPlayScene.AI_INTRODUCE

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.LISTEN_LATER
        ) -> AudioPlayMonitorPlayScene.AI_LISTEN_LATER

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.PLAYER
        ) -> AudioPlayMonitorPlayScene.AI_BOARD_CAST

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.HOT_INTERPRETATION
        ) -> AudioPlayMonitorPlayScene.AI_HOT

        RadioScene(
            RadioSceneType.RADIO_724,
            RadioSubSceneType.RADIO_724_LIST
        ) -> AudioPlayMonitorPlayScene.HOME_PAGE_724_POST

        RadioScene(
            RadioSceneType.RADIO_724,
            RadioSubSceneType.RADIO_724_DETAIL
        ) -> AudioPlayMonitorPlayScene.HOME_PAGE_724_DETAIL_POST

        RadioScene(
            RadioSceneType.RADIO_DETAIL,
            RadioSubSceneType.RADIO_DETAIL_INSERT
        ) -> AudioPlayMonitorPlayScene.RADIO_DETAIL_INSERT

        RadioScene(
            RadioSceneType.RADIO_DETAIL,
            RadioSubSceneType.RADIO_DETAIL
        ) -> AudioPlayMonitorPlayScene.RADIO_DETAIL

        RadioScene(
            RadioSceneType.RADIO_DETAIL,
            RadioSubSceneType.ARTICLE_DETAIL_RELATED
        ) -> AudioPlayMonitorPlayScene.ARTICLE_DETAIL_RELATED

        RadioScene(
            RadioSceneType.PERSONAL_PAGE,
            RadioSubSceneType.PERSONAL_PAGE
        ) -> AudioPlayMonitorPlayScene.PERSONAL_PAGE

        RadioScene(
            RadioSceneType.MORNING_POST,
            RadioSubSceneType.MORNING_POST
        ) -> AudioPlayMonitorPlayScene.HOME_PAGE_MORNING_POST

        RadioScene(
            RadioSceneType.HI_CAR,
            RadioSubSceneType.HI_CAR
        ) -> AudioPlayMonitorPlayScene.HI_CAR

        RadioScene(
            RadioSceneType.MEDIA_CENTER,
            RadioSubSceneType.MEDIA_CENTER
        ) -> AudioPlayMonitorPlayScene.MEDIA_CENTER

        RadioScene(
            RadioSceneType.HI_CAR,
            RadioSubSceneType.RECOMMEND_HICAR
        ) -> AudioPlayMonitorPlayScene.HI_CAR_INTRODUCE

        RadioScene(
            RadioSceneType.HI_CAR,
            RadioSubSceneType.PICK_HICAR
        ) -> AudioPlayMonitorPlayScene.HI_CAR_PICK

        RadioScene(
            RadioSceneType.RADIO_DETAIL,
            RadioSubSceneType.AI_PODCAST_DETAIL_MODULE
        ) -> AudioPlayMonitorPlayScene.AI_POD_CAST

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.PODCAST_DETAIL_MODULE
        ) -> AudioPlayMonitorPlayScene.POD_CAST

        RadioScene(
            RadioSceneType.LIKE_RADIO,
            RadioSubSceneType.PODCAST_FOLLOW_MODULE
        ) -> AudioPlayMonitorPlayScene.POD_CAST_FOLLOW

        RadioScene(
            RadioSceneType.NEWS_LIST,
            RadioSubSceneType.NEWS_LIST
        ) -> AudioPlayMonitorPlayScene.NEWS_LIST

        RadioScene(
            RadioSceneType.HOT_QUESTION,
            RadioSubSceneType.QUESTION_ANSWER
        ) -> AudioPlayMonitorPlayScene.QUESTION_ANSWER

        RadioScene(
            RadioSceneType.RADIO_TAB,
            RadioSubSceneType.RADIO_TAB
        ) -> AudioPlayMonitorPlayScene.RADIO_TAB

        RadioScene(
            RadioSceneType.RADIO_TAB,
            RadioSubSceneType.RADIO_ALBUM
        ) -> AudioPlayMonitorPlayScene.RADIO_ALBUM

        RadioScene(
            RadioSceneType.AUDIO_HISTORY,
            RadioSubSceneType.AUDIO_HISTORY
        ) -> AudioPlayMonitorPlayScene.AUDIO_HISTORY

        RadioScene(
            RadioSceneType.PLUGIN,
            RadioSubSceneType.PLUGIN
        ) -> AudioPlayMonitorPlayScene.PLUG_IN

        RadioScene(
            RadioSceneType.HOT_EVENT,
            RadioSubSceneType.HOT_EVENT
        ) -> AudioPlayMonitorPlayScene.HOT_EVENT

        RadioScene(
            RadioSceneType.CARE_VIDEO,
            RadioSubSceneType.CARE_VIDEO
        ) -> AudioPlayMonitorPlayScene.CARE_VIDEO

        else -> AudioPlayMonitorPlayScene.UNKNOWN
    }
}


enum class RadioSceneType(val type: Int) {
    INIT(-1),                              // 初始状态
    LIKE_RADIO(1),                         // 爱听频道
    RADIO_724(2),                          // 724
    RADIO_DETAIL(3),                       // 图文底层
    MORNING_POST(4),                       // 早晚报
    HI_CAR(5),                             // 华为HICar
    MEDIA_CENTER(6),                       // 播控中心
    PERSONAL_PAGE(7),                      // 个人主页
    STRUCK_PAGE(8),                        // 专题
    HOME_PAGE_NEWS(9),                     // 要闻
    HOT_QUESTION(10),                      // 热问
    LAUNCH_PAGE_NEWS(11),                  // 冷启动
    RADIO_TAB(12),                          // 电台Tab
    NEWS_LIST(13),                          // 要闻所有子频道列表
    FAVOR_LIST(14),                          // 历史页的收藏子TAB
    AUDIO_HISTORY(15),                     // 音频历史
    PLUGIN(16),                            // Plugin 模块
    HOT_EVENT(17),                         // 专题（新）
    CARE_VIDEO(18),                        // Care 视频
}


enum class RadioSubSceneType(val subType: Int) {
    INIT(-1),                              // 初始状态
    RELATE(101),                           // 头部焦点模块
    MORNING_POST(102),                     // 早晚报模块
    LIVE_POST(103),                        // 实时播报模块
    TODAY_RECOMMEND(104),                  // 今日推荐
    LISTEN_LATER(105),                     // 稍后听
    PLAYER(106),                           // 播客
    HOT_INTERPRETATION(107),               // 热点解读
    RADIO_724_LIST(108),                   // 724列表
    RADIO_724_DETAIL(109),                 // 724底层页
    RADIO_DETAIL_INSERT(110),              // 插入音频
    HI_CAR(111),                           // 华为HICar
    RADIO_DETAIL(112),                     // 图文底层
    MEDIA_CENTER(113),                     // 播控中心
    PERSONAL_PAGE(114),                    // 个人主页
    RECOMMEND_HICAR(115),                  // hicar 推荐
    PICK_HICAR(116),                       // hicar 精选
    STRUCK_PAGE(117),                      // 专题
    HOT_MODULE(118),                       // 热点精选
    PODCAST_DETAIL_MODULE(119),            // 播客详情
    AI_PODCAST_DETAIL_MODULE(120),         // AI播客
    QUESTION_ANSWER(121),                  // 热问回答
    PODCAST_FOLLOW_MODULE(122),            // 播客关注
    LAUNCH_HOT_MODULE(123),                // 冷启动热点精选
    RADIO_TAB(124),                        // 电台Tab
    NEWS_LIST(125),                        // 图文列表,复用底层
    AUDIO_HISTORY(126),                    // 音频历史
    FAVOR_LIST(127),                       // 历史页的收藏子TAB
    PLUGIN(128),                           // Plugin 模块
    ARTICLE_DETAIL_RELATED(129),           // 图文底层相关推荐
    HOT_EVENT(130),                        // 专题（新）
    RADIO_ALBUM(131),                      // 电台合集
    CARE_VIDEO(132),                       // Care 视频
}

enum class PodcastTotalScene(val scene: String) {
    NONE(""),                                // 无
    CHANNEL("channel"),                      // 非音频频道的频道列表
    DETAIL_USER("detail_user"),              // 个人页场景
    IMMERSIVE("immersive"),                  // 沉浸式
}

enum class PodcastScene(val scene: String) {
    NONE(""),                                                    // 无
    CHANNEL_LIST("channel_list"),                                // 信息流列表
    HOT("hot"),                                                  // 热点精选
    REC_TODAY("rec_today"),                                      // 今日推荐
    AUDIO_BANNER("audio_banner"),                                // 头部焦点模块
    HOTSPOT("hotspot"),                                          // 最新播客
    REC_PODCASTS("rec_podcasts"),                                // 播客精选：频道
    DETAIL_USER("detail_user"),                                  // 个人主页
    HISTORY("history"),                                          // 历史
    TAB2("tab2"),                                                // tab2
    IMMERSIVE("immersive"),                                      // 沉浸式
    POD_CAST_FOCUS_UPDATE("focus_update");                       // 播客关注更新

    companion object {
        fun fromValue(value: String): PodcastScene {
            return entries.firstOrNull { it.scene == value } ?: NONE
        }
    }
}
