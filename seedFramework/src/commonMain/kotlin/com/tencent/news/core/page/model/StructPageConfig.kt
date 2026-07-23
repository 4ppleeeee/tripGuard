@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.page.model

import com.tencent.news.core.compose.platform.StructPageArgs
import com.tencent.news.core.dt.constants.DtPageId
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.list.api.IStructDataRepo
import com.tencent.news.core.list.api.IStructDataValidator
import com.tencent.news.core.list.controller.IFeedsDataProcessor
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.QnListConfig
import com.tencent.news.core.list.vm.ReportAction
import com.tencent.news.core.page.constants.StructWidgetShowType
import com.tencent.news.core.platform.api.DynamicParamsProvider
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.qnchannel.api.IChannelInfo
import com.tencent.news.qnchannel.api.setEnableGlobalCache


// 结构化品字形底层页，相关配置项：
data class StructPageConfig constructor(

    // 【核心】页面数据源
    val dataRepo: IStructDataRepo,

    // 对下发数据的合法性进行校验
    val dataInvalidator: IStructDataValidator? = null,

    // 额外的数据处理器，业务特有的加工逻辑放这里
    val dataProcessors: List<IFeedsDataProcessor> = emptyList(),

    // 【核心】主频道信息
    // - 底层页：这个就是主tab的channelInfo；
    // - 频道：这个就是大圣的channelInfo
    val defaultChannelInfo: IChannelInfo,

    // 顶部固定TitleBar：
    // - false：是指Header一直通栏到TitleBar底下，会随着内容滑动，TitleBar有渐变效果（参考：专题页）
    // - true：TitleBar固定白色放在顶部，内容在底部；滑动时没有联动效果（参考：设置页）
    val fixTitleBarAboveContent: Boolean = false,

    // 将 ChannelBar 固定在 TitleBar 下面，不放在 hanging 联动区
    // （只有 fixTitleBarAboveContent=true 或 没有 TitleBar 时才有用）
    val fixChannelBarBelowTitleBar: Boolean = false,

    // 固定 Header 在内容上方（不跟随列表滚动）：
    // - false（默认）：Header 作为 LazyColumn 的第一个 item，会随列表一起滚动（适用于大部分品字形底层页）
    // - true：Header 独立固定在内容列表之上（对齐 Android 视频底层页，顶部播放器固定，下方列表独立滚动）
    //   此模式下 Header 不参与折叠联动，列表高度会相应减去 Header 高度
    val fixHeaderAboveContent: Boolean = false,

    // 内容区要撑满全屏，不避让TitleBar位置
    val contentFullScreen: Boolean = false,

    // 使用Box布局代替Hover，实现悬停效果（留个开关暂留1版，后面全量）
    val useNewHoverMode: Boolean = getShiplySwitch("composeUseNewHoverMode", true),

    // 强制隐藏 TitleBar 区域（有下发 widget 也不展示）
    val forceHideTitleBarArea: Boolean = false,

    // 强制隐藏 Header 区域（有下发 widget 也不展示）
    val forceHideHeaderArea: Boolean = false,

    // 禁止 HorizontalPager 用户手势滑动（仍允许程序化切页）
    val disableHorizontalPagerGesture: Boolean = false,

    // StatusBar颜色模式
    val defaultStatusBarColorMode: StatusBarColorMode = StatusBarColorMode.DARK_BG_LIGHT_ICON,

    // 【预留，待实现】开启StatusBar沉浸模式：
    // - true：StatusBar与内容连在一起
    // - false：StatusBar是黑底白字（低版本安卓的效果，一般页面都不会用这个效果）
    val immersiveStatusBar: Boolean = true,

    // 页面展示类型，部分页面需要自定义处理
    val pageShowType: Int = StructWidgetShowType.DEFAULT,

    // 禁用皮肤
    val forbidSkin: Boolean = false,

    // 全局缓存页面数据，且每次进入页面时reset一次（打开这个，首屏展示更快）
    val globalCacheAndReset: Boolean = false,

    // 【预留，待实现】禁用黑白模式
    val forbidGreyMode: Boolean = false,


    // 主动的数据上报回调，在页面曝光、退出 等时机会回调这里的方法
    val reportAction: PageReportAction? = null,

    // 【预留，待实现】大同上报参数
    val dtReport: PageDtReport? = null,
    val dtDynamicReport: ((rootWidget: StructPageWidget2) -> PageDtReport)? = null,

    // 延迟绑定 dtReport 直到页面数据加载成功
    val delayDtReportUntilDataReady: Boolean = false,

    // 锚点tabId
    val anchorTabId: String? = null,

    // 是否支持负反馈
    val supportDislikeBtn: Boolean = false,

    // 列表底部是否展示footer，支持加载更多（一些独立页面只有一刷数据的情况，会关掉）
    val enableFooter: Boolean = true,

    // 列表顶部是否展示header，支持下拉刷新（一些独立页面不需要下拉刷新的情况，会关掉）
    val enableHeader: Boolean = false,

    // 是否启用下拉刷新（仿照新闻妹 MessageList 的下拉刷新方案）
    // 当此项为 true 时，子列表支持下拉回弹并在回弹超过阈值时触发 RESET + PULL_DOWN 刷新
    val enablePullRefresh: Boolean = false,

    // 是否展示顶部加载空内容错误 toast
    val showTopRefreshEmptyToast: Boolean = true,

    // 页面缓存配置（要支持预加载，做首屏秒开时，使用这个）
    val cacheConfig: IStructPageWidgetCacheConfig? = null,

    // 是否展示loading lottie
    val loadingViewType: StructPageLoadingViewType = StructPageLoadingViewType.NORMAL_LOTTIE,

    // 列表底部边距
    val mainContentBottomPadding: Float = 0f,

    // iOS底部安全区适配：true-内容区撑满全屏
    val expandBottomSafeAreaForPage: Boolean = false,

    // iOS底部安全区适配：true-列表末尾添加一个空白块高度，撑开安全区，更符合iPhoneX沉浸式滚动的设计
    // （普遍配合 expandBottomSafeAreaForPage 使用）
    val expandBottomSafeAreaForList: Boolean = false,

    // 最大页面高度
    val maxPageHeight: Float = 0f,

    // 是否缓存flexCtrl
    val enableCacheFlexCtrl: Boolean = false,

    // 列表惯性滑动速度上限，不配置时不限制
    val flingSpeedLimit: Float? = null
) {
    init {
        if (globalCacheAndReset) {
            defaultChannelInfo.setEnableGlobalCache(true)
        }
    }
}

enum class StatusBarColorMode {
    DARK_BG_LIGHT_ICON,     // 初始时黑底白字（随页面滑动后反向变化）
    LIGHT_BG_DARK_ICON,     // 初始时白底黑字（随页面滑动后反向变化）
    ALWAYS_LIGHT_ICON,      // 图标永远是白色（不随页面滑动改变）
    ALWAYS_DARK_ICON        // 图标永远是黑色（不随页面滑动改变）
}

data class PageDtReport constructor(
    val pageId: DtPageId,
    val channelId: String,
    val isLandingPage: Boolean = false,
    val pageParams: Map<String, Any>? = null,           // 页面上报参数（静态固定参数）
    val dynamicParams: DynamicParamsProvider? = null    // 页面上报参数（大同动态参数，触发上报时才获取）
) {
    companion object {

        // 有item的底层页，一般都可以用这个（类似：dtPageDetail）
        fun createArticleDetail(
            pageItem: IKmmFeedsItem,
            channelId: String = "",
            isLandingPage: Boolean = false,
            dynamicParams: DynamicParamsProvider? = null
        ) = PageDtReport(
            pageId = DtPageId.ARTICLE_DETAIL,
            channelId = channelId.takeIfNotBlank() ?: pageItem.ctxDto.newsChannel,
            isLandingPage = isLandingPage,
            pageParams = FrameworkServiceBridge.impl.getFeedsItemReportParams(pageItem),
            dynamicParams = dynamicParams
        )

        fun createArticleDetail(pageArgs: StructPageArgs) = createArticleDetail(
            pageItem = pageArgs.pageItem,
            channelId = pageArgs.channelId,
            isLandingPage = pageArgs.isLandingPage,
        )

    }
}

data class PageReportAction(
    val onPageExpose: ReportAction? = null,
    val onPageExit: ReportAction? = null,
)

data class StructListConfig(
    val normalWindowListConfig: QnListConfig,
    val bigWindowListConfig: QnListConfig,
)
