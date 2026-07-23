package com.tencent.kmm.demo.startup.sdk.kuikly

import android.app.Application
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext

fun initKuiklyAdapter(context: StartupContext, onResult: OnReceiveStartupTaskResult<Unit>) {

    val application = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    with(KuiklyRenderAdapterManager) {
        krImageAdapter = KRImageAdapter(application)
        krLogAdapter = KRLogAdapter
        krUncaughtExceptionHandlerAdapter = KRUncaughtExceptionHandlerAdapter
        krColorParseAdapter = KRColorParserAdapter(application)
        krRouterAdapter = KRRouterAdapter
        // 评论输入框表情后置处理（仅 processor="comment_input" 时生效）
        krTextPostProcessorAdapter = KRTextPostProcessorAdapter(application)
    }
}
