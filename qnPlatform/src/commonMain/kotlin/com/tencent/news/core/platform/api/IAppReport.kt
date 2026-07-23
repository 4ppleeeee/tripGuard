package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.IReportDoc
import com.tencent.news.core.platform.QnPlatformLogic


interface IAppReport : IReportDoc {

    fun reportBeacon(event: String, params: Map<String, String>?)

    fun reportBugly(msg: String, error: Throwable? = null)

    fun reportDt(event: String, params: Map<String, String>?)

    fun setPageStartFrom(from: String)

    fun resetPageStartFrom()

    fun getCurrentReportChannelId(): String // 获取当前频道id

}

fun appReport(): IAppReport {
    val original = QnPlatformLogic.appReport ?: defaultAppReport
    return AppReportProxy(original)
}

private val defaultAppReport by lazy { DefaultAppReport() }

private class AppReportProxy(private val delegate: IAppReport) : IAppReport by delegate {
    override fun reportBeacon(event: String, params: Map<String, String>?) {
        delegate.reportBeacon(event, params)
        // 假请求，只方便测试抓包
        reportBeaconAsNet.invoke(event, params)
    }
}

class DefaultAppReport : IAppReport {
    override fun reportBeacon(event: String, params: Map<String, String>?) {
        println("灯塔上报 ${event}: $params")
    }

    override fun reportBugly(msg: String, error: Throwable?) {}

    override fun reportDt(event: String, params: Map<String, String>?) {
        println("大同上报 ${event}: $params")
    }

    override fun setPageStartFrom(from: String) {
        println("大同上报 ${from}: $from")
    }

    override fun resetPageStartFrom() {
        println("大同上报 reset page start from")
    }

    override fun getCurrentReportChannelId(): String {
        return ""
    }
}