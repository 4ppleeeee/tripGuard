package com.tencent.news.core.compose.scaffold.card

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.extension.IRegistryDoc
import com.tencent.news.core.extension.IStructWidgetRegistryDoc
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.vm.IFeedsVMItemStub

/**
 * 信息流卡片
 */
interface IFeedsItemCardStub {
    @Composable
    operator fun invoke(feedsItem: IFeedsVMItemStub, feedsItemCtx: FeedsItemCtx)
}

/**
 * 信息流广告卡片
 */
fun interface IAdFeedsItemCard {
    @Composable
    operator fun invoke(adFeedsItem: IKmmAdFeedsItem, feedsItemCtx: FeedsItemCtx)
}

/**
 * 信息流卡片注册表
 */
interface IFeedsItemCardRegistryStub : IRegistryDoc, IStructWidgetRegistryDoc {
    operator fun invoke(feedsItem: IFeedsVMItemStub): IFeedsItemCardStub?
}

interface IFeedsItemCardService : IStructWidgetRegistryDoc {
    @Composable
    fun Build(feedsItem: IFeedsVMItemStub, feedsItemCtx: FeedsItemCtx)
}

@Suppress("RedundantConstructorKeyword")
data class FeedsItemCtx constructor(
    // Cell在列表中的位置
    val indexInList: Int,
    val listSize: Int = INVALID_NUM, // 列表内容总个数（不包含footer）
    val isFirstItem: Boolean = indexInList == 0,            // 判断列表收尾cell，可做样式逻辑
    val isLastItem: Boolean = indexInList == listSize - 1,  // 判断列表收尾cell，可做样式逻辑
    val dislikeHandler: IDislikeHandler? = null,
    val contentPadding: Dp = 0.dp
)

interface IDislikeHandler {
    fun onDisLike(item: IKmmFeedsItem)
}