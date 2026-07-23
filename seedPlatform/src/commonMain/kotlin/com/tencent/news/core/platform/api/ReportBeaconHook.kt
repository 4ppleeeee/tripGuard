package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.PlatformBeaconLog

/**
 * Beacon 上报 Hook 测试功能
 * 用于在 debug 模式下将 beacon 上报数据同步发送到测试服务器
 */
internal val reportBeaconAsNet: (event: String, params: Map<String, String>?) -> Unit =
    { event, params ->
        if (isDebug() && getShiplySwitch("enable_test_beacon_report", false)) {
            sendTestBeaconReport(event, params)
        }
    }

/**
 * 发送测试 Beacon 上报请求
 */
private fun sendTestBeaconReport(event: String, params: Map<String, String>?) {
    val requestParams = mutableMapOf<String, Any>()
    params?.let {
        requestParams["params"] = it
    }

    val baseUrl = getShiplyConfig("test_beacon_report_url", "https://beacon.woa.com/mockRequest")
    val url = "$baseUrl/$event"

    quickRequest<String>(
        url = url,
        params = requestParams,
        needGlobalParams = false,
        useJsonPost = true,
        responseOnMain = false,
        parser = originJsonParser(),
        onResponse = { response ->
            if (response.isValid()) {
                PlatformBeaconLog.debug { "TestBeacon上报成功: $event" }
            } else {
                PlatformBeaconLog.debug { "TestBeacon上报失败: ${response.errorMsg()}" }
            }
        }
    )
}
