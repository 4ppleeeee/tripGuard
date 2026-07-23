package com.tencent.news.core.list.model

import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnKmmHotEvent = @Serializable(IKmmHotEvent.QnSerializer::class) IKmmHotEvent

interface IKmmHotEvent : IHotEventDtoItem, ICmsModelDoc, IKmmParcelable, IKmmKeep {

    // 这几个对外调用非常多，先留一个在外面，baseDto里也有
    var cmsId: String

    object QnSerializer : QnCompatSerializer<IKmmHotEvent>(
        qnParser = { QnKmmModelConvert.hotEventParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
    )

    companion object : IQnInterfaceCreator<IKmmHotEvent> {
        override fun defaultSerializer() = QnSerializer
    }

}