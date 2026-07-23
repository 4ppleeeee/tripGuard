@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.view

import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.getCurTimeMillis

internal object ReComposeChecker {
    private var lastCheckTime = getCurTimeMillis()
    private var reComposeCount = 0

    internal fun trace(limitCount: Int = 10) {
        if (!isDebug()) {
            return
        }
        if (getCurTimeMillis() - lastCheckTime > 1000) {
            lastCheckTime = getCurTimeMillis()
            reComposeCount = 0
        }
        if (reComposeCount > limitCount) {
            lastCheckTime = getCurTimeMillis()
            reComposeCount = 0
            debugToast("【性能警告⚠️】1秒内重组次数 >${limitCount}")
        }
        reComposeCount++
    }
}