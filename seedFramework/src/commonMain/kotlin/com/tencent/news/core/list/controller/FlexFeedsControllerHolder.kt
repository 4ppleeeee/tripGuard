package com.tencent.news.core.list.controller

import com.tencent.news.core.channel.constants.NewsChannel
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.list.api.FlexCreateCallback
import com.tencent.news.core.list.api.IFlexFeedsControllerHolder
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.qncore.QnCoreAppStatus
import com.tencent.news.core.service.FrameworkService
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.qnchannel.api.IChannelInfo

// holder主要作用：当列表核心状态发生变化时，负责自动重建
internal class FlexFeedsControllerHolder(
    val needCheckStatus: Boolean,
    val channelInfoGetter: LazyImpl<IChannelInfo>,
    val rootWidget: LazyImpl<StructPageWidget2>,
    val pageItem: LazyImpl<IKmmFeedsItem?>? = null,
    val adFeedsContext: LazyImpl<IAdFeedsContext?>? = null,
) : IFlexFeedsControllerHolder {

    private var statusKey: String = ""

    private var feedsCtrl: IFlexibleFeedsController? = null

    private val channelInfo: IChannelInfo
        get() = channelInfoGetter()

    override fun createOrGet(
        checkReCreate: Boolean,
        onCreate: FlexCreateCallback?
    ): IFlexibleFeedsController? {
        // 【重要】检查ctrl重建：
        if (checkReCreate) {
            clearFeedsCtrlIfStatusChanged()
        }

        val curCtrl = feedsCtrl
        if (curCtrl != null) {
            return curCtrl
        }

        val newCtrl = FrameworkService.createFlexFeedsController(
            rootWidget = rootWidget(),
            pageItem = { pageItem?.invoke() },
            adFeedsContext = adFeedsContext?.invoke()
        )

        this.feedsCtrl = newCtrl
        this.statusKey = generateStatusKey()

        onCreate?.invoke(newCtrl)

        return newCtrl
    }

    override fun needResetForStatusChanged(): Boolean {
        if (!needCheckStatus) return false

        if (isStatusChanged()) {
            return true
        }
        val curCtrl = feedsCtrl ?: return false

        // 有可能提前触发过 createOrGet，导致ctrl重建了，此时 statusKey 也刷新了；
        // 但列表还没初始化过，这时候也要重新reset
        return curCtrl.rootWidget.isMainContentEmpty()
    }

    private fun isStatusChanged(): Boolean {
        if (!needCheckStatus) return false
        if (statusKey.isEmpty()) return false

        val newStatusKey = generateStatusKey()
        return statusKey != newStatusKey
    }

    private fun clearFeedsCtrlIfStatusChanged() {
        val oldStatusKey = statusKey
        if (isStatusChanged()) {
            this.feedsCtrl = null
            this.statusKey = ""
            NewsChannelLog.debug { "重建列表 ctrl，statueKey发生变化：${oldStatusKey} -> ${generateStatusKey()}" }
        }
    }

    private fun getChannelKey(): String =
        feedsCtrl?.rootWidget?.pageConfig?.defaultChannelInfo?.channelKey.getNonNull()

    private fun channelRebuildKey(): String =
        channelInfo.env.rebuildStatusKey?.let { "_$it" } ?: ""

    // 这个key变化，代表列表数据发生重大变化，需要重建
    private fun generateStatusKey(): String {
        val channelKey = getChannelKey()

        // 个性化推荐状态（变化后会切换列表接口和后台推荐模式）：
        val rcmdStatus = if (NewsChannel.isNewsTop(channelKey)) {
            // 要闻关闭个性化，也会进入主编精选模式
            QnCoreAppStatus.isPersonalizedSwitchOpen() && !QnCoreAppStatus.isInNewsTopManualMode()
        } else {
            QnCoreAppStatus.isPersonalizedSwitchOpen()
        }

        return "${channelKey}_${rcmdStatus}${channelRebuildKey()}"
    }

}
