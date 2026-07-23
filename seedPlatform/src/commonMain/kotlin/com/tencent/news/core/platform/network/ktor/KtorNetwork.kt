package com.tencent.news.core.platform.network.ktor

import com.tencent.news.core.extension.cancelResult
import com.tencent.news.core.extension.errorResult
import com.tencent.news.core.extension.successResult
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.NetworkResponse
import com.tencent.news.core.platform.api.PBNetworkBuilder
import com.tencent.news.core.platform.api.PBNetworkResponse
import com.tencent.news.core.platform.api.appStatus
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.platform.network.NetworkManager
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import io.ktor.client.call.body
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString

/**
 * 基于 Ktor 的 INetwork 实现
 *
 * 通用请求流程放在 commonMain，具体 Ktor 引擎由各平台 createEngine() 提供：
 * Android → OkHttp，iOS → Darwin，OHOS → OhosHttpEngine。
 *
 * 使用示例：
 * ```kotlin
 * initKtorNetwork()
 * ```
 */
class KtorNetwork(
    private val client: HttpClient = createDefaultHttpClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : INetwork {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override fun <T> jsonPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return KtorNetworkRequest(scope, client, builder, RequestType.JSON_POST, mainDispatcher)
            .also { it.execute() }
    }

    override fun <T> formPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return KtorNetworkRequest(scope, client, builder, RequestType.FORM_POST, mainDispatcher)
            .also { it.execute() }
    }

    override fun <T> getRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return KtorNetworkRequest(scope, client, builder, RequestType.GET, mainDispatcher)
            .also { it.execute() }
    }

    override fun <T> sseRequest(builder: NetworkBuilder<T>): INetworkRequest {
        // SSE 暂用 GET 实现，后续可以扩展为真正的 SSE 流
        return KtorNetworkRequest(scope, client, builder, RequestType.GET, mainDispatcher)
            .also { it.execute() }
    }

    override fun <T> jsonMultiPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return KtorNetworkRequest(
            scope,
            client,
            builder,
            RequestType.MULTIPART_POST,
            mainDispatcher
        ).also { it.execute() }
    }

    /**
     * 发送原始二进制 POST 请求。
     *
     * 当前 Ktor 默认实现暂时复用 form 请求兜底；OHOS ArkTS 注入实现已提供真实 stream 请求。
     * 后续需要在 KtorNetwork 内补齐真正的 bytes body 实现。
     */
    override fun <T> streamPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return formPostRequest(builder)
    }

    override fun <T> postPb(builder: PBNetworkBuilder<T>): INetworkRequest {
        return KtorPBNetworkRequest(scope, client, builder, mainDispatcher)
            .also { it.execute() }
    }

    override fun netState(): NetState {
        // Ktor 没有内置网络状态检测，迁移期复用宿主平台状态实现。
        return appStatus().netState()
    }

    override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        appStatus().addNetStatusChangeListener(netStatusListener)
    }

    override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        appStatus().removeNetStatusChangeListener(netStatusListener)
    }

    fun close() {
        scope.cancel()
        client.close()
    }
}

internal class KtorPBNetworkRequest<T>(
    private val scope: CoroutineScope,
    private val client: HttpClient,
    private val builder: PBNetworkBuilder<T>,
    private val mainDispatcher: CoroutineDispatcher,
) : INetworkRequest {

    private var job: Job? = null

    override fun execute() {
        job = scope.launch {
            try {
                val response = client.post(builder.url) {
                    applyCommonConfig()
                    contentType(ContentType.Application.OctetStream)
                    setBody(builder.bodyEncoder())
                }
                val rawBody = response.body<ByteArray>()
                val parsedData = runCatching { builder.bodyDecoder(rawBody) }
                    .getOrElse { cause ->
                        dispatchResponse(
                            PBNetworkResponse(
                                result = errorResult("PB response decode error: ${cause.message}"),
                                parsedData = null,
                                rawBody = rawBody,
                                serverCode = response.status.value,
                                serverMsg = response.status.description,
                            )
                        )
                        return@launch
                    }

                dispatchResponse(
                    PBNetworkResponse(
                        result = if (response.status.value in 200..299) {
                            successResult("HTTP ${response.status.value}")
                        } else {
                            errorResult("HTTP ${response.status.value}")
                        },
                        parsedData = parsedData,
                        rawBody = rawBody,
                        serverCode = response.status.value,
                        serverMsg = response.status.description,
                    )
                )
            } catch (e: CancellationException) {
                dispatchResponse(PBNetworkResponse(cancelResult(), null))
            } catch (e: Exception) {
                dispatchResponse(PBNetworkResponse(errorResult(e.message ?: "Unknown error"), null))
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }

    private suspend fun dispatchResponse(response: PBNetworkResponse<T>) {
        if (builder.responseOnMain) {
            withContext(mainDispatcher) {
                builder.onResponse.invoke(response)
            }
        } else {
            builder.onResponse.invoke(response)
        }
    }

    private fun HttpRequestBuilder.applyCommonConfig() {
        if (builder.needGlobalParams) {
            NetworkManager.getGlobalHeaders().forEach { (key, value) -> header(key, value) }
        }
        builder.headers?.forEach { (key, value) -> header(key, value) }
        if (builder.connectTimeout > 0 || builder.readTimeout > 0) {
            timeout {
                if (builder.connectTimeout > 0) connectTimeoutMillis = builder.connectTimeout
                if (builder.readTimeout > 0) requestTimeoutMillis = builder.readTimeout
            }
        }
    }
}

internal enum class RequestType {
    GET, JSON_POST, FORM_POST, MULTIPART_POST
}

internal class KtorNetworkRequest<T>(
    private val scope: CoroutineScope,
    private val client: HttpClient,
    private val builder: NetworkBuilder<T>,
    private val requestType: RequestType,
    private val mainDispatcher: CoroutineDispatcher,
) : INetworkRequest {

    private var job: Job? = null

    override fun execute() {
        job = scope.launch {
            try {
                val response = performRequest()
                val json = response.bodyAsText()
                val respHeaders = mutableMapOf<String, String>()
                response.headers.forEach { key, values ->
                    respHeaders[key.lowercase()] = values.firstOrNull() ?: ""
                }
                val parserResult = builder.parser?.onParseJson(json)

                val networkResponse = NetworkResponse(
                    json = json,
                    result = successResult("HTTP ${response.status.value}"),
                    parserResult = parserResult,
                    headers = respHeaders
                )

                dispatchResponse(networkResponse)
            } catch (e: CancellationException) {
                val cancelResponse = NetworkResponse<T>(
                    json = null,
                    result = cancelResult(),
                    parserResult = null,
                )
                dispatchResponse(cancelResponse)
            } catch (e: Exception) {
                val errorResponse = NetworkResponse<T>(
                    json = null,
                    result = errorResult(e.message ?: "Unknown error"),
                    parserResult = null,
                )
                dispatchResponse(errorResponse)
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }

    private suspend fun dispatchResponse(response: NetworkResponse<T>) {
        if (builder.responseOnMain) {
            withContext(mainDispatcher) {
                builder.onResponse.invoke(response)
            }
        } else {
            builder.onResponse.invoke(response)
        }
    }

    private suspend fun performRequest(): HttpResponse {
        val finalUrl = buildFinalUrl()
        val globalHeaders = if (builder.needGlobalParams) {
            NetworkManager.getGlobalHeaders()
        } else {
            emptyMap()
        }

        return when (requestType) {
            RequestType.GET -> client.get(finalUrl) {
                applyCommonConfig(globalHeaders)
                builder.params?.forEach { (key, value) ->
                    parameter(key, value.toString())
                }
            }

            RequestType.JSON_POST -> client.post(finalUrl) {
                applyCommonConfig(globalHeaders)
                contentType(ContentType.Application.Json)
                val bodyMap = mutableMapOf<String, Any>()
                builder.params?.let { bodyMap.putAll(it) }
                val bodyJson = KtJson.encodeToString(bodyMap.toJsonElement())
                setBody(bodyJson)
            }

            RequestType.FORM_POST -> client.post(finalUrl) {
                applyCommonConfig(globalHeaders)
                contentType(ContentType.Application.FormUrlEncoded)
                val formParams = Parameters.build {
                    builder.params?.forEach { (key, value) ->
                        append(key, value.toString())
                    }
                }
                setBody(FormDataContent(formParams))
            }

            RequestType.MULTIPART_POST -> client.post(finalUrl) {
                applyCommonConfig(globalHeaders)
                setBody(MultiPartFormDataContent(formData {
                    builder.params?.forEach { (key, value) ->
                        append(key, value.toString())
                    }
                    if (builder.uploadFilePath.isNotEmpty()) {
                        append(
                            builder.uploadFileName.ifEmpty { "file" },
                            byteArrayOf(),
                            Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    builder.uploadFileMediaType.ifEmpty { "application/octet-stream" })
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"${builder.uploadFileName}\""
                                )
                            }
                        )
                    }
                }))
            }
        }
    }

    private fun HttpRequestBuilder.applyCommonConfig(globalHeaders: Map<String, String>) {
        // 全局 headers
        globalHeaders.forEach { (key, value) -> header(key, value) }
        // 请求自定义 headers
        builder.headers?.forEach { (key, value) -> header(key, value) }
        // 超时设置（通过 HttpTimeout 插件）
        if (builder.connectTimeout > 0 || builder.readTimeout > 0) {
            timeout {
                if (builder.connectTimeout > 0) connectTimeoutMillis = builder.connectTimeout
                if (builder.readTimeout > 0) requestTimeoutMillis = builder.readTimeout
            }
        }
    }

    /**
     * 将 [Any?] 递归转换为 [JsonElement]，支持嵌套 Map / List / 基础类型。
     *
     * 修复原来 `.value.toString()` 导致嵌套 Map 被序列化为 Kotlin toString 字符串的问题。
     */
    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is JsonElement -> this
        is Map<*, *> -> JsonObject(entries.associate { (k, v) -> k.toString() to v.toJsonElement() })
        is List<*> -> JsonArray(map { it.toJsonElement() })
        is Array<*> -> JsonArray(map { it.toJsonElement() })
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        else -> JsonPrimitive(toString())
    }

    private fun Map<String, Any>.toJsonElement(): JsonElement =
        JsonObject(entries.associate { (k, v) -> k to v.toJsonElement() })

    private fun buildFinalUrl(): String {
        var url = builder.url
        builder.forceUrlParams?.let { forceParams ->
            val separator = if (url.contains("?")) "&" else "?"
            val queryString = forceParams.entries.joinToString("&") { "${it.key}=${it.value}" }
            url = "$url$separator$queryString"
        }
        return url
    }
}

// ========================= HttpClient 工厂 =========================

/**
 * 创建默认的 HttpClient（用于 JSON 请求）。
 * 实际引擎由各平台 createEngine() 提供：
 * - Android → OkHttp
 * - iOS → Darwin
 * - OHOS → OhosHttpEngine
 *
 * 参考: https://ktor.io/docs/client-create-multiplatform-application.html
 */
private fun createDefaultHttpClient(): HttpClient {
    return HttpClient(createEngine()) {
        // 超时配置
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
    }
}


expect fun createEngine(): HttpClientEngine
