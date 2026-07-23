package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.platform.ForceNoScale
import com.tencent.news.core.compose.scaffold.theme.ForceDarkTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnLottie
import com.tencent.news.core.page.model.StructPageLoadingViewType
import com.tencent.news.core.platform.FrameworkLottie
import com.tencent.news.core.view.setup.ViewServiceBridge


@Composable
fun StructPageLoadingView(
    modifier: Modifier = Modifier,
    viewType: StructPageLoadingViewType = StructPageLoadingViewType.NORMAL_LOTTIE,
    background: Color? = null,
    forceDarkTheme: Boolean = false,
) {
    val defaultLoading = ViewServiceBridge.impl.defaultLoadingView()
    if (defaultLoading != null) {
        defaultLoading()
        return
    }

    when (viewType) {
        StructPageLoadingViewType.EMPTY ->
            StructEmptyLoadingView(modifier, forceDarkTheme)

        StructPageLoadingViewType.NORMAL_LOTTIE ->
            StructNormalLottieLoadingView(modifier, background, forceDarkTheme)
    }

}

@Composable
private fun StructEmptyLoadingView(modifier: Modifier = Modifier, forceDarkTheme: Boolean = false) {
    LoadingViewContainer(modifier, forceDarkTheme = forceDarkTheme)
}

@Composable
private fun StructNormalLottieLoadingView(
    modifier: Modifier = Modifier,
    background: Color? = null,
    forceDarkTheme: Boolean = false,
) {
    LoadingViewContainer(modifier, background = background, forceDarkTheme = forceDarkTheme) {
        ForceNoScale {
            QnLottie(
                modifier = Modifier.width(150.dp).height(50.dp),
                fileName = FrameworkLottie.loading,
                tag = "loading",
                autoPlay = true,
                infinity = true
            )
        }
    }
}

@Composable
private fun LoadingViewContainer(
    modifier: Modifier = Modifier,
    background: Color? = null,
    forceDarkTheme: Boolean = false,
    loadingView: @Composable (() -> Unit)? = null
) {
    val bgColor =
        background ?: if (forceDarkTheme) Color(0xFF1f1f1f) else QNTheme.colorScheme.bgPage
    val content: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(bgColor)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            loadingView?.invoke()
        }
    }

    if (forceDarkTheme) {
        ForceDarkTheme {
            content()
        }
    } else {
        content()
    }
}
