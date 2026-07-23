package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.animation.animateColorAsState
import com.tencent.kuikly.compose.animation.core.FastOutSlowInEasing
import com.tencent.kuikly.compose.animation.core.animateFloatAsState
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.draw.clipToBounds
import com.tencent.kuikly.compose.ui.draw.scale
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.height
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.platform.fsp
import com.tencent.news.core.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.tan

/**
 * 闪光效果包装器，为子内容添加从左到右的闪光动画效果
 *
 * @param modifier 应用于容器的修饰符
 * @param effectWidth 闪光效果的宽度
 * @param containerWidth 容器宽度，用于计算动画位移的终点
 * @param animationDuration 单次动画持续时间（毫秒）
 * @param initialDelay 初始延迟时间（毫秒）
 * @param autoRepeat 是否自动重复动画
 * @param repeatDelay 重复间隔时间（毫秒）
 * @param useShimmer 是否使用渐变闪光效果，false则使用图片
 * @param shimmerColors 渐变闪光的颜色列表，从左到右
 * @param shimmerAngle 闪光效果的倾斜角度，单位为度，默认为0度（水平）
 * @param shimmerImage 用于闪光效果的图片资源，如果提供则优先使用图片
 * @param content 要包装的内容组件
 */
@Composable
fun BlinkEffect(
    modifier: Modifier = Modifier,
    effectWidth: Float = 60f,
    containerWidth: Float = 260f, // 默认容器宽度
    containerHeight: Float = 0f,  // 默认容器高度
    animationDuration: Int = 1200, // 增加动画持续时间，扫光速度变慢
    initialDelay: Long = 500,
    autoRepeat: Boolean = true,
    repeatDelay: Long = 500, // 减少重复间隔
    useShimmer: Boolean = true,
    shimmerColors: List<Color> = listOf(
        Color(0x00FFFFFF),
        Color(0x80FFFFFF),
        Color(0x00FFFFFF)
    ),
    shimmerAngle: Float = 0f, // 添加闪光效果的倾斜角度参数，默认为0度
    shimmerImage: @Composable (() -> Unit)? = null, // 用于闪光效果的自定义图片组件
    animating: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    // 直接使用一个浮点状态来控制偏移量
    var offsetX by remember { mutableFloatStateOf(-effectWidth) }

    // 跟踪动画是否可见
    var isVisible by remember { mutableStateOf(false) }

    // 动画控制协程
    LaunchedEffect(containerHeight, containerWidth, animating) {
        if (!animating) {
            isVisible = false
            return@LaunchedEffect
        }
        
        // 初始延迟
        delay(initialDelay)

        // 动画循环
        while (true) {
            // 显示动画
            isVisible = true

            // 开始位置（容器外左侧）
            offsetX = -effectWidth

            // 短暂延迟确保状态更新
            delay(20)

            // 计算实际有效的动画距离和所需时间
            val effectiveDistance = containerWidth + effectWidth // 从完全进入到完全离开的距离
            val effectiveAnimationDuration = (animationDuration * 0.8).toInt() // 使用80%的动画时间用于有效区域
            val frameTime = 16L // 约60fps

            // 手动执行动画 - 使用帧计数
            var elapsedTime = 0L
            val visibilityThreshold = containerWidth // 超过此位置视为离开可见区域
            var hasLeftVisibleArea = false

            while (elapsedTime < effectiveAnimationDuration && isActive && !hasLeftVisibleArea) {
                // 计算进度，使用非线性缓动
                val progress = (elapsedTime.toFloat() / effectiveAnimationDuration).coerceIn(0f, 1f)
                val easedProgress = FastOutSlowInEasing.transform(progress)

                // 更新位置
                offsetX = -effectWidth + effectiveDistance * easedProgress

                // 检查是否已离开可见区域
                if (offsetX >= visibilityThreshold) {
                    hasLeftVisibleArea = true
                }

                // 帧等待和时间累加
                delay(frameTime)
                elapsedTime += frameTime
            }

            // 确保完全移出，即使提前跳出了循环
            offsetX = containerWidth + effectWidth

            // 隐藏闪光效果
            isVisible = false

            // 如果不需要重复，就退出循环
            if (!autoRepeat) break

            // 等待下一次动画
            delay(repeatDelay)
        }
    }

    Box(
        modifier = modifier.clipToBounds()
    ) {
        // 底层内容
        content()

        // 闪光效果层 - 仅在可见时显示
        if (isVisible) {
            if (shimmerImage != null) {
                // 使用自定义图片组件
                Box(
                    modifier = Modifier
                        .width(effectWidth.dp)
                        .fillMaxHeight()
                        .offset(x = offsetX.dp)
                ) {
                    shimmerImage()
                }
            } else if (useShimmer) {
                // 使用渐变实现闪光效果
                Box(
                    modifier = Modifier
                        .offset(x = offsetX.dp)
                        .let { if (containerHeight == 0f) it.fillMaxSize() else it.height(containerHeight.dp) }
                        .width(effectWidth.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = shimmerColors,
                                start = if (shimmerAngle == 0f) {
                                    Offset(0f, 0f)
                                } else {
                                    // 根据角度计算起始点和结束点
                                    val angleInRadians = (shimmerAngle * 3.1415926f / 180.0f)
                                    val x = 0f
                                    val y = (effectWidth * tan(angleInRadians))
                                    Offset(x, y)
                                },
                                end = if (shimmerAngle == 0f) {
                                    Offset(effectWidth, 0f)
                                } else {
                                    // 根据角度计算起始点和结束点
                                    val angleInRadians = (shimmerAngle * 3.1415926f / 180.0f)
                                    val x = effectWidth
                                    val y = 0f
                                    Offset(x, y)
                                }
                            )
                        )
                )
            } else {
                // 使用图片实现闪光效果
                QnImage(
                    painter = Res.drawable.tad_gameC,
                    modifier = Modifier
                        .offset(x = offsetX.dp)
                        .width(effectWidth.dp)
                        .height(60f),
                    contentDescription = ""
                )
            }
        }
    }
}

/**
 * 带有闪光效果的文本组件
 */
@Composable
internal fun BlinkText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: Int = 14,
    fontWeight: FontWeight? = null,
    effectWidth: Float = 60f,
    animationDuration: Int = 1200,
    initialDelay: Long = 500,
    autoRepeat: Boolean = true,
    repeatDelay: Long = 500,
) {
    BlinkEffect(
        modifier = modifier,
        effectWidth = effectWidth,
        animationDuration = animationDuration,
        initialDelay = initialDelay,
        autoRepeat = autoRepeat,
        repeatDelay = repeatDelay
    ) {
        QnText(
            text = text,
            color = color,
            fontSize = fontSize.sp,
            fontWeight = fontWeight
        )
    }
}

/**
 * 带有闪光效果和点击回弹效果的按钮组件
 *
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param backgroundColor 按钮背景色
 * @param textColor 文本颜色
 * @param cornerRadius 圆角大小
 * @param fontSize 文本字号
 * @param fontWeight 文本字重
 * @param height 按钮高度
 * @param effectWidth 闪光效果的宽度
 * @param animationDuration 单次动画持续时间（毫秒）
 * @param initialDelay 初始延迟时间（毫秒）
 * @param autoRepeat 是否自动重复动画
 * @param repeatDelay 重复间隔时间（毫秒）
 * @param enableBounceEffect 是否启用回弹效果
 * @param bounceScale 最大放大倍数，默认1.1倍
 * @param shimmerAngle 闪光效果的倾斜角度，单位为度，默认为0度（水平）
 * @param shimmerImage 用于闪光效果的图片资源，如果提供则优先使用图片
 */
@Composable
internal fun BlinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4169E1),
    textColor: Color = Color.White,
    cornerRadius: Float = 24f,
    fontSize: Int = 16,
    fontWeight: FontWeight? = null,
    height: Float = 48f,
    effectWidth: Float = 80f,
    containerWidth: Float = 280f,
    animationDuration: Int = 1200, // 增加动画持续时间，扫光速度变慢
    initialDelay: Long = 500,
    autoRepeat: Boolean = true,
    repeatDelay: Long = 500, // 减少重复间隔
    enableBounceEffect: Boolean = false,
    bounceScale: Float = 0.9f, // 默认值修改为按下缩小到90%
    shimmerAngle: Float = 0f, // 添加闪光效果的倾斜角度参数，默认为0度
    shimmerImage: @Composable (() -> Unit)? = null // 用于闪光效果的自定义图片组件
) {
    // 回弹动画状态
    var isPressed by remember { mutableStateOf(false) }

    // 缩放动画值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) bounceScale else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 100 else 150, // 缩小快，恢复慢
            easing = FastOutSlowInEasing
        )
    )

    // 使用安全的协程范围
    val coroutineScope = rememberCoroutineScope()

    // 跟踪是否正在执行动画，防止重复点击
    var isAnimating by remember { mutableStateOf(false) }

    BlinkEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(if (enableBounceEffect) scale else 1f) // 只有启用时才应用缩放
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable {
                if (enableBounceEffect && !isAnimating) {
                    // 设置正在动画中标志，防止重复触发
                    isAnimating = true

                    // 使用composable安全的协程范围
                    coroutineScope.launch {
                        // 触发缩小动画
                        isPressed = true
                        delay(100) // 缩小持续时间

                        // 触发恢复动画
                        isPressed = false
                        delay(150) // 等待恢复完成

                        // 执行点击操作
                        onClick()

                        // 重置动画状态
                        isAnimating = false
                    }
                } else if (!isAnimating) {
                    // 直接执行点击回调
                    onClick()
                }
            },
        effectWidth = effectWidth,
        containerWidth = containerWidth,
        animationDuration = animationDuration,
        initialDelay = initialDelay,
        autoRepeat = autoRepeat,
        repeatDelay = repeatDelay,
        shimmerColors = listOf(
            Color(0x00FFFFFF),
            Color(0x60FFFFFF),
            Color(0x00FFFFFF)
        ),
        shimmerAngle = shimmerAngle,
        shimmerImage = shimmerImage
    ) {
        // 在Box中居中放置文本
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            QnText(
                text = text,
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 带有回弹效果的闪光按钮，缩小后恢复的效果
 * 与普通BlinkButton相同，但默认启用回弹效果
 */
@Composable
fun BlinkBounceButton(
    title: String,
    desc: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4169E1),
    titleColor: Color = Color.White,
    descColor: Color = titleColor,
    cornerRadius: Float = 24f,
    titleFontSize: Int = 16,
    descFontSize: Int = 10,
    titleFontWeight: FontWeight? = FontWeight.Bold,
    descFontWeight: FontWeight? = null,
    height: Dp = 48.dp, // 固定高度为48f，不随desc变化
    effectWidth: Float = 80f,
    containerWidth: Float = 280f,
    animationDuration: Int = 1200,
    initialDelay: Long = 500,
    autoRepeat: Boolean = true,
    repeatDelay: Long = 500,
    bounceScale: Float = 0.9f,
    shimmerAngle: Float = 0f,
    shimmerImage: @Composable (() -> Unit)? = null
) {
    // 回弹动画状态
    var isPressed by remember { mutableStateOf(false) }

    // 缩放动画值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) bounceScale else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 100 else 150, // 缩小快，恢复慢
            easing = FastOutSlowInEasing
        )
    )

    // 使用安全的协程范围
    val coroutineScope = rememberCoroutineScope()

    // 跟踪是否正在执行动画，防止重复点击
    var isAnimating by remember { mutableStateOf(false) }

    // 使用BlinkEffect包装按钮，实现闪光效果
    BlinkEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable {
                if (!isAnimating) {
                    // 设置正在动画中标志，防止重复触发
                    isAnimating = true

                    // 使用composable安全的协程范围
                    coroutineScope.launch {
                        // 触发缩小动画
                        isPressed = true
                        delay(100) // 缩小持续时间

                        // 触发恢复动画
                        isPressed = false
                        delay(50) // 等待恢复完成

                        // 执行点击操作
                        onClick()

                        // 重置动画状态
                        isAnimating = false
                    }
                }
            },
        effectWidth = effectWidth,
        containerWidth = containerWidth,
        animationDuration = animationDuration,
        initialDelay = initialDelay,
        autoRepeat = autoRepeat,
        repeatDelay = repeatDelay,
        shimmerColors = listOf(
            Color(0x00FFFFFF),
            Color(0x80FFFFFF),
            Color(0x00FFFFFF)
        ),
        shimmerAngle = shimmerAngle,
        shimmerImage = shimmerImage
    ) {
        // 绘制文本内容 - 垂直布局
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.fdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题文本
            QnText(
                text = title,
                color = titleColor,
                fontSize = titleFontSize.fsp,
                fontWeight = titleFontWeight,
                textAlign = TextAlign.Center
            )

            // 描述文本 - 如果有
            if (desc != null) {
                Spacer(modifier = Modifier.height(6.dp))

                QnText(
                    text = desc,
                    color = descColor,
                    fontSize = descFontSize.fsp,
                    fontWeight = descFontWeight,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 纯点击效果按钮 - 只有点击缩小再恢复的效果，没有扫光效果
 *
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param backgroundColor 按钮背景色
 * @param textColor 文本颜色
 * @param cornerRadius 圆角大小
 * @param fontSize 文本字号
 * @param fontWeight 文本字重
 * @param height 按钮高度
 * @param bounceScale 按下时缩小比例，默认为0.9(缩小到90%)
 * @param enabled 按钮是否启用，默认为true
 */
@Composable
fun ClickScaleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4169E1),
    textColor: Color = Color.White,
    cornerRadius: Float = 24f,
    fontSize: Int = 16,
    fontWeight: FontWeight? = null,
    height: Dp = 48.dp,
    bounceScale: Float = 0.9f,
    enabled: Boolean = true
) {
    // 回弹动画状态
    var isPressed by remember { mutableStateOf(false) }

    // 缩放动画值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) bounceScale else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 100 else 150, // 缩小快，恢复慢
            easing = FastOutSlowInEasing
        )
    )

    // 使用安全的协程范围
    val coroutineScope = rememberCoroutineScope()

    // 跟踪是否正在执行动画，防止重复点击
    var isAnimating by remember { mutableStateOf(false) }

    // 根据启用状态调整颜色
    val finalBgColor = if (enabled) backgroundColor else Color(0xFFCCCCCC)
    val finalTextColor = if (enabled) textColor else Color(0xFF888888)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(finalBgColor)
            .clickable(enabled = enabled) {
                if (!isAnimating) {
                    // 设置正在动画中标志，防止重复触发
                    isAnimating = true

                    // 使用composable安全的协程范围
                    coroutineScope.launch {
                        // 触发缩小动画
                        isPressed = true
                        delay(100) // 缩小持续时间

                        // 触发恢复动画
                        isPressed = false
                        delay(150) // 等待恢复完成

                        // 执行点击操作
                        onClick()

                        // 重置动画状态
                        isAnimating = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        QnText(
            text = text,
            color = finalTextColor,
            fontSize = fontSize.fsp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 带有背景色变化和缩放动画的按钮
 *
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param normalBgColor 正常状态背景色
 * @param pressedBgColor 按下状态背景色
 * @param textColor 文本颜色
 * @param pressedTextColor 按下状态文本颜色，默认与正常状态相同
 * @param cornerRadius 圆角大小
 * @param fontSize 文本字号
 * @param fontWeight 文本字重
 * @param height 按钮高度
 * @param bounceScale 按下时缩小比例，默认为0.95(缩小到95%)
 * @param borderWidth 边框宽度，默认为1dp
 * @param borderColor 边框颜色
 * @param enabled 按钮是否启用，默认为true
 */
@Composable
fun ColorChangeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    normalBgColor: Color = Color.White,
    pressedBgColor: Color = Color(0xFFE0E0E0), // 默认按下时变灰
    textColor: Color = Color.Black,
    pressedTextColor: Color? = null, // 默认与正常状态文本颜色相同
    cornerRadius: Float = 24f,
    fontSize: Int = 16,
    fontWeight: FontWeight? = null,
    height: Float = 48f,
    bounceScale: Float = 0.95f,
    borderWidth: Float = 1f,
    borderColor: Color = Color.Gray,
    enabled: Boolean = true
) {
    // 按下状态
    var isPressed by remember { mutableStateOf(false) }

    // 缩放动画值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) bounceScale else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 100 else 150, // 缩小快，恢复慢
            easing = FastOutSlowInEasing
        )
    )

    // 根据启用状态调整颜色
    val finalNormalBgColor = if (enabled) normalBgColor else Color(0xFFEEEEEE)
    val finalPressedBgColor = if (enabled) pressedBgColor else Color(0xFFDDDDDD)
    val finalTextColor = if (enabled) textColor else Color(0xFF888888)
    val finalPressedTextColor = if (enabled) (pressedTextColor ?: textColor) else Color(0xFF888888)

    // 使用animateColorAsState实现背景色过渡动画
    val currentBgColor by animateColorAsState(
        targetValue = if (isPressed) finalPressedBgColor else finalNormalBgColor,
        animationSpec = tween(
            durationMillis = if (isPressed) 50 else 200, // 颜色变化比缩放更快显示，更慢恢复
            easing = FastOutSlowInEasing
        )
    )

    // 使用animateColorAsState实现文本颜色过渡动画
    val currentTextColor by animateColorAsState(
        targetValue = if (isPressed) finalPressedTextColor else finalTextColor,
        animationSpec = tween(
            durationMillis = if (isPressed) 50 else 200,
            easing = FastOutSlowInEasing
        )
    )

    // 使用安全的协程范围
    val coroutineScope = rememberCoroutineScope()

    // 跟踪是否正在执行动画，防止重复点击
    var isAnimating by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(currentBgColor)
            .border(
                width = borderWidth.dp,
                color = if (enabled) borderColor else Color(0xFFCCCCCC)
            )
            .clickable(enabled = enabled) {
                if (!isAnimating) {
                    // 设置正在动画中标志，防止重复触发
                    isAnimating = true

                    // 使用composable安全的协程范围
                    coroutineScope.launch {
                        // 触发按下效果
                        isPressed = true
                        delay(150) // 保持按下状态的时间

                        // 触发恢复动画
                        isPressed = false
                        delay(200) // 等待恢复完成

                        // 执行点击操作
                        onClick()

                        // 重置动画状态
                        isAnimating = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        QnText(
            text = text,
            color = currentTextColor,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 带有背景色变化和缩放动画的按钮 - 支持标题和描述的版本
 *
 * @param title 按钮标题
 * @param desc 按钮描述，可为null
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param normalBgColor 正常状态背景色
 * @param pressedBgColor 按下状态背景色
 * @param titleColor 标题颜色
 * @param descColor 描述颜色
 * @param pressedTitleColor 按下状态标题颜色，默认与正常状态相同
 * @param pressedDescColor 按下状态描述颜色，默认与正常状态相同
 * @param cornerRadius 圆角大小
 * @param titleFontSize 标题字号
 * @param descFontSize 描述字号
 * @param titleFontWeight 标题字重
 * @param descFontWeight 描述字重
 * @param height 按钮高度
 * @param bounceScale 按下时缩小比例，默认为0.95(缩小到95%)
 * @param borderWidth 边框宽度，默认为1dp
 * @param borderColor 边框颜色
 * @param enabled 按钮是否启用，默认为true
 */
@Composable
fun ColorChangeButton(
    title: String,
    desc: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    normalBgColor: Color = Color.White,
    pressedBgColor: Color = Color(0xFFE0E0E0), // 默认按下时变灰
    titleColor: Color = Color.Black,
    descColor: Color = titleColor,
    pressedTitleColor: Color? = null, // 默认与正常状态文本颜色相同
    pressedDescColor: Color? = null, // 默认与正常状态文本颜色相同
    cornerRadius: Float = 24f,
    titleFontSize: Int = 16,
    descFontSize: Int = 10,
    titleFontWeight: FontWeight? = null,
    descFontWeight: FontWeight? = null,
    height: Dp = 48.dp,
    bounceScale: Float = 0.95f,
    borderWidth: Float = 1f,
    borderColor: Color = Color.Gray,
    enabled: Boolean = true
) {
    // 按下状态
    var isPressed by remember { mutableStateOf(false) }

    // 缩放动画值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) bounceScale else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 100 else 150, // 缩小快，恢复慢
            easing = FastOutSlowInEasing
        )
    )

    // 根据启用状态调整颜色
    val finalNormalBgColor = if (enabled) normalBgColor else Color(0xFFEEEEEE)
    val finalPressedBgColor = if (enabled) pressedBgColor else Color(0xFFDDDDDD)
    val finalTitleColor = if (enabled) titleColor else Color(0xFF888888)
    val finalDescColor = if (enabled) descColor else Color(0xFF888888)
    val finalPressedTitleColor =
        if (enabled) (pressedTitleColor ?: titleColor) else Color(0xFF888888)
    val finalPressedDescColor = if (enabled) (pressedDescColor ?: descColor) else Color(0xFF888888)

    // 使用animateColorAsState实现背景色过渡动画
    val currentBgColor by animateColorAsState(
        targetValue = if (isPressed) finalPressedBgColor else finalNormalBgColor,
        animationSpec = tween(
            durationMillis = if (isPressed) 50 else 200, // 颜色变化比缩放更快显示，更慢恢复
            easing = FastOutSlowInEasing
        )
    )

    // 使用animateColorAsState实现文本颜色过渡动画
    val currentTitleColor by animateColorAsState(
        targetValue = if (isPressed) finalPressedTitleColor else finalTitleColor,
        animationSpec = tween(
            durationMillis = if (isPressed) 50 else 200,
            easing = FastOutSlowInEasing
        )
    )

    val currentDescColor by animateColorAsState(
        targetValue = if (isPressed) finalPressedDescColor else finalDescColor,
        animationSpec = tween(
            durationMillis = if (isPressed) 50 else 200,
            easing = FastOutSlowInEasing
        )
    )

    // 使用安全的协程范围
    val coroutineScope = rememberCoroutineScope()

    // 跟踪是否正在执行动画，防止重复点击
    var isAnimating by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .background(currentBgColor)
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = borderWidth.dp,
                color = if (enabled) borderColor else Color(0xFFCCCCCC)
            )
            .clickable(enabled = enabled) {
                if (!isAnimating) {
                    // 设置正在动画中标志，防止重复触发
                    isAnimating = true

                    // 使用composable安全的协程范围
                    coroutineScope.launch {
                        // 触发按下效果
                        isPressed = true
                        delay(150) // 保持按下状态的时间

                        // 触发恢复动画
                        isPressed = false
                        delay(200) // 等待恢复完成

                        // 执行点击操作
                        onClick()

                        // 重置动画状态
                        isAnimating = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 绘制文本内容 - 垂直布局
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.fdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题文本
            QnText(
                text = title,
                color = currentTitleColor,
                fontSize = titleFontSize.fsp,
                fontWeight = titleFontWeight,
                textAlign = TextAlign.Center
            )

            // 描述文本 - 如果有
            if (desc != null) {
                Spacer(modifier = Modifier.height(4.fdp))

                QnText(
                    text = desc,
                    color = currentDescColor,
                    fontSize = descFontSize.fsp,
                    fontWeight = descFontWeight,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 