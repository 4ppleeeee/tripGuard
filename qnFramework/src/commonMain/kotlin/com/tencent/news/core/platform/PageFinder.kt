package com.tencent.news.core.platform

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.detail.IKmmNewsDetailPage
import com.tencent.news.core.detail.IKmmVideoDetailPage
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.api.appPageStack

interface IPageFinder {
    fun getActiveNewsDetailPage(): IKmmContext? = null
    fun getActiveVideoDetailPage(): IKmmContext? = null
    fun isVideoDetailPage(context: IKmmContext) = false
    fun getVideoDetailPageItem(context: IKmmContext): IKmmFeedsItem? = null
}

object PageFinder : IPageFinder {

    private var finderHooker: IPageFinder? = null

    @KmmInternalApi
    fun setFinderProxy(finder: IPageFinder) {
        this.finderHooker = finder
    }

    override fun getActiveNewsDetailPage(): IKmmContext? {
        return finderHooker?.getActiveNewsDetailPage() ?: appPageStack()?.getActivePages()?.find {
            it is IKmmNewsDetailPage
        }
    }

    override fun getActiveVideoDetailPage(): IKmmContext? {
        return finderHooker?.getActiveVideoDetailPage() ?: appPageStack()?.getActivePages()?.find {
            isVideoDetailPage(it)
        }
    }

    override fun isVideoDetailPage(context: IKmmContext): Boolean {
        return finderHooker?.isVideoDetailPage(context) == true || context is IKmmVideoDetailPage
    }

    override fun getVideoDetailPageItem(context: IKmmContext): IKmmFeedsItem? {
        return finderHooker?.getVideoDetailPageItem(context)
            ?: (context as? IKmmVideoDetailPage)?.getItemData()
    }
}