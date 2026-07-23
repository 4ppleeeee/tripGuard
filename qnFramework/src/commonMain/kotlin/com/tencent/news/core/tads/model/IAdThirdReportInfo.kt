package com.tencent.news.core.tads.model

interface IAdThirdReportInfo {
    val reportUrl: String?
    val reportTime: Int?
    val clickType: Int?
    val validClickType: Int get() = clickType ?: CLICK_TYPE_NORMAL
    val reportType: Int?
    var isDetected: Boolean

    companion object {
        const val CLICK_TYPE_NORMAL = 1
        const val CLICK_TYPE_NEW = 2
        const val REPORT_TYPE_API = 1
    }
}