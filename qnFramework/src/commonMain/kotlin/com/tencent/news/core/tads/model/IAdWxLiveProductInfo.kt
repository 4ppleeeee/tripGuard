package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnAdWxLiveProductInfo = @Serializable(IAdWxLiveProductInfo.QnSerializer::class) IAdWxLiveProductInfo

interface IAdWxLiveProductInfo : IKmmKeep {
    val imgUrls: List<String>?
    val originPrice: Int
    val sellPrice: Int
    val showBoxItems: List<IAdShowBoxItemInfo>?
    val productTitle: String
    val totalSales: Int
    val isCrossShopActivityProduct: Boolean // 是否跨店满减活动商品

    object QnSerializer : QnInterfaceSerializer<IAdWxLiveProductInfo>(IAdWxLiveProductInfo::class)

    companion object : IQnInterfaceCreator<IAdWxLiveProductInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}

interface IAdShowBoxItemInfo : IKmmKeep {
    val couponName: String
    val endTimeMs: Long
    val guaranteeWording: String
}