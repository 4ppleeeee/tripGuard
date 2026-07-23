package com.tencent.news.core.list.api


interface IExposure {

    // 【旧-灯塔上报】基础上报参数（目前都用大同了，这个是否还有用待考证）
    val baseReportData: MutableMap<String?, String?>?

    // 【旧-灯塔上报】全量上报参数
    @Deprecated("废弃了，不要这个手动参数了")
    val fullReportData: MutableMap<String?, String?>?
        get() = null // 暂留一会，方便ios兼容

    // 【新】大同上报参数
    val autoReportData: MutableMap<String, Any>?


    fun hasExposed(key: String?): Boolean

    fun setHasExposed(key: String?)

    fun triggerOnce(key: String, action: () -> Unit) {
        if (!hasExposed(key)) {
            setHasExposed(key)
            action.invoke()
        }
    }

    fun getExposureKey(): String = ""

}