package com.tencent.news.core.page.model


annotation class StructWidgetRegistry(val type: String)

object StructWidgetType {

    const val STRUCT_PAGE = "struct_page"

    const val COMMON_TITLE_BAR = "common_title_bar"     // 基础TitleBar容器：左中右3个区域可放置按钮
    const val SIMPLE_TITLE_BAR = "simple_title_bar"     // 简单TitleBar：只包含标题和返回按钮

    const val COMMON_HEADER = "common_header"           // 基础Header样式：背景图+标题
    const val POST_HEADER = "post_header"               // 早晚报Header样式：背景图+标题
    const val LIST_HEADER = "list_header"               // 列表形式Header：内部放置多个item cell
    const val QA_LIST_HEADER = "qa_list_header"         // QA问答专题专用列表Header
    const val VIDEO_HEADER = "video_header"             // 视频内嵌专题Header
    const val COLUMN_HEADER = "column_header"
    const val COLLECTION_HEADER = "collection_header"
    const val HOT_MODULE_HEADER = "hot_module_header"   // 热点精选列表头部

    const val BOTTOM_BAR = "bottom_bar"
    const val CATALOGUE = "catalogue"                   // 导航目录
    const val CP_SEARCH_HANGING = "cp_search_hanging"   // CP搜索悬浮组件

    const val CHANNEL_BAR = "channel_bar"                       // 基础样式导航条
    const val TIMELINE_CHANNEL_BAR = "timeline_channel_bar"     // 事件脉络导航条
    const val HOT_MODULE_CHANNEL_BAR = "hot_module_channel_bar" // 热点精选导航条
    const val THING_PAGE_CHANNEL_BAR = "thing_page_channel_bar" // 事件页（thing page）通用导航条，非 QA 专属

    const val COMMON_PAGER = "common_pager"

    const val TITLE_BTN = "title_btn"
    const val FOCUS_BTN = "focus_btn"
    const val FAVORITE_BTN = "favorite_btn"

    @Deprecated("讨论区已下线")
    const val DISCUSS_BTN = "discuss_btn"       // ‘讨论区’定位按钮
    const val EMOJI_BTN = "emoji_btn"           // emoji图标按钮（旧ActionButton实现，bar_config.json中定义）
    const val SEARCH_BTN = "search_btn"
    const val AUDIO_BTN = "audio_btn"
    const val HOTSPOT_BTN = "hotspot_btn"       // ‘网友热议’定位按钮（旧ActionButton实现，bar_config.json中定义）
    const val SHARE_BTN = "share_btn"
    const val IP_SHARE_BTN = "ip_share_btn"
    const val COMMENT_BTN = "comment_btn"
    const val PUBLISH_BTN = "publish_btn"

    const val TIMELINE_JUMP_EVENT_BTN = "timeline_jump_event_btn"   // 事件脉络-‘查看专题’按钮
    const val TIMELINE_SHARE_BTN = "timeline_share_btn"             // 事件脉络-分享按钮

    @Deprecated("个人页改版没上")
    const val USER_ICON_BTN = "user_icon_btn"
    const val INPUT_BTN = "input_btn"               // 发评论按钮
    const val ACTION_BTN = "action_btn"
    const val COLUMN_PAY_BTN = "column_pay_btn"     // '付费专栏'购买按钮
    const val COLUMN_GIFT_BTN = "column_gift_btn"   // '付费专栏'购买赠送咨询 按钮
    const val PRESENT_ENTRY_BTN = "present_entry_btn"   // '礼物入口'按钮

    const val MORNING_POST_PROGRESS_BTN = "morning_post_progress_btn"   // 进度按钮
    const val PLAY_AUDIO_BTN = "play_audio_btn"                         // 播放音频按钮
    const val MORNING_POST_CUSTOMIZE_BTN = "morning_post_customize_btn" // 早报定制按钮

    const val MORNING_POST_CUSTOMIZE_LOADING = "morning_post_customize_loading" // 早报定制loadingView
    const val CHANNEL_724_CATEGORY_BANNER = "channel_724_category_banner"   // 724分类banner
    const val CHANNEL_724_HEADER = "channel_724_header"   // 724 header
    const val ASK_BTN = "ask_btn"                   // 问答 提问按钮

    const val SCHEME_BTN = "scheme_btn"             // 通用按钮组件，点击跳scheme


    const val GAME_ENTRY_PENDANT = "game_bonbon_pendant"    // BonBon游戏首页挂件
    const val GAME_NOTIFICATION_CARD = "game_notification_card" // BonBon游戏通知弹窗
    const val TIMELINE_PENDANT = "timeline_pendant"         // 事件脉络底层页面挂件
    const val TIMELINE_UNREAD_PENDANT = "timeline_unread_pendant" // 事件脉络未读挂件
    const val TIMELINE_ABSTRACT_SWITCH_PENDANT = "timeline_abstract_switch_pendant" // 事件脉络摘要开关悬浮挂件

    const val REFRESH_INDICATOR = "refresh_indicator"

    const val BTN_LIST = "btn_list"                 // 按钮列表，一般用于浮层里
    const val LAYERS = "layers"                     // 全屏浮层挂件

    const val ITEM_CARD = "item_card"
    const val CHANNEL = "channel"
    const val NEWS_LIST = "news_list"
    const val AD_LIST = "ad_list"

    // 页码组件：用于结构化页面的分段/分页控制（不进入 feeds 列表）
    const val INDEXS = "indexs"
    const val INDEX = "index"
    const val AUDIO_RADIO_VERTICAL_PAGER = "audio_radio_vertical_pager"  // 电台垂直分页滑动组件

    const val IP_BOTTOM_BAR = "ip_action_bar"       // IP页底部操作栏


    const val FLEX_BAR = "flex_bar"
    const val FLEX_SHARE_BTN = "flex_share_btn"
    const val FLEX_COMMENT_BTN = "flex_comment_btn"
    const val FLEX_LIKE_BTN = "flex_like_btn"

    // 本地构建的widget组件，包装成vm了；widgetType就没啥用了可以统一用这个占位
    const val VM_WRAPPER = "vm_wrapper"
    const val SIMPLE_WIDGET = "simple_widget"

    const val AUDIO_PODCAST_FOLLOW_UPDATE_BTN = "audio_podcast_follow_update_btn"  // 播客页右上角关注更新按钮

}