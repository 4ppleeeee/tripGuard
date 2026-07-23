package com.tencent.news.core.list.api

import com.tencent.news.core.extension.errorResult
import com.tencent.news.core.extension.successResult
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.DefaultNetworkRequest
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 支持 suspend lambda 的网络请求构建器
 *
 * 类似于 [com.tencent.news.core.platform.api.LocalNetworkBuilder]，
 * 但支持在 execute 时启动协程执行 suspend 方法，适用于需要通过协程获取数据的场景。
 *
 * 使用场景举例：
 * - PB 网络请求（suspend 方法）
 * - 需要异步 IO 操作的数据获取
 *
 * @param scope 协程作用域，用于启动 suspend 任务
 * @param suspendAction 需要执行的 suspend 方法，返回 [StructPageWidget] 或 null
 * @param onResponse 请求回调，与框架的网络回调链路对齐
 */
class SuspendNetworkBuilder(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val suspendAction: suspend () -> StructPageWidget?,
) : NetworkBuilder<StructPageWidget>("suspend_network", null) {

    private var job: Job? = null

    override fun curRequest(): INetworkRequest? = null

    override fun execute(): INetworkRequest {
        return executeSuspend()
    }

    override fun executeJsonPost(): INetworkRequest {
        return executeSuspend()
    }

    override fun executeFormPost(): INetworkRequest {
        return executeSuspend()
    }

    override fun executeGet(): INetworkRequest {
        return executeSuspend()
    }

    private fun executeSuspend(): INetworkRequest {
        val request = SuspendNetworkRequest()
        job = scope.launch {
            try {
                val result = suspendAction()
                val response = NetworkResponse(
                    json = "suspend_local_data", // 后面有些判空的地方，加一个占位
                    result = successResult("suspend_local_data"),
                    parserResult = result,
                )
                onResponse.invoke(response)
            } catch (e: Exception) {
                val response = NetworkResponse<StructPageWidget>(
                    json = "",
                    result = errorResult(e.message ?: "suspend action failed"),
                    parserResult = null,
                )
                onResponse.invoke(response)
            }
        }
        request.job = job
        return request
    }

    /**
     * 支持取消的网络请求
     */
    private class SuspendNetworkRequest : DefaultNetworkRequest() {
        var job: Job? = null
        override fun cancel() {
            job?.cancel()
        }
    }
}
