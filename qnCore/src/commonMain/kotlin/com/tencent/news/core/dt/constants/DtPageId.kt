package com.tencent.news.core.dt.constants

enum class DtPageId(override val id: String) : IDtPageId {

    GAME_HALL("pg_game_hall"),                      // BonBong游戏首页
    GAME_DETAIL("pg_game_detail"),                  // 游戏详情页
    GAME_WELFARE("pg_game_welfare"),                // 福利页
    GAME_USER_CENTER("pg_game_usercenter"),         // 游戏-‘我的’
    GAME_ACTIVITY("pg_game_activity"),              // 游戏活动页
    GAME_OTHER("pg_game_other"),                    // 其他游戏h5页面都归到这里

    ARTICLE_DETAIL("pg_detail"),                    // 文章详情页
    TIMELINE("pg_timeline"),                        // 事件脉络
    SPONSOR("pg_support_ta_pay"),                   // 支持加热购买页
    SEARCH_RESULT_USER("pg_search_result_user"),    // 个人中心搜索结果
    PG_NAV_TAB("pg_navtab"),                        // 子TAB
    AIGC_DETAIL("pg_news_sis"),                     // 新闻妹页
    AIGC_STREAM("pg_news_sis_call"),                // 新闻妹打电话页
    AIGC_ASSIST("pg_news_sis_assist"),              // 新闻妹助手页
    DIGITAL("pg_digiman"),                          // 数字人
    AIGC_DISCOVERY("pg_news_sis_discover"),         // 新闻妹发现页
    AIQA_DEATAIL("pg_hotask_event"),                // AI问答详情页
    AIAvatarPage("pg_window_ai_user_head"),         // AI换头像

    PAY_GIVE("pg_pay_give"),                        // 付费赠送页
    MY_GIFT("pg_my_gift"),                          // 礼品卡列表
    PAY_GIFT_SEND("pg_pay_gift"),                   // 会员礼品卡页面
    CP_MEMBER_RANK("pg_cp_member_rank"),            // 会员排行榜
    CP_MEMBER_PAYMENT_PANEL("pg_cp_pay"),           // 会员支付面板
    CP_MEMBER_SINGLE_ARTICLE_PAYMENT_PANEL("pg_cp_pay_single"), // 单篇文章支付面板
    COLUMN_BUY_PANEL("pg_column_pay"),              // 专栏购买面板
    DIAMOND_CHARGE_PANEL("pg_diamond_charge"),      // 钻石充值面板
    PAY_MEMBER_AREA("pg_cp_member"),                // 会员专区
    CP_COLUMN_LIST("pg_cp_column"),                 // 专栏文章列表页

    CHANNEL("pg_channel"),                          // 二级频道
    SUB_TAB("pg_subtab"),                           // 页面子tab

    AUDIO_POD_CAST_DETAIL("pg_podcast_hotcate"),    // 音频播客合集详情页
    AUDIO_PODCAST_FOLLOW("pg_podcast_focus"),       // 播客关注更新页面
    AUDIO_PODCAST_PICK("pg_podcast"),               // 播客精选页面

    PG_HOMETOWN_SELECT("pg_hometown_select"),       // 城市选取
    AUDIO_RADIO_STATION_SUB("pg_subtab"),           // 电台-广场/订阅Tab
    AUDIO_RADIO_STATION("pg_tab"),                  // 电台-电台Tab

    PG_HISTORY("pg_history"),                       // 我的历史
    PG_SEARCH_RESULT_HISTORY("pg_search_result_history"), // 资产搜索结果页
}
