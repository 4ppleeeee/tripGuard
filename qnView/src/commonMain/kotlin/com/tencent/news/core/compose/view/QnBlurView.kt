package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.views.BlurView

/**
 * 高斯模糊背板组件（叶子节点）
 */
@Composable
fun QnBlurView(
    modifier: Modifier = Modifier,
    blurRadius: Float = 0f,
) {
    MakeKuiklyComposeNode<BlurView>(
        factory = { BlurView() },
        modifier = modifier,
        viewInit = { },
        viewUpdate = {
            it.getViewAttr().blurRadius(blurRadius)
        },
    )
}