package com.tencent.news.core.compose

import android.view.View
import com.tencent.kuikly.core.render.android.const.KRCssConst
import com.tencent.kuikly.core.render.android.css.ktx.getViewData
import com.tencent.kuikly.core.render.android.css.ktx.putViewData
import com.tencent.kuikly.core.render.android.css.ktx.removeViewData
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.news.core.compose.scaffold.modifiers.BizEventParams
import com.tencent.news.core.compose.scaffold.modifiers.DATONG_ELEMENT
import com.tencent.news.core.compose.scaffold.modifiers.DATONG_PAGE
import com.tencent.news.core.compose.scaffold.modifiers.ElementReportParams
import com.tencent.news.core.compose.scaffold.modifiers.PageReportParams
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_BIZ_EVENT_MANUAL
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_ELEMENT_CLICK_MANUAL
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_PAGE_LOGIC_DESTROY
import com.tencent.news.core.compose.scaffold.modifiers.REPORT_DATONG_TRAVERSE_PAGE
import com.tencent.news.core.platform.api.dtReport

internal abstract class DatongPropHandler : KuiklyRenderViewPropHandler {

    private val dtLayoutHasCalled = "dt_layout_change_had_call"

    override fun setViewExternalProp(renderViewExport: IKuiklyRenderViewExport, propValue: Any): Boolean {
        val view = renderViewExport.view()
        val layChangedHadCall = view.getViewData<Boolean>(dtLayoutHasCalled) ?: false
        if (layChangedHadCall) {
            setDtReportInfo(renderViewExport, propValue)
        } else {
            view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    // 等布局出来以后再设置，不然对于动态添加的view，layout还没出来，大同的曝光上报会无效
                    view.putViewData(dtLayoutHasCalled, true)
                    view.removeOnLayoutChangeListener(this)
                    // 内部的点击事件时通过GestureDetector来实现的，因此大同hook不到setOnClickListener
                    // 这里在点击回调前，手动上报
                    view.putViewData(KRCssConst.PRE_CLICK, object : KuiklyRenderCallback {
                        override fun invoke(result: Any?) {
                            // 点击上报逻辑
                            reportElementClickEvent(view, propValue)
                        }
                    })
                    setDtReportInfo(renderViewExport, propValue)
                }
            })
        }
        return true
    }

    abstract fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any)

    open fun reportElementClickEvent(view: View, propValue: Any) {}


    override fun resetViewExternalProp(renderViewExport: IKuiklyRenderViewExport): Boolean {
        renderViewExport.view().removeViewData<KuiklyRenderCallback>(KRCssConst.PRE_CLICK)
        renderViewExport.view().removeViewData<Boolean>(dtLayoutHasCalled)
        return true
    }
}

internal class AndroidDatongPageReport : DatongPropHandler() {

    override val prop: String = DATONG_PAGE

    override fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any) {

        val dt = (propValue as? PageReportParams) ?: return

        if (dt.logicDestroy) {
            dtReport()?.pageLogicDestroy(renderViewExport.view())
        }

        dtReport()?.setPageReportInfo(
            page = renderViewExport.view(),
            pageId = dt.pageId.id,
            contentId = dt.contentId,
            params = dt.pageParams,
            dynamicParams = dt.dynamicParamsProvider
        )
    }

}

internal class AndroidDatongElementReport : DatongPropHandler() {

    override val prop: String = DATONG_ELEMENT

    override fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any) {

        val dt = (propValue as? ElementReportParams) ?: return
        val logicParentView = dt.logicParentViewRef?.invoke()?.let { renderViewExport.kuiklyRenderContext?.getView(it) }

        dtReport()?.setElementReportInfo(
            element = renderViewExport.view(),
            elementId = dt.elementId.id,
            identifier = dt.identifier,
            enableExposure = dt.enableExposure,
            enableExposureEnd = dt.enableExposureEnd,
            minExposureRatio = dt.minExposureRatio,
            params = dt.params,
            dynamicParams = dt.dynamicParamsProvider,
            logicParentView = logicParentView
        )
    }

    override fun reportElementClickEvent(view: View, propValue: Any) {
        val dt = (propValue as? ElementReportParams) ?: return
        if (dt.enableClick) {
            dtReport()?.reportElementClickEvent(view)
        }
    }

    override fun resetViewExternalProp(renderViewExport: IKuiklyRenderViewExport): Boolean {
        dtReport()?.reset4Reuse(renderViewExport.view())
        return super.resetViewExternalProp(renderViewExport)
    }
}

/**
 * 通过调用[DtReportModifier.reportElementClickEvent]或[DtReportModifier.reportElementExposureEvent]主动上报大同
 */
internal class AndroidDatongElementManualReport(override val prop: String) : DatongPropHandler() {

    override fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any) {

        val dt = (propValue as? ElementReportParams) ?: return
        val logicParentView = getLogicParentView(renderViewExport, dt)
        when (prop) {
            REPORT_DATONG_ELEMENT_CLICK_MANUAL -> {
                reportElementClickEvent(renderViewExport.view(), dt, logicParentView)
            }

            REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL -> {
                dtReport()?.reportElementExposureEvent(
                    view = renderViewExport.view(),
                    elementId = dt.elementId.id,
                    params = dt.params
                )
            }
        }
    }

    private fun getLogicParentView(
        renderViewExport: IKuiklyRenderViewExport,
        dt: ElementReportParams
    ): View? {
        return dt.logicParentViewRef?.invoke()?.let { renderViewExport.kuiklyRenderContext?.getView(it) }
    }

    private fun reportElementClickEvent(
        view: View,
        dt: ElementReportParams,
        logicParentView: View?,
    ) {
        if (logicParentView == null) {
            dtReport()?.reportElementClickEvent(
                view = view,
                elementId = dt.elementId.id,
                params = dt.params
            )
            return
        }
        dtReport()?.setElementReportInfo(
            element = view,
            elementId = dt.elementId.id,
            identifier = dt.identifier,
            enableExposure = dt.enableExposure,
            enableExposureEnd = dt.enableExposureEnd,
            minExposureRatio = dt.minExposureRatio,
            params = dt.params,
            dynamicParams = dt.dynamicParamsProvider,
            logicParentView = logicParentView
        )
        dtReport()?.reportElementClickEvent(view)
    }
}

/**
 * 通过调用[DtReportModifier.reportBizEvent]上报自定义事件
 */
internal class AndroidBizEventManualReport : DatongPropHandler() {

    override val prop: String = REPORT_DATONG_BIZ_EVENT_MANUAL

    override fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any) {
        val dt = (propValue as? BizEventParams) ?: return
        dtReport()?.reportBizEvent(renderViewExport.view(), dt.event.eventId, dt.params)
    }
}

/**
 * 通过调用[DtReportModifier.traversePage]触发曝光检测
 */
internal class AndroidTraversePageReport : DatongPropHandler() {

    override val prop: String = REPORT_DATONG_TRAVERSE_PAGE

    override fun setViewExternalProp(renderViewExport: IKuiklyRenderViewExport, propValue: Any): Boolean {
        setDtReportInfo(renderViewExport, propValue)
        return true
    }

    override fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any) {
        renderViewExport.view().post {
            dtReport()?.traversePage(renderViewExport.view())
        }
    }
}


/**
 * 通过调用[DtReportModifier.pageLogicDestroy]去掉当前页面绑定
 */
internal class AndroidPageLogicDestroyReport : DatongPropHandler() {

    override val prop: String = REPORT_DATONG_PAGE_LOGIC_DESTROY

    override fun setDtReportInfo(renderViewExport: IKuiklyRenderViewExport, propValue: Any) {
        renderViewExport.view().post {
            dtReport()?.pageLogicDestroy(renderViewExport.view())
        }
    }
}
