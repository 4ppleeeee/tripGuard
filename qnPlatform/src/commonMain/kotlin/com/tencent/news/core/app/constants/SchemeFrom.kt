package com.tencent.news.core.app.constants

import com.tencent.news.core.extension.isNotNullOrEmpty


object SchemeFrom {
    const val ICON = "icon"
    const val PUSH = "push"
    const val SOGOU = "sogou"
    const val WAP = "wap"
    const val WEIXIN = "weixin"
    const val QQ = "mobileQQPush"
    const val QQ_NEWS = "qqnews"
    const val APP_LINK = "app_link"
    const val OUTSIDE_OPENURL = "outside_openurl"
    const val SSO_LOGIN = "login"
    const val THIRD_PARTY_SHARE = "share"
    const val WEIXIN_OTHER = "weixinOther"
    const val UNKNOWN = "other"
    const val READERSHARE = "reader_share"
    const val INNER = "inner"
    // iOS
    const val IOS_SHORTCUT = "ios_shortcut"
    const val LOCAL_PUSH = "localPush"
    const val OTHER_APP = "otherApp"                    // 中间过渡值，方便一些业务逻辑。用于还未解析出来应用信息前的判断
    const val LIVE_ACTIVITY = "liveActivity"
    const val DYNAMIC_ISLAND = "dynamicIsland"
    const val WIDGET_24HOUR = "widget24hour"
    const val HOT_WIDGET = "hot_widget"
    const val SHORTCUT = "icon"                         // iOS，shortcut, 长按新闻图标
    const val PLUG = "plug"                             // 插件，weixin & mobileQQPush

    // 【是否是‘落地页’拉起】 与数据侧 montezhao 了解到，端上流量侧的判断是：
    // pg_page_start_from上报参数不为空，且不是icon（即主启）的时候算落地页
    // 特定业务场景下分析，数据侧才会用 in (push, weixin, qq)
    fun isFromLanding(schemeFrom: String?): Boolean =
        schemeFrom.isNotNullOrEmpty() && schemeFrom != ICON

    fun isFromPlugin(schemeFrom: String?): Boolean =
        schemeFrom in setOf(QQ, WEIXIN, PLUG)

    fun isFromWXPlugin(schemeFrom: String?) = schemeFrom == WEIXIN

    fun isFromIcon(schemeFrom: String?) = schemeFrom == ICON

}