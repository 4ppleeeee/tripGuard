package com.tencent.news.core.live.model

import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.live.vm.ILiveVMHolder
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")

typealias QnNewsRoomInfoData = @Serializable(IKmmNewsRoomInfoData.QnSerializer::class) IKmmNewsRoomInfoData

typealias ILiveRoomInfoData = IKmmNewsRoomInfoData // 优化命名

interface IKmmNewsRoomInfoData : IKmmNewsRoomInfoDtoItem, IKmmKeep, IKmmParcelable, ICmsModelDoc {

    /**
     * 直播 ViewModel Holder
     * 用于管理直播相关的各种 ViewModel
     */
    val vm: ILiveVMHolder

    // 本地绑定的一些参数，先对齐旧map设计；其实应该抽取一个ctxDto
    val extraInfo: MutableMap<String, Any>

    fun reInitDtoHolder()

    object QnSerializer : QnCompatSerializer<IKmmNewsRoomInfoData>(
        qnParser = { QnKmmModelConvert.newsLiveInfoParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IKmmNewsRoomInfoData> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IKmmNewsRoomInfoData? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IKmmNewsRoomInfoData): String = KtJson.safeEncode(QnSerializer, data)
    }
}
