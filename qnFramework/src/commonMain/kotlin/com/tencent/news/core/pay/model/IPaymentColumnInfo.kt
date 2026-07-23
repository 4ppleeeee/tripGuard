package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnPaymentColumnInfo = @Serializable(IPaymentColumnInfo.QnSerializer::class) IPaymentColumnInfo

interface IPaymentColumnInfo : IKmmParcelable {
    var isColumnPaid: Boolean           // 专栏的付费状态
    var isColumnArticlePaid: Boolean    // 专栏下的单篇文章的付费状态
    var readCountAll: Int               // 专栏中所有文章的阅读总数
    val paymentInfo: IPaymentInfo?      // 专栏的付费信息
    var showGift: Boolean               // 是否显示礼物
    var giftDesc: String                // 礼物描述
    var userColumnRelateList: IUserVipRelateList?   // 用户专栏关联列表
    val isNeedPay: Boolean

    fun articleOrColumnIsPaid(): Boolean

    object QnSerializer : QnInterfaceSerializer<IPaymentColumnInfo>(IPaymentColumnInfo::class)

    companion object : IQnInterfaceCreator<IPaymentColumnInfo> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IPaymentColumnInfo? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IPaymentColumnInfo): String = KtJson.safeEncode(QnSerializer, data)
    }
}