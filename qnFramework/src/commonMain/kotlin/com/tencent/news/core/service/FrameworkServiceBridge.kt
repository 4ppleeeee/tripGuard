package com.tencent.news.core.service

import com.tencent.news.core.list.controller.IFeedsDataProcessor
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.IShareBtnWidgetViewModel
import com.tencent.news.core.page.model.ShareBtnWidget
import com.tencent.news.core.tads.api.IAdAppChecker
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.core.tads.api.IAdFeedsController
import com.tencent.news.core.tads.click.AdReportConfig
import com.tencent.news.core.tads.constants.AdGdtClickActType
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.qnchannel.api.IChannelInfo

/**
 * qnFramework 服务桥接器
 *
 * 将 qnFramework 对 qnCore Service 层（UserService、FeedsService、AdService）的依赖
 * 统一收口到此处，由 qnCore 在模块初始化时注入实现，避免 qnFramework 直接依赖 qnCore
 */
object FrameworkServiceBridge {

    lateinit var impl: IFrameworkServiceBridge
        private set

    fun register(bridge: IFrameworkServiceBridge) {
        impl = bridge
    }

}

/**
 * 桥接接口定义，由 qnCore 实现并注册
 */
interface IFrameworkServiceBridge {

    // 取item大同上报公参
    fun getFeedsItemReportParams(feedsItem: IKmmFeedsItem): Map<String, Any>?

    // 判断用户关注态
    fun isFollowUser(suid: String): Boolean

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

    // 分享按钮的临时依赖
    fun createShareBtnVM(widget: ShareBtnWidget): IShareBtnWidgetViewModel

    // ---- AdService 桥接 ----

    // 判断广告位是否关闭
    fun isCloseAd(loid: Int, adChannel: String): Boolean

    // 创建信息流广告controller
    fun createAdFeedsController(
        majorLoid: Int,
        adChannel: String,
        adFeedsContext: IAdFeedsContext? = null,
    ): IAdFeedsController

    // 广告频道替换逻辑
    fun exchangeAdRequestChannel(
        majorLoid: Int,
        pageItem: IKmmFeedsItem?,
        channelInfo: IChannelInfo,
    ): String

    // 创建非法item过滤处理器
    fun createInvalidItemFilterProcessor(): IFeedsDataProcessor

}
