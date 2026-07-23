package com.tencent.news.core.platform.api

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

/**
 * 协程版本的 PB 请求，支持 suspend 调用（失败时抛出 PBRequestException）
 *
 * 使用示例：
 * ```kotlin
 * val rsp: MyResponse = quickPBRequestSuspend(
 *     url = "trpc.demo.feed/GetRecommendFeed",
 *     bodyEncoder = { MyRequest.ADAPTER.encode(MyRequest(attach_info = "xxx")) },
 *     bodyDecoder = { bytes -> MyResponse.ADAPTER.decode(bytes) },
 * )
 * ```
 *
 * @param T 业务响应体解码后的类型
 * @param url 请求 URL 或命令字
 * @param bodyEncoder 将业务请求体编码为 ByteArray
 * @param bodyDecoder 将响应 body 解码为业务对象
 * @param extra 扩展字段
 * @param headers 自定义请求头
 * @param connectTimeout 连接超时（毫秒）
 * @param readTimeout 读取超时（毫秒）
 */
suspend fun <T> quickPBRequestSuspend(
    url: String,
    bodyEncoder: () -> ByteArray,
    bodyDecoder: (ByteArray) -> T,
    extra: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    connectTimeout: Long = -1,
    readTimeout: Long = -1,
): T = suspendCancellableCoroutine { continuation ->
    val request = PBNetworkBuilder(
        url = url,
        bodyEncoder = bodyEncoder,
        bodyDecoder = bodyDecoder,
        extra = extra,
        headers = headers,
        responseOnMain = false,
        connectTimeout = connectTimeout,
        readTimeout = readTimeout,
        onResponse = { response ->
            if (response.isValid()) {
                response.parsedData?.let {
                    continuation.resumeWith(Result.success(it))
                } ?: continuation.resumeWithException(
                    PBRequestException("PB 解析结果为 null", response)
                )
            } else {
                continuation.resumeWithException(
                    PBRequestException(response.errorMsg(), response)
                )
            }
        }
    ).execute()

    continuation.invokeOnCancellation {
        request.cancel()
    }
}

/**
 * 协程版本的 PB 请求，返回 Result<T>（不抛异常）
 *
 * 使用示例：
 * ```kotlin
 * val result: Result<MyResponse> = quickPBRequest(
 *     url = "trpc.demo.feed/GetRecommendFeed",
 *     bodyEncoder = { MyRequest.ADAPTER.encode(MyRequest(attach_info = "xxx")) },
 *     bodyDecoder = { bytes -> MyResponse.ADAPTER.decode(bytes) },
 * )
 * result.onSuccess { rsp -> ... }
 * result.onFailure { e -> ... }
 * ```
 */
suspend fun <T> quickPBRequest(
    url: String,
    bodyEncoder: () -> ByteArray,
    bodyDecoder: (ByteArray) -> T,
    extra: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    connectTimeout: Long = -1,
    readTimeout: Long = -1,
): Result<T> = suspendCancellableCoroutine { continuation ->
    val request = PBNetworkBuilder(
        url = url,
        bodyEncoder = bodyEncoder,
        bodyDecoder = bodyDecoder,
        extra = extra,
        headers = headers,
        responseOnMain = false,
        connectTimeout = connectTimeout,
        readTimeout = readTimeout,
        onResponse = { response ->
            if (response.isValid()) {
                continuation.resumeWith(Result.success(Result.success(response.parsedData!!)))
            } else {
                continuation.resumeWith(
                    Result.success(Result.failure(PBRequestException(response.errorMsg(), response)))
                )
            }
        }
    ).execute()

    continuation.invokeOnCancellation {
        request.cancel()
    }
}
