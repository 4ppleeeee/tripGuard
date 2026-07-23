package com.tencent.news.qnchannel.api

import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IKmmShareInfo
import com.tencent.news.core.tag.model.QnTagInfo
import kotlinx.coroutines.flow.MutableStateFlow


class ChannelEnv(
    private val channelInfo: () -> IChannelInfo, // 懒加载防止ios循环依赖泄露
) : IChannelEnv, IKmmKeep {

    override var newsChannel: String = ""
        get() = field.takeIfNotBlank() ?: channelInfo().channelKey

    override var channelType: String = ""
        set(value) {
            field = value.getNonNull() // 做个保护，宿主java或oc可能给null
        }

    override var pageItem: IKmmFeedsItem? = null
    override var shareData: IKmmShareInfo? = null
    override var pageArgs: IComposePageArgs? = null
    override var rebuildStatusKey: String? = null
    override var shareDataState: MutableStateFlow<IKmmShareInfo?> = MutableStateFlow(null)
    override var tagInfoState: MutableStateFlow<QnTagInfo?> = MutableStateFlow(null)

}