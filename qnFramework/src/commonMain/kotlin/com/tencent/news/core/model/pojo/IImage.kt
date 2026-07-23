package com.tencent.news.core.model.pojo

import com.tencent.news.core.detail.model.IDetailAttribute
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnImage = @Serializable(IImage.QnSerializer::class) IImage

interface IImage : IDetailAttribute {
    var desc: String?
    var url: String? // 用于放压缩过的图，一般是641，如果是gif这里放的是gif静态图
    var linkUrl: String? // 可点击跳转的链接型图片

    val aigcMark: String // aigc提示语
    var linkTrustLevel: Int // 可点击跳转的链接安全等级
    var urlNight: String?
    var width: String?
    var height: String?
    var compressUrl: String? // 这个压缩过的，等价于url，基本不用了
    var origUrl: String? // 这里放的不是真正原图，有一定压缩，一般是1000，图文正文展示的就是这个
    var bigOrigUrl: String? // 真正原图，用于gallery的查看原图
    var thumb: String?
    var gifUrl: String?
    var isGif: String?
    var gifSize: String?
    var size: Int
    var bgImage: String?
    var type: String? // "image/jpeg","image/gif"
    var staticUrl: String?
    var jumpUrl: String? // 网络引用的图片, 引用的链接
    var style: String? // 图文底层页用，CMS下发的图片样式
    var reported: Boolean
    val intWidth: Int
    val intHeight: Int
    var isLong: Int
    var fullPic: String?

    fun getMatchImageUrl(): String

    object QnSerializer : QnInterfaceSerializer<IImage>(IImage::class)

    companion object : IQnInterfaceCreator<IImage> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): IImage? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IImage?): String = KtJson.safeEncode(QnSerializer, data)
    }
}