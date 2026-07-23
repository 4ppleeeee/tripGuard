package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnFrameworkLogic

interface IAppUpload {
    fun createVideoUploadTask(
        input: VideoUploadInput,
        listener: UploadTaskListener,
    ): UploadTask

    fun createImageUploadTask(
        input: ImageUploadInput,
        listener: UploadTaskListener,
    ): UploadTask

    fun prepareUploadConnection()

    fun setAppForegroundState(isForeground: Boolean)

    /**
     * 拉起系统媒体选择能力，选择图片或视频，通过 callback 返回选中文件的本地路径列表。
     *
     * @param context 当前页面上下文，用于获取平台页面实例
     * @param type 选择类型：仅图片、仅视频或图片+视频
     * @param source 媒体来源：相册、拍照或相册/拍照二选一
     * @param cropConfig 图片裁剪配置，传 null 表示不裁剪
     * @param callback 选择结果回调，返回选中文件的本地绝对路径列表；取消或失败时返回空列表
     */
    fun pickMedia(
        context: IKmmContext?,
        type: PickMediaType = PickMediaType.IMAGE_AND_VIDEO,
        source: PickMediaSource = PickMediaSource.ALBUM,
        cropConfig: PickMediaCropConfig? = null,
        callback: (paths: List<String>) -> Unit,
    ) {
        callback(emptyList())
    }
}

interface UploadTask {
    fun upload()

    fun cancel(): Boolean

    fun resume(): Boolean
}

interface UploadTaskListener {
    fun onProgress(current: Long, total: Long) {}

    fun onSuccess(result: UploadResult) {}

    fun onFailure(code: Int, message: String?) {}
}

data class VideoUploadInput(
    val filePath: String,
    val priority: UploadPriority = UploadPriority.LOW,
    val timestamp: Long = 0,
    val videoType: Int = 0,
    val encodePriority: Int = 0,
    val keyFrame: List<Int> = emptyList(),
    val source: String = "common",
    val width: Int = 0,
    val height: Int = 0,
    val duration: Int = 0,
    val bitrate: Int = 0,
)

data class ImageUploadInput(
    val filePath: String,
    val scene: ImageUploadScene = ImageUploadScene.COVER,
    val priority: UploadPriority = UploadPriority.LOW,
    val timestamp: Long = 0,
)

data class UploadResult(
    val filePath: String,
    val url: String = "",
    val videoId: String = "",
    val fileId: String = "",
)

enum class UploadPriority {
    LOW,
    MEDIUM,
    HIGH,
}

/** 系统媒体来源 */
enum class PickMediaSource {
    /** 仅从相册选择 */
    ALBUM,

    /** 仅拍照 */
    CAMERA,

    /** 由平台展示相册/拍照选择入口 */
    ALBUM_OR_CAMERA,
}

/** 系统相册选择类型 */
enum class PickMediaType {
    /** 仅图片 */
    IMAGE_ONLY,

    /** 仅视频 */
    VIDEO_ONLY,

    /** 图片和视频 */
    IMAGE_AND_VIDEO,
}

/** 图片裁剪配置 */
data class PickMediaCropConfig(
    /** 是否启用裁剪 */
    val enabled: Boolean = true,
    /** 裁剪框宽高比宽度 */
    val aspectX: Int = 1,
    /** 裁剪框宽高比高度 */
    val aspectY: Int = 1,
    /** 输出图片宽度，0 表示使用平台默认值 */
    val outputX: Int = 0,
    /** 输出图片高度，0 表示使用平台默认值 */
    val outputY: Int = 0,
)

enum class ImageUploadScene {
    COVER,
    AVATAR,
    IMAGE,
}

@OptIn(KmmInternalApi::class)
fun appUpload(): IAppUpload = QnFrameworkLogic.appUpload ?: defaultAppUpload

private val defaultAppUpload by lazy { DefaultAppUpload() }

private class DefaultAppUpload : IAppUpload {
    override fun createVideoUploadTask(
        input: VideoUploadInput,
        listener: UploadTaskListener,
    ): UploadTask = UnsupportedUploadTask(listener)

    override fun createImageUploadTask(
        input: ImageUploadInput,
        listener: UploadTaskListener,
    ): UploadTask = UnsupportedUploadTask(listener)

    override fun prepareUploadConnection() = Unit

    override fun setAppForegroundState(isForeground: Boolean) = Unit
}

private class UnsupportedUploadTask(
    private val listener: UploadTaskListener,
) : UploadTask {
    override fun upload() {
        listener.onFailure(CODE_UNSUPPORTED, "Upload SDK is not initialized")
    }

    override fun cancel(): Boolean = false

    override fun resume(): Boolean {
        upload()
        return false
    }
}

private const val CODE_UNSUPPORTED = -1
