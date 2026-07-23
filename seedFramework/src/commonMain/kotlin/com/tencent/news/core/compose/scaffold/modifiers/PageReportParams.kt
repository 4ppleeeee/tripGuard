package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.news.core.dt.constants.DtPageId
import com.tencent.news.core.extension.toJson
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.platform.api.DynamicParamsProvider

data class PageReportParams(
    val pageId: DtPageId,
    val contentId: String = "",
    val logicDestroy: Boolean = false,
    val pageParams: Map<String, Any>? = null,
    val dynamicParamsProvider: DynamicParamsProvider? = null
) {
    fun asReportInfo(): Any {
        // Android、iOS支持直接传对象，Harmony不支持，暂时用JSONObject
        if (!isHarmonyPlatform()) {
            return this
        }
        return mutableMapOf<String, Any?>().apply {
            put("pageId", pageId.id)
            put("contentId", contentId)
            put("pageParams", pageParams.toJson())
        }.toJson()
    }
}