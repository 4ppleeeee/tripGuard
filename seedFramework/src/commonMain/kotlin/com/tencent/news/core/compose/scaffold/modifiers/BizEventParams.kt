package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.news.core.dt.constants.DtBizEvent
import com.tencent.news.core.extension.toJson
import com.tencent.news.core.isHarmonyPlatform

/**
 * 大同自定义事件参数
 */
data class BizEventParams(
    val event: DtBizEvent,
    val params: Map<String, Any>? = null,
) {
    fun asReportInfo(): Any {
        // Android、iOS支持直接传对象，Harmony不支持，暂时用JSONObject
        if (!isHarmonyPlatform()) {
            return this
        }
        return mutableMapOf<String, Any?>().apply {
            put("eventId", event.eventId)
            put("params", params.toJson())
        }.toJson()
    }
}