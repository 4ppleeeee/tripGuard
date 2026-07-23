@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package com.tencent.news.core.compose.scaffold.modifiers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.gestures.detectTapGestures
import com.tencent.kuikly.compose.foundation.gestures.forEachGesture
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.absoluteOffset
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyItemScope
import com.tencent.kuikly.compose.foundation.lazy.LazyListScope
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.BasicTextField
import com.tencent.kuikly.compose.foundation.text.KeyboardActions
import com.tencent.kuikly.compose.foundation.text.KeyboardOptions
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.composed
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.focus.FocusRequester
import com.tencent.kuikly.compose.ui.focus.focusRequester
import com.tencent.kuikly.compose.ui.focus.onFocusChanged
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.DefaultAlpha
import com.tencent.kuikly.compose.ui.graphics.SolidColor
import com.tencent.kuikly.compose.ui.graphics.painter.ColorPainter
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.input.pointer.PointerId
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.input.pointer.positionChanged
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.layout.boundsInParent
import com.tencent.kuikly.compose.ui.layout.boundsInRoot
import com.tencent.kuikly.compose.ui.layout.layout
import com.tencent.kuikly.compose.ui.layout.onGloballyPositioned
import com.tencent.kuikly.compose.ui.layout.onSizeChanged
import com.tencent.kuikly.compose.ui.layout.positionInRoot
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.TextLayoutResult
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.DpSize
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.max
import com.tencent.kuikly.compose_dsl.kuikly.extension.keyboardHeightChange
import com.tencent.kuikly.compose_dsl.kuikly.extension.placeHolder
import com.tencent.kuikly.compose_dsl.kuikly.extension.setEvent
import com.tencent.kuikly.compose_dsl.kuikly.extension.setProp
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.BoxShadow
import com.tencent.kuikly.core.base.event.EventHandlerFn
import com.tencent.kuikly.core.views.KeyboardParams
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.utils.ComposeUtils
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.platform.api.appStatus
import com.tencent.news.core.platform.api.appWindow
import com.tencent.news.core.platform.api.isLandscape
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * 用于标识当前是否在需要自动处理安全区域的场景中的CompositionLocal
 */
val LocalInAutoSafeAreaScene = staticCompositionLocalOf { false }

fun Modifier.backgroundColor(color: Color): Modifier {
    return this.background(color)
}

fun Modifier.margin(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f
): Modifier {
    return Modifier.padding(
        start = max(0f, start).dp,
        top = max(0f, top).dp,
        end = max(0f, end).dp,
        bottom = max(0f, bottom).dp
    ).then(this)
}

@Deprecated(message = "Use Image(painter, contentDescription) instead")
@Composable
@NonRestartableComposable
fun QnImageCompat(
    src: String? = null,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    placeholderSrc: Any? = null,
    borderRadius: Float = 0f,
    dragEnable: Boolean = false,
) {
    val placeholder = when (placeholderSrc) {
        is Painter -> placeholderSrc
        is DrawableResource -> painterResource(placeholderSrc)
        is String -> rememberAsyncImagePainter(placeholderSrc)
        else -> null
    }
    val painter = if (src.isNullOrEmpty()) {
        ColorPainter(Color.Transparent)
    } else {
        rememberAsyncImagePainter(src, placeholder = placeholder)
    }
    val mergedModifier = modifier
        .then(if (borderRadius > 0f) Modifier.clip(RoundedCornerShape(borderRadius)) else Modifier)
        .then(if (dragEnable) Modifier.dragEnable(dragEnable) else Modifier)
    QnImage(
        painter = painter,
        contentDescription = contentDescription,
        modifier = mergedModifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
    )
}

@Composable
@NonRestartableComposable
fun QnNetworkImage(
    src: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    borderRadius: Float = 0f,
) {
    val painter = rememberAsyncImagePainter(src)
    val mergedModifier = modifier
        .then(if (borderRadius > 0f) Modifier.clip(RoundedCornerShape(borderRadius)) else Modifier)
    QnImage(
        painter = painter,
        contentDescription = contentDescription,
        modifier = mergedModifier,
    )
}

fun Modifier.margin(all: Dp): Modifier {
    val rAll = max(0.dp, all)
    return Modifier.padding(rAll).then(this)
}

fun Modifier.margin(horizontal: Dp = 0.dp, vertical: Dp = 0.dp): Modifier =
    margin(start = horizontal, top = vertical, end = horizontal, bottom = vertical)

fun Modifier.margin(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): Modifier {
    return Modifier.padding(
        start = max(0.dp, start),
        top = max(0.dp, top),
        end = max(0.dp, end),
        bottom = max(0.dp, bottom)
    ).then(this)
}

fun Modifier.padding(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f
): Modifier {
    return this.padding(
        start = start.dp,
        top = top.dp,
        end = end.dp,
        bottom = bottom.dp
    )
}

fun Modifier.height(height: Float = 0f): Modifier {
    return this.height(height.dp)
}

fun Modifier.width(width: Float = 0f): Modifier {
    return this.width(width.dp)
}

fun Modifier.absoluteOffset(x: Float, y: Float): Modifier {
    return this.absoluteOffset(x.dp, y.dp)
}

fun Modifier.offset(x: Float, y: Float): Modifier {
    return this.offset(x.dp, y.dp)
}

fun Modifier.offsetWithParentAdjustment(
    x: Dp = 0.dp,
    y: Dp = 0.dp
) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val xPx = x.roundToPx()
    val yPx = y.roundToPx()

    // 计算实际需要的宽度和高度
    val width = placeable.width + xPx
    val height = placeable.height + yPx

    layout(width, height) {
        placeable.placeRelative(xPx, yPx)
    }
}

@Suppress("FunctionName")
fun LazyListScope.Hover(
    modifier: Modifier,
    key: Any? = null,
    hoverMarginTop: Dp = 0.dp,
    listState: IQnListState,
    content: @Composable LazyItemScope.() -> Unit
) {
    @OptIn(ExperimentalFoundationApi::class)
    stickyHeaderWithMarginTop(
        key = key,
        hoverMarginTop = hoverMarginTop,
        listState = listState.realListState as LazyListState
    ) {
        Column(modifier = modifier) {
            content()
        }
    }
}

fun Modifier.borderRadius(radius: Dp) = clip(RoundedCornerShape(radius))

fun Modifier.borderRadius(radius: Float) = clip(RoundedCornerShape(radius.dp))

fun Modifier.borderRadius(
    topLeft: Dp,
    topRight: Dp,
    bottomLeft: Dp,
    bottomRight: Dp
) = clip(
    RoundedCornerShape(
        topStart = topLeft,
        topEnd = topRight,
        bottomEnd = bottomRight,
        bottomStart = bottomLeft,
    )
)

fun Modifier.borderRadius(
    topLeft: Float,
    topRight: Float,
    bottomLeft: Float,
    bottomRight: Float
) = clip(
    RoundedCornerShape(
        topStart = topLeft.dp,
        topEnd = topRight.dp,
        bottomEnd = bottomRight.dp,
        bottomStart = bottomLeft.dp,
    )
)

fun Color.changeAlpha(alpha: Float) = this.copy(alpha = alpha)

/**
 * 实现类似 iOS viewWillAppear 事件的 Modifier 扩展
 * @param onAppear 组件首次组合时触发的回调（仅执行一次）
 * @param key 可选控制触发条件的重组键值
 */
@Composable
fun Modifier.willAppear(onAppear: () -> Unit): Modifier =
    willAppear(activeChecker = { true }, onAppear)

@Composable
fun Modifier.willAppear(activeChecker: () -> Boolean, onAppear: () -> Unit): Modifier {
    var hasAppeared by remember { mutableStateOf(false) }
    if (hasAppeared) {
        return this
    }
    return this.onGloballyPositioned {
        if (activeChecker() && !hasAppeared) {
            hasAppeared = true
            onAppear()
        }
    }
}

// 可见曝光：本方法要比 willAppear 准确（willAppear基本相当于onLayout，一些预载的view上屏也会触发）
@Composable
fun Modifier.willExpose(
    activeChecker: (() -> Boolean) = { true },
    exposePercentSlop: Float = 0.01f,
    onAppear: () -> Unit
): Modifier {
    var hasExposed by remember { mutableStateOf(false) }
    if (hasExposed) {
        return this
    }
    return appearPercentage { percent ->
        if (percent >= exposePercentSlop) {
            // 触发曝光口径后，再检查 activeChecker
            if (activeChecker() && !hasExposed) {
                hasExposed = true
                onAppear()
            }
        }
    }
}

fun Modifier.touchListener(
    onTouchEvent: (type: TouchType, position: Offset) -> Unit
) = pointerInput(Unit) {
    forEachGesture {
        awaitPointerEventScope {
            // 初始化触控点跟踪
            var currentPointerId: PointerId? = null

            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { change ->
                    when {
                        // 按下事件
                        change.pressed && currentPointerId == null -> {
                            currentPointerId = change.id
                            onTouchEvent(TouchType.Down, change.position)
                        }

                        // 移动事件（需匹配当前跟踪的指针）
                        change.id == currentPointerId && change.positionChanged() -> {
                            onTouchEvent(TouchType.Move, change.position)
                        }

                        // 抬起/取消事件
                        !change.pressed && change.id == currentPointerId -> {
                            onTouchEvent(TouchType.Up, change.position)
                            currentPointerId = null
                            return@awaitPointerEventScope
                        }
                    }
//                    change.consume() // 阻止事件冒泡
                }
            }
        }
    }
}

enum class TouchType { Down, Move, Up }

fun Modifier.dragEnable(enable: Boolean): Modifier {
    return this.setProp("dragEnable", enable)
}

fun Modifier.scrollsToTop(enable: Boolean): Modifier {
    return this.setProp("scrollsToTop", enable)
}

/**
 * 控制 ScrollView 滚动时禁止响应页面侧滑退出
 * @param disable true 表示禁止侧滑退出
 */
fun Modifier.disableSwipeBack(disable: Boolean = false): Modifier {
    return this.setProp("disableSwipeBack", disable)
}

/**
 * iOS 横向滚动组件在最左侧右滑时，优先让页面侧滑返回手势响应。
 */
fun Modifier.iosLeadingEdgeSwipeBackPriority(enable: Boolean = false): Modifier {
    return this.setProp("iosLeadingEdgeSwipeBackPriority", enable)
}

@Composable
fun Button(
    onClick: () -> Unit = {},
    onClick2: (Offset) -> Unit = {},
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit) = {}
) {
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    val onClickUpdate = rememberUpdatedState(onClick)
    val onClick2Update = rememberUpdatedState(onClick2)
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .onGloballyPositioned {
                rootPosition = it.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // 获取点击的相对于 Box 的坐标
                    val clickedPositionInBox = offset
                    // 转换为相对于根节点的位置
                    val clickedPositionInRoot = with(density) {
                        Offset(
                            (clickedPositionInBox.x + rootPosition.x).toDp().value,
                            (clickedPositionInBox.y + rootPosition.y).toDp().value
                        )
                    }
                    onClickUpdate.value.invoke()
                    onClick2Update.value.invoke(clickedPositionInRoot)
                }
            } then modifier, contentAlignment = Alignment.Center) {
        content()
    }
}

class Border(
    val lineWidth: Dp,
    val lineStyle: BorderStyle,
    val color: Color
)

fun Modifier.border(border: Border) =
    this.border(width = border.lineWidth, color = border.color)

fun Modifier.border(width: Float, color: Color, stroke: BorderStyle) =
    this.border(width = width.dp, color = color)

@Composable
fun TextField(
    modifier: Modifier,
    value: String = "",
    placeholder: String = "",
    autoFocus: Boolean = true,
    onValueChange: (String) -> Unit,
    onBlur: () -> Unit = {},
    onFocus: () -> Unit = {},
    keyboardHeightChange: (KeyboardParams) -> Unit = {},
    textStyle: TextStyle = TextStyle.Default,
    placeholderColor: Color? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    var lastFocus by remember { mutableStateOf(false) }

    val currentOnBlur by rememberUpdatedState(onBlur)
    val currentOnFocus by rememberUpdatedState(onFocus)
    val currentKeyboardHeightChange by rememberUpdatedState(keyboardHeightChange)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnTextLayout by rememberUpdatedState(onTextLayout)

    // 检测是否在需要自动处理安全区域的场景中
    val isInAutoSafeAreaScene = LocalInAutoSafeAreaScene.current

    // 获取安全区域高度
    val safeAreaHeight = if (isInAutoSafeAreaScene) {
        ComposeUtils.getSafeAreaHeight()
    } else {
        0f
    }

    // 包装keyboardHeightChange回调，在自动安全区域场景中自动减去安全区域高度
    val wrappedKeyboardHeightChange: (KeyboardParams) -> Unit = { params ->
        val adjustedParams = params.copy(height = params.height.fdp.value - safeAreaHeight)
        currentKeyboardHeightChange(adjustedParams)
    }

    var updatedModifier = modifier
        .keyboardHeightChange(wrappedKeyboardHeightChange)
        .focusRequester(focusRequester)
        .onFocusChanged {
            if (it.isFocused) {
                if (!lastFocus) {
                    currentOnFocus()
                    lastFocus = true
                }
            } else if (lastFocus) {
                currentOnBlur()
                lastFocus = false
            }
        }
        .let {
            if (placeholderColor != null) {
                it.placeHolder(placeholder, placeholderColor)
            } else it
        }

    if (appWindow().isLandscape()) {
        updatedModifier = updatedModifier.setProp("imeNoFullscreen", true)
    }

    BasicTextField(
        modifier = updatedModifier,
        value = value,
        onValueChange = currentOnValueChange,
        textStyle = textStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        onTextLayout = currentOnTextLayout,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = cursorBrush
    )

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
}

fun Modifier.onDragBegin(handler: EventHandlerFn) = setEvent("dragBegin", handler)

fun Modifier.onDragEnd(handler: EventHandlerFn) = setEvent("dragEnd", handler)

@Composable
fun View(modifier: Modifier) = Box(modifier = modifier)


@Composable
fun View(
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit
) = Box(modifier = modifier, content = content)

fun Modifier.boxShadow(
    offsetX: Float,
    offsetY: Float,
    shadowRadius: Float,
    shadowColor: Color
): Modifier {
    return this.setProp(
        "boxShadow",
        BoxShadow(offsetX, offsetY, shadowRadius, shadowColor.toKuiklyColor()).toString()
    )
}

fun Modifier.appearPercentage(
    onPercentageChanged: (Float) -> Unit
): Modifier = this.then(
    Modifier.onGloballyPositioned { layoutCoordinates ->
        val parent = layoutCoordinates.parentLayoutCoordinates ?: return@onGloballyPositioned
        val parentBounds = parent.boundsInRoot()
        val layoutBounds = layoutCoordinates.boundsInRoot()

        // 计算重叠区域
        val visibleTop = maxOf(layoutBounds.top, parentBounds.top)
        val visibleBottom = minOf(layoutBounds.bottom, parentBounds.bottom)
        val visibleHeight = (visibleBottom - visibleTop).coerceAtLeast(0f)
        val percent = visibleHeight / layoutBounds.height

        onPercentageChanged(percent.coerceIn(0f, 1f))
    }
)

fun Modifier.centerYPercentage(
    onPercentageChanged: (Float) -> Unit
): Modifier = this.then(
    Modifier.onGloballyPositioned { layoutCoordinates ->
        val parent = layoutCoordinates.parentLayoutCoordinates ?: return@onGloballyPositioned
        val parentBounds = parent.boundsInRoot()
        val layoutBoundsInParent = layoutCoordinates.boundsInParent()
        val percent =
            (layoutBoundsInParent.top + layoutBoundsInParent.bottom) / 2f / parentBounds.height
        onPercentageChanged(percent)
    }
)

fun Modifier.blurRadius(
    radius: Float,
): Modifier {
    return this.setProp(
        "blurRadius",
        radius
    )
}

/**
 * 背景模糊效果
 * @param radius 模糊半径
 */
fun Modifier.backgroundBlurRadius(
    radius: Float,
): Modifier {
    return this.setProp(
        "backgroundBlurRadius",
        radius
    )
}

fun Modifier.onSizeChangedDp(
    onSizeChanged: (IntSize) -> Unit
): Modifier = composed {
    val density = LocalDensity.current
    this.onSizeChanged {
        with(density) {
            val dpSize = IntSize(
                width = it.width.toDp().value.toInt(),
                height = it.height.toDp().value.toInt()
            )
            onSizeChanged(dpSize)
        }
    }
}

// 精准监听dp时使用这个，回调的是float
fun Modifier.onSizeChangedDp2(
    onSizeChanged: (DpSize) -> Unit
): Modifier = composed {
    val density = LocalDensity.current
    this.onSizeChanged {
        with(density) {
            onSizeChanged(DpSize(it.width.toDp(), it.height.toDp()))
        }
    }
}
