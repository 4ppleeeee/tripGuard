package com.tencent.news.core.compose.utils

import androidx.compose.runtime.Composable
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.compose.platform.safeAreaHeight

/**
 * 调用Compose相关能力的工具，如果放在qnCore里加了internal会调用不到，不加鸿蒙编译会失败，统一放这里
 */
object ComposeUtils {
    @Composable
    fun getSafeAreaHeight(): Float {
        return if (isIOSPlatform() || isHarmonyPlatform()) {
            return safeAreaHeight()
        } else {
            0f
        }
    }

}