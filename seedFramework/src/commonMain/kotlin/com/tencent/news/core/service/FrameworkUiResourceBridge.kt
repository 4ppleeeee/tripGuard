package com.tencent.news.core.service

/**
 * qnFramework UI 资源桥接器。
 */
object FrameworkUiResourceBridge {

    var impl: IFrameworkUiResourceBridge = EmptyFrameworkUiResourceBridge
        private set

    fun register(bridge: IFrameworkUiResourceBridge) {
        impl = bridge
    }

}

interface IFrameworkUiResourceBridge {

    fun getGlobalLoadingLottie(): String = ""

    fun getGlobalFooterLottie(): String =
        "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250611154710/Production/qn_group_shanghua_38_loding.lottie"

    fun getGlobalPullRefreshLottie(): String = ""

}

private object EmptyFrameworkUiResourceBridge : IFrameworkUiResourceBridge
