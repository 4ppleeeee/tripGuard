package com.tencent.news.core.page.model

import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.controller.FeedsProcessResult
import com.tencent.news.core.list.controller.FeedsRequestEnv
import com.tencent.news.core.list.controller.IFeedsDataProcessor
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.service.FrameworkService

object StructPageWidgetCache {
    private val cache = mutableMapOf<String, StructPageWidget2>()

    fun getOrCreatePageWidget(
        key: String?,
        createAction: () -> StructPageWidget2
    ): StructPageWidget2 {
        return getPageWidget(key) ?: createAction().apply {
            putPageWidget(key, this)
        }
    }

    fun getPageWidget(key: String?): StructPageWidget2? {
        key ?: return null
        return cache[key]
    }

    fun putPageWidget(key: String?, widget: StructPageWidget2?) {
        key ?: return
        widget ?: return
        cache[key] = widget
    }

    fun removePageWidget(key: String?) {
        key ?: return
        cache.remove(key)
    }

    fun preload(widget: StructPageWidget2?) {
        widget ?: return
        val controller = FrameworkService.createFlexFeedsController(
            rootWidget = widget,
            pageItem = { widget.findPageItem() }
        )
        controller.doListRefresh(
            FeedsRefreshRequest(ListRefreshForward.RESET, ListRefreshAction.PRELOAD),
            object : IFeedsDataProcessor {
                override fun onProcessSucceed(
                    requestEnv: FeedsRequestEnv,
                    newPageWidget: StructPageWidget,
                    feedsResult: FeedsProcessResult
                ) {
                    super.onProcessSucceed(requestEnv, newPageWidget, feedsResult)
                    widget.pageConfig.cacheConfig?.lastUpdateTime = getCurTimeMillis()
                }
            }
        )?.execute()
    }

    fun canUseCacheData(widget: StructPageWidget2?): Boolean {
        widget ?: return false
        return widget.getFeedsList().isNotNullOrEmpty() &&
                !widget.pageConfig.cacheConfig.isExpired()
    }

    private fun IStructPageWidgetCacheConfig?.isExpired(): Boolean {
        this ?: return true
        // 如果还没有重置过缓存，认为过期
        if (lastUpdateTime == -1L) {
            return true
        }
        val currentTime = getCurTimeMillis()
        val timeDifference = currentTime - lastUpdateTime
        val fifteenMinutesInMillis = expiredTime
        return timeDifference > fifteenMinutesInMillis
    }

}