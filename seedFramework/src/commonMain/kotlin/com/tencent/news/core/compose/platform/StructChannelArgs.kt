package com.tencent.news.core.compose.platform

import com.tencent.news.core.channel.model.QnKmmChannelInfo
import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

@Serializable
@Suppress("ModelClassRule", "RedundantConstructorKeyword")
data class StructChannelArgs constructor(
    val channelInfo: QnKmmChannelInfo,
    val nativeDialogAnchorOffsetY: Float = 0f
) : IKmmKeep, IComposePageArgs
