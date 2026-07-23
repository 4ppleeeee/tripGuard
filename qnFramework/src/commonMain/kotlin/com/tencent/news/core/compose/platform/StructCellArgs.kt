package com.tencent.news.core.compose.platform

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.QnListItem
import kotlinx.serialization.Serializable

@Serializable
@Suppress("ModelClassRule", "RedundantConstructorKeyword")
data class StructCellArgs constructor(
    val feedsItem: QnListItem,          // 页面Item
    val channelId: String,              // 频道id
    val index: Int,
    val aspectRatio: Float,
    val maxWidth: Float,
    val heightInDp: Float = 0f,
    val contentPadding: Int = 0,        // 内容区域的 padding 值（单位：dp），0 表示使用卡片默认值
    val extraPageData: Map<String, String> = emptyMap()
) : IKmmKeep, IComposePageArgs, IComposePageSize {

    override val viewAspectRatio: Float = aspectRatio
    override val initHeightInDp: Int = heightInDp.toInt()

    /**
     * 保留 ObjC/Swift 侧历史初始化器签名，避免已有 ITEM_CELL 宿主因 K/N 导出签名变化编译失败。
     */
    constructor(
        feedsItem: QnListItem,
        channelId: String,
        index: Int,
        aspectRatio: Float,
        maxWidth: Float,
        heightInDp: Float,
        contentPadding: Int
    ) : this(
        feedsItem = feedsItem,
        channelId = channelId,
        index = index,
        aspectRatio = aspectRatio,
        maxWidth = maxWidth,
        heightInDp = heightInDp,
        contentPadding = contentPadding,
        extraPageData = emptyMap()
    )

    companion object {

        fun simpleCreate(feedsItem: IKmmFeedsItem) = StructCellArgs(
            feedsItem = feedsItem,
            channelId = feedsItem.ctxDto.newsChannel,
            index = 0,
            aspectRatio = 0f,
            heightInDp = 0f,
            maxWidth = 0f
        )
    }

}
