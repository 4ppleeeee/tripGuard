package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.IReportDoc
import com.tencent.news.core.platform.QnPlatformLogic

interface IAppReport : IReportDoc {

    // Beacon/灯塔事件上报
    fun reportBeacon(event: String, params: Map<String, String>?)

    // Bugly 异常与诊断上报
    fun reportBugly(msg: String, error: Throwable? = null)

    // DT/大同事件上报
    fun reportDt(event: String, params: Map<String, String>?)

    // DT/大同页面来源标记
    fun setPageStartFrom(from: String)

    fun resetPageStartFrom()

    /**
     * 打包日志目录并上传到 Bugly，用于 debug 调试场景
     * 各端自行获取日志目录路径并打包上传
     * @param onResult 上传结果回调，true 表示成功，false 表示失败
     */
    fun uploadLogToBugly(onResult: (Boolean) -> Unit = {})

    /**
     * 为反馈页打包日志。
     *
     * 端侧需要把日志 zip 转成 Base64，再交给业务反馈容器上传到工单附件。
     */
    fun prepareFeedbackLogZipBase64(onResult: (FeedbackLogZipPayload?) -> Unit = {}) {
        onResult(null)
    }

    fun getCurrentReportChannelId(): String // 获取当前频道id

}

data class FeedbackLogZipPayload(
    val base64: String,
    val fileSuffix: String = "zip",
)

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
        println("Beacon report $event: $params")
    }

    override fun reportBugly(msg: String, error: Throwable?) {}

    override fun reportDt(event: String, params: Map<String, String>?) {
        println("DT report $event: $params")
    }

    override fun setPageStartFrom(from: String) {
        println("DT page start from: $from")
    }

    override fun resetPageStartFrom() {
        println("DT reset page start from")
    }

    override fun uploadLogToBugly(onResult: (Boolean) -> Unit) {
        println("Upload log to Bugly")
        onResult(false)
    }

    override fun prepareFeedbackLogZipBase64(onResult: (FeedbackLogZipPayload?) -> Unit) {
        onResult(null)
    }

    override fun getCurrentReportChannelId(): String {
        return ""
    }
}
