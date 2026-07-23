package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.FeedbackLogZipPayload
import com.tencent.news.core.platform.api.IAppReport
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppReport = JSValue

fun setupOhosAppReport(report: IOhosAppReport) {
    QnPlatformLogic.appReport = OhosAppReportProvider(report.asOhosAppReport())
}

private class OhosAppReportProvider(
    private val report: OhosAppReport
) : IAppReport {
    override fun reportBeacon(event: String, params: Map<String, String>?) {
        report.reportBeacon(event, params.toJsonString())
    }

    override fun reportBugly(msg: String, error: Throwable?) {
        report.reportBugly(msg, error?.stackTraceToString().orEmpty())
    }

    override fun reportDt(event: String, params: Map<String, String>?) {
        report.reportDt(event, params.toJsonString())
    }

    override fun setPageStartFrom(from: String) {
        report.setPageStartFrom(from)
    }

    override fun resetPageStartFrom() {
        report.resetPageStartFrom()
    }

    override fun uploadLogToBugly(onResult: (Boolean) -> Unit) {
        report.uploadLogToBugly()
        onResult(true)
    }

    override fun prepareFeedbackLogZipBase64(onResult: (FeedbackLogZipPayload?) -> Unit) {
        report.prepareFeedbackLogZipBase64 { base64 ->
            onResult(
                base64.takeIf { it.isNotBlank() }?.let { FeedbackLogZipPayload(base64 = it) }
            )
        }
    }
}

@KNCallback
interface OhosAppReport {

    fun reportBeacon(event: String, paramsJson: String)

    fun reportBugly(msg: String, errorMessage: String)

    fun reportDt(event: String, paramsJson: String)

    fun setPageStartFrom(from: String)

    fun resetPageStartFrom()

    fun uploadLogToBugly()

    fun prepareFeedbackLogZipBase64(onResult: (base64: String) -> Unit)
}

private fun Map<String, String>?.toJsonString(): String {
    if (this.isNullOrEmpty()) {
        return "{}"
    }
    return buildString {
        append("{")
        this@toJsonString.entries.joinTo(this, separator = ",") { (key, value) ->
            "\"${key.escapeJson()}\":\"${value.escapeJson()}\""
        }
        append("}")
    }
}

private fun String.escapeJson(): String {
    return buildString(length) {
        for (char in this@escapeJson) {
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}
