package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.layout.MeasurePolicy
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose_dsl.kuikly.extension.KuiklyDefaultMeasurePolicy
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.Attr
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.PlatformType
import com.tencent.news.core.compose.scaffold.modifiers.QnImageCompat
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.getPlatformType
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.page.model.StructLottie
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.view.constants.LottieDownloadState
import kotlinx.serialization.Serializable

private const val DATA = "data"
private const val THEME = "applyTheme" // 切换主题 日夜间
private const val PROGRESS = "setProgress" // 设置进度
private const val STATE = "state" // 当前state，内部使用

@Suppress("ModelClassRule")
@Serializable
data class ProgressRange(
    val startProgress: Float,
    val endProgress: Float
) : IKmmKeep

@Suppress("ModelClassRule")
@Serializable
data class QnLottieData(
    val name: String,
    val status: String = "",
    val aspectRatio: Float = 0F,        // 宽高比
    val autoPlay: Boolean = false,      // 自动播
    val loop: Boolean = false,          // 循环播放
    val tag: String = name,             // 鸿蒙专用
    val tintColor: String = "",         // tintColor
    val tintColorKey: String = "",      // tintColorKey
    val scaleType: QnLottieScaleType? = null,
    val scale: Float? = 1f,
    val cornerInDp: Float? = null,  // 由于QnLottie是自定义view，外部Box的corner不生效，需要自己实现一下
    val enableInteraction: Boolean = true, // 控制iOS中视图的userInteractionEnabled属性
    val progressRange: ProgressRange? = null, // 动画区间
    val textDelegate: Map<String, String>? = null, // 文本替换
) : IKmmKeep {
    internal fun toProp(): Any {
        if (isHarmonyPlatform()) {
            return KtJson.safeEncode(this)
        }
        return this
    }
}

enum class QnLottieScaleType(val nativeInt: Int) {
    MATRIX(0),
    FIT_XY(1),
    FIT_START(2),
    FIT_CENTER(3),
    FIT_END(4),
    CENTER(5),
    CENTER_CROP(6),
    CENTER_INSIDE(7)
}

@Composable
fun QnLottie(
    modifier: Modifier = Modifier,
    url: String,
    aspectRatio: Float = 0F,
    autoPlay: Boolean = false,
    infinity: Boolean = false,
    progress: Float = Float.NaN,
    scaleType: QnLottieScaleType? = null
) {
    QnLottie(
        modifier = modifier,
        fileName = url,
        aspectRatio = aspectRatio,
        autoPlay = autoPlay,
        infinity = infinity,
        progress = progress,
        scaleType = scaleType
    )
}

@Composable
fun QnLottie(
    modifier: Modifier = Modifier,
    lottie: StructLottie,
    aspectRatio: Float = 0F,
    autoPlay: Boolean = false,
    infinity: Boolean = false,
    progress: Float = Float.NaN,
) {
    val url = when (getPlatformType()) {
        PlatformType.ANDROID -> lottie.urlAndroid
        PlatformType.IOS -> lottie.urlIOS
        PlatformType.HARMONY -> lottie.urlOhos
    }

    QnLottie(
        modifier = modifier,
        fileName = url,
        aspectRatio = aspectRatio,
        autoPlay = autoPlay,
        infinity = infinity,
        progress = progress
    )
}

@Composable
fun QnLottie(
    modifier: Modifier = Modifier,
    fileName: String,
    aspectRatio: Float = 0F,
    autoPlay: Boolean = false,
    infinity: Boolean = false,
    status: String = "",
    progress: Float = Float.NaN,
    state: LottieState? = null,
    tag: String = fileName,
    measurePolicy: MeasurePolicy? = null,
    tintColor: Color? = null,
    tintColorKey: String? = null,
    scaleType: QnLottieScaleType? = null,
    scale: Float = 1f,
    cornerInDp: Float? = null,
    enableInteraction: Boolean = true,
    progressRange: ProgressRange? = null,
    textDelegate: Map<String, String>? = null,
) {
    if (fileName.isEmpty()) {
        return
    }

    val isDark = isAppInDarkTheme()
    val data = QnLottieData(
        name = fileName,
        status = status,
        aspectRatio = aspectRatio,
        autoPlay = autoPlay,
        loop = infinity,
        tag = tag,
        tintColor = tintColor?.toKuiklyColor()?.hexColor?.toString(16) ?: "",
        tintColorKey = tintColorKey ?: "",
        scaleType = scaleType,
        scale = scale,
        cornerInDp = cornerInDp,
        enableInteraction = enableInteraction,
        progressRange = progressRange,
        textDelegate = textDelegate,
    )

    MakeKuiklyComposeNode<QnLottieView>(
        factory = {
            QnLottieView()
        },
        modifier = modifier,
        measurePolicy = measurePolicy ?: KuiklyDefaultMeasurePolicy,
        viewInit = { },
        viewUpdate = {
            it.getViewAttr().run {
                if (state != null) {
                    with(STATE, state)
                }
                with(DATA, data.toProp())
                with(THEME, isDark)
                with(PROGRESS, progress)
            }
        },
    )
}

private class QnLottieView : DeclarativeBaseView<LottieViewAttr, LottieViewEvent>() {
    override fun createAttr(): LottieViewAttr {
        return LottieViewAttr()
    }

    override fun createEvent(): LottieViewEvent {
        return LottieViewEvent()
    }

    override fun viewName(): String {
        return "QnLottieView"
    }
}


object QnLottieAttr {
    const val SET_LOTTIE_DOWNLOAD_STATUS_LISTENER = "setLottieDownloadStatusListener"

    // compose内部字段
    internal const val STATE = "state"
}

private class LottieViewAttr : Attr() {
    fun with(key: String, value: Any): LottieViewAttr = this.apply {
        key with value
        if (value is LottieState) {
            (view() as? QnLottieView)?.getViewEvent()?.let { event ->
                event.setLottieDownloadStatusListener(value.downloadStatusListener)
            }
        }
    }
}

private class LottieViewEvent : Event() {
    internal fun setLottieDownloadStatusListener(listener: OnLottieDownloadStatusListener?) {
        if (listener != null) {
            register(QnLottieAttr.SET_LOTTIE_DOWNLOAD_STATUS_LISTENER) {
                val status = LottieDownloadStatus.decode(it) ?: return@register
                listener.onDownloadStatusChange(status)
            }
        } else {
            unRegister(QnLottieAttr.SET_LOTTIE_DOWNLOAD_STATUS_LISTENER)
        }
    }
}

/**
 * Lottie状态
 */
enum class LottieStatus {
    INIT,
    PLAYING,
    PLAYED
}

/**
 * Lottie下载状态
 */
enum class LottieDownloadStatus(val value: String) {
    PENDING(LottieDownloadState.PENDING),        // 待定 - 等待下载
    DOWNLOADING(LottieDownloadState.DOWNLOADING),    // 开始下载 - 正在下载中
    COMPLETED(LottieDownloadState.COMPLETED),      // 下载完成 - 下载成功完成
    FAILED(LottieDownloadState.FAILED);         // 下载失败 - 下载过程中出现错误

    public companion object {
        /**
         * 宿主专用：将状态转换为JSON字符串
         * 宿主调用此方法将状态变化传递给Compose
         */
        public fun toJsonString(status: LottieDownloadStatus): String {
            return """{"DOWNLOAD":"${status.name}"}"""
        }

        /**
         * 只处理JSONObject和String类型，其他类型返回null
         */
        public fun decode(value: Any?): LottieDownloadStatus? {
            return when (value) {
                is JSONObject -> {
                    val statusValue = value.optString("DOWNLOAD")
                    if (statusValue.isNotEmpty()) decode(statusValue) else null
                }

                is String -> try {
                    valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }

                else -> null
            }
        }
    }
}

class LottieState {

    var playStatus = mutableStateOf(LottieStatus.INIT)

    var downloadStatus = mutableStateOf(LottieDownloadStatus.PENDING)

    internal fun onPlayStatusChanged(status: LottieStatus) {
        playStatus.value = status
    }

    // 下载状态监控
    val downloadStatusListener: OnLottieDownloadStatusListener =
        OnLottieDownloadStatusListener {
            downloadStatus.value = it
        }

}

fun interface OnLottieDownloadStatusListener {
    fun onDownloadStatusChange(state: LottieDownloadStatus)
}

@Composable
fun QnLottie(
    modifier: Modifier = Modifier,
    fileName: String,
    placeholderUrl: String,
    aspectRatio: Float = 0F,
    autoPlay: Boolean = false,
    infinity: Boolean = false,
    status: String = "",
    progress: Float = Float.NaN,
    state: LottieState? = null,
    tag: String = fileName,
    measurePolicy: MeasurePolicy? = null,
    tintColor: Color? = null,
    tintColorKey: String? = null,
    scaleType: QnLottieScaleType? = null,
    cornerInDp: Float? = null,
    enableInteraction: Boolean = true,
    progressRange: ProgressRange? = null
) {
    val lottieState = remember { LottieState() }
    val currentState = state ?: lottieState

    // 根据下载状态决定显示内容
    val showPlaceholder = currentState.downloadStatus.value != LottieDownloadStatus.COMPLETED

    Box(modifier = modifier) {
        if (showPlaceholder && placeholderUrl.isNotEmpty()) {
            // 显示占位图
            QnImageCompat(
                src = placeholderUrl,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 始终创建Lottie视图，但根据状态控制可见性
        QnLottie(
            modifier = if (showPlaceholder) Modifier.size(0.dp) else Modifier.matchParentSize(),
            fileName = fileName,
            aspectRatio = aspectRatio,
            autoPlay = autoPlay && !showPlaceholder, // 只有在不显示占位图时才自动播放
            infinity = infinity,
            status = status,
            progress = progress,
            state = currentState,
            tag = tag,
            measurePolicy = measurePolicy,
            tintColor = tintColor,
            tintColorKey = tintColorKey,
            scaleType = scaleType,
            cornerInDp = cornerInDp,
            enableInteraction = enableInteraction,
            progressRange = progressRange
        )
    }
}