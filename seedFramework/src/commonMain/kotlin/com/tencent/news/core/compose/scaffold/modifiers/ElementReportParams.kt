package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.extension.toJson
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.platform.api.DynamicParamsProvider

data class ElementReportParams constructor(
    val elementId: DtElementId,
    val identifier: String = "",
    val enableClick: Boolean = true,
    val enableExposure: Boolean = false,
    val enableExposureEnd: Boolean = false,
    val minExposureRatio: Float = 0.01F,
    val params: Map<String, Any>? = null,
    val dynamicParamsProvider: DynamicParamsProvider? = null,
    val logicParentViewRef: (() -> Int?)? = null,
) {
    fun asReportInfo(): Any {
        // Android、iOS支持直接传对象，Harmony不支持，暂时用JSONObject
        if (!isHarmonyPlatform()) {
            return this
        }
        return mutableMapOf<String, Any?>().apply {
            put("elementId", elementId.id)
            put("identifier", identifier)
            put("enableExposure", enableExposure)
            put("enableExposureEnd", enableExposureEnd)
            put("minExposureRatio", minExposureRatio)
            put("enableClick", enableClick)
            put("params", params.toJson())
            put("logicParentView", logicParentViewRef?.invoke())
        }.toJson()
    }
}