package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

// 内容付费
@Suppress("AnnotationOnSeparateLine")
typealias QnPaymentInfo = @Serializable(IPaymentInfo.QnSerializer::class) IPaymentInfo

interface IPaymentInfo : IKmmParcelable {
    var needPay: Int                // 文章是否需要付费(付费信息相关，注意区分), 目前全部为1
    var isFreeToRead: Int           // 文章是否免费阅读
    var textFreePercent: Float
    var videoFreeDuration: Float
    var articleSequence: Int        // 专栏特有，文章序号
    var recommendDesc: String       // 推荐语，豆腐块使用
    var recommendIcon: String       // 热点精选模块上角标-日间
    var recommendIconNight: String  // 热点精选模块上角标-夜间

    object QnSerializer : QnInterfaceSerializer<IPaymentInfo>(IPaymentInfo::class)

    companion object : IQnInterfaceCreator<IPaymentInfo> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IPaymentInfo? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IPaymentInfo): String = KtJson.safeEncode(QnSerializer, data)
    }
}