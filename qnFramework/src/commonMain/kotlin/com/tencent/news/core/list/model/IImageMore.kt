package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnImageMore = @Serializable(IImageMore.QnSerializer::class) IImageMore

interface IImageMore : IKmmParcelable, IKmmKeep {

    var img330330: String
    var img870870: String
    var f329155: String
    var f155155: String
    var f642364: String

    object QnSerializer : QnInterfaceSerializer<IImageMore>(IImageMore::class)

    companion object : IQnInterfaceCreator<IImageMore> {
        override fun defaultSerializer() = QnSerializer
    }

}

