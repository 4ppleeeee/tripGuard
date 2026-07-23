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
import com.tencent.news.core.compose.scaffold.registry.LocalStructDtPageRef
import com.tencent.news.core.page.extension.StructPageWidgetEx.buildDtReport
import com.tencent.news.core.page.extension.StructPageWidgetEx.hasDtReport
import com.tencent.news.core.page.model.StructPageUiState
import com.tencent.news.core.page.model.StructPageWidget2

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
            contentId = pageWidget.findPageItem()?.baseDto?.idStr.orEmpty(),
        ).nativeRef {
            pageViewRefState.value = it
        }
    }

    // 页面创建/退出：绑定手动上报回调
    DisposableEffect(Unit) {
        val reportAction = pageWidget.pageConfig.reportAction
        reportAction?.onPageExpose?.invoke()
        onDispose {
            reportAction?.onPageExit?.invoke()
        }
    }

    CompositionLocalProvider(LocalStructDtPageRef provides pageViewRefState) {
        Box(modifier = rootModifier) {
            content()
        }
    }

}