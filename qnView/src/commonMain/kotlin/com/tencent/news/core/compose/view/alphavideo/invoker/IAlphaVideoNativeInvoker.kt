package com.tencent.news.core.compose.view.alphavideo.invoker

import com.tencent.news.core.compose.view.alphavideo.QnAlphaVideoState
import com.tencent.news.core.list.trace.AlphaVideoLog

typealias Action = () -> Unit

/**
 * Native 侧 Invoker 接口
 *
 * 实现 bindAction 将自身方法绑定到 Compose Invoker 的 action 闭包上，
 * 使得业务侧调用 Compose Invoker 时能穿透到 Native 侧。
 */
internal interface IAlphaVideoNativeInvoker {
    fun bindAction(state: QnAlphaVideoState)
}

/**
 * Compose 侧 Invoker 标记接口
 */
internal interface IAlphaVideoComposeInvoker

/** 日志扩展：Compose 侧调用 */
internal fun IAlphaVideoComposeInvoker.log(msg: String) {
    AlphaVideoLog.debug("ComposeInvoker") { "业务侧 -> ComposeView $this msg:$msg" }
}

/** 日志扩展：Native 侧调用 */
internal fun IAlphaVideoNativeInvoker.log(msg: String) {
    AlphaVideoLog.debug("NativeInvoker") { "ComposeView -> NativeView $this msg:$msg" }
}
