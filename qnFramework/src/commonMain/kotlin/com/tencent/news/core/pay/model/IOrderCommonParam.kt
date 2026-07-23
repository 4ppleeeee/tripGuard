package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnOrderCommonParam = @Serializable(IOrderCommonParam.QnSerializer::class) IOrderCommonParam

interface IOrderCommonParam : IKmmParcelable {
    var sessionId: String
    var sessionType: String
    var pf: String
    var pfKey: String
    var offerId: String

    object QnSerializer : QnInterfaceSerializer<IOrderCommonParam>(IOrderCommonParam::class)

    companion object : IQnInterfaceCreator<IOrderCommonParam> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IOrderCommonParam? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IOrderCommonParam): String = KtJson.safeEncode(QnSerializer, data)
    }
}