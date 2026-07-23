package com.tencent.news.core.detail

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.tads.constants.INVALID_NUM
import kotlinx.serialization.Serializable

/**
 * @Author: auto
 * @Date: 2025/1/XX
 */

interface IUnderlineParams : IKmmParcelable {
    // 文章id，必传
    var cmsid: String

    // 划线的开始位置，从0开始计算，必传
    var offset: Int

    // 划线长度，必传
    var length: Int

    // 划线内容，必传
    var content: String

    // 划线来源，值为article或者comment
    var type: Int
}

/**
 * 创建划线请求参数
 */
@Serializable
data class UnderlineParams(
    // 文章id，必传
    override var cmsid: String = "",
    // 划线的开始位置，从0开始计算，必传
    override var offset: Int = INVALID_NUM,
    // 划线长度，必传
    override var length: Int = INVALID_NUM,
    // 划线内容，必传
    override var content: String = "",
    // 划线来源，值为article或者comment
    override var type: Int = INVALID_NUM,
) : IKmmKeep, IUnderlineParams {
    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(cmsid)
        dest.writeInt(offset)
        dest.writeInt(length)
        dest.writeString(content)
        dest.writeInt(type)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        cmsid = from.readString()
        offset = from.readInt()
        length = from.readInt()
        content = from.readString()
        type = from.readInt()
    }

    fun safeEnCode() = KtJson.safeEncode(this)

}


/**
 * 创建划线响应数据
 */
@Serializable
data class UnderlineResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: UnderlineCreateData? = null,
) : IKmmKeep

/**
 * 创建划线响应数据中的 data 字段
 */
@Serializable
data class UnderlineCreateData(
    val id: String = "",
) : IKmmKeep

/**
 * 查询划线响应数据
 */
@Serializable
data class GetUnderlineResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: UnderlineListData? = null,
) : IKmmKeep

/**
 * 查询划线响应数据中的 data 字段
 */
@Serializable
data class UnderlineListData(
    val underlines: List<UnderlineItem>? = null,
) : IKmmKeep

/**
 * 删除划线响应数据
 */
@Serializable
data class DeleteUnderlineResponse(
    val code: Int = 0,
    val msg: String = "",
) : IKmmKeep

/**
 * 划线项接口 —— 对外暴露的只读属性，用于 IContextDto 等基础接口层引用，
 * 避免将具体业务 data class 泄漏到接口层。
 */
interface IUnderlineItem : IKmmKeep {
    val id: String            // 划线ID
    val offset: Int           // 划线的开始位置
    val length: Int           // 划线长度
    val content: String       // 划线内容
    val type: Int             // 1=私域划线，2=公域评论划线，3=私域评论划线
    val count: Int            // 划线数量
    val articleId: String?    // 文章id
    val has_select: Boolean   // 当前登录用户是否划过该线
    var showCommentIcon: Boolean?  // 是否展示评论图标，true展示，false不展示
}

/**
 * 划线项
 */
@Serializable
data class UnderlineItem(
    override val id: String = "",
    override val offset: Int = 0,
    override val length: Int = 0,
    override val content: String = "",
    override val type: Int = 0, // 1=私域划线，2=公域评论划线，3=私域评论划线
    override var count: Int = 0, // 划线数量
    override var articleId: String? = null, // 文章id
    override var has_select: Boolean = false, // 当前登录用户是否划过该线
    private var show_comment_icon: Boolean? = false, // 是否展示评论图标（JSON字段名）
) : IUnderlineItem {
    override var showCommentIcon: Boolean?
        get() = show_comment_icon
        set(value) { show_comment_icon = value }
}

/**
 * 恢复划线结果
 */
@Serializable
data class RestoreUnderlineResult(
    val success: Boolean = false,
    val successCount: Int = 0,
    val failCount: Int = 0,
    val contentMismatchCount: Int = 0,
    val error: String = ""
) : IKmmKeep
/**
 * 根据划线ID查询划线响应数据
 */
@Serializable
data class GetUnderlineByIdResponse(
    val code: Int = 0,
    val msg: String = "",
    val data: UnderlineByIdData? = null,
) : IKmmKeep

/**
 * 根据划线ID查询划线响应数据中的 data 字段
 */
@Serializable
data class UnderlineByIdData(
    val underline: UnderlineItem? = null,
) : IKmmKeep
