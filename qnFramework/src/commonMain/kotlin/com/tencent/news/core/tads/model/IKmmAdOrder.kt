package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.api.IExportModel
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.list.model.IKmmIndexItem
import com.tencent.news.core.list.model.IOriginJson
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.core.tads.api.IDownloadOriginData
import com.tencent.news.core.tads.extension.isDownloadDataValid
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder

@Suppress("AnnotationOnSeparateLine")
typealias QnAdOrder = @Serializable(IKmmAdOrder.QnSerializer::class) IKmmAdOrder

typealias IAdOrder = IKmmAdOrder // 简化下命名

/**
 * 广告数据接口：
 * 【注意】接口名不要随意改动，主端有强依赖
 */
interface IKmmAdOrder :
    IKmmIndexItem,      // 信息流业务统一的标记接口，目前无实际功能意义
    IAdVMItem,          // 所有UI层的vm放这里
    IAdDtoItem,         // 所有原始数据的dto放这里
    IAdSerialDtoItem,   // 额外扩展的，需要序列化的dto
    IExposure,          // 支持曝光排重
    IOriginJson,        // 解析时保留原始json
    IDownloadOriginData,
    IExportModel {

    object QnSerializer : QnInterfaceSerializer<IKmmAdOrder>(IKmmAdOrder::class) {
        override fun deserialize(decoder: Decoder): IKmmAdOrder {
            val result = super.deserialize(decoder)
            // 子订单数据二次加工（保证所有反序列化路径都能走到）
            FrameworkServiceBridge.impl.bindSubOrderInfo(result)
            return result
        }
    }

    override fun enableDownload(): Boolean {
        return isDownloadDataValid()
    }

    companion object : IQnInterfaceCreator<IKmmAdOrder> {
        override fun defaultSerializer() = QnSerializer

        fun safeDecode(json: String?): IKmmAdOrder? {
            // bindSubOrderInfo 已在 QnSerializer.deserialize() 中统一处理
            return KtJson.safeDecode(QnSerializer, json)
        }

        fun safeEncode(data: IKmmAdOrder?): String = KtJson.safeEncode(QnSerializer, data)
    }

}