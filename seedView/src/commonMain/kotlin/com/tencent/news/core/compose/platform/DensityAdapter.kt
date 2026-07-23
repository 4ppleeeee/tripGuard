package com.tencent.news.core.compose.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.compose.scaffold.theme.LocalUiMode
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.platform.api.appScreenInfo
import com.tencent.news.core.platform.api.appStatus

/**
 * IOS375Scale 作用域内的屏宽/375缩放比例，默认1F（不缩放）。
 * 用于在IOS375Scale内部让sp值抵消scaleBy375的影响。
 */
val LocalIOS375ScaleRatio = staticCompositionLocalOf { 1F }

@Composable
@Stable
expect fun getWindowHeightInDp(): Float

/**
 * 不根据屏幕密度进行缩放的dp单位
 */
@Stable
val Int.fdp: Dp
    get() = (this.toFloat() / noTitleScaleRatio).dp

/**
 * 不根据屏幕密度进行缩放的dp单位
 */
@Stable
val Double.fdp: Dp
    get() = (this.toFloat() / noTitleScaleRatio).dp

/**
 * 不根据屏幕密度进行缩放的sp单位
 */
@Stable
val Float.fdp: Dp
    get() = this.dp / noTitleScaleRatio

/**
 * 不根据屏幕密度进行缩放的sp单位
 */
@Stable
val Int.fsp: TextUnit
    get() = this.sp / noTitleScaleRatio

/**
 * 不根据屏幕密度进行缩放的sp单位
 */
@Stable
val Double.fsp: TextUnit
    get() = this.sp / noTitleScaleRatio

/**
 * 不根据屏幕密度进行缩放的sp单位
 */
@Stable
val Float.fsp: TextUnit
    get() = this.sp / noTitleScaleRatio

// 系统默认density
val sysDensity: Float
    @Composable get() = LocalConfiguration.current.pageData.density

// 当前缩放比例，适用于所有场景，但仅用于compose上下文
val suitableScaleRatio: Float
    @Composable get() = LocalDensity.current.density / sysDensity

// 当前缩放比例，适用于非标题场景，但兼容非compose上下文
private val noTitleScaleRatio: Float
    get() = getSuitableDensityScaleRatio(appStatus().currentTextScaleGradient())

// 获取适合当前density的缩放比例，适用于非标题场景
private fun getSuitableDensityScaleRatio(level: DensityScaleGradient): Float {
    val expectedLevel = if (level >= DensityScaleGradient.L5) DensityScaleGradient.L4 else level
    return appStatus().getScaleRatioByGradient(expectedLevel).toFloat()
}

@Composable
private fun getNormalDensityScale(): Float {
    val textScaleLevel = LocalUiMode.current.textScaleLevel
    return getSuitableDensityScaleRatio(textScaleLevel)
}

/**
 * 正常缩放:最大缩放到[DensityScaleGradient.L4]
 */
@Composable
fun NormalDensityScale(content: @Composable () -> Unit) {
    val density = Density(sysDensity * getNormalDensityScale(), 1F)
    SetDensityScale(density) {
        content()
    }
}

/**
 * 在NormalDensityScale基础上针对iOS进行 屏宽/375 适配，使用于少数商业化场景。
 * dp会乘以scaleBy375系数进行屏幕适配，sp不受scaleBy375影响。
 * QnText内部已自动处理，调用方无需额外修改。
 */
@Composable
fun IOS375Scale(content: @Composable () -> Unit) {
    if (isIOSPlatform()) {
        val scaleBy375 = appScreenInfo().getScreenWidth().toFloat() / 375F
        val density = Density(sysDensity * getNormalDensityScale() * scaleBy375, 1F)
        CompositionLocalProvider(LocalIOS375ScaleRatio provides scaleBy375) {
            SetDensityScale(density) {
                content()
            }
        }
    } else {
        content()
    }
}

/**
 * 标题缩放:最大缩放到[DensityScaleGradient.L5]
 */
@Composable
fun TitleDensityScale(disable: Boolean = false, content: @Composable () -> Unit) {
    val scale = if (!disable) {
        val expectedLevel = appStatus().currentTextScaleGradient()
        appStatus().getScaleRatioByGradient(expectedLevel).toFloat()
    } else {
        1F
    }
    SetDensityScale(Density(sysDensity * scale, 1F)) {
        content()
    }
}

/**
 * 特殊缩放：最大缩放到[max]梯度
 */
@Composable
fun MaxDensityScale(max: DensityScaleGradient, content: @Composable () -> Unit) {
    val expectedLevel = minOf(max, appStatus().currentTextScaleGradient())
    val scale = appStatus().getScaleRatioByGradient(expectedLevel).toFloat()
    SetDensityScale(Density(sysDensity * scale, 1F)) {
        content()
    }
}

/**
 * 强制缩放到指定比例
 */
@Composable
fun ForceDensityScale(fontScale: Float, content: @Composable () -> Unit) {
    SetDensityScale(Density(sysDensity * fontScale, 1F)) {
        content()
    }
}

/**
 * 强制不缩放
 */
@Composable
fun ForceNoScale(content: @Composable () -> Unit) {
    SetDensityScale(Density(sysDensity, 1F)) {
        content()
    }
}

@Composable
private fun SetDensityScale(density: Density, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDensity provides density) {
        content()
    }
}

/**
 * 约束当前dp最大缩放到[gradient]
 */
fun Dp.max(gradient: DensityScaleGradient): Dp {
    val currentLevel = appStatus().currentTextScaleGradient()
    if (gradient >= currentLevel) {
        return this
    }
    val scale = gradient / currentLevel
    return this * scale
}

/**
 * 约束当前sp最大缩放到[gradient]
 */
fun TextUnit.max(gradient: DensityScaleGradient): TextUnit {
    val currentLevel = appStatus().currentTextScaleGradient()
    if (gradient >= currentLevel) {
        return this
    }
    val scale = gradient / currentLevel
    return this * scale
}

/**
 * 获取页面宽度。禁止直接使用[LocalConfiguration.current.pageViewWidth]
 */
@Composable
fun pageViewWidth(): Dp {
    // 比如375*3.25的屏幕宽度，适配density * 1.3之后，
    // 返回的pageViewWidth应该是375*3.25 / (3.25 * 1.3) = 288
    return LocalConfiguration.current.pageViewWidth.dp / suitableScaleRatio
}

@Composable
fun pageViewWidthValue(): Float {
    return pageViewWidth().value
}

/**
 * 获取页面高度。禁止直接使用[LocalConfiguration.current.pageViewHeight]
 */
@Composable
fun pageViewHeight(): Dp {
    // 比如820*3.25的屏幕宽度，适配density * 1.3之后，
    // 返回的pageViewWidth应该是820*3.25 / (3.25 * 1.3) = 630
    return LocalConfiguration.current.pageViewHeight.dp / suitableScaleRatio
}

@Composable
fun pageViewHeightValue(): Float {
    return pageViewHeight().value
}

@Composable
fun statusBarHeight(): Dp {
    return LocalConfiguration.current.statusBarHeight.dp / suitableScaleRatio
}

@Composable
fun statusBarHeightFdp(): Dp {
    return LocalConfiguration.current.statusBarHeight.fdp
}

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}
