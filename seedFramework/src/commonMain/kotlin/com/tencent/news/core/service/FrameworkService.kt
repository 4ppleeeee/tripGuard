package com.tencent.news.core.service

import com.tencent.news.core.list.api.IFlexFeedsControllerHolder
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.controller.FlexFeedsControllerHolder
import com.tencent.news.core.list.controller.FlexibleFeedsController
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.extension.ChannelWidgetEx.createOrGetFlexController
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.service.api.IFrameworkServiceRegistry
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.qnchannel.api.IChannelInfo

object FrameworkService : IFrameworkServiceRegistry {

    override fun createFlexFeedsController(
        rootWidget: StructPageWidget2,
        pageItem: (() -> IKmmFeedsItem?)?,
        adFeedsContext: IAdFeedsContext?
    ): IFlexibleFeedsController = FlexibleFeedsController(rootWidget, pageItem, adFeedsContext)

    override fun createOrGetFlexController(
        channelWidget: ChannelWidget,
        adFeedsContext: IAdFeedsContext?
    ) = channelWidget.createOrGetFlexController(adFeedsContext)

    override fun createChannelFeedsControllerHolder(
        channelInfo: LazyImpl<IChannelInfo>,
        rootWidget: LazyImpl<StructPageWidget2>,
        adFeedsContext: LazyImpl<IAdFeedsContext?>?
    ): IFlexFeedsControllerHolder {
        return FlexFeedsControllerHolder(
            needCheckStatus = getShiplySwitch("enable_channel_recreate_check_status", true),
            channelInfo,
            rootWidget = rootWidget,
            pageItem = null,
            adFeedsContext
        )
    }

}