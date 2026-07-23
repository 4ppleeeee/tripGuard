package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnPaymentMemberInfo = @Serializable(IPaymentMemberInfo.QnSerializer::class) IPaymentMemberInfo

interface IPaymentMemberInfo : IKmmParcelable {
    var articleTotalCount: Int
    var discountPrice: String
    var price: String
    var isSinglePay: Boolean // 会员单篇是否购买
    var singleCloseTimeText: String //  会员单篇到期时间， "2025年5月31日"

    object QnSerializer : QnInterfaceSerializer<IPaymentMemberInfo>(IPaymentMemberInfo::class)

    companion object : IQnInterfaceCreator<IPaymentMemberInfo> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IPaymentMemberInfo? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IPaymentMemberInfo): String = KtJson.safeEncode(QnSerializer, data)
    }
}