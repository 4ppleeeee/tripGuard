package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.list.model.new
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.IKmmDeepClone
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnTagInfo = @Serializable(IKmmTagInfo.QnSerializer::class) IKmmTagInfo

typealias ITagInfo = IKmmTagInfo // 命名优化一下

interface IKmmTagInfo : ITagInfoDtoItem,
    IExposure, IKmmDeepClone, IKmmKeep, IKmmParcelable,
    ICmsModelDoc {

    object QnSerializer : QnCompatSerializer<IKmmTagInfo>(
        qnParser = { QnKmmModelConvert.tagInfoParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IKmmTagInfo> {

        const val TAG_M_724 = "TAG_M_724" // tagType标记是tabM里的724

        override fun defaultSerializer() = QnSerializer

        fun safeDecode(json: String): IKmmTagInfo? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IKmmTagInfo): String = KtJson.safeEncode(QnSerializer, data)

        // 解析纯kmm model类，给宿主用的
        fun safeDecodeKmm(json: String): IKmmTagInfo? =
            KtJson.safeDecode(QnSerializer.kmmSerializer(), json)

        // 纯kmm model类转为json string，给宿主用的
        @Suppress("UNCHECKED_CAST")
        fun safeEncodeKmm(data: IKmmTagInfo): String =
            KtJson.safeEncode(QnSerializer.kmmSerializer() as KSerializer<IKmmTagInfo>, data)

        fun createById(tagId: String) =
            createTagInfo(tagId = tagId, tagName = "")

        fun createByName(tagId: String, tagName: String) =
            createTagInfo(tagId = tagId, tagName = tagName)

        fun createTagInfo(tagId: String, tagName: String, tagScene: String = ""): IKmmTagInfo {
            return IKmmTagInfo.new {
                baseDto.tagId = tagId
                baseDto.tagName = tagName
                baseDto.tagScene = tagScene
            }
        }
    }

}
