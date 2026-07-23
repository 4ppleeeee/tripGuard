package com.tencent.news.core.platform.api

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * App 选图结果（平台层通用类型），由 [IAppMediaPicker] 等平台接口返回，表示 App 侧（宿主）
 * 通过相册 / 拍照等方式选出的媒体。目前仅支持图片类型。
 *
 * 注意：此类是平台层的通用类型，与 AIGC 业务层的 ChatMultiMedia 区分开。
 * 上层业务如需在对话请求中使用，可通过 toChatMultiMedia() 方法转换。
 *
 * @param localId 宿主为每张图分配的唯一 id，上传完成前标识这一张本地图片；
 *  用于后续 [IAppMediaPicker.uploadMedia] / [IAppMediaPicker.cancelUpload] 寻址。
 *  选完图立即返回时 [url] 可能为空，业务层需要通过 localId 追踪上传结果。
 */
@Stable
@Serializable
data class AppPickedMedia(
    val type: String = TYPE_IMAGE,
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0,
    /** 宿主分配的本地 id，上传期间持续存在；上传成功后业务层仍可保留用于区分 */
    val localId: String = "",
    /**
     * 本地缩略图路径。由宿主在 pickImages 时落盘并返回（如 tmp dir 下的 JPEG），
     * 用于在上传未完成前让 UI 层直接渲染真实缩略图，而不是灰底占位。
     * 上传成功后可以保留或由宿主清理。
     */
    val localPath: String = "",
) {
    fun isValidImage(): Boolean = type == TYPE_IMAGE
    fun hasUploadedUrl(): Boolean = url.isNotBlank()
    fun hasLocalPreview(): Boolean = localPath.isNotBlank()

    companion object {
        const val TYPE_IMAGE = "image"
    }
}
