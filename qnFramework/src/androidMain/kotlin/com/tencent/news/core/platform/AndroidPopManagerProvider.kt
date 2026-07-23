package com.tencent.news.core.platform

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.pop.api.IPopUpManager
import com.tencent.news.core.pop.api.PopManagerProvider

object AndroidPopManagerProvider {
    fun get(context: IKmmContext): IPopUpManager {
        return PopManagerProvider.get(context)
    }
}