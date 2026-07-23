package com.tencent.news.core.compose.platform

import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.compose.platform.args.IStructPageArgs
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getQueryParam
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.QnListItem
import kotlinx.serialization.Serializable

@Serializable
@Suppress("ModelClassRule", "RedundantConstructorKeyword")
data class StructPageArgs constructor(
    override val feedsItem: QnListItem,             // 页面Item
    override val channelId: String,                 // 频道id
    override val schemeFrom: String? = null,        // 拉起渠道
    override val originScheme: String? = null,      // 拉起scheme

    @Deprecated("废弃，专题pageArgs独立一个了")
    val isComponent: Boolean = false                // 在宿主以一个组件而非页面的形式出现，在专题中会隐藏bottombar
) : IKmmKeep, IStructPageArgs, IComposePageSize, IComposeDemoPage {

    init {
        compatItemNewsChannel()
    }

    override var runInDemo: Boolean = false // 【debug】标识在demo运行，可以用于判断一些辅助逻辑

    override var viewAspectRatio: Float = 0f
    override var initHeightInDp: Int = 0

    val isLandingPage: Boolean get() = SchemeFrom.isFromLanding(schemeFrom)

    override val pageItem: IKmmFeedsItem
        get() = feedsItem

    private fun compatItemNewsChannel() {
        if (feedsItem.ctxDto.newsChannel.isEmpty() && channelId.isNotEmpty()) {
            feedsItem.ctxDto.newsChannel = channelId
        }
    }

    override fun toString(): String = "StructPageArgs[${identifier}]${feedsItem}"

    companion object {

        fun simpleCreate(item: IKmmFeedsItem) = StructPageArgs(
            feedsItem = item,
            channelId = item.ctxDto.newsChannel,
            schemeFrom = item.ctxDto.jumpStartScheme.getQueryParam("from"),
            originScheme = item.ctxDto.jumpStartScheme,
        )
    }

}