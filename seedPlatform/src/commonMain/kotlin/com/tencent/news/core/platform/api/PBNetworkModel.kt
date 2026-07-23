package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.ResultEx

/**
 * PB 请求异常
 */
data class PBRequestException(
    override val message: String,
    val response: IPBNetworkResponse<*>? = null,
) : Exception(message)

/**
 * PB 网络响应接口
 */
interface IPBNetworkResponse<T> {
    val result: ResultEx
    val parsedData: T?
    val rawBody: ByteArray?
    val serverCode: Int
    val serverMsg: String

    fun isValid(): Boolean = result.succeed && parsedData != null
    fun errorMsg(): String = "errorCode:${result.errorCode} error:${result.msg}"
}

data class PBNetworkResponse<T>(
    override val result: ResultEx,
    override val parsedData: T?,
    override val rawBody: ByteArray? = null,
    override val serverCode: Int = 0,
    override val serverMsg: String = "",
) : IPBNetworkResponse<T>

typealias PBNetworkCallback<T> = (IPBNetworkResponse<T>) -> Unit

/**
 * PB 请求构造器
 *
 * 用法示例：
 * ```kotlin
 * PBNetworkBuilder<MyResponse>(
 *     url = "trpc.demo.feed/GetRecommendFeed",
 *     bodyEncoder = { MyRequest.ADAPTER.encode(MyRequest(attach_info = "xxx")) },
 *     bodyDecoder = { bytes -> MyResponse.ADAPTER.decode(bytes) },
 * ) {
 *     onResponse = { resp ->
 *         if (resp.isValid()) {
 *             val data = resp.parsedData!!
 *         }
 *     }
 * }
 * ```
 *
 * @param T 业务响应体解码后的类型
 * @param url 请求 URL 或命令字
 * @param bodyEncoder 将业务请求体编码为 ByteArray 的函数
 * @param bodyDecoder 将响应 body ByteArray 解码为业务对象的函数
 */
data class PBNetworkBuilder<T>(
    var url: String,
    var bodyEncoder: () -> ByteArray,
    var bodyDecoder: (ByteArray) -> T,
    var extra: Map<String, String>? = null,
    var headers: HeaderParams? = null,
    var readTimeout: Long = -1,
    var connectTimeout: Long = -1,
    var needGlobalParams: Boolean = true,
    var responseOnMain: Boolean = false,
    var onResponse: PBNetworkCallback<T> = {},
) {
    fun execute(): INetworkRequest {
        return appNetwork().postPb(this).also {
            networkRequest = it
        }
    }

    var networkRequest: INetworkRequest? = null

    fun cancel() {
        networkRequest?.cancel()
    }
}
