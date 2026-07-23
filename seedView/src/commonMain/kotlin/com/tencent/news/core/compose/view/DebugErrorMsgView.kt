@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.platform.fsp
import com.tencent.news.core.extension.throwDebugException
import com.tencent.news.core.platform.api.isDebug

@Composable
fun DebugErrorMsgView(msg: () -> String, error: Throwable? = null) {
    if (!isDebug()) return

    val errorInfo = if (error != null) "\n${error.throwDebugException()}" else ""
    QnText(
        text = msg() + errorInfo,
        color = Color.Red,
        fontSize = 10.fsp
    )
}