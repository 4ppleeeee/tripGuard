package com.tencent.news.core.service

import com.tencent.news.core.list.controller.IFeedsDataProcessor

/**
 * qnFramework 信息流扩展能力桥接器。
 */
object FrameworkFeedsBridge {

    var impl: IFrameworkFeedsBridge = EmptyFrameworkFeedsBridge
        private set

    fun register(bridge: IFrameworkFeedsBridge) {
        impl = bridge
    }

}

interface IFrameworkFeedsBridge {

    fun createInvalidItemFilterProcessor(): IFeedsDataProcessor?

}

private object EmptyFrameworkFeedsBridge : IFrameworkFeedsBridge {
    override fun createInvalidItemFilterProcessor(): IFeedsDataProcessor? = null
}
