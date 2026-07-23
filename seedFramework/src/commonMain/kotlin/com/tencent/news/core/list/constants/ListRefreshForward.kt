package com.tencent.news.core.list.constants

import com.tencent.news.core.app.constants.QueryType
import com.tencent.news.core.extension.IListEnumDoc


/**
 * 对应信息流请求 forward 参数（后台识别该取值，不会轻易增加）
 *
 * @see ListRefreshAction
 */
enum class ListRefreshForward(val code: Int) : IListEnumDoc {

    TOP_REFRESH(QueryType.QUERY_BY_PULL_DOWN),      // 顶部刷新
    BOTTOM_REFRESH(QueryType.QUERY_BY_PULL_UP),     // 底部刷新
    RESET(QueryType.QUERY_BY_RESET);                // 首刷

    override fun toString(): String = code.toString()

}

object ListRefreshForwardEx {
    fun getByCode(code: Int): ListRefreshForward {
        return when (code) {
            ListRefreshForward.RESET.code -> ListRefreshForward.RESET
            ListRefreshForward.TOP_REFRESH.code -> ListRefreshForward.TOP_REFRESH
            ListRefreshForward.BOTTOM_REFRESH.code -> ListRefreshForward.BOTTOM_REFRESH
            else -> ListRefreshForward.BOTTOM_REFRESH
        }
    }
}