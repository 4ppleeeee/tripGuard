package com.tencent.news.core.tads.articles

import com.tencent.news.core.extension.IVMDoc
import com.tencent.news.core.list.api.IDislikeListener
import com.tencent.news.core.tads.vm.VMHolder

interface IAdArticleVMHolder : IVMDoc {
    val midArticleSmallCell: VMHolder<IAdArticleMidSmallVM>          // 文中小图广告
    val midArticleLargeCell: VMHolder<IAdArticleMidLargeVM>          // 文中大图广告
    val midArticleSmallVerCell: VMHolder<IAdArticleMidSmallVerVM>    // 文中竖版小图广告
    val midArticleLargeVerCell: VMHolder<IAdArticleMidLargeVerVM>    // 文中竖版大图广告

    fun setDislikeListener(dislikeListener: IDislikeListener?)

    /**
     * 宿主通知当前展示的文中视频广告起播。
     * 时机：cell 在原生滚动容器内可见（scrollViewWillDisplay）/ 进前台且 cell 可见。
     */
    fun notifyVideoPlay()

    /**
     * 宿主通知当前展示的文中视频广告暂停。
     * 时机：cell 滚出可见区（scrollViewEndDisplay）/ 退后台。
     */
    fun notifyVideoPause()
}
