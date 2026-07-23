package com.tencent.news.core.page.extension

import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.page.model.PageDtReport
import com.tencent.news.core.page.model.StructPageBusinessType
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.page.model.StructWidget
import com.tencent.news.core.view.ILogicContextHolder

private typealias BizType = StructPageBusinessType

object StructPageWidgetEx {

    val StructPageWidget?.businessType: String? get() = this?.data?.business_type

    fun StructPageWidget?.isVideoTopicPage(): Boolean = businessType == BizType.VIDEO_TOPIC
    fun StructPageWidget?.isTopicPage(): Boolean = businessType == BizType.TOPIC
    fun StructPageWidget?.isIpPage(): Boolean = businessType == BizType.IP
    fun StructPageWidget?.isQaPage(): Boolean = businessType == BizType.QA
    fun StructPageWidget?.isCommentPage(): Boolean = businessType == BizType.COMMENT

    // 通过item也可以查询pageWidget
    fun ILogicContextHolder?.findStructPageWidget(): StructPageWidget2? =
        this?.logicContext?.pageWidget?.getTarget()

    fun ILogicContextHolder?.findStructPageVM(): IStructPageViewModel? =
        findStructPageWidget()?.asWidgetVM

    fun StructWidget?.findStructPageWidget(): StructPageWidget? =
        this?.findStructPageWidget()

    // 是否能展示顶部目录导航（有多tab时候不能展示，UI有冲突）
    fun StructPageWidget?.canShowTopCatalogue(): Boolean =
        !this?.catalogue?.data?.catalogueData.isNullOrEmpty() && !hasDirectoryChannel()

    // 是否下发了‘右侧目录导航’样式
    // （注意：理论上客户端可以判断是多tab时候直接启用这个模式，没必要新增一个 channelShowType，目前后台其实也是判断的多tab下发这个type）
    fun StructPageWidget?.hasDirectoryChannel(): Boolean = this?.getChannelWidgets()?.find {
        it.data?.channel_info?.channelShowType == ChannelShowType.HOT_EVENT_WITH_DIRECTORY
    } != null

    fun StructPageWidget2.hasDtReport(): Boolean =
        pageConfig.dtDynamicReport != null || pageConfig.dtReport != null

    // 优先用动态参数：
    fun StructPageWidget2.buildDtReport(): PageDtReport? =
        pageConfig.dtDynamicReport?.invoke(this) ?: pageConfig.dtReport

}