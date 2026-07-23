package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.RowScope
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.platform.statusBarHeight
import com.tencent.news.core.compose.scaffold.modifiers.debugBackground
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.theme.FullWidthThinDivider
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.theme.QnSkin
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.page.model.CommonTitleBarWidget
import com.tencent.news.core.page.model.StructWidgetType
import com.tencent.news.core.platform.ScreenUtils
import com.tencent.news.core.platform.api.statusBarController
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent

// 默认TitleBar 组件高度
val TitleBarHeight: Dp = ScreenUtils.getTitleBarHeight().dp

// TitleBar 区域总高度（包含状态栏高度）
val DefaultTitlebarAreaHeight: Dp
    @Composable
    get() = statusBarHeight() + TitleBarHeight

@Composable
fun StructSimpleTitleBar(widget: CommonTitleBarWidget?) {
    val height = statusBarHeight() + TitleBarHeight

    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.BottomStart
    ) {
        TitleBarTheme {
            if (isIOSPlatform()) {
                Box(modifier = Modifier.offset(y = (-2).dp)) {
                    ThemedStructTitleBar(widget, height)
                }
            } else {
                ThemedStructTitleBar(widget, height)
            }
        }
    }
}


@Composable
fun StructTitleBar(
    widget: CommonTitleBarWidget,
    isIconAlwaysDark: Boolean = widget.ui.isBarIconDark,
    isAlwaysShowTitle: Boolean = widget.ui.alwaysShowCenter,
    height: Dp? = null
) {
    val isHeaderCollapsed by LocalHeaderCollapseStatus.current
    val isDarkTheme = isAppInDarkTheme()
    val isTransparentBg = !isHeaderCollapsed || widget.ui.alwaysTransparentBg

    val statusBarSwitch =
        LocalStructPageViewModel.current?.pageUiConfig?.statusBarChangeSwitch?.collectAsState()
    val lifecycleFlow = LocalStructPageViewModel.current?.pageFlow
    LaunchedEffect(isTransparentBg) {
        lifecycleFlow?.collect { event ->
            if (event == PageLifecycleEvent.ON_RESUME) {
                if (statusBarSwitch?.value.isTrue()) {
                    if (isTransparentBg) {
                        statusBarController.setWhiteBar()
                    } else {
                        if (isDarkTheme) {
                            statusBarController.setWhiteBar()
                        } else {
                            statusBarController.setBlackBar()
                        }
                    }
                }
            }
        }
    }

    val titleBarTheme = provideDefaultTitleBarTheme(
        isTransparentBg,
        isDarkTheme,
        isIconAlwaysDark,
        isAlwaysShowTitle
    )
    CompositionLocalProvider(LocalTitleBarTheme provides titleBarTheme) {
        ThemedStructTitleBar(widget, height, isHeaderCollapsed)
    }
}

@Composable
fun ThemedStructTitleBar(
    widget: CommonTitleBarWidget?,
    height: Dp? = null,
    isHeaderCollapsed: Boolean? = null
) {
    widget ?: return

    val alwaysTransparentBg = widget.ui.alwaysTransparentBg

    val bgColor = if (alwaysTransparentBg) {
        QnColor.transparent
    } else if (isHeaderCollapsed != null && QnSkin?.barBgColor != null) {
        if (isHeaderCollapsed.isTrue()) {
            QnSkin?.barBgColor ?: QnColor.bgPage
        } else {
            QnColor.transparent
        }
    } else {
        currentTitleBarTheme.titleBarBgColor
    }



    Column(modifier = Modifier.fillMaxWidth().background(bgColor)) {
        // 撑开状态栏
        StatusBarSpacer()

        Box(Modifier.fillMaxWidth().height(height ?: TitleBarHeight)) {
            Row(
                modifier = Modifier.align(Alignment.Center)
                    .padding(start = 9.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeftBtns(widget)

                RightBtns(widget)
            }

            CenterBtns(widget)

            BottomDivider(widget)
        }
    }
}

@Composable
private fun RowScope.LeftBtns(widget: CommonTitleBarWidget) {
    Row(
        modifier = Modifier.debugBackground(Color.Red).weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start)
    ) {
        if (!widget.ui.hideBackBtn) {
            BackBtn()
        }

        if (currentTitleBarTheme.isHeaderCollapsed && !currentTitleBarTheme.hideLeftBtns) {
            widget.leftBtns?.forEach {
                if (it.getWidgetType() == StructWidgetType.TITLE_BTN) {
                    Box(modifier = Modifier.weight(1f, false)) {
                        ViewService.btn.Build(it)
                    }
                } else {
                    ViewService.btn.Build(it)
                }
            }
        }
    }
}

@Composable
private fun RowScope.RightBtns(widget: CommonTitleBarWidget) {
    Row(
        modifier = Modifier.debugBackground(Color.Black),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start)
    ) {
        widget.actionBtns?.forEach {
            ViewService.btn.Build(it)
        }
    }
}

@Composable
private fun BoxScope.CenterBtns(widget: CommonTitleBarWidget) {
    val btns = widget.centerBtns ?: return
    Row(
        modifier = Modifier.debugBackground(Color.Blue).align(Alignment.Center),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End)
    ) {
        btns.forEach {
            ViewService.btn.Build(it)
        }
    }
}

@Composable
private fun BoxScope.BottomDivider(widget: CommonTitleBarWidget) {
    if (!widget.ui.isHideBottomLine && currentTitleBarTheme.isHeaderCollapsed && QnSkin == null) {
        val dividerColor = if (widget.data?.title == MESSAGE_PAGE_TITLE) {
            QnColor.user.messageDivider
        } else {
            QnColor.lineFine
        }
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            FullWidthThinDivider(color = dividerColor)
        }
    }
}

private const val MESSAGE_PAGE_TITLE = "我的消息"