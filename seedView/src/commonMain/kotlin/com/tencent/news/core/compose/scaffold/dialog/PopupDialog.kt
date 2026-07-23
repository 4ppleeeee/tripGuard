package com.tencent.news.core.compose.scaffold.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.animation.AnimatedVisibility
import com.tencent.kuikly.compose.animation.EnterTransition
import com.tencent.kuikly.compose.animation.ExitTransition
import com.tencent.kuikly.compose.animation.animateColorAsState
import com.tencent.kuikly.compose.animation.fadeIn
import com.tencent.kuikly.compose.animation.fadeOut
import com.tencent.kuikly.compose.animation.slideInVertically
import com.tencent.kuikly.compose.animation.slideOutVertically
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.gestures.Orientation
import com.tencent.kuikly.compose.foundation.gestures.draggable
import com.tencent.kuikly.compose.foundation.gestures.rememberDraggableState
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.onGloballyPositioned
import com.tencent.kuikly.compose.ui.layout.onPlaced
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.news.core.compose.scaffold.modifiers.backgroundColor
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.compose.scaffold.modifiers.height
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.news.core.compose.scaffold.ComposeDialog
import com.tencent.news.core.compose.scaffold.modifiers.DtCurrentView
import com.tencent.news.core.compose.scaffold.modifiers.traversePage
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.extension.dialogPenetrateClickEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tencent.news.core.compose.utils.ComposeUtils

enum class PopupDialogState {
    INIT, VISIBLE, DISMISS;

    fun isVisible(): Boolean {
        return this == VISIBLE
    }
}

/**
 * Author: joejhzhou
 * Date: 2025/3/7
 */
@Composable
fun PopUpDialog(
    modifier: Modifier,
    dismiss: () -> Unit,
    onBackgroundClick: (() -> Unit)? = null,
    // fixme fitzwu: 兼容remember失效，等中台修复后删除
    initState: PopupDialogState = PopupDialogState.INIT,
    onHorizontalDrag: ((delta: Float) -> Unit)? = null,
    onVerticalDrag: ((delta: Float) -> Unit)? = null,
    safeAreaBackgroundColor: Color? = null,
    enterAnim: EnterTransition = slideInVertically { height -> height } + fadeIn(),
    exitAnim: ExitTransition = slideOutVertically { height -> height } + fadeOut(),
    backgroundColor: ((state: PopupDialogState) -> Color)? = null,
    // fix 出现点击穿透的时候改为true
    forceProtectClickEvent: Boolean? = false,
    content: @Composable (Modifier) -> Unit
) {
    var state by remember { mutableStateOf(initState) }

    var nativeRef by remember { mutableStateOf<DtCurrentView?>(null) }

    val dimColor by animateColorAsState(
        targetValue = if (backgroundColor != null) {
            backgroundColor(state)
        } else if (state.isVisible()) {
            if (state.isVisible()) Color.Black.changeAlpha(0.3f) else Color.Transparent
        } else {
            Color.Transparent
        }
    )

    val closeDialog = suspend {
        state = PopupDialogState.DISMISS
        delay(300)
        onBackgroundClick?.invoke()
        dismiss()
    }

    val horizontalState = rememberDraggableState { delta ->
        onHorizontalDrag?.invoke(delta)
    }
    val verticalState = rememberDraggableState { delta ->
        onVerticalDrag?.invoke(delta)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .nativeRef { nativeRef = it }
            .onPlaced { }
            .onGloballyPositioned {
                if (state == PopupDialogState.INIT) {
                    state = PopupDialogState.VISIBLE
                }
            }
            .backgroundColor(dimColor)
            .draggable(
                enabled = onHorizontalDrag != null,
                state = horizontalState,
                orientation = Orientation.Horizontal
            )
            .draggable(
                enabled = onVerticalDrag != null,
                state = verticalState,
                orientation = Orientation.Vertical
            )
            .clickable {
                scope.launch { closeDialog() }
            }.dialogPenetrateClickEvent(forceProtectClickEvent ?: false),
        verticalArrangement = Arrangement.Bottom
    ) {

        LaunchedEffect(state) {
            if (state.isVisible()) {
                // 延迟触发上报检测
                delay(1000)
                traversePage(nativeRef)
            }
        }

        AnimatedVisibility(
            visible = state.isVisible(), enter = enterAnim, exit = exitAnim
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(modifier = Modifier.fillMaxWidth().clickable { }) {
                    // 主内容
                    content.invoke(Modifier)

                    // 安全区域作为垂直排列的一部分，全屏模式下不显示
                    // 获取底部安全区域高度，全屏模式下为0
                    val safeAreaHeight = ComposeUtils.rememberSafeAreaBottomHeight()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(safeAreaHeight)
                            .backgroundColor(safeAreaBackgroundColor ?: QNTheme.colorScheme.bgBlock)
                    )
                }
            }
        }
    }
}

abstract class PopUpDialog : ComposeDialog() {
    override fun sceneName(): String = "PopUpDialog"

    override fun enableReportPerformance() = true

    // fixme fitzwu: 兼容remember失效，等中台修复后删除
    open val initState = PopupDialogState.INIT

    @Composable
    open fun getSafeAreaBackgroundColor(): Color? = null

    // 滑动手势事件
    open val onHorizontalDrag: ((delta: Float) -> Unit)? = null
    open val onVerticalDrag: ((delta: Float) -> Unit)? = null

    open val enterAnim: EnterTransition? = null
    open val exitAnim: ExitTransition? = null

    open val disableAnim: Boolean = false

    open val getBackgroundColor: ((state: PopupDialogState) -> Color)? = null

    // fix 出现点击穿透的时候改为true
    open val forceProtectClickEvent: Boolean? = false

    open fun onBackgroundClick() {}

    @Composable
    override fun OnSetContent() {
        val scope = rememberCoroutineScope()

        PopUpDialog(
            Modifier,
            dismiss = {
                scope.launch {
                    // 确保在主线程调用
                    onCloseDialog()
                }
            },
            onBackgroundClick = {
                scope.launch {
                    onBackgroundClick()
                }
            },
            initState = initState,
            onHorizontalDrag = onHorizontalDrag,
            onVerticalDrag = onVerticalDrag,
            safeAreaBackgroundColor = getSafeAreaBackgroundColor(),
            enterAnim = if (disableAnim) EnterTransition.None else (enterAnim
                ?: (slideInVertically { height -> height } + fadeIn())),
            exitAnim = if (disableAnim) ExitTransition.None else (exitAnim
                ?: (slideOutVertically { height -> height } + fadeOut())),
            backgroundColor = getBackgroundColor,
            forceProtectClickEvent = forceProtectClickEvent,
            content = {
                DialogContent(it, dismiss = {
                    scope.launch {
                        // 确保在主线程调用
                        onCloseDialog()
                    }
                })
            })
    }

    @Composable
    abstract fun DialogContent(modifier: Modifier, dismiss: () -> Unit)
}