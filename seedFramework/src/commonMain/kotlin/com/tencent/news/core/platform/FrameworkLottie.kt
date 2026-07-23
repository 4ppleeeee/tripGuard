package com.tencent.news.core.platform

import com.tencent.news.core.service.FrameworkUiResourceBridge

object FrameworkLottie {

    private val impl get() = FrameworkUiResourceBridge.impl

    val footerLoading get() = impl.getGlobalFooterLottie()

    val loading get() = impl.getGlobalLoadingLottie()

    val pullRefresh get() = impl.getGlobalPullRefreshLottie()

}
