package com.tencent.news.core.tads.constants

import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.tads.constants.AdRefreshType.BOTTOM_REFRESH
import com.tencent.news.core.tads.constants.AdRefreshType.RESET
import com.tencent.news.core.tads.constants.AdRefreshType.TOP_REFRESH


/**
 * 广告信息流刷新类型
 */

enum class AdRefreshType(val code: Int) { // 【注意】它的code与 ListRefreshForward 不一样
    RESET(0),           // 首刷
    TOP_REFRESH(1),     // 顶部刷新
    BOTTOM_REFRESH(2);  // 底部刷新

    override fun toString(): String = code.toString()
}

object AdRefreshTypeEx {
    fun mapFrom(forward: ListRefreshForward): AdRefreshType {
        return when (forward) {
            ListRefreshForward.RESET -> RESET
            ListRefreshForward.TOP_REFRESH -> TOP_REFRESH
            ListRefreshForward.BOTTOM_REFRESH -> BOTTOM_REFRESH
        }
    }
}