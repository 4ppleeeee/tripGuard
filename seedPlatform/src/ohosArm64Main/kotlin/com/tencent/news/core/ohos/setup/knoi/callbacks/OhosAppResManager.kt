package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IResManager
import com.tencent.news.core.platform.api.PaletteParam
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

/**
 * ArkTS 侧注入入口使用的 knoi 句柄类型。
 *
 * 与 OhosAppAlert/OhosAppShare 等保持一致：@ServiceProvider 的方法参数不能直接使用 @KNCallback 接口，
 * 只能使用 knoi 允许的基础类型（JSValue 即是其中之一），真正的接口对象在 setupOhosAppResManager
 * 内部通过 asOhosAppResManager() 取得（该扩展由 knoi 基于 @KNCallback 自动生成）。
 */
typealias IOhosAppResManager = JSValue

/**
 * 注入鸿蒙端 IResManager 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于鸿蒙系统 API 的真实实现桥接到 KMP 层的 IResManager：
 * - getAssetJson：读取 rawfile 资源。
 * - copyToClipboard：调用 @kit.BasicServicesKit 的 pasteboard。
 * - preloadImage / preloadAlphaVideo：使用 http + 沙盒缓存做真实预加载。
 * - saveImage / saveVideo：使用 photoAccessHelper 的 PhotoCreationConfig 将媒体写入系统相册。
 * - selectImage：使用 photoAccessHelper.PhotoViewPicker 拉起相册选图。
 * - getPaletteColor：使用 @kit.ArkGraphics2D 的 effectKit.createColorPicker 真实取色。
 * - preloadLottieToMemory：将 lottie json 下载至沙盒缓存，后续 QnLottie 使用缓存路径。
 *
 * 接口设计上所有异步方法均通过 trailing lambda 回调结果，保持与 OhosLoginService 一致的 knoi 调用风格，
 * 不依赖 JSValue.asPromise()。
 */
fun setupOhosAppResManager(resManager: IOhosAppResManager) {
    QnPlatformLogic.resManager = OhosAppResManagerProvider(resManager.asOhosAppResManager())
}

private class OhosAppResManagerProvider(
    private val native: OhosAppResManager,
) : IResManager {

    override fun getAssetJson(fileName: String): String {
        if (fileName.isBlank()) {
            return ""
        }
        return runCatching { native.getAssetJson(fileName) }.getOrDefault("")
    }

    override fun preloadImage(
        url: String,
        onSuccess: (() -> Unit)?,
        onFail: (() -> Unit)?,
    ) {
        if (url.isBlank()) {
            onFail?.invoke()
            return
        }
        native.preloadImage(url) { success ->
            if (success) onSuccess?.invoke() else onFail?.invoke()
        }
    }

    override fun copyToClipboard(content: String) {
        if (content.isEmpty()) {
            return
        }
        runCatching { native.copyToClipboard(content) }
    }

    override fun saveImage(url: String, metadata: Map<String, String>?) {
        if (url.isBlank()) {
            return
        }
        // metadata 当前在鸿蒙端无真实业务诉求，保留参数但不透传
        native.saveImage(url) { _, _ -> /* UI 侧提示由 ArkTS 端 showToast 兜底 */ }
    }

    override fun saveVideo(localFilePath: String): Boolean {
        if (localFilePath.isBlank()) return false
        native.saveVideo(localFilePath)
        return true
    }

    override fun selectImage(context: IKmmContext?, callback: (url: List<String>) -> Unit) {
        native.selectImage(DEFAULT_PHOTO_PICK_MAX_COUNT) { resultToken ->
            callback.invoke(parseMediaResult(native.consumeMediaResult(resultToken)))
        }
    }

    override fun getPaletteColor(
        imageUrl: String,
        param: PaletteParam,
        defaultColor: Int?,
        onGot: (color: Int) -> Unit,
    ) {
        if (imageUrl.isBlank()) {
            onGot.invoke(defaultColor ?: 0)
            return
        }
        // ArkTS 侧只认字符串参数，这里用 JSON 编码保持与 QnCore 鸿蒙端一致的协议格式。
        val paramString = buildString {
            append("{\"imageSize\":")
            append(param.imageSize)
            append(",\"colorCount\":")
            append(param.colorCount)
            append(",\"imageRect\":\"")
            append(param.imageRect)
            append("\"}")
        }
        native.getPaletteColor(imageUrl, paramString) { colorString ->
            val color = parseHexColor(colorString, defaultColor)
            onGot.invoke(color)
        }
    }

    override fun preloadLottieToMemory(
        context: IKmmContext?,
        url: String,
        status: String,
        isDay: Boolean,
    ) {
        if (url.isBlank()) {
            return
        }
        native.preloadLottieToMemory(url) { /* 缓存结果在 ArkTS 侧自行日志，KMP 层无需关心 */ }
    }

    override fun preloadAlphaVideo(
        url: String,
        onSuccess: (() -> Unit)?,
        onFail: (() -> Unit)?,
    ) {
        if (url.isBlank()) {
            onFail?.invoke()
            return
        }
        native.preloadAlphaVideo(url) { success ->
            if (success) onSuccess?.invoke() else onFail?.invoke()
        }
    }

    /**
     * 解析 ArkTS 侧返回的颜色字符串（期望形如 "#RRGGBB" 或 "#AARRGGBB"），失败则返回默认颜色。
     */
    private fun parseHexColor(colorString: String, defaultColor: Int?): Int {
        val fallback = defaultColor ?: 0
        val trimmed = colorString.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("#")) {
            return fallback
        }
        val hex = trimmed.substring(1)
        if (hex.length != HEX_RGB_LENGTH && hex.length != HEX_ARGB_LENGTH) {
            return fallback
        }
        return runCatching {
            val longValue = hex.toLong(HEX_RADIX)
            if (hex.length == HEX_RGB_LENGTH) {
                // 不带 alpha 时，补齐为不透明
                (ALPHA_OPAQUE shl ALPHA_SHIFT) or longValue.toInt()
            } else {
                longValue.toInt()
            }
        }.getOrDefault(fallback)
    }

    private fun parseMediaResult(urls: String): List<String> {
        if (urls.isEmpty()) {
            return emptyList()
        }
        return urls.lines().filter { it.isNotEmpty() }
    }

    companion object {
        private const val DEFAULT_PHOTO_PICK_MAX_COUNT = 1
        private const val HEX_RADIX = 16
        private const val HEX_RGB_LENGTH = 6
        private const val HEX_ARGB_LENGTH = 8
        private const val ALPHA_SHIFT = 24
        private const val ALPHA_OPAQUE = 0xFF
    }
}

/**
 * ArkTS 侧鸿蒙资源能力实现接口。
 *
 * knoi 编译时会自动生成 TS 接口定义（类似 OhosAppAlert / OhosLoginService），
 * ArkTS 侧 OhosAppResManagerCallback 实现该接口并通过 getHarmonyStartupProvider().setupAppResManager 注入。
 *
 * 所有异步方法通过 trailing lambda 回调（kniogen 会把其翻译成 (...) => void 的 Function 参数），
 * 与 OhosLoginService.qqLogin 的调用风格保持一致。
 */
@KNCallback
interface OhosAppResManager {

    /**
     * 同步读取应用 rawfile 资源内容。失败时 ArkTS 侧应返回空字符串。
     */
    fun getAssetJson(fileName: String): String

    /**
     * 同步将文本写入系统剪贴板。
     */
    fun copyToClipboard(content: String)

    /**
     * 预加载网络图片到沙盒缓存。
     * @param onResult 回调 (success) —— success 为 true 表示图片已成功加载/落盘。
     */
    fun preloadImage(url: String, onResult: (success: Boolean) -> Unit)

    /**
     * 保存图片到系统相册。
     * @param onResult 回调 (success, message) —— message 会作为用户可见提示文案使用。
     */
    fun saveImage(url: String, onResult: (success: Boolean, message: String) -> Unit)

    /**
     * 保存本地视频文件到系统相册。
     * @param localFilePath 本地视频文件路径。
     */
    fun saveVideo(localFilePath: String)

    /**
     * 拉起系统相册选图。
     * @param maxCount 期望的最多选择数量。
     * @param onResult 回调选择结果 token，Kotlin 侧需通过 consumeMediaResult 同步取真实结果。
     */
    fun selectImage(maxCount: Int, onResult: (resultToken: Int) -> Unit)

    /**
     * 拉起系统相册选择媒体（图片/视频/混合）。
     *
     * @param mediaType 1-图片，2-视频，3-图片+视频
     * @param maxCount 期望的最多选择数量。
     * @param cropEnabled 是否启用图片裁剪。
     * @param aspectX 裁剪框宽高比宽度。
     * @param aspectY 裁剪框宽高比高度。
     * @param outputX 输出图片宽度，0 表示平台默认。
     * @param outputY 输出图片高度，0 表示平台默认。
     * @param onResult 回调选择结果 token，Kotlin 侧需通过 consumeMediaResult 同步取真实结果。
     */
    fun selectMedia(
        mediaType: Int,
        maxCount: Int,
        cropEnabled: Boolean,
        aspectX: Int,
        aspectY: Int,
        outputX: Int,
        outputY: Int,
        onResult: (resultToken: Int) -> Unit,
    )

    /**
     * 展示媒体来源选择菜单，并根据用户选择拉起相册或相机。
     *
     * @param mediaType 1-图片，2-视频，3-图片+视频
     * @param maxCount 期望的最多选择数量。
     * @param cropEnabled 是否启用图片裁剪。
     * @param aspectX 裁剪框宽高比宽度。
     * @param aspectY 裁剪框宽高比高度。
     * @param outputX 输出图片宽度，0 表示平台默认。
     * @param outputY 输出图片高度，0 表示平台默认。
     * @param onResult 回调选择结果 token，Kotlin 侧需通过 consumeMediaResult 同步取真实结果。
     */
    fun selectMediaSource(
        mediaType: Int,
        maxCount: Int,
        cropEnabled: Boolean,
        aspectX: Int,
        aspectY: Int,
        outputX: Int,
        outputY: Int,
        onResult: (resultToken: Int) -> Unit,
    )

    /**
     * 拉起系统相机拍照。
     *
     * @param cropEnabled 是否启用图片裁剪。
     * @param aspectX 裁剪框宽高比宽度。
     * @param aspectY 裁剪框宽高比高度。
     * @param outputX 输出图片宽度，0 表示平台默认。
     * @param outputY 输出图片高度，0 表示平台默认。
     * @param onResult 回调拍照结果 token，Kotlin 侧需通过 consumeMediaResult 同步取真实结果。
     */
    fun takePhoto(
        cropEnabled: Boolean,
        aspectX: Int,
        aspectY: Int,
        outputX: Int,
        outputY: Int,
        onResult: (resultToken: Int) -> Unit,
    )

    /**
     * 根据媒体选择结果 token 同步获取真实结果，获取后 ArkTS 侧会移除对应缓存。
     */
    fun consumeMediaResult(resultToken: Int): String

    /**
     * 对指定图片区域取色。
     * @param paletteParam JSON 编码的 PaletteParam，格式：
     *   {"imageSize":330,"colorCount":1,"imageRect":"0,0,330,330"}
     * @param onResult 回调 16 进制颜色字符串（形如 "#RRGGBB" 或 "#AARRGGBB"）。
     */
    fun getPaletteColor(
        imageUrl: String,
        paletteParam: String,
        onResult: (color: String) -> Unit,
    )

    /**
     * 预加载 Lottie json 到沙盒缓存，后续 QnLottie 会命中缓存直接使用本地文件。
     */
    fun preloadLottieToMemory(url: String, onResult: (success: Boolean) -> Unit)

    /**
     * 预加载透明视频（alpha video）到沙盒缓存。
     */
    fun preloadAlphaVideo(url: String, onResult: (success: Boolean) -> Unit)

    /**
     * 上传图片（复用鸿蒙端上传流）。
     *
     * 进度和结果通过 getHarmonyStartupProvider().handleUploadImageProgress/handleUploadImageResult
     * 正向回调 Kotlin，不再通过 knoi 闭包传递，避免应用切后台期间 KNativePtr 被 GC 回收触发 SIGSEGV。
     *
     * @param serialNo 上传任务序列号，用于正向回调时匹配 pending callbacks
     * @param filePath 本地文件路径（file:// 或绝对路径）
     * @param scene 图片上传场景（0: COVER, 1: AVATAR, 2: IMAGE）
     * @param authJson 上传鉴权参数（JSON 字符串，由 Kotlin 侧组装）
     */
    fun uploadImage(
        serialNo: Int,
        filePath: String,
        scene: Int,
        authJson: String,
        onProgress: (current: Int, total: Int) -> Unit,
        onResult: (code: Int, message: String, url: String, fileId: String) -> Unit,
    )

    /**
     * 上传视频（复用鸿蒙端上传流）。
     *
     * 进度和结果通过 getHarmonyStartupProvider().handleUploadVideoProgress/handleUploadVideoResult
     * 正向回调 Kotlin，不再通过 knoi 闭包传递，避免应用切后台期间 KNativePtr 被 GC 回收触发 SIGSEGV。
     *
     * @param serialNo 上传任务序列号，用于正向回调时匹配 pending callbacks
     * @param filePath 本地文件路径（file:// 或绝对路径）
     * @param authJson 上传鉴权参数（JSON 字符串，由 Kotlin 侧组装）
     */
    fun uploadVideo(
        serialNo: Int,
        filePath: String,
        authJson: String,
        onProgress: (current: Int, total: Int) -> Unit,
        onResult: (code: Int, message: String, videoId: String, fileId: String) -> Unit,
    )
}
