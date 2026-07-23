package com.tencent.news.core.tads.model

import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnAdExtendMaterial = @Serializable(IAdExtendMaterial.QnSerializer::class) IAdExtendMaterial

// 以下字段在不同广告位中约定的含义不同
interface IAdExtendMaterial {
    var img1: String
    var img2: String
    var img3: String

    object QnSerializer : QnInterfaceSerializer<IAdExtendMaterial>(IAdExtendMaterial::class)

    companion object : IQnInterfaceCreator<IAdExtendMaterial> {
        override fun defaultSerializer() = QnSerializer
    }

}