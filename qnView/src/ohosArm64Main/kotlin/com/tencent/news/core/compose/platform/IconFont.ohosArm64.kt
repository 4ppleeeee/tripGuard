package com.tencent.news.core.compose.platform

import com.tencent.news.core.ohos.setup.knoi.consumer.ohosResService

actual fun getIconFontMapping(): Map<String, String> {
    return ohosResService.getIconFontMapping().mapKeys { (key, _) ->
        key.removePrefix("icon_")
    }
}