package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.news.core.page.model.IStructLoadStateWidget
import com.tencent.news.core.page.model.StructPageUiState
import com.tencent.news.core.page.model.StructWidget
import com.tencent.news.core.service.ViewService

// 支持 loading、error、success 三种状态的view
@Composable
fun <T> StructPage(
    modifier: Modifier = Modifier,
    uiState: StructPageUiState,
    loadStateWidget: IStructLoadStateWidget,
    background: Color? = null,
    onRetryClick: suspend () -> Unit,
    forceDarkTheme: Boolean = false,
    errorImagePainter: Painter? = null,
    content: @Composable (T) -> Unit
) {
    val pageScope = rememberCoroutineScope()

    StructPageTheme(uiState) {
        when (it) {
            is StructPageUiState.Error ->
                loadStateWidget.BuildStateViewOrElse({ error }) {
                    StructPageErrorView(
                        modifier = modifier,
                        pageScope = pageScope,
                        background = background,
                        onRefresh = onRetryClick,
                        forceDarkTheme = forceDarkTheme,
                        errorImagePainter = errorImagePainter,
                    )
                }

            is StructPageUiState.Loading ->
                loadStateWidget.BuildStateViewOrElse({ loading }) {
                    StructPageLoadingView(modifier, it.viewType, background, forceDarkTheme)
                }

            is StructPageUiState.Success<*> -> content(it.response as T)
        }
    }
}

@Composable
fun IStructLoadStateWidget.BuildStateViewOrElse(
    widget: IStructLoadStateWidget.() -> StructWidget?,
    defaultContent: @Composable () -> Unit
) {
    val widget = widget()
    if (widget != null) {
        Box {
            ViewService.layer.Build(this, widget)
        }
    } else {
        defaultContent()
    }
}
