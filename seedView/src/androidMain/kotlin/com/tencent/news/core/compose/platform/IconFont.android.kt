package com.tencent.news.core.compose.platform

import com.tencent.news.core.compose.andComposeBridge

actual fun getIconFontMapping(): Map<String, String> {
    return andComposeBridge?.getIconFontMapping() ?: emptyMap()
}