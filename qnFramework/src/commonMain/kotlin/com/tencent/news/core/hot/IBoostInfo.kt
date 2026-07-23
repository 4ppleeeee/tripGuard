package com.tencent.news.core.hot

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.list.model.new
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

/**
 * 加热信息
 * 备注: 加热信息
 */
@Suppress("AnnotationOnSeparateLine")
typealias QnBoostInfo = @Serializable(IBoostInfo.QnSerializer::class) IBoostInfo

interface IBoostInfo : IKmmParcelable, IKmmKeep {
    var boostCount: Int       // 加热数
    var isShowBoost: Boolean  // 是否展示加热

    object QnSerializer : QnInterfaceSerializer<IBoostInfo>(IBoostInfo::class)

    companion object : IQnInterfaceCreator<IBoostInfo> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IBoostInfo? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IBoostInfo): String = KtJson.safeEncode(QnSerializer, data)
        fun newEmpty(): IBoostInfo = IBoostInfo.new()
    }
}