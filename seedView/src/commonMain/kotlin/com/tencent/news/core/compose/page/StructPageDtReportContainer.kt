package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.news.core.compose.scaffold.modifiers.DtLogicParentView
import com.tencent.news.core.compose.scaffold.modifiers.dtPage
import com.tencent.news.core.compose.scaffold.registry.CollectPageOnPause
import com.tencent.news.core.compose.scaffold.registry.CollectPageOnResume
import com.tencent.news.core.compose.scaffold.registry.LocalStructDtPageRef
import com.tencent.news.core.page.extension.StructPageWidgetEx.buildDtReport
import com.tencent.news.core.page.extension.StructPageWidgetEx.hasDtReport
import com.tencent.news.core.page.model.StructPageUiState
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.platform.qnFileLog

// 处理页面级别大同上报参数绑定：
@Composable
fun StructPageDtReportContainer(
    pageWidget: StructPageWidget2,
    uiState: StructPageUiState,
    content: @Composable () -> Unit
) {
    // 判断是否需要延迟绑定 dtReport
    val shouldDelayDtReport = pageWidget.hasDtReport() &&
            pageWidget.pageConfig.delayDtReportUntilDataReady

    val applyDtReport = if (shouldDelayDtReport) {
        // 是否要将上报，延迟到ui展示时才报（一般需要下发一些后台动态参数时，用这个时机）
        uiState is StructPageUiState.Success<*>
    } else {
        true
    }

    var rootModifier = Modifier.Companion.fillMaxSize()

    val pageViewRefState = remember { mutableStateOf<DtLogicParentView?>(null) }

    // 根布局：绑定大同上报参数
    if (applyDtReport) {
        rootModifier = rootModifier.dtPage(
            dtReport = pageWidget.buildDtReport(),
            contentId = pageWidget.findPageItem()?.flexDto?.idStr.orEmpty(),
        ).nativeRef {
            pageViewRefState.value = it
        }
    }

    // 页面生命周期回调上报：
    CollectPageReportAction(pageWidget)

    CompositionLocalProvider(LocalStructDtPageRef provides pageViewRefState) {
        Box(modifier = rootModifier) {
            content()
        }
    }

}

/**
 * 收集 [StructPageWidget2] 上挂载的 [com.tencent.news.core.page.model.PageReportAction]，
 * 并在页面生命周期事件（创建/退出/Resume/Pause）触发对应回调。
 *
 * 该方法既被外层 PageWidget 复用（[StructPageDtReportContainer]），
 * 也被子 Tab SubPageWidget 复用（[StructChannelList] / [StructSubPageView]），
 * 以便所有 SubPageWidget 上配置的 reportAction 都能被框架统一收口。
 */
@Composable
internal fun CollectPageReportAction(pageWidget: StructPageWidget2) {
    val reportAction = pageWidget.pageConfig.reportAction ?: return
    val pageWidgetName = pageWidget::class.simpleName

    // 页面创建/退出：绑定手动上报回调
    DisposableEffect(pageWidget) {
        qnFileLog()?.logI(
            "PageVisitDebug",
            "[CollectReportAction] DisposableEffect onEnter pageWidget=$pageWidgetName -> invoke onPageExpose"
        )
        reportAction.onPageExpose?.invoke()

        onDispose {
            qnFileLog()?.logI(
                "PageVisitDebug",
                "[CollectReportAction] DisposableEffect onDispose pageWidget=$pageWidgetName -> invoke onPageExit"
            )
            reportAction.onPageExit?.invoke()
        }
    }

    CollectPageOnResume(pageWidget) {
        reportAction.onPageResume?.invoke()
    }
    CollectPageOnPause(pageWidget) {
        reportAction.onPagePause?.invoke()
    }
}