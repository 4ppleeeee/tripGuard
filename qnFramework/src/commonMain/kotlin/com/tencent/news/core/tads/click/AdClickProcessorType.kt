package com.tencent.news.core.tads.click

// todo 后续构造需要添加一个新的参数，标志外跳，内部处理等等，不然大家加一个Processor忘记在isOuterHandle和isOnlyLogicHandle补充容易gg
enum class AdClickProcessorType(val key: String) {
    UNKNOWN("unknown"),

    EXIT_FULLSCREEN("exit_fullscreen"),
    APP_INSTALL("app_install"),
    APP_OPEN_INSTALLED("app_open_installed"),
    TAB2_JUMP("tab2_jump"),
    LINK_REPORT("link_report"),
    DOWNLOAD_CARD("download_card"),
    LIVE_OPEN_SCHEME("live_open_scheme"),
    CLICK_COUNT("click_count"),
    ASYNC_REPORT("async_report"),
    EXCHANGE_CGI("exchange_cgi"),
    RESERVE_DIALOG("reserve_dialog"),
    CALL_PHONE_DIALOG("call_phone_dialog"),
    CONSULT_DIALOG("consult_dialog"),
    JUMP_ACTION_V2("jump_action_v2"),
    JUMP_ACTION_V1("jump_action_v1"),
    LANDING_PAGE("landing_page"),
    WX_NATIVE_PAGE("wx_native_page"),
    SCHEME_ANDROID("scheme_android"),
    SCHEME_IOS("scheme_ios"),
    SCHEME_OHOS("scheme_ohos"),
    SCHEME_NATIVE("scheme_native"),
    MARKET_AUTO_DOWNLOAD("market_auto_download"),
    OPEN_ANDROID_APP_STORE("open_android_app_store"),
    OPEN_IOS_APP_STORE("open_ios_app_store"),
    OPEN_OHOS_APP_STORE("open_ohos_app_store"),
    WX_MINI_PROGRAM("wx_mini_program"),
    LINK_EVENT("link_event"),
    HAD_READ("had_read"),
    LIVE_ORDER("live_order"),
    MOSAIC_XJ("mosaic_xj"),
    HALF_CARD("half_card"),


    // 下述是 AdClickRequest中携带的IAdClickInterceptor
    VIDEO_COMPANION_VM("video_companion_vm"),
    VIDEO_COMPANION("video_companion"),
    ONLY_MARKET_AUTO_DOWNLOAD("only_market_auto_download"),
    ONLY_MOSAIC_XJ("only_mosaic_xj"),
    ONLY_H5_CLICK("only_h5_click"),

    // 链路子Processor细分
    SUB_WEB("web_Processor"),
    SUB_WX("wx_Processor"),
    SUB_DIRECTLY("directly_processor"),

    // 各个子Processor细分，最终用于上报的
    DG_LANDING_PAGE("downgrade_landing_page"),    // H5(V2兜底降级)
    COMPONENT_WIDGET("component_widget"),         // 创意组件
    WX_GAME("wx_game"),                           // 微信小游戏
    WX_STORE("scheme_store"),                  // 微信小店
    DEEP_LINK("deep_link"),                    // 应用直达
}

fun AdClickProcessorType.isOuterHandle(): Boolean {
    return this == AdClickProcessorType.WX_NATIVE_PAGE ||
            this == AdClickProcessorType.SCHEME_ANDROID ||
            this == AdClickProcessorType.SCHEME_IOS ||
            this == AdClickProcessorType.OPEN_ANDROID_APP_STORE ||
            this == AdClickProcessorType.OPEN_IOS_APP_STORE ||
            this == AdClickProcessorType.WX_MINI_PROGRAM ||
            this == AdClickProcessorType.MARKET_AUTO_DOWNLOAD
}

fun AdClickProcessorType.isOnlyLogicHandle(): Boolean {
    return this == AdClickProcessorType.LINK_EVENT ||
            this == AdClickProcessorType.HAD_READ ||
            this == AdClickProcessorType.LIVE_ORDER ||
            this == AdClickProcessorType.ASYNC_REPORT ||
            this == AdClickProcessorType.CLICK_COUNT ||
            this == AdClickProcessorType.LINK_REPORT ||
            this == AdClickProcessorType.EXCHANGE_CGI
}

fun AdClickProcessorType.canGoOnNextInterceptType(): Boolean {
    return this in setOf(
        AdClickProcessorType.ONLY_H5_CLICK, AdClickProcessorType.ONLY_MOSAIC_XJ,
        AdClickProcessorType.ONLY_MARKET_AUTO_DOWNLOAD
    )
}