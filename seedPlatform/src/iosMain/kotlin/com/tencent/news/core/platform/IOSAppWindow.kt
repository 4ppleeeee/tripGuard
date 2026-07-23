package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.IAppWindow

/**
 * 由 iOS 宿主侧调用，注入原生窗口管理实现。
 * Swift 侧直接实现 IAppWindow 协议，无需中间桥接层。
 */
fun setupIOSAppWindow(appWindow: IAppWindow) {
    QnPlatformLogic.appWindow = appWindow
}