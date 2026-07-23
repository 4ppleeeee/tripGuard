package com.tencent.news.core.dt.constants

enum class DtElementId(override val id: String) : IDtElementId {
    // 文章卡片
    ArticleCard("em_item_article"),

    // 来源面板
    SourcePanel("em_source_panel"),

    // 分享入口按钮
    ShareBtn("em_share"),

    // 分享面板
    SharePanel("em_more_panel"),

    // 海报预览面板
    SharePreViewCardPanel("em_cardpanel"),

    // 海报卡片
    SharePosterCardPanel("em_card"),

    // 截屏分享面板
    ShareScreenPanel("em_share_screenpanel"),

    ShareCardPanel("em_more_cardpanel"),

    // 系统分享按钮
    ShareSystem("em_share_system"),

    SharePdf("em_share_pdf"),

    // 微信分享按钮
    ShareFriends("em_share_friends"),

    // 朋友圈分享按钮
    ShareMoments("em_share_moments"),

    // QQ分享按钮
    ShareQQ("em_share_qq"),

    // 微博分享按钮
    ShareWeibo("em_share_weibo"),

    // QZone分享按钮
    ShareQZone("em_share_qzone"),

    // 新浪微博分享按钮
    ShareSina("em_share_sina_weibo"),

    // 企业微信分享按钮
    ShareWorkWeixin("em_share_enterprise_wechat"),

    // 复制链接按钮
    ShareCopyLink("em_share_copy_link"),

    // 冷启页推荐模块
    AgentRecommend("em_news_sis_homepage"),

    // 冷启页换一批推荐智能体按钮
    AgentRecommendChange("em_change"),

    // 海报分享按钮
    SharePostCard("em_share_cardpanel"),

    // 截屏分享按钮
    ShareScreenshot("em_share_screenshot"),

    // 保存图片
    ShareSaveImage("em_save_image"),

    // 保存视频
    ShareSaveVideo("em_save_video"),

    // 定制早晚报按钮
    PostCustomizeBtn("em_customize"),

    // 定制早晚报面板
    PostCustomizePanel("em_interest_top_panel"),

    // 早晚报底层页日签按钮
    PostDailySignBtn("em_daily_sign"),

    // 天气
    Weather("em_weather"),

    // 播放音频按钮
    TtsBtn("em_tts"),

    // 音色切换按钮
    TtsVoiceBtn("em_tts_voice"),

    // 确认按钮
    ConfirmBtn("em_confirm_btn"),

    // 关注按钮
    FollowBtn("em_focus"),

    // 评论按钮
    CommentBtn("em_cmt"),

    // 支付按钮
    PayBtn("em_pay"),

    // 充值并购买
    RechargeAndBuyBtn("em_recharge_buy"),

    // 充值
    RechargeBtn("em_recharge_only"),

    // 新闻妹
    AigcDialog("em_dialog"),

    AigcDialogQuestion("em_dialog_question"),

    AigcDialogSay("em_dialog_say"),

    AigcDialogPubBtn("em_dialog_pub_btn"),

    AigcChoosePic("em_choose_pic"),

    AigcPhoneBtn("em_news_sis_all"),

    AigcDialogPic("em_news_sis_pic"),

    AigcDialogPoster("em_news_sis_poster"),

    AigcDialogTimeline("em_news_sis_timeline"),

    // AIGC工具栏按钮事件（如：福建舰入列动态、苹果公司资讯、定制等）
    EM_NEWS_SIS_EVENT("em_news_sis_event"),

    // 弹窗
    PopWindow("em_window"),
    PopWindowBtn("em_window_btn"),
    EM_CLOSE("em_close"),
    EM_MOD("em_mod"),

    // ai热问
    AIQAMod("em_mod"),

    AIQAItemFunction("em_item_function"),

    AIQADetail("em_detail"),

    EM_CITE("em_cite"),

    EM_JUMP_ARTICLE("em_jump_article"),

    AigcSpeak("em_speak"),

    PayGive("em_pay_give"),

    PayActivity("em_scheme"),

    PaySuccessGuideView("em_window_pay_success"),

    EM_CONFIRM("em_confirm"),

    EM_RECHARGE("em_recharge"),

    EM_WINDOW_PAY_AGREEMENT("em_window_pay_agreement"),

    EM_WINDOW("em_window"),

    EM_WINDOW_BTN("em_window_btn"),
    EM_CP_MEMBER_CONTENT("em_cp_member_content"),
    EM_CP_MEMBER_RANK("em_cp_member_rank"),
    EM_MY_CP_MEMBER("em_my_cp_member"),

    // 音频按钮
    AigcAudio("em_audio"),

    // 爱听/播客频道入口
    EM_AUDIO_CHL("em_audio_chl"),

    // 音频底层页定时
    EM_AUDIO_TIMER("em_audio_timer"),

    // 音频底层页稍后听
    EM_LISTEN_LATER("em_listen_later"),

    // 音频底层页播放按钮
    EM_AUDIO_PLAY("em_audio_play"),

    // 音频底层页上一篇按钮
    EM_AUDIO_PREV("em_audio_prev"),

    // 音频底层页下一篇按钮
    EM_AUDIO_NEXT("em_audio_next"),

    // 播单按钮
    EM_AUDIO_LIST("em_audio_list"),

    // 目录浮层
    EM_DIRECTORY_PANEL("em_directory_panel"),

    // 倍速按钮
    EM_AUDIO_SPEED_OPTION("em_audio_speed_option"),

    EM_ITEM_NAV("em_item_nav"),         // 一级频道导航（一般使用参数：nav_item_id）目前有些不规范的底层页也用的这个
    EM_ITEM_SUB_NAV("em_item_subnav"),  // 底层页中的导航（事件/tag等等，一般使用参数：nav_id）

    EM_ITEM_USER("em_item_user"),

    EM_SCHEME("em_scheme"),

    EM_USER_HEAD("em_user_head"),

    // 付费购买
    EM_CP_PAY("em_cp_pay"),

    // 中插广告卡片
    EM_ITEM_AD("em_item_ad"),

    // 中插广告按钮
    EM_AD_BTN("em_ad_btn"),

    // 字幕开关按钮
    EM_SUBTITLES_BTN("em_subtitles_btn"),

    // 新闻妹设置按钮
    EM_ASSIST_SETTING("em_assist_setting"),

    // 方言选择入口
    EM_DIALECT_SELECT("em_dialect_select"),

    // 中插条
    EM_INSERT_BAR("em_insert_bar"),

    // 设置面板
    EM_SETTING_PANEL("em_setting_panel"),

    // 合集选择面板
    EM_SELECT_COLLECTION_PANEL("em_select_collection_panel"),

    // 设置按钮
    EM_SETTING_BTN("em_setting_btn"),

    // 签到领积分
    EM_CHECK_IN_BTN("em_task_sign_btn"),

    // 积分任务中心订单入口
    EM_TASK_BILL_BTN("em_task_bill_btn"),

    // 积分任务中心规则说明入口
    EM_TASK_RULES("em_task_rules"),

    // 积分任务中心更多入口
    EM_TASK_DETAIL("em_detail"),

    // 积分任务中心横滑导航
    EM_TASK_NAV_BTN("em_task_nav_btn"),

    // 积分任务中心小游戏按钮
    EM_GAME_BTN("em_game_btn"),

    // 积分任务中心任务条目
    EM_TASK_ITEM("em_task_item"),

    // 积分任务中心任务完成按钮
    EM_TASK_BTN("em_task_btn"),

    // 积分任务中心看广告按钮
    EM_TASK_AD("em_task_ad"),

    // 积分任务中心签到提醒按钮
    EM_TASK_NOTICE_BTN("em_task_notice_btn"),

    // 积分任务中心筛选入口
    EM_FILTER("em_filter"),

    // 签到领好礼
    EM_CHECK_IN_GIFT("em_redeem_gift"),

    // 音频播客热门分类卡片
    EM_ITEM_CARD("em_radio_station"),

    // 提醒条
    EM_REMIND_BAR("em_remind_bar"),

    // ===== Tab4 用户中心相关 =====
    // 消息按钮
    EM_MESSAGE("message"),
    // 扫码按钮
    EM_SCAN("em_scan"),
    // 设置按钮（Tab4 右上角）
    EM_USER_SERVICE_BTN("em_user_service_btn"),
    // 昵称
    EM_USER_NICK("em_user_nick"),
    // 佩戴勋章
    EM_MEDAL("em_medal"),
    // 佩戴VIP
    EM_USER_VIP("em_user_vip"),
    // 装扮按钮
    EM_SKIN("em_skin"),
    // 已购按钮
    EM_USER_CP_MEMBER_BTN("em_user_cp_member_btn"),
    // 会员频道按钮
    EM_MEMBER_CHL_BTN("em_member_chl_btn"),
    // 金币按钮
    EM_COIN_CENTER_BTN("em_coin_center_btn"),
    // 金币中心按钮
    EM_COIN_BTN("em_coin_btn"),
    // 积分按钮/积分数
    EM_USER_TASK_CENTER("em_user_task_center"),
    // 签到/积分按钮
    EM_HEAD_ENTRY("em_head_entry"),
    // 立即登录按钮
    EM_QUICK_LOGIN("em_quick_login"),
    // 其它账号登录按钮
    EM_LOGIN("em_login"),
    // 微信登录按钮
    EM_LOGIN_WX("em_login_wx"),
    // QQ登录按钮
    EM_LOGIN_QQ("em_login_qq"),
    // 手机号登录按钮
    EM_LOGIN_MOBILE("em_login_mobile"),
    // 华为登录按钮
    EM_LOGIN_HW("em_login_hw"),
    // 扫码登录按钮
    EM_QR_CODE("em_qr_code"),
    // 活动通知运营位
    EM_BULLETIN("em_bulletin"),
    // 抽取AI头像入口
    EM_AI_USER_HEAD("em_ai_user_head"),
    // 气泡提示
    EM_BUBBLE_TIP("em_bubble_tip"),
    // 广告Banner
    EM_ITEM_BANNER("em_item_banner"),
    // 电台频道入口
    EM_AI_STATION_CHL("em_ai_station_chl"),
    // 常用功能模块
    EM_USER_SERVICE("em_user_service"),
    // 关注按钮（Tab4）
    EM_USER_FOCUS("em_user_focus"),
    // 收藏按钮
    EM_COLLECT("collect"),
    FAVORITE_BTN("em_favor"),
    RADIO_FAVORITE_BTN("em_up"),

    // 历史按钮
    EM_HISTORY("history"),
    // 已赞按钮
    EM_THUMB_UP("thumb_up"),
    // 稍后听按钮
    EM_LISTEN_LATER_TAB4("listen_later"),
    // 更多功能模块
    EM_USER_MORE("em_user_more"),
    // 更多功能模块内按钮
    EM_USER_MORE_BTN("em_user_more_btn"),
    // 创作服务模块
    EM_USER_CPCENTER("em_user_cpcenter"),
    // 创作服务模块内按钮
    EM_USER_CPCENTER_BTN("em_user_cpcenter_btn"),
    // 我的历史页右上角按钮
    EM_HISTORY_BTN("em_history_btn"),
    // 推送历史页底部“优化”按钮
    EM_OPTIMIZE("em_optimize"),
    // 我的历史页空页面引导按钮
    EM_TO_YAOWEN("em_to_yaowen"),
    // 搜索按钮
    EM_SEARCH("em_search"),
    // em_item_section
    EM_ITEM_SECTION("em_item_section"),
    // em_item_article
    EM_ITEM_ARTICLE("em_item_article"),
    // 搜索结果项
    EM_ITEM_SEARCH("em_item_search"),
    // 专题banner模块曝光
    EM_ITEM_BANNER_MODULE("em_item_banner_module"),
    // 垂直cell内的按钮
    EM_VERT_CELL("em_vert_cell"),
    // 垂直cell模块
    EM_MOD_VERT_CELL("em_mod_vert_cell"),
    // 英语评分 / 普通话结果评分
    EM_SCORE("em_score"),
    // 普通话题目卡
    EM_QUESTION("em_question"),
    // 普通话再测一次
    EM_RETRY("em_retry"),
    // 空页面元素（如播客关注页空态）
    EM_ITEM_EMPTY("em_item_empty"),

    // 脉络摘要节点卡片
    EM_SUMMARY("em_summary"),

    // 脉络摘要展开 / 收起按钮
    EM_EXPAND("em_expand"),

    // 脉络展示摘要开关
    EM_SHOW_SUMMARY("em_show_summary"),

    // 脉络页未读气泡 / status cell 点击（通过 e_pos 区分来源：hang=气泡、in_mod=status cell）
    EM_NEW_CONTENT("em_new_content"),

    EM_MOD_ARTICLE("em_mod_article"),

    // 专栏底层页"查看我的专栏"按钮（已购态）
    EM_MY_COLUMN("em_mycolumn"),

    // 海报
    Poster("em_poster"),
    // tab4 积分中心
    EM_POINT_CENTER_ENTRY("em_point_center_entry"),
}
