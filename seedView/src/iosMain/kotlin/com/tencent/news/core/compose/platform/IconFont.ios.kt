package com.tencent.news.core.compose.platform

import com.tencent.news.core.platform.api.iosComposeBridge

actual fun getIconFontMapping(): Map<String, String> {
    return iosComposeBridge?.getIconFontMapping() ?: emptyMap()
}
