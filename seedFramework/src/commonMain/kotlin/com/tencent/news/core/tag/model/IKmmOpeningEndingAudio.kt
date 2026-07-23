package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.serializer.SafeInt

interface IKmmOpeningEndingAudio : IKmmKeep {
    var ending: String?
    var endingDuration: SafeInt?
    var opening: String?
    var openingDuration: SafeInt?
    var speakId: String?
    val switching: String?
    val switchingDuration: SafeInt?
}