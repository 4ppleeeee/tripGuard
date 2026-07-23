package com.tencent.news.core.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.news.core.compose.page.StructComposePage
import com.tencent.news.core.compose.page.StructComposePage4VM
import com.tencent.news.core.platform.AppStateManager

/**
 * 监听登录状态变化事件，返回一个递增的 key。
 *
 * 当用户切换账号（退出登录后重新登录）时，key 值变化会触发
 * [remember] 重建 ViewModel，
 * 确保使用新账号的 personId 请求数据。
 *
 * 设置给 [StructComposePage] 或 [StructComposePage4VM] 的 key 参数
 */
@Composable
fun rememberLoginKey(): Int {
    var loginKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        AppStateManager.loginStatusFlow.collect {
            loginKey++
        }
    }
    return loginKey
}