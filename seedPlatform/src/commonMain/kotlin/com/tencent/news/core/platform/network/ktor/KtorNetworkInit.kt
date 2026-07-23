package com.tencent.news.core.platform.network.ktor

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.network.NetworkManager

/**
 * 初始化网络模块（基于 Ktor）
 * 在 Application.onCreate 中调用即可
 */
fun initKtorNetwork() {
    NetworkManager.init(KtorNetwork())
    QnPlatformLogic.network = NetworkManager.network
}
