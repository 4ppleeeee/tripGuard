package com.tencent.news.core.user.model

import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.list.model.new
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnUserInfo = @Serializable(IUserInfo.QnSerializer::class) IUserInfo

interface IUserInfo : IUserInfoDtoItem, IKmmKeep, IKmmParcelable, ICmsModelDoc {

    object QnSerializer : QnCompatSerializer<IUserInfo>(
        qnParser = { QnKmmModelConvert.guestInfoParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IUserInfo> {
        override fun defaultSerializer() = QnSerializer

        fun safeDecode(json: String?): IUserInfo = KtJson.safeDecode(QnSerializer, json) ?: new()
        fun safeEncode(data: IUserInfo): String = KtJson.safeEncode(QnSerializer, data)

        // 解析纯kmm model类，给宿主用的
        fun safeDecodeKmm(json: String): IUserInfo? =
            KtJson.safeDecode(QnSerializer.kmmSerializer(), json)

        // 纯kmm model类转为json string，给宿主用的
        @Suppress("UNCHECKED_CAST")
        fun safeEncodeKmm(data: IUserInfo): String =
            KtJson.safeEncode(QnSerializer.kmmSerializer() as KSerializer<IUserInfo>, data)
    }

}