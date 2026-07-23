package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.news.core.platform.getCurTimeMillis

/** 默认点击防抖间隔（毫秒） */
private const val DEFAULT_DEBOUNCE_MS = 300L

/**
 * 创建一个带防抖的点击回调，在指定时间间隔内的重复点击会被忽略。
 *
 * @param debounceMs 防抖间隔（毫秒），默认 300ms
 * @param onClick 实际点击回调
 * @return 包装后的防抖点击回调
 */
@Composable
fun rememberDebouncedClick(
    debounceMs: Long = DEFAULT_DEBOUNCE_MS,
    onClick: () -> Unit,
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    return {
        val now = getCurTimeMillis()
        if (now - lastClickTime >= debounceMs) {
            lastClickTime = now
            onClick()
        }
    }
}
