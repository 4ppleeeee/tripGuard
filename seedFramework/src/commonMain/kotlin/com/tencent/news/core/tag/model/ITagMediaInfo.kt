package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnTagMediaInfo = @Serializable(ITagMediaInfo.QnSerializer::class) ITagMediaInfo

interface ITagMediaInfo : IKmmKeep, IKmmParcelable {
    var mediaId: String    // 媒体ID
    var mediaName: String  // 媒体名称

    object QnSerializer : QnInterfaceSerializer<ITagMediaInfo>(ITagMediaInfo::class)

    companion object : IQnInterfaceCreator<ITagMediaInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}
