package com.tencent.news.core.list.extension

import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.service.FrameworkService
import com.tencent.news.core.tads.api.IAdFeedsContext

object FlexControllerEx {

    fun StructPageWidget2.toFlex(
        adFeedsContext: IAdFeedsContext? = null
    ): IFlexibleFeedsController =
        FrameworkService.createFlexFeedsController(
            rootWidget = this,
            pageItem = { findPageItem() },
            adFeedsContext = adFeedsContext
        )

}