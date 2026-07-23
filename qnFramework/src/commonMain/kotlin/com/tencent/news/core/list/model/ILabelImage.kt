package com.tencent.news.core.list.model

import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnLabelImage = @Serializable(ILabelImage.QnSerializer::class) ILabelImage

interface ILabelImage : IKmmParcelable {
    var url2x: String
    var urlNight2x: String

    // 客户端未使用，需解析透传给hippy侧
    var url3x: String
    var urlNight3x: String

    // 右上角标签
    var topRightIconUrl: String
    var topRightIconUrlNight: String

    object QnSerializer : QnCompatSerializer<ILabelImage>(
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<ILabelImage> {
        override fun defaultSerializer() = QnSerializer
    }
}