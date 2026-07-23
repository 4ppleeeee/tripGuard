package com.tencent.news.core.compose.scaffold.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.scaffold.ComposeDialog
import kotlinx.coroutines.launch

/**
 * Overlay 级别弹窗的基类（View 级别 Compose 弹窗）
 *
 * 与 [PopUpDialog] 不同，这是一个干净的全屏空壳：
 * - 无背景遮罩
 * - 无入场/退场动画
 * - 无拖拽手势
 * - 无安全区域处理
 *
 * 直接继承 [ComposeDialog]，仅提供全屏 Box 容器，
 * 业务侧在 [DialogContent] 中通过 contentAlignment 自行控制位置。
 */
abstract class AbsPopUpViewDialog : ComposeDialog() {

    override fun sceneName(): String = "PopUpViewDialog"

    @Composable
    override fun OnSetContent() {
        super.OnSetContent()
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier.fillMaxSize()) {
            DialogContent(
                modifier = Modifier,
                dismiss = {
                    scope.launch {
                        onCloseDialog()
                    }
                }
            )
        }
    }

    @Composable
    abstract fun DialogContent(modifier: Modifier, dismiss: () -> Unit)
}
