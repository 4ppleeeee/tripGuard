package com.tencent.news.core.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.base.EdgeInsets
import com.tencent.news.core.compose.scaffold.registry.LocalComposeSafeAreaInsetsFlow
import com.tencent.news.core.platform.api.ScreenOrientation
import com.tencent.news.core.platform.api.appWindow

/**
 * 调用Compose相关能力的工具，如果放在qnCore里加了internal会调用不到，不加鸿蒙编译会失败，统一放这里
 */
object ComposeUtils {
    @Composable
    fun getSafeAreaHeight(): Float {
        return rememberSafeAreaBottomHeight()
    }

    @Composable
    fun rememberSafeAreaBottomHeight(): Float {
        val configurationSafeAreaBottom = LocalConfiguration.current.safeAreaInsets.bottom
        var safeAreaBottom by remember { mutableStateOf(configurationSafeAreaBottom) }
        LaunchedEffect(configurationSafeAreaBottom) {
            safeAreaBottom = configurationSafeAreaBottom
        }
        val safeAreaInsetsFlow = LocalComposeSafeAreaInsetsFlow.current
        LaunchedEffect(safeAreaInsetsFlow) {
            safeAreaInsetsFlow?.collect { data ->
                val safeAreaInsets = data["safeAreaInsets"] as? String
                val bottom = safeAreaInsets?.safeAreaBottomOrNull()
                if (bottom != null) {
                    safeAreaBottom = bottom
                }
            }
        }
        return safeAreaBottom
    }

    @Composable
    fun EdgeInsets.toPaddingValues(): PaddingValues {
        val orientation: ScreenOrientation = appWindow().getScreenOrientation()

        return when (orientation) {
            ScreenOrientation.PORTRAIT -> {
                PaddingValues(bottom = this.bottom.dp)
            }

            else -> {
                PaddingValues(start = this.top.dp, end = this.bottom.dp)
            }
        }
    }

    private fun String.safeAreaBottomOrNull(): Float? {
        return split(" ").getOrNull(2)?.toFloatOrNull()
    }

}
