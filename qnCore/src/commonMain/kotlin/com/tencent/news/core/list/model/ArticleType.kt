package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IListEnumDoc

typealias KmmArticleType = ArticleType

object ArticleType : IListEnumDoc {

    const val NONE = "-1"                   // 一些伪造的item，不需要ArticleType时可以用这个

    const val NEWS = "0"                    // 【P0】普通图文
    const val VIDEO_DETAIL = "4"            // 【P0】横版视频
    const val NEWS_H5_WITH_BOTTOM_BAR = "6" // 跳转web页并且带bottomBar
    const val INTERACT = "8"                // 互动频道底层页面
    const val AD = "9"                      // 【P0】广告
    const val WEIBO_WEB = "10"              // 微博频道web底层页
    const val OUT_LINK_WEB = "11"           // 外链底层页
    const val NO_SHARE_WEB = "13"           // 没有分享的链接型webView底层页
    const val OUT_LINK_WEB_ANDROID = "15"   // 外链底层页,与安卓对齐
    const val CUSTOM_WEB_BROWSER = "17" // 普通h5底层页
    const val SPECIAL = "100"               // 【废弃】专题文章
    const val VIDEOSPECIAL = "101"          // 视频专辑文章
    const val ROSELIVE = "102"              // 玫瑰直播
    const val HOT_EVENT = "116"             // 【P0】事件/专题
    const val VERTICAL_VIDEO = "118"        // 【P0】竖版视频
    const val SPECIAL_V2 = "120"            // 新专题文章
    const val HOT_TRACE = "121"             // 热点追踪
    const val TAG = "123"                   // 【P0】tag详情页/早晚报/724
    const val LONG_VIDEO = "223"            // IP视频底层页
    const val VIDEO_ALBUM_NEW = "224"       // 新视频专辑文章
    const val TV_LID = "227"                // 长视频 - lid 一般是综艺
    const val TV_CID = "228"                // 长视频 - cid 一般是电影和电视剧
    const val TV_VID = "229"                // 长视频
    const val LONG_VIDEO_WEB = "230"        // 长视频专用 webView

    const val ARTICLE_QUESTION = "232"      // 新问答-问题
    const val ARTICLE_ANSWER = "233"        // 新问答-答案
    const val AIGC_AGENT = "234"            // 智能体
    const val WEIBO = "302"                 // 【可能废弃】图文微博
    const val VIDEO_WEIBO = "303"           // 【可能废弃】视频微博
    const val COMMENT_WEIBO = "304"         // 【可能废弃】微博底层页（评论数据）
    const val CCTV_VIDEO = "311"            // 央视频底层
    const val RADIO_STATION = "307"         // 新增电台介质
    const val NEW_AUDIO = "308"             // 新增的音频介质
    const val SPECIAL_MODULE = "503"        // 专题模块
    const val MORNING_POST_RECOMMEND = "521" // 早报底部推荐
    const val HOTSPOT_24HOURS = "524"       // 24小时热点
    const val HOT_SPOT_V1 = "525"           // 热点精选模块
    const val HOT_SPOT_TEXT = "528"         // 热点精选-文字版
    const val HOT_SPOT_V2 = "531"           // 热点精选-新版仿专题样式
    const val CHANNEL_CHOICE = "532"        // 品类精选

    const val IP_VIDEO_PHASE = "540"        // 视频专辑 IP期本期看点
    const val IP_VIDEO_ALL_PHASE = "541"    // 视频专辑 IP往期回顾

    const val LIVE = "575"                  // 直播
    const val EVENT_TIMELINE = "578"        // 事件脉络
    const val CHANNEL_MULTI_BUTTON = "602"   // 多个按钮样式文章，如电竞频道顶部
    const val SCHEME = "700"                // 根据 Item.directScheme 进行跳转的文章

    const val TT_AUDIO: String = "706"      // 新版 听听音频 - 非专辑文章

    const val PURE_WEB = "1001"             // 外链底层页
    const val NO_DETAIL_PAGE = "2205"       // 无底层页类型，用于无具体要求文章类型的item设置占位文章类型
    const val HTML5 = "2999"        // 通用WebCell（类似财经列表是一个h5小页面，点击可进任意类型）
    const val NO_JUMP = "3000"              // 不需要进行跳转的文章

    const val GAME_MODULE = "7000"          // 游戏模块 @7520
    const val GAME_ENTRY_PAGE = "7001"      // BonBon游戏首页（上报用） @7560

    const val USER_HEADER_MODULE = "7100"   // 用户个人页头部cell

    const val AD_PROMOTION = "90007"        // 相关阅读 推广app

    const val CHANNEL_GROUP = "90061"       // 频道集合页（将一堆频道嵌入到一个底层页里）

    const val SPONSOR = "90070"           // 赞助内容加热compose落地页
    const val ARTICLETYPE_HOT_EVENT_TIMELINE_BODY: String = "90071"
    const val ARTICLETYPE_SPONSOR_CELL: String = "90072" // 支持加热内容cell
    const val AIQA = "90073"                // AI问答
    const val AI_AVATAR = "90074"           // AI换头像
    const val SPONSOR_DETAIL = "90075"           // 赞助内容加热详情compose落地页
    const val PAY_PRESENT_CARD = "90076"           // 购买礼品卡页面
    const val PRESENT_CARD_LIST = "90077"           // 购买礼品卡页面列表
    const val PRESENT_CARD_SEND_PAGE = "90078"           // 赠送礼品卡页面
    const val EDIT_PAYMENT_SELECT = "90079"           // 问答编辑器付费专栏选择页面
    const val EVENT_TIMELINE_DETAIL = "90080"           // 事件脉络
    const val MEMBER_RANK_PAEGE = "90081"           // 会员排行榜
    const val MY_HISTORY = "90082"                  // 我的历史

    const val AD_WEB = "1000015"            // 广告web底层页
    const val AD_CANVAS = "1000024"         // 巨幕广告底层页

    const val AD_MOSAIC = "2000002"         // 广告原生落地页

    const val NO_TYPE_SHARE_WEB = "9999999" // 所有没有articleType的H5页面分享支持，有特殊的分享文案，支持阅读基因，新年彩蛋分享
}
