package com.tencent.news.core.compose

import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewPropExternalHandler
import com.tencent.news.core.compose.scaffold.modifiers.DATONG_ELEMENT
import com.tencent.news.core.compose.scaffold.modifiers.DATONG_PAGE
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_BIZ_EVENT_MANUAL
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_ELEMENT_CLICK_MANUAL
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_PAGE_LOGIC_DESTROY
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_TRAVERSE_PAGE

internal class KuiklyRenderViewPropDispatcher : IKuiklyRenderViewPropExternalHandler {

    private val handlers = mapOf(
        DATONG_PAGE to AndroidDatongPageReport(),
        DATONG_ELEMENT to AndroidDatongElementReport(),
        REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL to AndroidDatongElementManualReport(REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL),
        REPORT_DATONG_ELEMENT_CLICK_MANUAL to AndroidDatongElementManualReport(REPORT_DATONG_ELEMENT_CLICK_MANUAL),
        REPORT_DATONG_BIZ_EVENT_MANUAL to AndroidBizEventManualReport(),
        REPORT_DATONG_TRAVERSE_PAGE to AndroidTraversePageReport(),
        REPORT_DATONG_PAGE_LOGIC_DESTROY to AndroidPageLogicDestroyReport()
    )

    override fun resetViewExternalProp(renderViewExport: IKuiklyRenderViewExport, propKey: String): Boolean {
        val handler = handlers.get(propKey) ?: return false
        return handler.resetViewExternalProp(renderViewExport)
    }

    override fun setViewExternalProp(
        renderViewExport: IKuiklyRenderViewExport,
        propKey: String,
        propValue: Any
    ): Boolean {
        val handler = handlers.get(propKey) ?: return false
        return handler.setViewExternalProp(renderViewExport, propValue)
    }
}

internal interface KuiklyRenderViewPropHandler {

    val prop: String

    fun resetViewExternalProp(renderViewExport: IKuiklyRenderViewExport): Boolean

    fun setViewExternalProp(renderViewExport: IKuiklyRenderViewExport, propValue: Any): Boolean
}

