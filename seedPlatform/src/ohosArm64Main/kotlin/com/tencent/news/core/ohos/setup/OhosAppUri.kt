package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.OhosPlatformUri
import com.tencent.news.core.platform.QnPlatformLogic

/**
 * 注入鸿蒙端 IAppUri 实现。
 *
 * 直接复用 [OhosPlatformUri]，底层使用 Ktor URL parser 在 Kotlin/Native 内完成
 * scheme/host/path/query 解析，避免每次 URI 访问跨 runtime 调用 ArkTS。
 * 与 Android 的 android.net.Uri、iOS 的 NSURLComponents 对齐，业务可通过
 * `appUri().parseUri(...)` / `safeParseUri()` 正常使用。
 *
 * 注入后，QnPlatformLogicChecker 对 IAppUri 的检测会展示为「已注入」，
 * 且 `QnPlatformLogic.appUri` 的值将领先于 commonMain 中的 fallback（getPlatformUri()）。
 */
fun setupOhosAppUri() {
    QnPlatformLogic.appUri = OhosPlatformUri
}
