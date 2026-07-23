package com.tencent.news.core.page.extension

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.extension.FlexControllerEx.toFlex
import com.tencent.news.core.list.page.StructChannelPageWidget
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.IStructSubPage
import com.tencent.news.core.tads.api.IAdFeedsContext

internal object ChannelWidgetEx {

    @OptIn(KmmInternalApi::class)
    fun ChannelWidget.createOrGetFlexController(
        adFeedsContext: IAdFeedsContext? = null
    ): IFlexibleFeedsController {
        val cacheCtrl = this.subTabFeedsCtrl
        if (cacheCtrl != null) {
            return cacheCtrl
        }

        // 注意时序：这个要在创建新widget前获取；否则新widget创建完成，pageWidget会被替换
        val rootWidget = findStructPageWidget2()

        val subPageWidget = if (this is IStructSubPage) {
            this.subPageWidget() // 推荐都采用这种方式
        } else {
            StructChannelPageWidget(this)
        }

        if (rootWidget != null) {
            // 留一个路径：子tab能找到外层父页面
            subPageWidget.parentRootWidget = rootWidget
        }

        val newCtrl = subPageWidget.toFlex(adFeedsContext)
        this.subTabFeedsCtrl = newCtrl
        return newCtrl
    }

}