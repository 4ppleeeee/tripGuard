@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.compose.scaffold.vm

import androidx.annotation.CallSuper
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.IPullRefreshHeaderViewModel
import com.tencent.news.core.compose.scaffold.ListScrollState
import com.tencent.news.core.compose.scaffold.OnDataProcessSuccess
import com.tencent.news.core.compose.scaffold.PullRefreshHeaderUiState
import com.tencent.news.core.compose.scaffold.PullRefreshResultUiState
import com.tencent.news.core.compose.scaffold.StructPageDataProcessEvent
import com.tencent.news.core.compose.scaffold.StructPageUiConfig
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.trace.ComposePageTrace
import com.tencent.news.core.extension.KColor
import com.tencent.news.core.extension.ResultCodeEx
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.api.IFeedsRefreshListener
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.api.ListRefreshReason
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.controller.FeedsProcessResult
import com.tencent.news.core.list.controller.FeedsRequestEnv
import com.tencent.news.core.list.controller.IFeedsDataProcessor
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.PicShowType
import com.tencent.news.core.page.model.ErrorInfo
import com.tencent.news.core.page.model.StructListHeaderState
import com.tencent.news.core.page.model.StructPageData
import com.tencent.news.core.page.model.StructPageLoadingViewType
import com.tencent.news.core.page.model.StructPageUiState
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.appAlert
import com.tencent.news.core.platform.api.appNetwork
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

private const val DEFAULT_PULL_REFRESH_RESULT_TEXT = "已更新至最新"

open class StructPageViewModel constructor(
    override val controller: IFlexibleFeedsController,
    override val pageFlow: SharedFlow<PageLifecycleEvent>,
    override val pageScope: CoroutineScope
) : IStructPageViewModel, IFeedsDataProcessor, IFeedsRefreshListener {

    init {
        pageRootWidget.bindStructPageVM { this }
        // 设置listRefreshListener，监听数据变化并自动刷新UI
        controller.listRefreshListener = this
    }

    override val scrollStateFlow: MutableSharedFlow<ListScrollState> by lazy {
        MutableSharedFlow(
            extraBufferCapacity = 1,    // 额外缓冲区大小
            onBufferOverflow = BufferOverflow.DROP_OLDEST  // 缓冲区满时丢弃最旧的事件
        )
    }

    override val loadingStateFlow: MutableStateFlow<StructPageUiState> by lazy {
        MutableStateFlow(StructPageUiState.Loading(viewType = getLoadingViewType()))
    }

    override val dataFlow: MutableSharedFlow<StructPageDataProcessEvent> by lazy {
        MutableSharedFlow(extraBufferCapacity = 1)
    }

    override val onCustomItemClick: ((item: IKmmFeedsItem) -> Boolean)? = null

    override val pullRefreshHeaderViewModel: IPullRefreshHeaderViewModel by lazy {
        PullRefreshHeaderViewModel()
    }

    override val pageUiConfig: StructPageUiConfig = StructPageUiConfig(
        statusBarChangeSwitch = MutableStateFlow(
            controller.getAllFeedsItemList().any {
                it.baseDto.picShowType == PicShowType.EVENT_VIDEO_HEADER
            }
        )
    )

    override fun refresh(request: FeedsRefreshRequest) {
        if (request.isLoading()) {
            loadingStateFlow.update { StructPageUiState.Loading(viewType = getLoadingViewType()) }
        }
        if (request.refreshAction == ListRefreshAction.AUTO_CACHE) {
            controller.doCacheRefresh(request = request, processor = this)
        } else {
            controller.doListRefresh(request = request, processor = this)?.execute()
        }
    }

    override suspend fun onPullRefresh(): PullRefreshResultUiState {
        refresh(
            FeedsRefreshRequest(
                refreshForward = ListRefreshForward.TOP_REFRESH,
                refreshAction = ListRefreshAction.PULL_DOWN
            )
        )
        val resultState = awaitPullRefreshResultState()
        return PullRefreshResultUiState(
            shouldShowResult = resultState != StructListHeaderState.ERROR,
            text = pullRefreshResultText()
        )
    }

    override fun updateFeedsResult(feedsResult: FeedsProcessResult) {
        loadingStateFlow.update { feedsResult.toSuccessState() }
    }

    private fun FeedsRefreshRequest.isLoading(): Boolean {
        return refreshForward == ListRefreshForward.RESET &&
                refreshAction != ListRefreshAction.CACHE_AFTER_RESET
    }

    @CallSuper
    override fun onProcessSucceed(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult
    ) {
        dataFlow.tryEmit(
            OnDataProcessSuccess(
                requestEnv = requestEnv,
                newPageWidget = newPageWidget,
                feedsResult = feedsResult
            )
        )
        when (requestEnv.getRefreshForward()) {
            ListRefreshForward.TOP_REFRESH ->
                handleTopRefreshSucceed(feedsResult)

            ListRefreshForward.BOTTOM_REFRESH ->
                handleBottomRefreshSucceed(feedsResult)

            ListRefreshForward.RESET ->
                handleResetRefreshSucceed(requestEnv, feedsResult)
        }
    }

    private fun handleResetRefreshSucceed(
        requestEnv: FeedsRequestEnv,
        feedsResult: FeedsProcessResult
    ) {
        // 如果不是使用缓存的数据 则需要更新缓存修改时间
        trySetCacheLstUpdateTime(requestEnv)

        ComposePageTrace.record("onPageDataReady")

        loadingStateFlow.update { feedsResult.toSuccessState() }

        ComposePageTrace.record("setPageDataAsSuccess")
    }

    private fun trySetCacheLstUpdateTime(
        requestEnv: FeedsRequestEnv
    ) {
        if (requestEnv.getRefreshAction() == ListRefreshAction.AUTO_CACHE) {
            return
        }
        pageRootWidget.pageConfig.cacheConfig?.apply {
            onRefreshCache()
            lastUpdateTime = getCurTimeMillis()
        }
    }

    private fun handleTopRefreshSucceed(feedsResult: FeedsProcessResult) {
        loadingStateFlow.update { feedsResult.toSuccessState() }
    }

    private fun handleBottomRefreshSucceed(feedsResult: FeedsProcessResult) {
        loadingStateFlow.update { feedsResult.toSuccessState() }
    }

    override fun onProcessError(requestEnv: FeedsRequestEnv, result: ResultEx) {
        when (requestEnv.getRefreshForward()) {
            ListRefreshForward.TOP_REFRESH -> {
                if (result.errorCode == ResultCodeEx.LIST_EMPTY && !controller.rootWidget.pageConfig.showTopRefreshEmptyToast) {
                    return
                }
                if (appNetwork().netState() == NetState.INAVAILABLE) {
                    appAlert().showToast("网络不给力，请检查网络后重试")
                } else {
                    appAlert().showToast("内容加载失败，请重试")
                }
            }
            ListRefreshForward.BOTTOM_REFRESH ->
                if (appNetwork().netState() == NetState.INAVAILABLE) {
                    appAlert().showToast("网络不给力，请检查网络后重试")
                } else {
                    appAlert().showToast("内容加载失败，请重试")
                }
            ListRefreshForward.RESET ->
                // 如果是使用了缓存后立即触发的reset不需要error，有历史缓存可以上屏
                if (requestEnv.getRefreshAction() != ListRefreshAction.CACHE_AFTER_RESET) {
                    loadingStateFlow.update { result.toErrorState() }
                }
        }
    }

    private fun FeedsProcessResult.toSuccessState() = StructPageUiState.Success(
        StructPageData(pageWidget = controller.rootWidget, feedsResult = this)
    )

    private fun ResultEx.toErrorState() = StructPageUiState.Error(
        ErrorInfo(code = errorCode, msg = msg, throwable = error)
    )

    override fun emitScrollEvent(scrollState: ListScrollState): Boolean {
        return scrollStateFlow.tryEmit(scrollState)
    }

    private fun getLoadingViewType(): StructPageLoadingViewType {
        return controller.rootWidget.pageConfig.loadingViewType
    }

    private suspend fun awaitPullRefreshResultState(): StructListHeaderState {
        controller.headerState.first {
            it == StructListHeaderState.LOADING
        }
        return controller.headerState.first {
            it == StructListHeaderState.COMPLETE ||
                    it == StructListHeaderState.ERROR ||
                    it == StructListHeaderState.WAITING_FOR_MORE ||
                    it == StructListHeaderState.NO_MORE
        }
    }

    private fun pullRefreshResultText(): String =
        controller.rootWidget.getRefreshWording().takeIfNotBlank() ?: DEFAULT_PULL_REFRESH_RESULT_TEXT

    private inner class PullRefreshHeaderViewModel : IPullRefreshHeaderViewModel {

        private val _uiState = MutableStateFlow(PullRefreshHeaderUiState.default(isDarkTheme = false))
        override val uiState: StateFlow<PullRefreshHeaderUiState> = _uiState.asStateFlow()

        override fun onThemeChanged(isDarkTheme: Boolean) {
//            val skin = skinManager()?.getRefreshHeaderSkin(refreshHeaderChannelId(), isDarkTheme)
//                ?: RefreshHeaderSkinStyle()
//            _uiState.value = skin.toPullRefreshHeaderUiState(isDarkTheme)
        }
    }

    private fun refreshHeaderChannelId(): String =
        controller.rootWidget.pageConfig.defaultChannelInfo.channelKey

//    private fun RefreshHeaderSkinStyle.toPullRefreshHeaderUiState(
//        isDarkTheme: Boolean
//    ): PullRefreshHeaderUiState {
//        val defaultState = PullRefreshHeaderUiState.default(isDarkTheme)
//        return PullRefreshHeaderUiState(
//            refreshingBgColor = refreshingBgColor.toRefreshColorInt(defaultState.refreshingBgColor),
//            refreshingTextColor = refreshingTextColor.toRefreshColorInt(defaultState.refreshingTextColor),
//            refreshedBgColor = refreshedBgColor.toRefreshColorInt(defaultState.refreshedBgColor),
//            refreshedTextColor = refreshedTextColor.toRefreshColorInt(defaultState.refreshedTextColor),
//            lottieUrl = lottieUrl
//        )
//    }

    private fun String.toRefreshColorInt(defaultColor: Int): Int {
        if (!KColor.isValidColor(this)) {
            return defaultColor
        }
        return try {
            KColor.toColorInt(this)
        } catch (e: NumberFormatException) {
            defaultColor
        } catch (e: IllegalArgumentException) {
            defaultColor
        }
    }

    /**
     * IFeedsRefreshListener 回调
     * 当数据发生增删改操作时，自动刷新UI
     */
    override fun onListRefresh(reason: ListRefreshReason, allData: List<IKmmFeedsItem>) {
        // 只处理增删改操作，网络刷新走原有逻辑
        when (reason) {
            ListRefreshReason.REMOVE,
            ListRefreshReason.INSERT,
            ListRefreshReason.REPLACE -> {
                // 构建新的FeedsProcessResult，使用新的列表副本以确保StateFlow能检测到变化
                val feedsResult = FeedsProcessResult(
                    allData = allData.toMutableList(),
                    newData = emptyList()
                )
                // 更新loadingStateFlow以触发UI刷新
                loadingStateFlow.value = feedsResult.toSuccessState()
            }
            else -> {
                // 其他情况不处理，由原有逻辑处理
            }
        }
    }

}
