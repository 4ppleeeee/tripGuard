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
import com.tencent.kuikly.compose.animation.slideIn
import com.tencent.kuikly.compose.animation.slideInVertically
import com.tencent.kuikly.compose.animation.slideOut
import com.tencent.kuikly.compose.animation.slideOutVertically
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.onPlaced
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.news.core.compose.scaffold.modifiers.LocalInAutoSafeAreaScene
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.compose.scaffold.registry.LocalBubbleViewController
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.compose.scaffold.registry.LocalScreenshot
import com.tencent.news.core.compose.scaffold.registry.LocalToastController
import com.tencent.news.core.compose.scaffold.theme.ColorScheme
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.compose.scaffold.theme.QNDialogTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.SafeAreaLayout
import com.tencent.news.core.compose.view.bubble.BubbleViewController
import com.tencent.news.core.compose.view.extension.dialogPenetrateClickEvent
import com.tencent.news.core.compose.view.extension.dialogPenetrateTouchEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BOTTOM_SHEET_ANIMATION_DURATION_MS = 200

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
        // 传入 theme，使安全区 Spacer 在渲染时能注入正确的 ColorScheme，
        // 避免首次渲染时读取宿主页面的 ColorScheme 导致日夜间颜色不对
        safeAreaThemeOverride = theme,
        customBackgroundContent = dialog?.customBackgroundContent,
        onDismissStart = { dialog?.onDismissStart?.invoke() },
        onDragOffsetChange = dialog?.onDragOffsetChange,
        onDismissed = { controller.dismissDialog(dialog) },
        penetrateClickEvent = dialog?.forceProtectClickEvent ?: false,
        enableDragToDismiss = dialog?.enableDragToDismiss ?: false,
        contentHandlesBottomSafeArea = dialog?.contentHandlesBottomSafeArea ?: false,
        dragToDismissConfig = dialog?.dragToDismissConfig ?: DragToDismissConfig(),
        themeMode = dialog?.themeMode ?: DialogThemeMode.AUTO,
        forcePenetrateTouchEvent = dialog?.forceProtectTouchEvent ?: true,
    ) {
        CompositionLocalProvider(
            LocalInAutoSafeAreaScene provides true,
            LocalColorScheme provides theme
        ) {
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
        themeMode = dialog?.themeMode ?: DialogThemeMode.AUTO,
        forcePenetrateTouchEvent = dialog?.forceProtectTouchEvent ?: true,
        onDismiss = {
            // 如果dialog.onDismiss为null，则不关闭弹窗（禁止点击背景关闭）
            if (dialog?.onDismiss != null) {
                controller.dismissDialog(dialog)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QNTheme.colorScheme.shadow25)
                .dialogPenetrateClickEvent(dialog?.forceProtectClickEvent ?: true)
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
            .background(QNTheme.colorScheme.shadow25)
            .dialogPenetrateClickEvent(dialog?.forceProtectClickEvent ?: false),
        forcePenetrateTouchEvent = dialog?.forceProtectTouchEvent ?: true,
        themeMode = dialog?.themeMode ?: DialogThemeMode.AUTO,
        onDismiss = {
            if (dialog?.onDismiss != null) {
                controller.dismissDialog(dialog)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
                .clickable { /* 添加一个点击事件，防止点击到背景将弹窗关掉 */ },
        ) {
            dialog?.content?.invoke(scope, controller)
        }
    }
}


/**
 * WARNING: 不要直接调用，使用DialogController.showDialog()
 *
 * Snackbar 样式弹窗：从底部滑入，不阻断背景事件，内容完全自定义。
 * 与官方 Snackbar 形态一致，无遮罩层，用户可正常与背景页面交互。
 */
@Composable
fun SnackbarDialog(
    dialog: IDialog?,
    scope: CoroutineScope,
    controller: DialogController
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 自定义内容
        dialog?.content?.invoke(scope, controller)
    }
}

@Composable
fun ToastDialog(
    dialog: IDialog?,
    scope: CoroutineScope,
    controller: DialogController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // 自定义内容
        dialog?.content?.invoke(scope, controller)
    }
}

/**
 * WARNING: 不要直接调用，使用DialogController.showDialog()
 *
 * 从屏幕右侧滑入的侧边栏弹窗，参考 BottomSheetDialog 实现。
 * 适用于设置面板、筛选面板、详情侧边栏等场景。
 */
@Composable
fun RightSheetDialog(
    dialog: IDialog?,
    scope: CoroutineScope,
    controller: DialogController,
    theme: ColorScheme
) {
    RightSheetDialogInternal(
        onDismissStart = { dialog?.onDismissStart?.invoke() },
        onDismissed = { controller.dismissDialog(dialog) },
        penetrateClickEvent = dialog?.forceProtectClickEvent ?: false,
        customBackgroundContent = dialog?.customBackgroundContent,
        themeMode = dialog?.themeMode ?: DialogThemeMode.AUTO,
        forcePenetrateTouchEvent = dialog?.forceProtectTouchEvent ?: true
    ) {
        CompositionLocalProvider(LocalInAutoSafeAreaScene provides true) {
            dialog?.content?.invoke(scope, controller)
        }
    }
}

/**
 * 从屏幕右侧划入的窗口
 */
@Composable
private fun RightSheetDialogInternal(
    onDismissed: () -> Unit,
    onDismissStart: (() -> Unit)? = null,
    penetrateClickEvent: Boolean = false,
    customBackgroundContent: (@Composable () -> Unit)? = null,
    themeMode: DialogThemeMode = DialogThemeMode.AUTO,
    forcePenetrateTouchEvent: Boolean,
    content: (@Composable () -> Unit),
) {
    var isVisible by mutableStateOf(false)
    val scope = rememberCoroutineScope()

    val performDismiss: () -> Unit = {
        scope.launch {
            onDismissStart?.invoke()
            isVisible = false
            delay(Spring.StiffnessMediumLow.toLong())
            onDismissed()
        }
    }

    DialogInternal(
        modifier = Modifier.dialogPenetrateClickEvent(penetrateClickEvent),
        forcePenetrateTouchEvent = forcePenetrateTouchEvent,
        themeMode = themeMode,
        onDismiss = { performDismiss() }
    ) {
        customBackgroundContent?.invoke()

        Box(
            modifier = Modifier.fillMaxSize()
                .onPlaced { isVisible = true },
            contentAlignment = Alignment.CenterEnd
        ) {
            val enterAnim = slideIn(animationSpec = tween(durationMillis = 200)) { fullSize ->
                IntOffset(x = fullSize.width, y = 0)
            } + fadeIn()

            val exitAnim = slideOut(animationSpec = tween(durationMillis = 200)) { fullSize ->
                IntOffset(x = fullSize.width, y = 0)
            } + fadeOut()

            AnimatedVisibility(visible = isVisible, enter = enterAnim, exit = exitAnim) {
                Box(
                    modifier = Modifier
                        .clickable { /* 防止点击内容区域关闭弹窗 */ }
                ) {
                    content.invoke()
                }
            }
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
    /**
     * 可选：覆盖安全区 Spacer 渲染时的 ColorScheme。
     * 安全区 Spacer 在 content 外部渲染，不在 CompositionLocalProvider(LocalColorScheme provides theme) 范围内，
     * 首次渲染时会读取宿主页面的 ColorScheme，导致日夜间首次不对、切换后才正确的问题。
     * 传入此参数后，安全区 Spacer 渲染时会用 CompositionLocalProvider 注入正确的 theme。
     */
    safeAreaThemeOverride: ColorScheme? = null,
    customBackgroundContent: (@Composable () -> Unit)? = null,
    onDismissStart: (() -> Unit)? = null,
    /**
     * 可选回调：拖拽过程中实时通知当前拖拽偏移量（像素）。
     * 可用于在拖拽时同步驱动外部动画（如视频跟手缩放），消除停顿感。
     */
    onDragOffsetChange: ((Float) -> Unit)? = null,
    penetrateClickEvent: Boolean = false,
    enableDragToDismiss: Boolean = false,
    contentHandlesBottomSafeArea: Boolean = false,
    dragToDismissConfig: DragToDismissConfig = DragToDismissConfig(),
    themeMode: DialogThemeMode = DialogThemeMode.AUTO,
    forcePenetrateTouchEvent: Boolean,
    content: (@Composable () -> Unit),
) {
    var isVisible by mutableStateOf(false)
    val scope = rememberCoroutineScope()

    val alpha by animateFloatAsState(
        0.25F,
        animationSpec = tween(durationMillis = BOTTOM_SHEET_ANIMATION_DURATION_MS)
    )

    // dismiss 的统一入口：设置 isVisible = false 触发退出动画，动画结束后调用 onDismissed
    val performDismiss: () -> Unit = {
        scope.launch {
            onDismissStart?.invoke()  // 退出动画开始的瞬间立即通知外层
            isVisible = false
            delay(BOTTOM_SHEET_ANIMATION_DURATION_MS.toLong())
            onDismissed()
        }
    }

    // 使用自定义背景或默认半透明遮罩
    val backgroundModifier = if (customBackgroundContent != null) {
        Modifier
    } else {
        Modifier.background(QNTheme.colorScheme.shadow25.changeAlpha(alpha))
    }
    DialogInternal(
        modifier = backgroundModifier.dialogPenetrateClickEvent(penetrateClickEvent),
        forcePenetrateTouchEvent = forcePenetrateTouchEvent,
        themeMode = themeMode,
        onDismiss = { performDismiss() }
    ) {
        // 如果有自定义背景，先渲染背景
        customBackgroundContent?.invoke()

        Column(
            modifier = Modifier.fillMaxSize()
                .onPlaced { isVisible = true }
        ) {
            val enterAnim = slideInVertically(
                animationSpec = tween(durationMillis = BOTTOM_SHEET_ANIMATION_DURATION_MS)
            ) { height -> height } + fadeIn(
                animationSpec = tween(durationMillis = BOTTOM_SHEET_ANIMATION_DURATION_MS)
            )
            val exitAnim = slideOutVertically(
                animationSpec = tween(durationMillis = BOTTOM_SHEET_ANIMATION_DURATION_MS)
            ) { height -> height } + fadeOut(
                animationSpec = tween(durationMillis = BOTTOM_SHEET_ANIMATION_DURATION_MS)
            )

            AnimatedVisibility(visible = isVisible, enter = enterAnim, exit = exitAnim) {
                val sheetContent = @Composable {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        SafeAreaLayout(
                            contentHandlesSafeArea = contentHandlesBottomSafeArea,
                            safeAreaThemeOverride = safeAreaThemeOverride,
                            safeAreaBackgroundColorProvider = safeAreaBackgroundColorProvider
                        ) {
                            // 主内容
                            Box(
                                modifier = Modifier.weight(1F),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(modifier = Modifier.clickable { /* do nothing, 仅防止点击空白处关闭页面*/ }) {
                                    content.invoke()
                                }
                            }
                        }
                    }
                }

                if (enableDragToDismiss) {
                    DragToDismissContainer(
                        onDismiss = {
                            // 拖拽关闭走统一的 dismiss 流程，确保外部状态正确重置
                            performDismiss()
                        },
                        config = dragToDismissConfig,
                        onDragOffsetChange = onDragOffsetChange,
                    ) {
                        sheetContent()
                    }
                } else {
                    sheetContent()
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
    forcePenetrateTouchEvent: Boolean,
    themeMode: DialogThemeMode = DialogThemeMode.AUTO,
    onDismiss: () -> Unit,
    content: (@Composable BoxScope.() -> Unit)
) {
    val screenshotState = LocalScreenshot.current
    var hasSubDialog by remember { mutableStateOf(false) }
    if (!hasSubDialog) {
        BackHandler {
            onDismiss()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        val bubbleViewController = remember { BubbleViewController() }
        // Dialog里边再弹一个Dialog
        val dialogController = remember { DialogController() }
        QNDialogTheme(screenshotState, themeMode) {
            CompositionLocalProvider(
                LocalBubbleViewController provides bubbleViewController,
                LocalDialogController provides dialogController,
                LocalToastController provides dialogController,
            ) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .clickable { onDismiss() }) {

                    // 屏蔽所有事件
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .dialogPenetrateTouchEvent(forcePenetrateTouchEvent)
                    )

                    content()
                    bubbleViewController.CollectBubbleState()
                    dialogController.CollectDialogState { hasSubDialog = it }
                }

                if (hasSubDialog) {
                    BackHandler {
                        onDismiss()
                    }
                }
            }
        }
    }
}
