package com.tencent.news.core.audio.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

@Serializable
data class RadioInfoExt(
    var inner_audio_info: QnVoice? = null,
    var has_listened: Boolean = false,
) : IKmmKeep