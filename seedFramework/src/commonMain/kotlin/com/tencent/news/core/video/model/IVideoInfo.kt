@file:Suppress("AnnotationOnSeparateLine")

package com.tencent.news.core.video.model

import com.tencent.news.core.detail.model.IDetailAttribute
import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.Serializable


typealias QnVideoInfo = @Serializable(IVideoInfo.QnSerializer::class) IVideoInfo

interface IVideoInfo : IVideoInfoDtoItem, IKmmKeep, ICmsModelDoc, IKmmParcelable, IDetailAttribute {

    object QnSerializer : QnCompatSerializer<IVideoInfo>(
        qnParser = { QnKmmModelConvert.videoInfoParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IVideoInfo> {
        override fun defaultSerializer() = QnSerializer
    }

}