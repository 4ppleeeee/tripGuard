package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.alpha
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.view.setup.ViewServiceBridge

/**
 * 海报预览管理器
 * 负责管理海报预览数据的生成和处理逻辑
 */
internal class PostPreviewManager(
    val shareData: IKmmShareData,
    val onDismissed: (shareResult: ShareResult) -> Unit,
) {
    /**
     * 创建带有默认本地资源的海报预览数据
     */
    @Composable
    fun createInitialPreviewDataWithPlaceholder(): PostPreviewData {
        return ViewServiceBridge.impl.createInitialPreviewDataWithPlaceholder(shareData)
    }

    /**
     * 渲染隐藏的截图组件来生成海报预览
     */
    @Composable
    fun renderHiddenScreenshotComponent(
        postPreviewData: PostPreviewData,
        onPreviewDataUpdated: (PostPreviewData) -> Unit
    ) {
        // 检查必要的数据是否为空
        val item = shareData.item
        val items = shareData.items
        if (item == null || items == null) {
            return
        }

        // 在后台渲染隐藏的截图组件来更新预览数据
        Box(
            modifier = Modifier
                .size(width = 375.dp, height = 600.dp)
                .alpha(0f) // 完全透明，视觉上隐藏
        ) {
            // 生成海报截图，使用安全的非空引用
            PosterScreenshotComponent(
                feedsItem = item,
                feedsList = items,
                style = "default",
                onScreenshotTaken = { path ->
                    // 当截图完成后，更新预览数据
                    if (path.isNotNullOrEmpty()) {
                        val newPreviewData = PostPreviewData(
                            posterImages = listOf(path),
                            posterViews = postPreviewData.posterViews,
                            posterStyles = postPreviewData.posterStyles,
                            defaultPlaceholderList = null
                        )
                        // 通知预览数据更新
                        onPreviewDataUpdated(newPreviewData)
                    }
                }
            )
        }
    }

}
