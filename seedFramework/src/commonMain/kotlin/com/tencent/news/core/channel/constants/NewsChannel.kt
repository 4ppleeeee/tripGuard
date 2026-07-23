package com.tencent.news.core.channel.constants

import com.tencent.news.core.platform.api.getShiplySwitch

object NewsChannel {

    const val LOCAL_PLACEHOLDER = "news_local_channel"      // 地方站占位标识

    const val PAID_PUSH = "news_news_paidpush"              // 付费push，用这个特殊频道值标识

    const val AD_TEST = "news_news_adtest"                  // 广告测试

    const val NEWS_TOP = "news_news_top"                    // 要闻
    const val GLOBAL_TOP = "news_global_top"                // 全球要闻
    const val NEW_TOP_BROWSE = "news_news_top_browse"       // 要闻-仅浏览
    const val VIDEO_TOP = "news_video_top"                  // 视频

    const val CARE_BOTTOM = "news_care_bottom"              // tab2-竖版视频
    const val DONG_TAI = "news_news_dongtai"                // tab3-关注
    const val SHORT_VIDEO = "news_video_child_xiaoshipin"   // 沉浸式视频底层页（竖版视频）

    const val TAG_BOTTOM = "news_news_tagbottom"            // Tag底层页（包含早晚报）
    const val NEWS_NEWS_724 = "news_news_724"               // 724频道

    const val MEMBER = "news_news_knowledge"                // 会员频道

    const val GAME = "news_news_game"                       // 游戏频道

    @kotlin.jvm.JvmField // 宿主有一处java调用
    val GAME_BONBON = getBonBonChannel()                    // bonbon游戏首页

    val MINI_GAME = "news_news_minigame"                    //  小游戏首页

    const val EVENT_TIMELINE = "news_news_event_timeline"   // 事件脉络

    const val QA = "news_news_ask"                          // 热问

    const val USER = "user_center"                          // 个人中心

    const val MARKETING = "news_news_ad1"   // 商业化营销频道

    const val LONG_VIDEO = "news_video_child_long"   // 放映厅

    const val NEWS_USER_CENTER = "news_user_center" // 给广告用的tab4请求channel

    const val CARE_FEATURE_BOTTOM = "news_hot_care_bottom" // 竖版精选底层

    const val NEWS_AUDIO = "news_news_audio"    // 音频播客频道

    const val NEWS_RADIO_STATION = "news_news_radiostation"    // 电台频道

    const val NEWS_RADIO_SQUARE = "news_radiostation_square"    // 广场

    const val NEWS_RADIO_SUB = "news_radiostation_sub"    // 订阅

    const val MINE_HISTORY: String = "mine_history"             // 历史-浏览历史

    const val MINE_HISTORY_PUSH: String = "mine_history_push"   // 历史-推送

    const val MINE_FAVOURITE: String = "mine_favourite"         // 历史-收藏

    const val MINE_LIKE_ARTICLE: String = "mine_like_article"   // 历史-已赞

    const val MINE_UNDERLINE: String = "my_underline"           // 历史-划线
    const val FIND = "news_news_find"                           // 发现频道

    private fun getBonBonChannel(): String {
        return if (getShiplySwitch("use_new_bonbon_channel", true)) { // 留个开关，保留1个版本
            "news_news_bonbongame2"
        } else {
            "news_news_gamebonbon"
        }
    }

    fun isNewsTop(channelKey: String?): Boolean =
        channelKey == NEWS_TOP || channelKey == GLOBAL_TOP

}
