package com.tencent.news.core.compose.scaffold

import com.tencent.kuikly.compose.foundation.lazy.LazyListScope
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose_dsl.kuikly.Views.PullToRefreshState
import com.tencent.kuikly.compose_dsl.kuikly.Views.pullToRefreshItem
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.StructListHeaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/**
 * LazyColumn 下拉刷新组件：封装 pullToRefreshItem 及其刷新逻辑
 *
 * 内部发起下拉刷新请求并等待 headerState 流转确认刷新完成后自动收起指示器。
 *
 * @param pullToRefreshState 下拉刷新状态，由外部通过 rememberPullToRefreshState 创建
 * @param scrollState LazyColumn 的滚动状态
 * @param coroutineScope 用于启动刷新协程
 * @param pageVM 页面 ViewModel，用于发起刷新请求和监听 headerState
 * @param onRefreshingChanged 刷新状态变化回调，用于同步外部 isRefreshing 状态
 */
@Suppress("FunctionName")
internal fun LazyListScope.PullRefresh4List(
    pullToRefreshState: PullToRefreshState,
    scrollState: LazyListState,
    coroutineScope: CoroutineScope,
    pageVM: IStructPageViewModel?,
    onRefreshingChanged: (Boolean) -> Unit,
    foregroundColor: Color?,
) {
    pageVM ?: return

    pullToRefreshItem(
        state = pullToRefreshState,
        onRefresh = {
            coroutineScope.launch {
                onRefreshingChanged(true)
                pageVM.refresh(
                    FeedsRefreshRequest(
                        refreshForward = ListRefreshForward.TOP_REFRESH,
                        refreshAction = ListRefreshAction.PULL_DOWN
                    )
                )
                // 先等待 headerState 变为 LOADING 或 ERROR（确认请求已开始，或请求创建失败直接报错）
                val startState = pageVM.controller.headerState.first {
                    it == StructListHeaderState.LOADING ||
                            it == StructListHeaderState.ERROR ||
                            it == StructListHeaderState.COMPLETE ||
                            it == StructListHeaderState.WAITING_FOR_MORE ||
                            it == StructListHeaderState.NO_MORE
                }
                // 请求成功开始后，再等待刷新结束（COMPLETE 之后会立即转为 WAITING_FOR_MORE 或 NO_MORE）
                if (startState == StructListHeaderState.LOADING) {
                    pageVM.controller.headerState.first {
                        it == StructListHeaderState.COMPLETE ||
                                it == StructListHeaderState.ERROR ||
                                it == StructListHeaderState.WAITING_FOR_MORE ||
                                it == StructListHeaderState.NO_MORE
                    }
                }
                onRefreshingChanged(false)
            }
        },
        scrollState = scrollState,
        content = { pullProgress: Float, isRefreshing: Boolean, refreshThreshold: Dp ->
            PullRefreshHeaderView(
                pullProgress = pullProgress,
                isRefreshing = isRefreshing,
                refreshThreshold = refreshThreshold,
                foregroundColor = foregroundColor,
            )
        }
    )
}
