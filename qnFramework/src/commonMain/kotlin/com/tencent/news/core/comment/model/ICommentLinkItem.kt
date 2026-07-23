package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.IEnumDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable

object ICommentLinkType : IEnumDoc {
    const val UNKNOWN = "0"
    const val COLUMN = "1"        // 专栏
    const val CPMEMBER = "2"      // 会员专区
    const val COLLECTION = "3"    // 合集
    const val ACTIVITY = "4"      // 活动
    const val EXCLUSIVE = "5"     // 专属链接
}

@Suppress("AnnotationOnSeparateLine")
typealias QnCommentLinkItem = @Serializable(ICommentLinkItem.QnSerializer::class) ICommentLinkItem

interface ICommentLinkItem : IKmmKeep, IKmmParcelable {
    var title: String   // 标题
    var url: String     // 链接
    var type: String    // 类型: 1-专栏，2-会员专区，3-合集，4-活动，5-专属链接
    var suid: String    // 用户ID
    var mediaId: String // 媒体ID
    var tagId: String   // 专栏ID or 合集ID

    fun jumpUrlOrScheme(): String   // 跳转链接

    object QnSerializer : QnInterfaceSerializer<ICommentLinkItem>(ICommentLinkItem::class)

    companion object : IQnInterfaceCreator<ICommentLinkItem> {
        override fun defaultSerializer() = QnSerializer
    }
}