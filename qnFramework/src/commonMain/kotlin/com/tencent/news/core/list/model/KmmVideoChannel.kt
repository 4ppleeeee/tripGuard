package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.serializer.SafeInt
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import com.tencent.news.core.video.model.IVideoInfo
import com.tencent.news.core.video.model.QnVideoInfo
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnKmmVideoChannel = @Serializable(QnKmmVideoChannelSerializer::class) IKmmVideoChannel

object QnKmmVideoChannelSerializer : QnCompatSerializer<IKmmVideoChannel>(
    qnParser = { QnKmmModelConvert.videoChannelParser },
    kmmSerializer = { GlobalModelSerializerFactory.getDefault() }
)

interface IKmmVideoChannel : IKmmKeep {
    var videoInfo: IVideoInfo?          // 【重要】视频播放的核心数据

    val enableLike: Boolean             // 是否支持点赞
    val enableReplay: Boolean           // 视频播放完了，下次触发自动播是否重复播放
}

@Serializable
abstract class KmmBaseVideoChannel : BaseKmmModel() {

    // 这几个id暂时没用到
    internal var egid: String = ""
    internal var eid: String = ""

    var openSupport: SafeInt = 0
    internal var video: QnVideoInfo? = null
    var isReplay: Int = 0

}

@Serializable
open class KmmVideoChannel : KmmBaseVideoChannel(), IKmmVideoChannel {

    override var videoInfo: IVideoInfo?
        get() = video
        set(value) {
            video = value
        }

    override var enableLike: Boolean
        get() = openSupport == 1
        set(value) {
            openSupport = if (value) 1 else 0
        }

    override val enableReplay: Boolean get() = (isReplay == 1)

    companion object {
        fun fromJson(json: String?): KmmVideoChannel =
            KtJson.safeDecode<KmmVideoChannel>(json) ?: KmmVideoChannel()

        fun toJson(item: KmmVideoChannel?): String = KtJson.safeEncode(item)
    }

}