package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.list.model.QnKmmFeedsItem
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnUserVipRelateList = @Serializable(IUserVipRelateList.QnSerializer::class) IUserVipRelateList

interface IUserVipRelateList : IKmmParcelable {
    var prevItem: QnKmmFeedsItem?   // 上一篇文章
    var nextItem: QnKmmFeedsItem?   // 下一篇文章

    fun isEmpty(): Boolean

    object QnSerializer : QnInterfaceSerializer<IUserVipRelateList>(IUserVipRelateList::class)

    companion object : IQnInterfaceCreator<IUserVipRelateList> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IUserVipRelateList? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IUserVipRelateList): String = KtJson.safeEncode(QnSerializer, data)
    }
}