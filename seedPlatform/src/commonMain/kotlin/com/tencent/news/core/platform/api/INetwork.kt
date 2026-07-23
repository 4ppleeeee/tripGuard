@file:Suppress("NON_EXPORTABLE_TYPE")

package com.tencent.news.core.platform.api

import com.tencent.news.core.app.safe.NetworkParamsSafe
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.ResultCodeEx
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.concatUriParams
import com.tencent.news.core.extension.errorResult
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.successResult
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.network.NetworkRelayParams
import com.tencent.news.core.platform.network.NetworkRelayProxy
import com.tencent.news.core.serializer.KtJson


/**
 * 线程优先级
 */
enum class NetPriority(val code: Int) {
    UNDEFINE(-1),
    IMPORTANT(0),
    PRELOAD(1),
    GENERAL(2),
    IMAGE(3),
    LOG(4),
}


/**
 * 网络状态
 */
enum class NetState(val nameStr: String) {
    WIFI("wifi"),
    WWAN("wwan"),
    INAVAILABLE("inavailable")
}

fun NetState.isAvailable() = this != NetState.INAVAILABLE

interface NetStateChangeListener {
    fun netStateChanged(old: NetState, new: NetState)
}

interface INetwork {

    // 请求体为标准的json格式：一般接入层使用trpc框架时，都推荐用这个，数据格式更规范
    fun <T> jsonPostRequest(builder: NetworkBuilder<T>): INetworkRequest

    // 请求体为表单格式：接入层老接口都是这个，数据格式校验更宽松（不推荐用）
    fun <T> formPostRequest(builder: NetworkBuilder<T>): INetworkRequest

    fun <T> getRequest(builder: NetworkBuilder<T>): INetworkRequest

    fun <T> sseRequest(builder: NetworkBuilder<T>): INetworkRequest

    // 提交post表单携带文件上传
    fun <T> jsonMultiPostRequest(builder: NetworkBuilder<T>): INetworkRequest

    // 发送原始二进制流 POST 请求（如 Protobuf 数据），Content-Type 为 application/octet-stream
    fun <T> streamPostRequest(builder: NetworkBuilder<T>): INetworkRequest

    // 发送 Protobuf 请求，和 JSON/Form/Multipart 同属请求体编码形式。
    fun <T> postPb(builder: PBNetworkBuilder<T>): INetworkRequest {
        builder.onResponse.invoke(PBNetworkResponse(errorResult("pbPostRequest 未实现"), null))
        return DefaultNetworkRequest()
    }

    fun netState(): NetState

    fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {}

    fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {}

    fun <T> pbPostRequest(builder: PBNetworkBuilder<T>): INetworkRequest = postPb(builder)

}

typealias HeaderParams = Map<String, String>

interface IPlatformNetParamsInjector {
    fun injectGlobalHeaders(headers: HeaderParams)

    fun injectGlobalParams(params: Map<String, Any>)

    fun injectGlobalCookies(cookies: String)

    fun injectUserAgent(ua: String)
}

interface IPlatformNetParamsProvider {
    fun getGlobalHeader(key: String): String?

    fun getGlobalParam(key: String): Any?

    fun getGlobalCookies(): String

    fun getUserAgent(): String
}


interface INetworkBuilder<T> {
    // 发起网络请求后，当前request对象会保存在这里
    fun curRequest(): INetworkRequest?

    // 根据builder自身的 useJsonPost 配置发起请求（调用 executeJsonPost 或 executeFormPost）
    fun execute(): INetworkRequest

    // 强制发起json格式请求：一般新接口用这个，格式更规范
    fun executeJsonPost(): INetworkRequest

    // 强制发起form格式请求：老的接口用这个，例如信息流channel_feed
    fun executeFormPost(): INetworkRequest

    // 强制发起get请求：
    fun executeGet(): INetworkRequest
}

fun interface INetworkParser<T> {

    fun onParseJson(json: String): T?

}

fun originJsonParser(): INetworkParser<String> = createParser { it }

fun <T> createParser(action: (json: String) -> T?): INetworkParser<T> {
    return object : INetworkParser<T> {
        override fun onParseJson(json: String) = action(json)
    }
}

typealias JsonNetworkBuilder = NetworkBuilder<String>

inline fun <reified T> ktJsonParser(): INetworkParser<T> =
    createParser { KtJson.safeDecode(it) }

inline fun <reified T> quickRequest(
    url: String,
    params: Map<String, Any>? = null,
    needGlobalParams: Boolean = true,
    useJsonPost: Boolean = true,
    responseOnMain: Boolean = false,
    header: HeaderParams? = null,
    connectTimeout: Long = -1,
    readTimeout: Long = -1,
    priority: NetPriority? = null,
    forceGet: Boolean = false,
    parser: INetworkParser<T> = ktJsonParser(),
    ignoreResponseFormat: Boolean = false,
    noinline onResponse: NetworkCallback<T> = {},
): INetworkRequest {
    val builder = NetworkBuilder(
        url = url,
        parser = parser,
        params = params,
        needGlobalParams = needGlobalParams,
        useJsonPost = useJsonPost,
        responseOnMain = responseOnMain,
        connectTimeout = connectTimeout,
        readTimeout = readTimeout,
        headers = header,
        onResponse = onResponse
    ).apply {
        priority?.let { this.priority = it }
        this.ignoreResponseFormat = ignoreResponseFormat
    }
    return if (forceGet) builder.executeGet() else builder.execute()
}


fun quickJsonRequest(
    url: String,
    params: Map<String, Any>? = null,
    needGlobalParams: Boolean = true,
    useJsonPost: Boolean = true,
    responseOnMain: Boolean = false,
    onResponse: NetworkCallback<String> = {},
) {
    NetworkBuilder(
        url = url,
        parser = originJsonParser(),
        params = params,
        needGlobalParams = needGlobalParams,
        useJsonPost = useJsonPost,
        responseOnMain = responseOnMain,
        onResponse = onResponse
    ).execute()
}

// 简易get请求，一般用于上报；不关心返回数据，知道请求成功即可
fun quickGetReport(
    url: String,
    params: Map<String, Any>? = null,
    headers: HeaderParams? = null,
    needGlobalParams: Boolean = false,
    responseOnMain: Boolean = false,
    onResponse: (succeed: Boolean) -> Unit = {},
) {
    NetworkBuilder(
        url = url,
        parser = originJsonParser(),
        params = params,
        headers = headers,
        needGlobalParams = needGlobalParams,
        responseOnMain = responseOnMain,
        onResponse = { onResponse.invoke(it.result.succeed) }
    ).executeGet()
}

@Suppress("NON_EXPORTABLE_TYPE")

open class NetworkBuilder<T> constructor(
    var url: String,
    var parser: INetworkParser<T>?,    // parser必须有，如果是保留原始json不额外解析，可以用 originJsonParser()

    var params: Map<String, Any>? = null,
    var headers: HeaderParams? = null,
    var dataProcessors: List<ITNDataProcessor<T>>? = null, // 数据拦截器
    // 当前请求缓存key，如果没有，则代表不需要使用缓存
    var cacheRequestKey: String? = null,

    var readTimeout: Long = -1,
    var connectTimeout: Long = -1,
    var needGlobalParams: Boolean = true,  //  不附加全局参数
    var useJsonPost: Boolean = true,
    var responseOnMain: Boolean = false,
    var onResponse: NetworkCallback<T> = {},
) : INetworkBuilder<T> {

    var relayParams: NetworkRelayParams? = null

    // 接入层部分参数需要强制放到url上，在这里添加：
    var forceUrlParams: Map<String, String>? = null

    // post请求携带文件
    var uploadFilePath: String = ""

    // post请求携带文件的参数名字
    var uploadFileName: String = ""
    var uploadFileMediaType: String = ""

    // 网络完整构建流程：builder -> build（构建请求对象，但还没发出去）-> execute
    // 这两个回调是为了兼容宿主旧逻辑：
    var onBeforeBuild: ((tnBuilder: Any) -> Unit)? = null // 安卓是 TNRequestBuilder
    var onBeforeExecute: ((INetworkRequest) -> Unit)? = null

    // 线程优先级
    var priority: NetPriority? = null

    // 是否忽略回包格式
    var ignoreResponseFormat: Boolean = false

    var networkRequest: INetworkRequest? = null

    fun updateParser(action: (json: String) -> IKmmKeep) { // 暂时用来规避泛型赋值问题
        parser = createParser { action(it) as? T }
    }

    override fun curRequest(): INetworkRequest? {
        return networkRequest
    }

    override fun execute(): INetworkRequest {
        return if (useJsonPost) {
            executeJsonPost()
        } else {
            executeFormPost()
        }
    }

    override fun executeJsonPost(): INetworkRequest {
        return appNetwork().jsonPostRequest(this).apply {
            networkRequest = this
        }
    }

    override fun executeFormPost(): INetworkRequest {
        return appNetwork().formPostRequest(this).apply {
            networkRequest = this
        }
    }

    override fun executeGet(): INetworkRequest {
        return appNetwork().getRequest(this).apply {
            networkRequest = this
        }
    }

    fun executeStreamPost(): INetworkRequest {
        return appNetwork().streamPostRequest(this).apply {
            networkRequest = this
        }
    }

    fun buildRequestLog(): String {
        return url.concatUriParams(params)
    }

    fun executeMock(mockJson: String, forceFailed: Boolean = false): INetworkRequest {
        val mockResponse = NetworkResponse<T>(
            json = mockJson,
            result = if (forceFailed) errorResult() else successResult("mockJson"),
            parserResult = parser?.onParseJson(mockJson) as? T
        )
        onResponse.invoke(mockResponse)
        return DefaultNetworkRequest()
    }

    fun asRelay(id: String? = null, maxAge: Long, isLastOne: Boolean) = apply {
        this.relayParams =
            NetworkRelayParams(enabled = true, maxAge = maxAge, isLastOne = isLastOne, id = id)
    }

}


class LocalNetworkBuilder<T>(
    private val localData: () -> T?,
    private val onResponse: (INetworkResponse<T>, T?) -> Unit,
) : INetworkBuilder<T> {

    override fun curRequest(): INetworkRequest? {
        return null
    }

    override fun execute(): INetworkRequest {
        return executeLocal()
    }

    override fun executeJsonPost(): INetworkRequest {
        return executeLocal()
    }

    override fun executeFormPost(): INetworkRequest {
        return executeLocal()
    }

    override fun executeGet(): INetworkRequest {
        return executeLocal()
    }

    private fun executeLocal(): INetworkRequest {
        val result = localData()
        val localResponse = NetworkResponse("", successResult("local_data"), result)
        onResponse.invoke(localResponse, result)
        return DefaultNetworkRequest()
    }

}

fun NetworkBuilder<*>?.isValid(): Boolean {
    return this?.url.isNotNullOrEmpty()
}


interface INetworkRequest {
    fun execute()
    fun cancel()
}


open class DefaultNetworkRequest : INetworkRequest {
    override fun execute() {
    }

    override fun cancel() {
    }
}

typealias NetworkCallback<T> = (INetworkResponse<T>) -> Unit


interface INetworkResponse<T> {
    val json: String?
    val result: ResultEx
    val parserResult: T?
    val headers: HeaderParams? // 【注意】resp里，约定header的key大小写不敏感，各端统一实现时将key转为小写

    fun isValid(): Boolean = !json.isNullOrEmpty() && result.succeed
    fun errorMsg(): String = "errorCode:${result.errorCode} error:${result.msg}"
}


data class NetworkResponse<T> constructor(
    override val json: String?,
    override val result: ResultEx,
    override val parserResult: T?,
    override val headers: HeaderParams? = null,
) : INetworkResponse<T>

fun appNetwork(): INetwork {
    val network = QnPlatformLogic.network
    if (network != null) {
        return SafeCheckNetworkProxy(NetworkRelayProxy(network))
    }
    return network ?: errorAppNetwork
}

private class SafeCheckNetworkProxy(private val target: INetwork) : INetwork by target {

    override fun <T> jsonPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        NetworkParamsSafe.checkParamsValid(builder.params)
        return target.jsonPostRequest(builder)
    }

    override fun <T> formPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        NetworkParamsSafe.checkParamsValid(builder.params)
        return target.formPostRequest(builder)
    }

    override fun <T> sseRequest(builder: NetworkBuilder<T>): INetworkRequest {
        NetworkParamsSafe.checkParamsValid(builder.params)
        return target.sseRequest(builder)
    }
}

abstract class SameImplNetwork : INetwork {

    // 兜底实现，返回错误
    abstract fun <T> defaultRequestImpl(builder: NetworkBuilder<T>): INetworkRequest

    final override fun <T> jsonPostRequest(builder: NetworkBuilder<T>) = defaultRequestImpl(builder)
    final override fun <T> formPostRequest(builder: NetworkBuilder<T>) = defaultRequestImpl(builder)
    final override fun <T> getRequest(builder: NetworkBuilder<T>) = defaultRequestImpl(builder)
    final override fun <T> sseRequest(builder: NetworkBuilder<T>) = defaultRequestImpl(builder)
    final override fun <T> jsonMultiPostRequest(builder: NetworkBuilder<T>) =
        defaultRequestImpl(builder)

    final override fun <T> streamPostRequest(builder: NetworkBuilder<T>) =
        defaultRequestImpl(builder)

    // 兜底实现，返回错误
    final override fun <T> postPb(builder: PBNetworkBuilder<T>): INetworkRequest {
        builder.onResponse.invoke(PBNetworkResponse(errorResult("pbPostRequest 未实现"), null))
        return DefaultNetworkRequest()
    }
}

private val errorAppNetwork by lazy { ErrorAppNetwork() }

private class ErrorAppNetwork : SameImplNetwork() {
    override fun <T> defaultRequestImpl(builder: NetworkBuilder<T>): INetworkRequest {
        builder.onResponse.invoke(
            NetworkResponse(
                json = "",
                result = ResultEx(
                    succeed = false,
                    msg = "QnPlatformLogic.network 未初始化",
                    errorCode = ResultCodeEx.ERROR
                ),
                parserResult = null
            )
        )

        return DefaultNetworkRequest()
    }

    override fun netState(): NetState = NetState.WIFI
}


interface ITNDataProcessor<T> {
    fun process(data: T?, originStr: String?)
}
