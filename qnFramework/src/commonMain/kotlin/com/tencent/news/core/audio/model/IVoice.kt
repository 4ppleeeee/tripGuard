package com.tencent.news.core.audio.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.list.model.new
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.serializer.SafeInt
import kotlinx.serialization.Serializable

/**
 * Created by calvinche
 * on 2019-10-31
 * Email: calvinche@tencent.com
 */

@Suppress("AnnotationOnSeparateLine")
typealias QnVoice = @Serializable(IVoice.QnSerializer::class) IVoice

typealias AudioVoicePlayType = Int

interface IVoice : IKmmKeep {
    var voiceId: String
    var voiceUrl: String
    var listenNum: SafeInt
    var voiceTimeLen: SafeInt
    var estimateTime: SafeInt                  // 预估时长
    var voiceSize: SafeInt                     // 音频大小，单位字节
    var voiceType: Int                         // 0 - 人声, 1 - tts 禁用非tts链接 - 客户端自用
    var audioPlayCount: SafeInt
    var speakerId: String                      // 旧字段，兼容保留
    var bizId: String                          // 音色业务id，新协议使用
    var gender: String
    var subtitles: String
    var subtitles_md5: String?
    var interactId: String // 交互id
    var voicePlayUrlType: VoicePlayUrlType
    var streamUrl: String
    var title: String
    var oriAudioPath: String
    var innerVoiceOriUrl: String

    val aigcMark: String // 是否有ai提示，提示内容

    object QnSerializer : QnInterfaceSerializer<IVoice>(IVoice::class)

    companion object : IQnInterfaceCreator<IVoice> {
        override fun defaultSerializer() = QnSerializer
        fun create(): IVoice = IVoice.new()
        fun safeEncode(data: IVoice): String = KtJson.safeEncode(QnSerializer, data)
        fun safeDecode(json: String): IVoice? = KtJson.safeDecode(QnSerializer, json)
    }
}


object VoiceType {
    const val VOICE_TYPE_TTS: AudioVoicePlayType = 0                       // 微信TTS
    const val VOICE_TYPE_URL_SUMMERY: AudioVoicePlayType = 101             // 摘要
    const val VOICE_TYPE_URL_FULL: AudioVoicePlayType = 102                // 全文
    const val VOICE_TYPE_URL_INNER: AudioVoicePlayType = 103               // 内嵌
    const val VOICE_TYPE_URL_TIMBRE: AudioVoicePlayType = 104              // 多音色
    const val VOICE_TYPE_URL_AI_AUDIO: AudioVoicePlayType = 105            // AI音频
}

enum class VoicePlayUrlType {
    URL,
    STREAM
}

/**
 * 音频item播放类型
 */
enum class AudioItemPlayType {
    NONE,                           // 未知：音频关闭或是未播放状态
    TTS,                            // 微信TTS
    URL,                            // 摘要、正文、内嵌、自定义stream
    TIMBRE,                         // CAI团队离线音色
}


