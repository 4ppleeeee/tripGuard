package com.tencent.news.core.audio.model

import com.tencent.news.core.audio.api.IAudioExpandDtoItem
import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IRelatedAiStation
import com.tencent.news.core.parcel.IKmmParcelable


interface IAudioDto : IItemDtoDoc, IKmmKeep, IKmmParcelable, IAudioExpandDtoItem {

    var radioExt: RadioInfoExt?
    var historyMediaType: Int?

    var hasListened: Boolean
    var articleType: String

    var ttsTitle: String    // tts播放存储标题
    var ttsPlayStartIndex: Int    // tts句子起播id
    var ttsPlayAudioIndex: Int   // tts播放音频索引，数组下标，从0开始
    var urlStartTime: Float    // Url起播时间点
    var sessionInfo: String    // Url起播时间点

    var audioCoverUrl: String?       // 音频封面


    var innerRadioInfo: IVoice?     // 内嵌音频
    var summaryRadioInfo: IVoice?   // 摘要音频
    var fullRadioInfo: IVoice?      // 全文音频

    var summaryRadioList: List<IVoice>?        // 摘要音频音色列表
    var fullRadioList: List<IVoice>?           // 全文音频音色列表
    var aiPodCastRadioList: List<IVoice>?      // AI音频音色列表
    var payFullTextRadioList: List<IVoice>?    // 付费完整音频（购买后）
    var payPartTextRadioList: List<IVoice>?    // 付费部分音频（购买前）
    var aiStationRadioList: List<IVoice>?      // AI电台多音色列表
    val relatedAiStation: IRelatedAiStation?   // 关联AI电台信息


    var realUseRadioInfo: IVoice    // 播放使用的音频
    var bindTtsTimbre: String?     // 绑定的TTS音色

    var isAuthorizeOrFreeArticle: Boolean  // 是否已授权，用于检查当前播放的付费状态，方便刷新

    val audioAlgInfo: String

    var listenCount: Long    // 兜底的收听数（音频优先使用音频维度的播放次数，没有时使用该兜底字段）

    var disableListenCountDisplay: Boolean  // 是否隐藏外显收听数
}
