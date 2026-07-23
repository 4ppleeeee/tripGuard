package com.tencent.news.core.tads.constants

// 广告竖版视频-行业三段式组件样式
object AdNativeStyleType {
    const val ECOMMERCE_MDPA = "ECOMMERCE_MDPA"             // 商品：mdpa
    const val ECOMMERCE_SDPA = "ECOMMERCE_SDPA"             // 商品：sdpa
    const val ECOMMERCE_GENERAL = "ECOMMERCE_GENERAL"       // 电商优惠券样式
    const val WECHAT_CONSULT = "WECHAT_CONSULT"             // 微信客服
    const val APP_DOWNLOAD = "APP_DOWNLOAD"                 // 应用下载
    const val GAME_DOWNLOAD = "GAME_DOWNLOAD"               // 游戏下载
    const val WECHAT_MINIGAME = "WECHAT_MINIGAME"           // 微信小游戏
    const val PLAYABLE_MINIGAME = "PLAYABLE_MINIGAME"       // 本地使用，微信试玩小游戏
    const val CHANNELS_LIVE_STREAM = "CHANNELS_LIVE_STREAM" // 直播小店
    const val WECHAT_STORE = "WECHAT_STORE"                 // 微信小店
    const val SHORT_DRAMA = "MINI_PROGRAM_SHORTPLAY"        // 短剧
    const val TRAVEL = "TRAVEL"                             // 旅游
    const val TOOL = "TOOL"                                 // 工具
    const val EDUCATION = "EDUCATION"                       // 教育
    const val NOVEL = "NOVEL"                               // 小说

    // default 是自己自定义的 非下发
    const val LOCAL_DEFAULT = "local_default"               // 本地默认
    const val DEFAULT = "default"                           // 默认（通常线上实验使用）
}