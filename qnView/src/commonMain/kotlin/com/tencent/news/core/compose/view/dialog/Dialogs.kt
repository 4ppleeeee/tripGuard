package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.BackHandler
import com.tencent.kuikly.compose.animation.AnimatedVisibility
import com.tencent.kuikly.compose.animation.core.Spring
import com.tencent.kuikly.compose.animation.core.animateFloatAsState
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.animation.fadeIn
import com.tencent.kuikly.compose.animation.fadeOut
import com.tencent.kuikly.compose.animation.slideInVertically
import com.tencent.kuikly.compose.animation.slideOutVertically
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.onPlaced
import com.tencent.news.core.compose.adaptive.AdaptiveDialog.adaptDialogPosition
import com.tencent.news.core.compose.adaptive.AdaptiveDialog.adaptDialogWidth
import com.tencent.news.core.compose.scaffold.modifiers.LocalInAutoSafeAreaScene
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.compose.scaffold.modifiers.height
import com.tencent.news.core.compose.scaffold.registry.LocalBubbleViewController
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.compose.scaffold.registry.LocalScreenshot
import com.tencent.news.core.compose.scaffold.theme.ColorScheme
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.compose.scaffold.theme.QNDialogTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.bubble.BubbleViewController
import com.tencent.news.core.compose.view.extension.dialogPenetrateClickEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tencent.news.core.compose.platform.safeAreaHeight

/**
 * WARNING: 不要直接调用，使用DialogController.showDialog()
 *
 * 在屏幕底部显示的dialog，有slideIn和slideOut动画。
 *
 * 注意：TextField组件会自动检测安全区域处理场景并处理键盘高度，
 * 无需再手动在keyboardHeightChange回调中减去safeAreaHeight。
 */
@Composable
fun BottomSheetDialog(
    dialog: IDialog?,
    scope: CoroutineScope,
    controller: DialogController,
    theme: ColorScheme
) {
    BottomSheetDialog(
        safeAreaBackgroundColorProvider = dialog?.safeAreaBackgroundColorProvider
            ?: { QNTheme.colorScheme.bgBlock },
        customBackgroundContent = dialog?.customBackgroundContent,
        onDismissed = { controller.dismissDialog(dialog) },
        forceProtectClickEvent = dialog?.forceProtectClickEvent ?: false,
        backdropClickable = dialog?.dismissOnBackdropClick != false
    ) {
        CompositionLocalProvider(LocalInAutoSafeAreaScene provides true, LocalColorScheme provides theme) {
            dialog?.content?.invoke(scope, controller)
        }
    }
}

/**
 * WARNING: 不要直接调用，使用DialogController.showDialog()
 *
 * 全屏显示的弹窗，以屏幕左上角为其实坐标。
 */
@Composable
fun FullScreenDialog(
    dialog: IDialog?,
    scope: CoroutineScope,
    controller: DialogController
) {
    DialogInternal(
        // 原逻辑：dialog.onDismiss 为 null 视为禁止点击背景关闭；
        // 新增：显式 dismissOnBackdropClick=false 也表示禁止点背景关闭。
        backdropClickable = dialog?.onDismiss != null && dialog.dismissOnBackdropClick,
        forceProtectClickEvent = dialog?.forceProtectClickEvent ?: false,
        onDismiss = { controller.dismissDialog(dialog) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QNTheme.colorScheme.shadow25)
        ) {
            dialog?.content?.invoke(scope, controller)
        }
    }
}

/**
 * WARNING: 不要直接调用，使用DialogController.showDialog()
 *
 * 居中显示的弹窗。
 */
@Composable
fun ScreenCenterDialog(
    dialog: IDialog?,
    scope: CoroutineScope,
    controller: DialogController
) {
    DialogInternal(
        modifier = Modifier
            .fillMaxSize()
            .background(QNTheme.colorScheme.shadow25),
        backdropClickable = dialog?.onDismiss != null && dialog.dismissOnBackdropClick,
        forceProtectClickEvent = dialog?.forceProtectClickEvent ?: false,
        onDismiss = { controller.dismissDialog(dialog) }) {
        Box(
            modifier = Modifier
                .adaptDialogWidth { wrapContentSize() }
                .align(Alignment.Center)
                .clickable { /* 添加一个点击事件，防止点击到背景将弹窗关掉 */ },
        ) {
            dialog?.content?.invoke(scope, controller)
        }
    }
}


/**
 * 从屏幕底部划入的窗口
 */
@Composable
private fun BottomSheetDialog(
    onDismissed: () -> Unit,
    safeAreaBackgroundColorProvider: @Composable () -> Color,
    customBackgroundContent: (@Composable () -> Unit)? = null,
    forceProtectClickEvent: Boolean = false,
    backdropClickable: Boolean = true,
    content: (@Composable () -> Unit),
) {
    var isVisible by mutableStateOf(false)
    val scope = rememberCoroutineScope()

    val alpha by animateFloatAsState(0.25F, animationSpec = tween(delayMillis = 300))
    
    // 使用自定义背景或默认半透明遮罩
    val backgroundModifier = if (customBackgroundContent != null) {
        Modifier
    } else {
        Modifier.background(QNTheme.colorScheme.shadow25.changeAlpha(alpha))
    }
    DialogInternal(
        modifier = backgroundModifier,
        backdropClickable = backdropClickable,
        forceProtectClickEvent = forceProtectClickEvent,
        onDismiss = {
            scope.launch {
                isVisible = false
                delay(Spring.StiffnessMediumLow.toLong())
                onDismissed()
            }
        }
    ) {
        // 如果有自定义背景，先渲染背景
        customBackgroundContent?.invoke()
        Column(
            modifier = Modifier.fillMaxSize().onPlaced { isVisible = true },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val enterAnim = slideInVertically { height -> height } + fadeIn()
            val exitAnim = slideOutVertically { height -> height } + fadeOut()

            AnimatedVisibility(visible = isVisible, enter = enterAnim, exit = exitAnim) {
                Column(modifier = Modifier.adaptDialogWidth { fillMaxWidth() }) {
                    // 主内容
                    Box(
                        modifier = Modifier.weight(1F),
                        contentAlignment = Alignment.BottomCenter.adaptDialogPosition()
                    ) {
                        content.invoke()
                    }

                    // 安全区域作为垂直排列的一部分
                    val safeAreaHeight = safeAreaHeight()
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(safeAreaHeight)
                            .background(safeAreaBackgroundColorProvider())
                    )
                }
            }
        }

    }
}


/**
 * 创建一个 Modal 实例。Modal 是一个自定义的模态窗口组件，用于在当前页面上显示一个浮动窗口。
 * 当模态窗口显示时，用户无法与背景页面进行交互，只能与模态窗口内的内容进行交互。
 * 模态窗口可以用于显示表单、提示信息、详细信息等场景。
 *
 *  @param modifier 可不用设置size，因为默认固定和屏幕等大
 *  @param content 构建全屏模态下的UI内容
 */
@Composable
private fun DialogInternal(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    backdropClickable: Boolean = true,
    forceProtectClickEvent: Boolean = false,
    content: (@Composable BoxScope.() -> Unit)
) {
    val screenshotState = LocalScreenshot.current
    var hasSubDialog by remember { mutableStateOf(false) }
    if (!hasSubDialog) {
        val parentDialogController = LocalDialogController.current
        BackHandler {
            parentDialogController.dismissDialog(null)
        }
    }
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        val bubbleViewController = remember { BubbleViewController() }
        // Dialog里边再弹一个Dialog
        val dialogController = remember { DialogController() }
        QNDialogTheme(screenshotState) {
            CompositionLocalProvider(
                LocalBubbleViewController provides bubbleViewController,
                LocalDialogController provides dialogController,
            ) {
                // 仅当 backdropClickable 为 true 时，背景才挂 clickable（点击触发 onDismiss）；
                // 为 false 时，点击背景不会产生任何副作用，避免出现"退场动画启动但弹窗未真正关闭"
                // 进而被 onPlaced 重新设回可见的"自动打开"问题。
                val backgroundModifier = if (backdropClickable) {
                    modifier.fillMaxSize().clickable { onDismiss() }
                } else {
                    modifier.fillMaxSize()
                }.dialogPenetrateClickEvent(forceProtectClickEvent)
                Box(modifier = backgroundModifier) {
                    content()
                    bubbleViewController.CollectBubbleState()
                    dialogController.CollectDialogState { hasSubDialog = it }
                }

                if (hasSubDialog) {
                    BackHandler {
                        dialogController.dismissDialog(null)
                    }
                }
            }
        }
    }
}
