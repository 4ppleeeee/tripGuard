package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.tmm.knoi.annotation.KNCallback
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

/**
 * 鸿蒙端 HTTP 请求能力接口，由 ArkTS 侧实现并通过 knoi @KNCallback 机制注入。
 *
 * ArkTS 侧实现需要：
 *  - jsonPostRequest / formPostRequest / getRequest：标准 HTTP 请求，返回 [QNHttpResponse]
 *  - multiJsonPostRequest：携带文件的 multipart 请求
 *  - streamPostRequest：发送原始二进制流（Content-Type: application/octet-stream）
 *  - sseRequest：Server-Sent Events 流式请求，通过 Flow 持续推送 [QNSseResponse]
 *  - pbPostRequest：Protobuf 二进制 POST 请求，返回 [QNHttpResponse]（body 为 base64 编码的二进制）
 */
@KNCallback
interface OhosHttpRequestService {

    /**
     * JSON 格式 POST 请求（Content-Type: application/json）。
     * @param builder 请求构造器，包含 url、params、headers 等
     * @param onResult 回调 [QNHttpResponse]
     */
    fun jsonPostRequest(builder: NetworkBuilder<*>, onResult: (response: QNHttpResponse) -> Unit)

    /**
     * 表单格式 POST 请求（Content-Type: application/x-www-form-urlencoded）。
     * @param builder 请求构造器
     * @param onResult 回调 [QNHttpResponse]
     */
    fun formPostRequest(builder: NetworkBuilder<*>, onResult: (response: QNHttpResponse) -> Unit)

    /**
     * GET 请求。
     * @param builder 请求构造器
     * @param onResult 回调 [QNHttpResponse]
     */
    fun getRequest(builder: NetworkBuilder<*>, onResult: (response: QNHttpResponse) -> Unit)

    /**
     * 携带文件的 multipart JSON POST 请求。
     * @param builder 请求构造器，uploadFilePath / uploadFileName / uploadFileMediaType 需已设置
     * @param onResult 回调 [QNHttpResponse]
     */
    fun multiJsonPostRequest(builder: NetworkBuilder<*>, onResult: (response: QNHttpResponse) -> Unit)

    /**
     * 原始二进制流 POST 请求（Content-Type: application/octet-stream）。
     * @param builder 请求构造器
     * @param onResult 回调 [QNHttpResponse]
     */
    fun streamPostRequest(builder: NetworkBuilder<*>, onResult: (response: QNHttpResponse) -> Unit)

    /**
     * Server-Sent Events 流式请求。
     * @param builder 请求构造器
     * @param onResult 持续回调 [QNSseResponse]，直到 [QNSseResponse.Success] 或 [QNSseResponse.Failed]
     */
    fun sseRequest(builder: NetworkBuilder<*>, onResult: (response: QNSseResponse) -> Unit)

    /**
     * Protobuf 二进制 POST 请求。
     * @param url 请求 URL
     * @param bodyBase64 base64 编码的请求体
     * @param headers 请求头
     * @param onResult 回调 [QNHttpResponse]，body 为 base64 编码的响应体
     */
    fun pbPostRequest(
        url: String,
        bodyBase64: String,
        headers: Map<String, String>,
        onResult: (response: QNHttpResponse) -> Unit,
    )
}

/**
 * HTTP 请求响应结果。
 */
sealed class QNHttpResponse : IKmmKeep {

    /** 请求成功，[data] 为响应体字符串 */
    data class Success(val data: String) : QNHttpResponse()

    /** 请求失败，[errCode] 为错误码，[errMsg] 为错误信息 */
    data class Failed(val errCode: Int, val errMsg: String) : QNHttpResponse()
}

/**
 * SSE 流式响应事件。
 */
sealed class QNSseResponse : IKmmKeep {

    /** 收到一条 SSE 事件，[data] 为事件数据 */
    data class Event(val data: String) : QNSseResponse()

    /** SSE 流正常结束 */
    object Success : QNSseResponse()

    /** SSE 流异常结束，[errCode] 为错误码，[errMsg] 为错误信息 */
    data class Failed(val errCode: Int, val errMsg: String) : QNSseResponse()
}

/**
 * HTTP 请求性能埋点，记录各阶段耗时。
 */
@OptIn(ExperimentalForeignApi::class)
class QNHttpPerformance : IKmmKeep {

    private var requestStartMs: Long = 0L
    private var requestEndMs: Long = 0L
    private var parseMillis: Long = 0L
    private var callbackMillis: Long = 0L
    private var errorMsg: String? = null

    fun onRequestStart() {
        requestStartMs = currentTimeMs()
    }

    fun onRequestEnd() {
        requestEndMs = currentTimeMs()
    }

    fun onRequestError(error: Throwable) {
        errorMsg = error.message
        onRequestEnd()
    }

    fun onMeasureParseMillis(block: () -> Unit) {
        val start = currentTimeMs()
        block()
        parseMillis = currentTimeMs() - start
    }

    fun onMeasureCallbackMills(block: () -> Unit) {
        val start = currentTimeMs()
        block()
        callbackMillis = currentTimeMs() - start
    }

    /** 总耗时（毫秒） */
    fun totalMs(): Long = if (requestEndMs > requestStartMs) requestEndMs - requestStartMs else 0L

    @OptIn(ExperimentalForeignApi::class)
    private fun currentTimeMs(): Long = memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        return@memScoped tv.tv_sec * 1000 + tv.tv_usec / 1000
    }
}
