package com.tencent.news.core.tads.service

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.tads.api.IAdAppChecker
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.core.tads.api.IAdFeedsController
import com.tencent.news.core.tads.click.AdReportConfig
import com.tencent.news.core.tads.constants.AdGdtClickActType
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.qnchannel.api.IChannelInfo

/**
 * qnFramework 广告能力桥接器。
 *
 * 广告不是底座必需能力，默认提供无业务语义的空实现；业务 core 需要广告时再注册真实实现。
 */
object FrameworkAdServiceBridge {

    var impl: IFrameworkAdServiceBridge = EmptyFrameworkAdServiceBridge
        private set

    fun register(bridge: IFrameworkAdServiceBridge) {
        impl = bridge
    }

}

interface IFrameworkAdServiceBridge {

    // 广告子订单处理
    fun bindSubOrderInfo(parentOrder: IKmmAdOrder)

    // 广告点击上报
    fun reportAdClick(
        adOrder: IKmmAdOrder?,
        actType: AdGdtClickActType,
        config: AdReportConfig = AdReportConfig()
    )

    // 检查app安装态
    fun getAppChecker(): IAdAppChecker

    // 判断广告位是否关闭
    fun isCloseAd(loid: Int, adChannel: String): Boolean

    // 创建信息流广告controller
    fun createAdFeedsController(
        majorLoid: Int,
        adChannel: String,
        adFeedsContext: IAdFeedsContext? = null,
    ): IAdFeedsController?

    // 广告频道替换逻辑
    fun exchangeAdRequestChannel(
        majorLoid: Int,
        pageItem: IKmmFeedsItem?,
        channelInfo: IChannelInfo,
    ): String

}

private object EmptyFrameworkAdServiceBridge : IFrameworkAdServiceBridge {

    override fun bindSubOrderInfo(parentOrder: IKmmAdOrder) {
    }

    override fun reportAdClick(
        adOrder: IKmmAdOrder?,
        actType: AdGdtClickActType,
        config: AdReportConfig
    ) {
    }

    override fun getAppChecker(): IAdAppChecker = EmptyAdAppChecker

    override fun isCloseAd(loid: Int, adChannel: String): Boolean = true

    override fun createAdFeedsController(
        majorLoid: Int,
        adChannel: String,
        adFeedsContext: IAdFeedsContext?,
    ): IAdFeedsController? = null

    override fun exchangeAdRequestChannel(
        majorLoid: Int,
        pageItem: IKmmFeedsItem?,
        channelInfo: IChannelInfo,
    ): String = ""

    private object EmptyAdAppChecker : IAdAppChecker {
        override fun isWxAppInstalled(): Boolean = false
        override fun isWXAppSupportAPI(): Boolean = false
        override fun isAppInstalled(openScheme: String?, pkgName: String?): Boolean = false
    }

}
