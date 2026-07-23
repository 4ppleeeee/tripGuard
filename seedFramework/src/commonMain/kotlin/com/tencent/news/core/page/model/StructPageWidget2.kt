@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.page.model

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.setup.LazyImpl
import kotlinx.serialization.Transient

// todo genesisli opt: 后续页面都会切换到这个widget2上，通过 pageConfig 描述页面所有配置
open class StructPageWidget2 constructor(
    @Transient val pageConfig: StructPageConfig,
) : StructPageWidget() {

    // 如果当前pageWidget是个子tab，这个parent是外层父页面的widget
    var parentRootWidget: StructPageWidget2? = null
        @KmmInternalApi set

    private var pageVM: IStructPageViewModel? = null
    private var pageVMProvider: LazyImpl<IStructPageViewModel>? = null
    var cachedFlexCtrl: IFlexibleFeedsController? = null

    override val asWidgetVM: IStructPageViewModel?
        get() {
            pageVM = pageVM ?: pageVMProvider?.invoke() // 缓存一下，防止每次get provider都创建
            return pageVM
        }

    fun bindStructPageVM(provider: LazyImpl<IStructPageViewModel>) {
        if (pageVMProvider != provider) {
            pageVM = null // 如果provider变了，清掉缓存；防止动态刷新的情况
        }
        pageVMProvider = provider
    }

    fun unbindStructPageVM() {
        if (!getShiplySwitch("enable_unbind_struct_page_vm", true)) {
            return
        }
        pageVM = null
        pageVMProvider = null
    }

    override fun pageSkinRes(): PageSkinRes? {
        if (pageConfig.forbidSkin) {
            return null
        }
        return super.pageSkinRes()
    }

    fun canShowTitleBarArea(): Boolean {
        if (pageConfig.forceHideTitleBarArea) {
            return false    // 强制隐藏 TitleBar 区域
        }
        if (titleBar != null) {
            return true     // 有 TitleBar 肯定能正常出
        }
        if (pageConfig.fixChannelBarBelowTitleBar) {
            // 没有 TitleBar 时，如果将 ChannelBar 提上去了，这个区域也要展示
            return pager?.channelBar != null
        }
        return false
    }

    fun getTitleBarCollapseRatio(): Double =
        titleBar?.action?.collapseRatio ?: 0.95

    fun canShowHeaderArea(): Boolean = header != null && !pageConfig.forceHideHeaderArea

    fun fixTitleBarAboveContent(): Boolean =
        pageConfig.fixTitleBarAboveContent || titleBar?.ui?.fixTitleBarAboveContent.isTrue()

    /**
     * Header 是否固定在内容上方（不跟随列表滚动）
     * 对齐 Android 视频底层页：顶部播放器固定，下方列表独立滚动
     */
    fun fixHeaderAboveContent(): Boolean =
        pageConfig.fixHeaderAboveContent && canShowHeaderArea()

    fun findPageItem(): IKmmFeedsItem? = pageConfig.defaultChannelInfo.env.pageItem

    fun getNewsChannel(): String = pageConfig.defaultChannelInfo.env.newsChannel

    fun buildPageWithManual2(action: StructPageWidget2.() -> Unit): StructPageWidget2 {
        this.action()

        bindWidgetProviderForAllWidgets()
        compatWidgetBuild()

        return this
    }

}