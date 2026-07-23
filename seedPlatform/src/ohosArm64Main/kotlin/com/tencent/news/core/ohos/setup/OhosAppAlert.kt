package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppAlert
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppAlert = JSValue

/**
 * 鸿蒙端 appAlert 注入。
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧的 toast/dialog 实现桥接到 KMP 层的 IAppAlert。
 */
fun setupOhosAppAlert(alert: IOhosAppAlert) {
    QnPlatformLogic.appAlert = alert.asOhosAppAlert()
}

/**
 * ArkTS 侧 Alert 回调接口，直接继承 IAppAlert。
 * knoi 会自动为 IAppAlert 的所有方法生成 ArkTS 侧的接口定义和桥接代码。
 */
@KNCallback
interface OhosAppAlert : IAppAlert
