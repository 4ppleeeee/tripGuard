package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.scaffold.theme.ForceLightTheme
import com.tencent.news.core.compose.view.QnScreenshot
import com.tencent.news.core.compose.view.rememberScreenshotState
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.service.ViewService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 海报截图组件
 * 用于生成海报截图
 *
 * 注意：这个组件需要在Composable上下文中使用
 * 不能直接在suspend函数中调用
 */
@Composable
fun PosterScreenshotComponent(
    feedsItem: IKmmFeedsItem,
    feedsList: List<IKmmFeedsItem>,
    style: String,
    onScreenshotTaken: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val screenshotState = rememberScreenshotState()
    var isComponentsLoaded by remember { mutableStateOf(false) }

    // 当图片加载完成时，添加延迟后触发截图
    LaunchedEffect(isComponentsLoaded) {
        if (isComponentsLoaded) {
            scope.launch {
                try {
                    // 添加延迟，确保UI完全渲染
                    kotlinx.coroutines.delay(300)

                    screenshotState.value.take(scope).collectLatest { path ->
                        onScreenshotTaken(path)
                    }
                } catch (e: Exception) {
                    onScreenshotTaken(null)
                }
            }
        }
    }

    QnScreenshot(
        modifier = modifier.wrapContentHeight(),
        state = screenshotState.value
    ) {
        // 强制使用亮色主题，确保海报截图不受夜间模式影响
        ForceLightTheme {
            ViewService.share.PosterShareCard(
                feedsItem = feedsItem,
                feedsList = feedsList,
                onImageLoaded = {
                    // 当 MorningPostShareCard 中的组件都加载完成后，标记为已加载
                    isComponentsLoaded = true
                }
            )
        }
    }
} 