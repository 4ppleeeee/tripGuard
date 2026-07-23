package com.tencent.news.core.compose.scaffold.card

import androidx.compose.runtime.Composable
import com.tencent.news.core.extension.IRegistryDoc
import com.tencent.news.core.extension.IStructWidgetRegistryDoc
import com.tencent.news.core.list.model.IKmmFeedsItem
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

interface IDislikeHandler {
    fun onDisLike(item: IKmmFeedsItem)
}