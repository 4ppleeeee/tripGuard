package com.tencent.news.core.list.page

import com.tencent.news.core.extension.concatUriPath
import com.tencent.news.core.list.api.IStructDataRepo
import com.tencent.news.core.list.api.StructDataEnv
import com.tencent.news.core.list.api.StructPageNetworkBuilder
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.StructPageConfig
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.page.model.pickInitRequest
import com.tencent.news.core.platform.api.AppHost
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.qnchannel.api.IChannelInfo

// 完全根据 ChannelWidget 的 action 创建网络请求：
internal class StructChannelPageWidget(channelWidget: ChannelWidget) : StructPageWidget2(
    StructPageConfig(
        dataRepo = StructChannelDataRepo(channelWidget),
        defaultChannelInfo = channelWidget.data?.channel_info ?: IChannelInfo.createDefault("all")
    )
) {
    init {
        buildPageWithContent(channelWidget, channelWidget.content)
    }
}

private class StructChannelDataRepo(private val channelWidget: ChannelWidget) : IStructDataRepo {

    override fun createResetRequest(
        defaultRequest: DataRequest,
        dataEnv: StructDataEnv
    ): NetworkBuilder<*> {
        val dataRequest = channelWidget.pickInitRequest()
        return StructPageNetworkBuilder(
            url = AppHost.READ_HOST.concatUriPath(dataRequest?.service),
            params = dataRequest?.reqdata,
            parser = null
        )
    }

}