@file:Suppress("PrivatePropertyName", "VariableNaming")

package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.share.api.ShareSceneType
import kotlinx.serialization.Serializable


interface IKmmShareInfo {

    val share_title: String
    val share_content: String
    val share_img: String
    val share_url: String
    val shareCount: Long
    val enable_share: Boolean

    // 海报分享使用的相关字段
    val share_img_post: String?
    val share_img_post_aspect_ratio: Float
    val share_head_img: String?
    val share_head_img_aspect_ratio: Float
    val share_heat: Long
    val read_count: Long
    val share_desc: String
    val showPreviewPoster: Boolean
    var useShareInfoFirst: Boolean

    // 分享场景，用于区分不同的分享场景
    val shareScene: ShareSceneType

    // 问答数量信息
    val qnInfo: IShareQaInfo?

    /**
     * 优先获取海报图, 如果没有海报图, 就使用头图
     */
    fun getShareImageAndRatio(): Pair<String, String>? {
        var image = share_img_post
        var ratio = share_img_post_aspect_ratio
        if (image.isNullOrBlank()) {
            image = share_head_img
            ratio = share_head_img_aspect_ratio
        }
        if (image.isNullOrBlank()) return null
        return image to ratio.toString()
    }

    fun getShareType(): String {
        return when {
            !share_img_post.isNullOrBlank() -> "design"
            !share_head_img.isNullOrBlank() -> "head"
            else -> "word"
        }
    }

}

interface IShareQaInfo {
    val answerNum: Int
    val questionNum: Int
}

@Serializable
open class KmmShareInfo : IKmmKeep, IKmmShareInfo {

    override var share_title: String = ""
    override var share_content: String = ""
    override var share_img: String = ""
    override var share_url: String = ""
    override var enable_share: Boolean = true

    private var share_count: Long = 0
    override var shareCount: Long
        get() = share_count
        set(value) {
            share_count = value
        }

    // 海报分享使用的相关字段
    override var share_img_post: String? = ""
    override var share_img_post_aspect_ratio: Float = 0f
    override var share_head_img: String? = ""
    override var share_head_img_aspect_ratio: Float = 1.78f
    override var share_heat: Long = 0
    override var read_count: Long = 0
    override var share_desc: String = ""
    override var showPreviewPoster: Boolean = false
    override var useShareInfoFirst: Boolean = false
    override var shareScene: ShareSceneType = ShareSceneType.DEFAULT


    private var qa_info: ShareQaInfo? = null
    override val qnInfo: IShareQaInfo?
        get() = qa_info

}

@Serializable
internal class ShareQaInfo : IShareQaInfo, IKmmKeep {
    private val answer_num: Int = 0
    private val question_num: Int = 0

    override val answerNum: Int
        get() = answer_num
    override val questionNum: Int
        get() = question_num
}