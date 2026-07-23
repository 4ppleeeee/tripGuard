package com.tencent.news.core.router.contants

import com.tencent.news.core.list.model.ArticleType


// compose实现view 的唯一标识key，用于 @Page 注解
object ComposeViewKey {

    const val DEMO_PAGE = Debug.DEMO_PAGE                      // 业务体验demo
    const val DEMO_CATEGORY_PAGE = Debug.DEMO_CATEGORY_PAGE    // demo二级子分类页面

    // todo 【注意】：内部类按照 ‘业务包名’ 拆分

    // 通用页面
    object Common {
        const val WEBVIEW = "/page/common/webview"   // 通用 WebView 页（BonBon H5 等）
    }

    // Tab2相关页面
    object Tab2 {
        const val PAGE = "/page/tab2"                            // Tab2主页
        const val IMMERSIVE_PAGE = "/page/tab2/immersive"        // 沉浸式页面
        const val COLLECTION_BASE_PAGE = "/page/tab2/collection" // 合集底层页
        const val USER_PAGE = "/page/tab2/user"                  // 用户页
    }

    object Channel {
        const val ITEM_CELL = "/channel/item_cell"               // 通用cell（item结构）
        const val STRUCT = "/channel/struct"                      // 通用频道

        const val AUDIO_POD = "/channel/audio_pod"   // 播客频道
    }

    // AI相关：
    object Aigc {
        const val PAGE = "/page/aigc/event/agent"   // 新闻妹
        const val SIMPLIFIED_PAGE = "/page/aigc/event/simplifiedAgent"   // 精简版新闻妹（仅对话内容+相关提问）
        const val EMBEDDED_SEARCH_PAGE = "/page/aigc/event/embeddedSearchAgent" // 搜索结果页内嵌新闻妹tab（隐藏TitleBar + native dialog）
        const val STREAM = "/page/aigc/stream"      // 新闻妹电话
        const val ASSISTANT_STREAM = "/page/aigc/assistant/stream"      // 助手电话（扩展版）
        const val MANDARIN_EXAM_MOCK_PREVIEW = "/page/aigc/assistant/mandarin_exam_mock_preview" // 普通话测试卡片 Mock 预览
        const val STREAM_VIDEO = "/page/aigc/stream_video"
        const val VIDEO_PAGE = "/page/aigc/video"   // AIGC视频播放页
        const val MEMORY_SETTING = "/page/aigc/memory_setting" // 智声记忆设置页

        const val QA_PAGE = "/page/aiqa"                        // AI问答
        const val AVATAR_PAGE = "/page/aiavatar"                // AI头像
        const val DISCOVERY_PAGE = "/page/aigc/event/discovery" // AI发现页
    }

    @Deprecated("用 Aigc")
    object AIAvatar {
        const val PAGE = Aigc.AVATAR_PAGE
    }

    @Deprecated("用 Aigc")
    object AigcDiscovery {
        const val PAGE = Aigc.DISCOVERY_PAGE
    }

    @Deprecated("用 Aigc")
    object AIQA {
        const val PAGE = Aigc.QA_PAGE
    }

    // 音频
    object Audio {
        const val PAGE = "/page/audio/detail"                                       // 音频底层页
        const val PLAY_LIST_DIALOG = "/page/audio/playlist"                         // 播单弹窗
        const val AI_AUDIO_AGREE_DIALOG = "/page/audio/agree"                       // 协议同意弹窗
        const val AUDIO_POD_ALBUM_DETAIL = "/page/audio/pod_album_detail"           // 播客专辑详情页
        const val AUDIO_POD_FOLLOW = "/page/audio/pod_follow"                       // 播客关注页
        const val AUDIO_MINIBAR = "/page/audio/minibar"
        const val AUDIO_TAB_RADIO = "/page/audio/tab_radio"                         // 电台音频页面
        const val AUDIO_RADIO_DETAIL = "/page/audio/radio_detail"                   // 电台二级页面
        const val AUDIO_RADIO_CHANNEL = "/page/audio/radio_children_channel"         // 儿童频道页面
    }

    // 早晚报
    object MorningPost {
        const val PAGE = "/page/morningpost"        // 早晚报主页
    }

    // 音色切换
    object ChangeTimbre {
        const val PAGE = "/page/change_timbre"        // 音色切换
    }

    // 专题相关：
    object Event {
        const val EVENT_DETAIL = ArticleType.HOT_EVENT  // 专题底层
        const val TIMELINE_PAGE = "/page/timeline"      // 事件脉络
    }

    // 脉络详情
    @Deprecated("用 Event.TIMELINE_PAGE")
    object Timeline {
        const val PAGE = "/page/timeline"        // 事件脉络
    }

    object Dialog {
        const val NATIVE_BRIDGE = "/dialog/native_bridge"
        const val DEMO = "/demo/dialog"             // DemoDialog
        const val DEMO_VIEW_DIALOG = "/demo/view_dialog"   // 测试 Overlay 级别弹窗
    }

    object Voice {
        const val VOICE_INPUT = "/voice/voice_input"   // 按住语音输入弹窗
    }

    object Setting {
        const val DEVELOPER_DEBUG = "page/developer/debug"
        const val CONTENTPREFERENCE = "page/content/preference"
    }

    object HotSpot {
        const val AI_AGGREGATION_PAGE = "/page/hotspot/ai_aggregation"        // 热点精选AI聚合
    }

    object Pay {
        const val PAY_PRESENT_CARD_PAGE = "page/pay/present/card" // 赠送购买 - 礼品卡页面
        const val PRESENT_CARD_LIST_PAGE = "page/pay/present/card/list" // 礼品卡列表页面
        const val PAY_PRESENT_CARD_SEND_PAGE = "page/pay/present/card_send" // 礼品卡赠送
        const val MEMBER_RANK_PAGE = "page/pay/rank" // 排行榜
        const val CP_MEMBER_PAYMENT_DIALOG = "page/pay/cp_member_payment_dialog" // CP会员付费弹窗
        const val CP_SINGLE_ARTICLE_PAYMENT_DIALOG =
            "page/pay/cp_single_article_payment_dialog" // 单篇文章购买弹窗
        const val COLUMN_PAYMENT_DIALOG = "page/pay/column_payment_dialog" // 专栏付费弹窗
        const val COLUMN_GIFT_CLAIM_DIALOG = "page/pay/column_gift_claim_dialog" // 专栏免费领取弹窗
        const val COINS_PAYMENT_DIALOG = "page/pay/coins_payment_dialog" // 代币付费弹窗
        const val PAYMENT_AGREEMENT_DIALOG = "page/pay/agreement_dialog" // 付费协议弹窗
        const val PAYMENT_LOADING_DIALOG = "page/pay/payment_loading_dialog" // 付费加载中
        const val VIP_THANKS_LETTER_DIALOG = "page/pay/vip_thanks_letter_dialog" // VIP感谢信弹窗

        const val MEMBER_AREA_PAGE = "page/pay/member_area" // 会员专区页（全屏）
        const val MEMBER_AREA_DIALOG = "page/pay/member_area_dialog" // 会员专区浮层
        const val PAYMENT_CHANNEL_PAGE = "page/pay/payment_channel" // 会员频道页
        const val COLUMN_LIST_PAGE = "page/pay/column_list" // CP专栏列表页
        const val MEMBER_AREA_VIDEO_SINGLE_TAB_PAGE = "page/pay/member_area/video_single_tab" // 会员专区单合集 tab 视频独立页
    }

    object QA {
        const val EDITOR_INSERT_PAYMENT_PAGE = "/page/qa/editor/insert_payment" // 问答插入付费内容页面
    }

    // 商业化
    object Ad {

        object Debug {
            const val AD_PAGE = "/page/ad/debug"
            const val SUPER_MASK = "/page/ad/debug/super_mask"
            const val TIMELINE_MAIN_FRAME = "/page/ad/debug/timeline_main_frame"
        }

        object Feeds {
            const val MULTI_IMAGE_VIEW = "/tads/feeds/multi_image_view"   // 微广多图
            const val DISPLAY_INTERACT_VIEW = "/tads/feeds/interact/display"  // 互动组件（扭动/时间轴）
            const val OVERLAY = "/tads/feeds/overlay"       // 覆盖蒙层
            const val MASK_VIEW = "/tads/feeds/mask_view"   // 超级蒙层
            const val TIMELINE_MAIN_FRAME = "/tads/feeds/timeline_main_frame"  // 时间线主框架
        }

        object Video {
            const val VERTICAL_PAGE = "/tads/video/vertical_page"   // 竖版视频-整个页面
            const val FULLSCREEN_PAGE = "/tads/video/fullscreen_page" // 竖版视频-全屏页面
            const val COMPANION = "/tads/video/companion"           // 竖版视频-挂卡
            const val NATIVE_CARD = "/tads/video/native_card"       // 竖版视频-原生卡
            const val FINISH_CARD = "/tads/video/finish_card"       // 竖版视频-finishCard（debug/demo）
            const val WX_STORE_END_CARD = "/tads/video/wx_store_end_card" // 竖版视频-图片横滑微信小店结束卡，仅在内部使用，不对外。
        }

        object Comment {
            const val STORE_PRODUCT_INFO = "/tads/comment/store_product_info" // 评论区广告小店商品信息条
        }

        object Game {
            const val ENTRY_PAGE = "/tads/game/entry_page"
            const val MINI_GAME_HALL_PAGE = "/tads/game/mini_game_hall_page"
            const val RESERVE_DIALOG = "/tads/game/reserve_dialog"
            const val CANCEL_RESERVE_DIALOG = "/tads/game/cancel_reserve_dialog"
            const val NOTIFICATION_DIALOG = "/tads/game/notification_dialog"
        }

        object Detail {
            const val IP_LONG_DETAIL = "/tads/ad_detail"   //  IP长视频广告挂件
        }

        object Article {
            const val MID_ARTICLE = "tabs/game/mid_article_card"            // 中插广告卡片
            const val LARGE_CARD = "tabs/large_card"                        // 大图广告卡片
            const val SMALL_CARD = "tabs/small_card"                        // 小图广告卡片
            const val SMALL_VER_CARD = "tabs/small_ver_card"                // 竖版小图广告卡片
            const val LARGE_VER_CARD = "tabs/large_ver_card"                // 竖版大图广告卡片
        }

        object Dialog {
            const val SUPER_MASK = "tabs/dialog/super_mask"             // 超级蒙层
        }

        object Setting {
            const val ROOT = "tabs/setting/root"            // 广告管理根页面
            const val INTERACT = "tabs/setting/interact"    // 互动广告管理页
            const val RECOMMEND = "tabs/setting/recommend"  // 跨平台广告推荐管理页
        }

    }

    // 加热支持页
    object Sponsor {
        const val PAGE = "/page/sponsor"            // 【加热支持】购买页
        const val DETAIL = "/page/sponsor_detail"   // 加热支持弹窗
    }

    // 编辑器
    object Editor {
        const val DETAIL = "/page/editor/declared_content"   // 编辑器声明页
        const val SELECT_COLLECTION = "/page/editor/select_collection"   // 合集选择弹窗
    }

    object Scheme {
        const val PAGE = "/page/scheme"
    }

    object Debug {
        const val TEST_MEMORY = "/debug/test_memory"
        const val TEST_PARCEL = "/debug/test_parcel"
        const val DEMO_PAGE = "/page/demo"                      // 业务体验demo
        const val DEMO_CATEGORY_PAGE = "/page/demo/category"    // demo二级子分类页面
        const val TEXT_SELECTION_DEMO = "/page/demo/text_selection"  // 文本选择Demo
        const val MIXED_TEXT_DEMO = "/page/demo/mixed_text"     // 混排文字Demo
        const val POINTS_POPUP_PREVIEW = "/page/demo/points_popup_preview" // 积分中心弹窗预览
        const val POINTS_TASK_CENTER_ANCHOR_PREVIEW =
            "/page/demo/points_task_center_anchor_preview" // 积分任务中心锚点预览

        const val VIEW_TEST = "/page/demo/view_test" // 控件测试

        const val ALPHA_VIDEO = "/page/demo/alpha_video"
        const val NETWORK_ENV = "/page/demo/network_env"
        const val SCORE_LINE_CARD = "/page/demo/score_line_card" // 分数线卡片Demo
        const val GAOKAO_TIME_CARD = "/page/demo/gaokao_time_card" // 高考时间卡片Demo
        const val ZHENTI_CARD = "/page/demo/zhenti_card" // 真题卡片Demo
    }

    // 直播
    object Live {
        const val GUEST_LIST_PAGE = "/page/live/guest/list"   // 直播嘉宾列表
        const val SHARE_CARD = "/page/live/share/card"   // 直播分享卡片
    }

    object Share {
        const val UNIVERSAL_PAGE = "/page/share/universal" // 通用分享页面
    }

    object Sport {
        const val MATCH_LIST = "/page/sport/match/list"    // 赛程页面
    }

    // CP搜索
    object CPSearch {
        const val PAGE = "/page/cp/search"    // CP搜索页面
    }

    // CP推荐
    object CPRecommend {
        const val LIST_PAGE = "/page/cp/recommend/list"    // CP推荐列表页
    }

    // 地方站频道
    object LocalChannel {
        const val EXTREME_WEATHER = "/page/local_channel/extreme_weather"   // 极端天气
    }

    // 地理位置相关
    object Location {
        const val CITY_SELECT = "/page/location/city_select" // 城市选取页
    }

    // 详情页相关
    object Collection {
        const val COLLECTION_PAGE = "page/detail/collection_page"       // 合集详情页
        const val COLLECTION_DIALOG = "page/detail/collection_dialog"   // 合集浮层
    }

    // 专栏详情页相关
    object Column {
        const val COLUMN_PAGE = "page/detail/column_page"         // 专栏详情页
        const val COLUMN_DIALOG = "page/detail/column_dialog"     // 专栏浮层
    }

    // 长视频（放映厅）
    object LongVideo {
        const val DETAIL_PAGE = "/page/longvideo/detail"   // TV长视频底层页
        const val CATEGORY_PAGE = "/page/longvideo/category" // TV长视频分类筛选页
    }

    // 用户相关
    object User {
        const val CHECK_IN_DIALOG = "/page/user/check_in_dialog"   // 积分签到弹窗
        const val USER_CENTER_PAGE = "/page/user/center"   // 用户中心页面
        const val POINTS_TASK_CENTER_PAGE = "/page/user/points_task_center"   // 积分任务中心页面
        const val POINTS_TASK_CENTER_POPUP_DIALOG = "/page/user/points_task_center_popup_dialog" // 积分任务中心弹窗
        const val MY_UNDERLINE_PAGE = "/page/user/my_underline"   // 我的划线页面
        const val MY_HISTORY_PAGE = "/page/user/my_history"   // 我的历史页面
        const val ASSETS_SEARCH_PAGE = "/page/user/assets_search"   // 资产搜索页面
        const val CREATOR_ACHIEVEMENT_POSTER_DIALOG = "/page/user/creator_achievement_poster_dialog" // tab4 作者成就海报弹窗
        const val COIN_CENTER_PAGE = "/page/user/coin_center" // 金币中心页面
    }

}
