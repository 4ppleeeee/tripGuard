package com.tencent.news.core.compose.scaffold

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.safeGet
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.controller.FeedsProcessResult
import com.tencent.news.core.list.controller.FeedsRequestEnv
import com.tencent.news.core.list.controller.IFeedsDataProcessor
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.IStructWidgetVM
import com.tencent.news.core.page.model.StructPageProcessResult
import com.tencent.news.core.page.model.StructPageUiState
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

// 通用的结构化页面vm
interface IStructPageViewModel : IStructWidgetVM {

    val controller: IFlexibleFeedsController    // 【重要】列表controller，核心数据操作都在这里
    val scrollStateFlow: SharedFlow<ListScrollState>                    // 页面滑动状态
    val pageScope: CoroutineScope                           // 页面协程作用域
    val loadingStateFlow: StateFlow<StructPageUiState>      // 监听页面状态变化：加载中、加载失败、加载成功（注意，这个loading是全屏的view）
    val pageUiConfig: StructPageUiConfig                    // 页面UI组件状态（例如：StatusBar等）
    val pageFlow: SharedFlow<PageLifecycleEvent>            // 页面基础生命周期（resume、pause等）
    val dataFlow: SharedFlow<StructPageDataProcessEvent?>   // 页面数据生命周期
    val onCustomItemClick: ((item: IKmmFeedsItem) -> Boolean)?  // 自定义item点击时调用
    val pullRefreshHeaderViewModel: IPullRefreshHeaderViewModel // 下拉刷新头 VM

    fun onPageDisposed() {}     // 页面销毁（注意flow是基于pageScope的，收不到销毁事件，需要用这个）
    fun emitScrollEvent(scrollState: ListScrollState): Boolean          // 主动触发页面滑动
    fun refresh(request: FeedsRefreshRequest)               // 触发页面刷新
    suspend fun onPullRefresh(): PullRefreshResultUiState   // 用户下拉刷新，返回结果条 UI 数据
    fun updateFeedsResult(feedsResult: FeedsProcessResult)  // 手动刷新页面数据
    fun onAfterShowMainContent() {}             // 页面UI构建之后回调（vm可以做一些自己的逻辑处理）
    fun onThemeChanged(isDarkTheme: Boolean) {} // 主题切换时调用
}

interface IPullRefreshHeaderViewModel {
    val uiState: StateFlow<PullRefreshHeaderUiState>

    fun onThemeChanged(isDarkTheme: Boolean)
}

object StructPageViewModelEx {

    // 页面widget（所有widget通信全靠它，页面数据也全在这里）
    val IStructPageViewModel.pageRootWidget: StructPageWidget2
        get() = controller.rootWidget

    // 当前页面列表里的全量item数据
    fun IStructPageViewModel.getAllFeedsList(): List<IKmmFeedsItem> =
        controller.getAllFeedsItemList()

    // 页面item（底层页性质的页面，这里应该都不为空）
    fun IStructPageViewModel.findPageItem(): IKmmFeedsItem? =
        controller.rootWidget.findPageItem()

    // 发起 controller.doListRefresh 请求，并将结果转换成flow自行处理
    fun IStructPageViewModel.refreshAsFlow(request: FeedsRefreshRequest): Flow<StructPageProcessResult> =
        callbackFlow {
            val networkBuilder = controller.doListRefresh(
                request = request,
                processor = object : IFeedsDataProcessor {
                    override fun onProcessSucceed(
                        requestEnv: FeedsRequestEnv,
                        newPageWidget: StructPageWidget,
                        feedsResult: FeedsProcessResult,
                    ) {
                        trySend(StructPageProcessResult(requestEnv, newPageWidget, feedsResult))
                    }

                    override fun onProcessError(requestEnv: FeedsRequestEnv, result: ResultEx) {
                        trySend(StructPageProcessResult(requestEnv, result = result))
                    }
                }
            )

            val request = networkBuilder?.execute()
            awaitClose {
                request?.cancel()
            }
        }

    fun IStructPageViewModel.scrollToIndex(
        index: Int,
        animate: Boolean = false,
        scrollRootHeader: Boolean = true
    ) {
        val targetItem = getAllFeedsList().safeGet(index)
        if (targetItem != null) {
            emitScrollEvent(
                ListScrollState(
                    currentItem = targetItem,
                    animate = animate,
                    scrollRootHeader = scrollRootHeader
                )
            )
        }
    }

}


// todo 后续拓展继续拓展 要和IFeedsDataProcessor中的接口对齐
sealed class StructPageDataProcessEvent

class OnDataProcessSuccess(
    val requestEnv: FeedsRequestEnv,
    val newPageWidget: StructPageWidget,
    val feedsResult: FeedsProcessResult
) : StructPageDataProcessEvent()
