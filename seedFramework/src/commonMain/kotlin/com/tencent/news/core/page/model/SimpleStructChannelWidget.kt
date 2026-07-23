package com.tencent.news.core.page.model

import com.tencent.news.core.list.api.IStructDataRepo
import com.tencent.news.qnchannel.api.IChannelInfo


// 建议子频道pageWidget
open class SimpleStructChannelWidget(
    channelInfo: IChannelInfo,
    dataRepo: IStructDataRepo,
    defaultRequestHost: String,
    override var data: ChannelWidgetData? = ChannelWidgetData(channelInfo),
) : StructPageChannelWidget(
    subPageWidget = {
        SimpleSubPageWidget(channelInfo, dataRepo, defaultRequestHost)
    },
    subPageVM = null
)

open class SimpleSubPageWidget(
    channelInfo: IChannelInfo,
    dataRepo: IStructDataRepo,
    defaultRequestHost: String,
) : StructPageWidget2(
    StructPageConfig(
        dataRepo = dataRepo,
        defaultChannelInfo = channelInfo,
        defaultRequestHost = defaultRequestHost,
    )
)
