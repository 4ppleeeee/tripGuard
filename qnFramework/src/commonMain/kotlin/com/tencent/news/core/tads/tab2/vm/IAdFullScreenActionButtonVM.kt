package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.app.constants.IconFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * 全屏行动按钮 VM：管理延迟高亮状态
 * - 初始：普通态（Normal）
 * - 延迟约 3s 后切换为高亮（Highlighted，主题色）
 */
interface IAdFullScreenActionButtonVM {
    val actionIconFont: IconFont?
    val actionText: String
    val actionHighlightColor: String
    val btnState: StateFlow<AdFullScreenActionButtonState>

    /**
     * 按钮点击入口默认行为（UI 层调用）。
     */
    fun onClick()

    fun startHighlightTimer(scope: CoroutineScope)
    fun cancelHighlightTimer()
    fun reset()
}

enum class AdFullScreenActionButtonState {
    Normal,         // 普通态
    Highlighted     // 高亮主题色（~3s 后）
}
