package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.ExternalApp
import com.tencent.news.core.platform.api.IAppInstallInfo

/**
 * 注入鸿蒙端 IAppInstallInfo 实现。
 *
 * 鸿蒙端目前暂无检测第三方 App 的通用实现，统一返回 false。
 * 如后续需要真实检测，可通过 knoi 回调到 ArkTS 侧，使用 bundleManager.canOpenLink 实现。
 */
fun setupOhosAppInstallInfo() {
    QnPlatformLogic.appInstallInfo = OhosAppInstallInfo()
}

internal class OhosAppInstallInfo : IAppInstallInfo {
    override fun isAppInstalled(app: ExternalApp): Boolean {
        // 鸿蒙端暂未支持通用 App 安装状态检测
        return false
    }
}
