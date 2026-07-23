package com.tencent.news.core.service.api

import com.tencent.news.core.extension.IServiceDoc
import com.tencent.news.core.list.api.IFlexFeedsControllerHolder
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.qnchannel.api.IChannelInfo

interface IFrameworkServiceRegistry : IServiceDoc {

    // 页面用这个：
    fun createFlexFeedsController(
        rootWidget: StructPageWidget2,
        pageItem: (() -> IKmmFeedsItem?)? = null,
        adFeedsContext: IAdFeedsContext? = null,
    ): IFlexibleFeedsController

    // 频道用这个：
    fun createOrGetFlexController(
        channelWidget: ChannelWidget,
        adFeedsContext: IAdFeedsContext? = null
    ): IFlexibleFeedsController

    // holder：带ctrl缓存与重建能力
    // （channel 这个方法是给‘列表’级别用的，由于目前还没都切换完kmm，所以可能返回空）
    fun createChannelFeedsControllerHolder(
        channelInfo: LazyImpl<IChannelInfo>,
        rootWidget: LazyImpl<StructPageWidget2>,
        adFeedsContext: LazyImpl<IAdFeedsContext?>? = null,
    ): IFlexFeedsControllerHolder

}