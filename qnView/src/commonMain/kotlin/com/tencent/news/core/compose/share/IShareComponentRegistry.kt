package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import com.tencent.news.core.extension.IStructWidgetRegistryDoc
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.IKmmShareData


interface IShareComponentRegistry : IStructWidgetRegistryDoc {

    // 海报分享（目前主要早报用）
    @Composable
    fun PosterShareCard(
        feedsItem: IKmmFeedsItem,
        feedsList: List<IKmmFeedsItem>,
        onImageLoaded: (() -> Unit)?
    )

    // 分享弹窗样式
    @Composable
    fun PostShareContent(
        postShareChannel: IShareChannel,
        shareData: IKmmShareData,
        feedsItem: IKmmFeedsItem?
    )

}