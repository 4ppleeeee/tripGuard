package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnComment = @Serializable(IComment.QnSerializer::class) IComment

interface IComment : ICommentDtoItem, IKmmKeep, IKmmParcelable, ICmsModelDoc {

    object QnSerializer : QnCompatSerializer<IComment>(
        qnParser = { QnKmmModelConvert.commentParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IComment> {
        override fun defaultSerializer() = QnSerializer

        fun safeDecode(json: String): IComment? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IComment): String = KtJson.safeEncode(QnSerializer, data)

        // 解析纯kmm model类，给宿主用的
        fun safeDecodeKmm(json: String): IComment? =
            KtJson.safeDecode(QnSerializer.kmmSerializer(), json)

        // 纯kmm model类转为json string，给宿主用的
        fun safeEncodeKmm(data: IComment): String =
            KtJson.safeEncode(QnSerializer.kmmSerializer() as KSerializer<IComment>, data)
    }

}