package com.tencent.news.core.app

import com.tencent.news.core.platform.api.PageLifecycleState
import com.tencent.news.core.platform.api.appPageStack

object IKmmContextEx {
    fun IKmmContext.getLifecycleState(): PageLifecycleState {
        return appPageStack()?.getPageLifecycleState(this) ?: PageLifecycleState.UNKNOWN
    }
}