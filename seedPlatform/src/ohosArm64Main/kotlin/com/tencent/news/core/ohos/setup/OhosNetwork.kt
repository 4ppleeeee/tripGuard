package com.tencent.news.core.ohos.setup

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.cancelResult
import com.tencent.news.core.extension.errorResult
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.successResult
import com.tencent.news.core.platform.PlatformNetworkLog
import com.tencent.news.core.ohos.setup.knoi.callbacks.OhosHttpRequestService
import com.tencent.news.core.ohos.setup.knoi.callbacks.QNHttpPerformance
import com.tencent.news.core.ohos.setup.knoi.callbacks.QNHttpResponse
import com.tencent.news.core.ohos.setup.knoi.callbacks.QNSseResponse
import com.tencent.news.core.ohos.setup.knoi.consumer.ohosNetworkService
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.DefaultNetworkRequest
import com.tencent.news.core.platform.api.HeaderParams
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.INetworkParser
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.platform.api.INetworkResponse
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.PBNetworkBuilder
import com.tencent.news.core.platform.api.PBNetworkResponse
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val TAG = "OhosNetwork"

internal typealias HttpExecutor<T> = suspend NetworkBuilder<T>.(request: OhosNetworkRequest<T>) -> Unit

private val coroutineScope by lazy {
    CoroutineScope(CoroutineName("ohosNetwork") + SupervisorJob() + Dispatchers.Default)
}

/**
 * 注入鸿蒙端 [INetwork] 实现。
 *
 * 前置依赖：
 *  - [ohosRequestService] 已由宿主通过 knoi @KNCallback 注入（ArkTS 侧 HTTP 能力）。
 *  - [ohosNetworkService] 已由 knoi @ServiceConsumer 自动绑定（网络状态订阅）。
 *  - `Dispatchers.Main` 已在 HarmonyStartupProvider 中通过 `initMainHandler(getEnv()!!)` 绑定。
 *
 * @param requestService ArkTS 侧注入的 HTTP 请求服务实例
 */
fun setupOhosNetwork(requestService: OhosHttpRequestService) {
    QnPlatformLogic.network = object : INetwork {

        private var currentNetState = NetState.WIFI
        private val netListeners = mutableListOf<NetStateChangeListener>()

        init {
            subscribeOhosNetState()
        }

        override fun <T> jsonPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
            return execInternal(builder) {
                requestService.jsonPostRequest(builder) { response ->
                    response.asNetworkResponse(builder, it)
                }
            }
        }

        override fun <T> formPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
            return execInternal(builder) {
                requestService.formPostRequest(builder) { response ->
                    response.asNetworkResponse(builder, it)
                }
            }
        }

        override fun <T> getRequest(builder: NetworkBuilder<T>): INetworkRequest {
            return execInternal(builder) {
                requestService.getRequest(builder) { response ->
                    response.asNetworkResponse(builder, it)
                }
            }
        }

        override fun <T> sseRequest(builder: NetworkBuilder<T>): INetworkRequest {
            return execInternal(builder) { request ->
                requestService.sseRequest(builder) { sseResponse ->
                    sseResponse.asNetworkResponse(builder, request)
                }
            }
        }

        override fun <T> jsonMultiPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
            return execInternal(builder) {
                requestService.multiJsonPostRequest(builder) { response ->
                    response.asNetworkResponse(builder, it)
                }
            }
        }

        override fun <T> streamPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
            return execInternal(builder) {
                requestService.streamPostRequest(builder) { response ->
                    response.asNetworkResponse(builder, it)
                }
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        override fun <T> postPb(builder: PBNetworkBuilder<T>): INetworkRequest {
            val bodyBase64 = runCatching {
                Base64.encode(builder.bodyEncoder())
            }.getOrDefault("")

            val request = OhosNetworkRequest<T>(coroutineScope, null) { _ -> }
            request.execute()

            requestService.pbPostRequest(
                url = builder.url,
                bodyBase64 = bodyBase64,
                headers = builder.headers ?: emptyMap(),
            ) { response ->
                when (response) {
                    is QNHttpResponse.Success -> {
                        val rawBytes = runCatching {
                            Base64.decode(response.data)
                        }.getOrNull()
                        val parsedData = rawBytes?.let {
                            runCatching { builder.bodyDecoder(it) }.getOrNull()
                        }
                        builder.onResponse.invoke(
                            PBNetworkResponse(
                                result = successResult(),
                                parsedData = parsedData,
                                rawBody = rawBytes,
                            )
                        )
                    }

                    is QNHttpResponse.Failed -> {
                        builder.onResponse.invoke(
                            PBNetworkResponse(
                                result = errorResult(msg = response.errMsg),
                                parsedData = null,
                                serverCode = response.errCode,
                                serverMsg = response.errMsg,
                            )
                        )
                    }
                }
            }
            return request
        }

        private fun <T> execInternal(
            builder: NetworkBuilder<T>,
            executor: HttpExecutor<T>,
        ): INetworkRequest {
            builder.onBeforeBuild?.invoke(builder)
            val request = OhosNetworkRequest(coroutineScope, builder, executor)
            builder.onBeforeExecute?.invoke(request)
            builder.networkRequest = request
            request.execute()
            return request
        }

        override fun netState(): NetState = currentNetState

        override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
            netListeners.add(netStatusListener)
        }

        override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
            netListeners.remove(netStatusListener)
        }

        private fun subscribeOhosNetState() {
            runCatching {
                ohosNetworkService.subscribeNetState { stateName ->
                    val netState = stateName.toNetState()
                    val oldState = currentNetState
                    currentNetState = netState
                    if (oldState != netState) {
                        netListeners.toList().forEach { listener ->
                            runCatching {
                                listener.netStateChanged(oldState, netState)
                            }
                        }
                    }
                    PlatformNetworkLog.debug(TAG) { "net state changed: $stateName -> $netState" }
                }
            }.onFailure {
                PlatformNetworkLog.debug(TAG) { "subscribe net state failed: ${it.message}" }
            }
        }
    }
}

private fun String.toNetState(): NetState {
    return NetState.values().firstOrNull { it.nameStr == this } ?: NetState.WIFI
}

// ========================= OhosNetworkRequest =========================

internal class OhosNetworkRequest<T>(
    val scope: CoroutineScope,
    val builder: NetworkBuilder<T>?,
    val executor: HttpExecutor<T>,
) : INetworkRequest {

    private var job: Job? = null
    internal var canceled: Boolean = false
    internal var performance: QNHttpPerformance = QNHttpPerformance()

    override fun execute() {
        performance.onRequestStart()
        job = scope.launch {
            runCatching {
                if (builder != null) {
                    executor.invoke(builder, this@OhosNetworkRequest)
                }
            }.onFailure { error ->
                performance.onRequestError(error)
            }
        }
    }

    override fun cancel() {
        canceled = true
        job?.cancel()
    }
}

// ========================= QNHttpResponse 扩展 =========================

private fun <T> QNHttpResponse.asNetworkResponse(
    builder: NetworkBuilder<T>,
    request: OhosNetworkRequest<T>,
) {
    val response = when (this) {
        is QNHttpResponse.Success -> OhosNetworkResponse<T>(0, "", data)
        is QNHttpResponse.Failed -> OhosNetworkResponse<T>(errCode, errMsg, "")
    }
    response.jsonParse(request, builder.parser).dispatchResult(request, builder)
}

private fun <T> QNSseResponse.asNetworkResponse(
    builder: NetworkBuilder<T>,
    request: OhosNetworkRequest<T>,
) {
    when (this) {
        is QNSseResponse.Event -> {
            OhosNetworkResponse<T>(0, "", data).dispatchResult(request, builder, isDone = false)
        }

        is QNSseResponse.Success -> {
            request.performance.onRequestEnd()
        }

        is QNSseResponse.Failed -> {
            OhosNetworkResponse<T>(errCode, errMsg, "").dispatchResult(request, builder)
        }
    }
}

// ========================= OhosNetworkResponse =========================

internal class OhosNetworkResponse<T>(
    internal val errorCode: Int,
    internal val errorMsg: String?,
    internal val originJson: String,
    internal val headers: HeaderParams? = null,
) : IKmmKeep {

    private var parserResult: T? = null

    fun jsonParse(
        request: OhosNetworkRequest<T>,
        parser: INetworkParser<T>?,
    ): OhosNetworkResponse<T> {
        PlatformNetworkLog.debug(TAG) { "start json parser: ${request.builder?.url}" }
        request.performance.onMeasureParseMillis {
            parserResult = parser?.onParseJson(originJson)
        }
        return this
    }

    fun dispatchResult(
        request: OhosNetworkRequest<T>,
        builder: NetworkBuilder<T>,
        isDone: Boolean = true,
    ): OhosNetworkResponse<T> {
        PlatformNetworkLog.debug(TAG) { "start dispatch response: ${request.builder?.url}" }

        val resultEx = when {
            request.canceled -> cancelResult()
            0 == errorCode -> successResult(msg = errorMsg.getNonNull())
            else -> errorResult(msg = errorMsg.getNonNull())
        }

        builder.dispatchOnThread {
            request.performance.onMeasureCallbackMills {
                onResponse(builder, resultEx)
            }
            if (isDone) {
                request.performance.onRequestEnd()
            }
        }
        return this
    }

    private fun NetworkBuilder<T>.dispatchOnThread(block: () -> Unit) {
        // 【注意】鸿蒙端 knoi callback 必须在主线程回调，否则找不到 ets callback
        if (responseOnMain) {
            coroutineScope.launch(Dispatchers.Main) { block() }
        } else {
            block()
        }
    }

    private fun onResponse(builder: NetworkBuilder<T>, resultEx: ResultEx) {
        builder.onResponse.invoke(object : INetworkResponse<T> {
            override val json: String = originJson
            override val result: ResultEx = resultEx
            override val parserResult: T? = this@OhosNetworkResponse.parserResult
            override val headers: HeaderParams? = this@OhosNetworkResponse.headers
        })
    }
}
