package com.tencent.news.core.compose.view.fullscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.platform.api.SafeAppRouter
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 全屏控制器
 *
 * 参考 [com.tencent.news.core.compose.view.dialog.DialogController] 设计，
 * 用于将指定 Composable 内容提升到页面根部全屏渲染。
 *
 * 典型场景：视频全屏播放时，需要让 VerticalPager 整体突破父容器约束占满整个屏幕。
 * 由于同一 Compose 树内共享状态（player 实例等），切换全屏时视频播放不会中断。
 *
 */
class FullScreenController {

    companion object {
        /** 当前处于全屏状态的控制器集合 */
        private val activeControllers = mutableSetOf<FullScreenController>()

        private val onExitCallbacks = mutableListOf<() -> Unit?>()

        init {
            // 注册路由跳转前回调，页面跳转时自动退出全屏
            SafeAppRouter.addBeforeNavigateCallback { exitAllFullScreen() }
        }

        /**
         * 退出所有活跃的全屏状态。
         * 供路由跳转时调用，确保跳转到新页面前退出全屏。
         */
        fun exitAllFullScreen() {
            // 复制一份避免 ConcurrentModification
            activeControllers.toList().forEach { it.exitAllFullScreenSlots() }
        }

        fun registerOnExitCallback(callback: () -> Unit?) {
            onExitCallbacks.add(callback)
        }

        fun unregisterOnExitCallback(callback: () -> Unit?) {
            onExitCallbacks.remove(callback)
        }
    }

    private val contentFlow = MutableStateFlow<List<FullScreenContent>>(emptyList())

    /** 退出动画进行中标记，防止重复触发 */
    private var isExiting = false

    /**
     * 在页面根部渲染一个全屏 slot。
     *
     * 多个 slot 按入栈顺序渲染，后入栈的 slot 会盖住先入栈的 slot。
     * 调用方必须用同一个 [key] 配对退出，避免影响其它全屏内容。
     *
     * @param initialWidth 进入全屏时的初始宽度（列表卡片宽度），用于缩放动画。
     *                     传 0.dp 则无动画直接全屏。
     * @param initialHeight 进入全屏时的初始高度（列表卡片高度），用于缩放动画。
     *                      传 0.dp 则无动画直接全屏。
     * @param onEnter 进入全屏时的回调（可选）
     * @param onExit 退出当前 slot 时的回调（可选）
     * @param content 全屏态展示的 Composable 内容
     */
    fun enterFullScreenSlot(
        key: Any,
        initialWidth: Dp = 0.dp,
        initialHeight: Dp = 0.dp,
        targetWidth: Dp = 0.dp,
        targetHeight: Dp = 0.dp,
        onEnter: (() -> Unit)? = null,
        onExit: (() -> Unit)? = null,
        content: @Composable BoxScope.() -> Unit
    ) {
        isExiting = false
        val fullContent = FullScreenContent(
            key = key,
            content = content,
            onEnter = onEnter,
            onExit = onExit,
            initialWidth = initialWidth,
            initialHeight = initialHeight,
            targetWidth = targetWidth,
            targetHeight = targetHeight,
        )
        upsertContent(fullContent)
    }

    fun exitFullScreenSlot(key: Any) {
        removeContent(key)
    }

    private fun upsertContent(content: FullScreenContent) {
        activeControllers.add(this)
        contentFlow.value = contentFlow.value.filterNot { it.key == content.key } + content
    }

    private fun removeContent(key: Any) {
        removeContents { it.key == key }
    }

    private fun removeContents(predicate: (FullScreenContent) -> Boolean) {
        val targets = contentFlow.value.filter(predicate)
        if (targets.isEmpty()) return
        contentFlow.value = contentFlow.value.filterNot(predicate)
        targets.forEach { it.onExit?.invoke() }
        if (contentFlow.value.isEmpty()) {
            activeControllers.remove(this)
        }
    }

    private fun exitAllFullScreenSlots() {
        if (isExiting) return
        isExiting = true
        removeContents { true }
        onExitCallbacks.forEach { it.invoke() }
        isExiting = false
    }

    /**
     * 在页面根部收集并渲染全屏内容。
     * 由框架在 ComposePage.setContentCompat() 中调用，业务不需要手动调用。
     */
    @Composable
    fun CollectFullScreenState() {
        val contents by contentFlow.collectAsState()

        contents.forEach { content ->
            key(content.key) {
                FullScreenAnimatedBox(content)
            }
        }
    }

    /**
     * 带缩放动画的全屏 Box
     *
     * 进入时：从竖屏卡片尺寸（交换映射后）动画过渡到当前屏幕宽高
     * 退出时：直接移除（退出动画由屏幕旋转过渡覆盖）
     *
     * 坐标映射说明：
     * 竖屏卡片的 width 在旋转到横屏后对应高度方向，height 对应宽度方向。
     * 因此动画初始值需要交换：initialHeight → 横屏初始宽度，initialWidth → 横屏初始高度。
     */
    @Composable
    private fun FullScreenAnimatedBox(
        content: FullScreenContent
    ) {
        // 无动画，直接全屏
        LaunchedEffect(content) {
            content.onEnter?.invoke()
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content.content.invoke(this)
        }
    }
}

/**
 * 全屏内容数据类
 */
internal class FullScreenContent(
    val key: Any,
    val content: @Composable BoxScope.() -> Unit,
    val onEnter: (() -> Unit)? = null,
    val onExit: (() -> Unit)? = null,
    /** 进入全屏时的初始宽度（列表卡片宽度），用于缩放动画 */
    val initialWidth: Dp = 0.dp,
    /** 进入全屏时的初始高度（列表卡片高度），用于缩放动画 */
    val initialHeight: Dp = 0.dp,
    val targetWidth: Dp = 0.dp,
    val targetHeight: Dp = 0.dp,
)
