package com.tencent.news.core.list.controller

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.annotation.RestrictedApi
import com.tencent.news.core.extension.ResultCodeEx
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.addIfAbsent
import com.tencent.news.core.extension.clearAndAddAll
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.extension.safeAddAll
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.extension.safeReplaceList
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.api.IContextProvider
import com.tencent.news.core.list.api.IFeedsRefreshListener
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.api.IListPagingRecorder
import com.tencent.news.core.list.api.ItemCursor
import com.tencent.news.core.list.api.ListRefreshReason
import com.tencent.news.core.list.api.StructDataEnv
import com.tencent.news.core.list.api.getLogKey
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshActionData
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.constants.isCloudReplaceAction
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.dataRepo
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.debugLog
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.errorLog
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.fileLog
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.getDefaultChannelInfo
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.getLogKey
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.pageConfig
import com.tencent.news.core.list.controller.FlexCtrlNetworkHelper.buildCommonNetworkBuilder
import com.tencent.news.core.list.controller.FlexCtrlRequestHelper.createResetDataRequest
import com.tencent.news.core.list.controller.FlexCtrlWidgetHelper.findAutoLoadListWidget
import com.tencent.news.core.list.controller.FlexCtrlWidgetHelper.findClickLoadListWidget
import com.tencent.news.core.list.controller.FlexCtrlWidgetHelper.findTopAutoLoadListWidget
import com.tencent.news.core.list.extension.FlexFeedsControllerEx.createDefaultPageVM
import com.tencent.news.core.list.extension.bindArticleListPos
import com.tencent.news.core.list.extension.bindArticleModulePos
import com.tencent.news.core.list.extension.bindArticleRealPos
import com.tencent.news.core.list.extension.bindArticleUUID
import com.tencent.news.core.list.extension.bindModArticleInfo
import com.tencent.news.core.list.extension.bindCtxDto
import com.tencent.news.core.list.extension.bindPageArticleInfo
import com.tencent.news.core.list.extension.bindRecTraceId
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.model.NewsFeedsSLO
import com.tencent.news.core.list.trace.FeedsLogHelper
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.StructListFooterState
import com.tencent.news.core.page.model.StructListHeaderState
import com.tencent.news.core.page.model.NewsListWidget
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.page.model.pickRefreshRequest
import com.tencent.news.core.page.model.toFeedsItemList
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.api.INetworkBuilder
import com.tencent.news.core.platform.api.INetworkResponse
import com.tencent.news.core.platform.api.LocalNetworkBuilder
import com.tencent.news.core.platform.api.appPageStack
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.platform.synchronized
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.core.tads.api.IAdFeedsController
import com.tencent.news.core.tads.constants.AdLoid
import kotlinx.coroutines.flow.MutableStateFlow

typealias StructNetworkResponse = (resp: INetworkResponse<*>, newPageWidget: StructPageWidget?) -> Unit

internal typealias FlexCtrl = FlexibleFeedsController

/**
 * ‘灵活列表’
 */
internal class FlexibleFeedsController(
    override val rootWidget: StructPageWidget2,
    internal val pageItem: (() -> IKmmFeedsItem?)? = null,
    private val adFeedsContext: IAdFeedsContext? = null,
) : IFlexibleFeedsController {

    init {
        adFeedsContext?.bindFeedDataProvider(this)

        rootWidget.bindStructPageVM { createDefaultPageVM() }
    }

    override var listRefreshListener: IFeedsRefreshListener? = null

    private val paramsHelper by lazy { FlexCtrlParamsHelper(this) }

    private val curAllDataLock = Lock()
    private val curAllData = mutableListOf<IKmmFeedsItem>()
    private val extraAllData = mutableListOf<IKmmFeedsItem>()

    // todo jiamin 当前不准确，注意使用！！后续要闻切换完，统一收拾
    @OptIn(RestrictedApi::class)
    override var contextProvider: IContextProvider? = object : IContextProvider {
        override fun getContext(): IKmmContext? {
            return appPageStack()?.getTopValidPage()
        }
    }

    private var _adCtrl: IAdFeedsController? = null
        get() = field ?: createAdCtrl().apply {
            field = this
            field?.contextProvider = this@FlexibleFeedsController.contextProvider
        }

    init {
        // 初始化时将rootWidget的现有数据赋值给curAllData
        synchronized(curAllDataLock) {
            curAllData.addAll(rootWidget.getFeedsList())
        }
    }

    private val globalProcessors by lazy {
        listOf(
            FrameworkServiceBridge.impl.createInvalidItemFilterProcessor(), // 非法item数据过滤
            ListFooterStateProcessor(this),     // footer状态更新
            ListHeaderStateProcessor(this),      // header状态更新
        )
    }

    private val defaultProcessors by lazy { mutableListOf<IFeedsDataProcessor>() }

    override val listPagingRecorder: IListPagingRecorder
        get() = paramsHelper.listPagingRecorder

    override val footerState = MutableStateFlow(StructListFooterState.WAITING_FOR_MORE)
    override val headerState = MutableStateFlow(StructListHeaderState.NO_MORE)
    override val isLocatingArticle = MutableStateFlow(false)

    // 触发列表刷新
    override fun doListRefresh(
        request: FeedsRefreshRequest,
        processor: IFeedsDataProcessor?
    ): INetworkBuilder<*>? {
        val requestEnv = createFeedsRequestEnv(request, processor)
        val result = createRequest(requestEnv)
        requestEnv.requestCreatedTime = getCurTimeMillis()
        return result
    }

    override fun appendDefaultProcessors(vararg processors: IFeedsDataProcessor) {
        processors.forEach {
            val sizeBefore = defaultProcessors.size
            defaultProcessors.addIfAbsent(it)
            val sizeAfter = defaultProcessors.size
            debugLog {
                val added = sizeAfter > sizeBefore
                "appendDefaultProcessors: processor=${it.hashCode()}, added=$added, totalDefaultProcessors=$sizeAfter, ctrl=${this.hashCode()}"
            }
        }
    }

    override fun removeDefaultProcessor(processor: IFeedsDataProcessor) {
        defaultProcessors.remove(processor)
    }

    override fun doCacheRefresh(request: FeedsRefreshRequest, processor: IFeedsDataProcessor) {
        super.doCacheRefresh(request, processor)
        val requestEnv = createFeedsRequestEnv(request, processor)
        debugLog {
            "doCacheRefresh: 使用缓存数据，ctrl=${this.hashCode()}, defaultProcessors.size=${defaultProcessors.size}, defaultProcessorIds=${defaultProcessors.map { it.hashCode() }}"
        }
        debugLog { "首刷使用缓存 data = ${rootWidget.getFeedsList()}" }
        // 使用 requestEnv.processor（包含 defaultProcessors 的 dispatcher）而非一次性 processor，
        // 确保 appendDefaultProcessors 注册的 processor（如 dataListChangedProcessor）也能被触发
        val feedsResult = FeedsItemListBuilder.buildItemListResult(
            requestEnv, rootWidget, rootWidget.getFeedsList(), getAdCtrl(), processor
        )
        requestEnv.processor.onProcessSucceed(requestEnv, rootWidget, feedsResult)
        notifyListDataChanged(ListRefreshReason.CACHE)
    }

    private fun createStructDataEnv(request: FeedsRefreshRequest): StructDataEnv {
        return StructDataEnv(
            refreshForward = request.refreshForward,
            refreshAction = request.refreshAction,
            refreshActionData = request.refreshActionData,

            channelInfo = pageConfig().defaultChannelInfo,
            pageItem = pageItem?.invoke(),
            anchorTabId = pageConfig().anchorTabId,
        ).apply {
            resetByTime = request.resetByTime
        }
    }

    private fun createProcessorReDispatcher(
        processor: IFeedsDataProcessor?
    ): FeedsDataProcessorReDispatcher {
        // processor支持多个，这里暂时不需要优先级控制，有需要后续再设计
        return FeedsDataProcessorReDispatcher(
            safeList(
                globalProcessors,               // 全局的默认 processor
                pageConfig().dataProcessors,    // 页面配置的通用 processor
                defaultProcessors,              // ctrl维度，动态注册的 processor
                listOf(processor),              // 本次请求，一次性的 processor（外部注入的放最后）
            )
        )
    }

    private fun createFeedsRequestEnv(
        request: FeedsRefreshRequest,
        processor: IFeedsDataProcessor?
    ): FeedsRequestEnv {
        val dataEnv = createStructDataEnv(request)
        val processorDispatcher = createProcessorReDispatcher(processor)
        return FeedsRequestEnv(dataEnv, request.commonParams, processorDispatcher)
    }

    private fun createRequest(requestEnv: FeedsRequestEnv): INetworkBuilder<*>? {
        val refreshForward = requestEnv.dataEnv.refreshForward
        val refreshAction = requestEnv.dataEnv.refreshAction
        val refreshActionData = requestEnv.dataEnv.refreshActionData

        // 模块展开
        if (refreshAction == ListRefreshAction.QUERY_EXPANSION) {
            val request = createBottomClickRequest(requestEnv, refreshActionData)
            if (request != null) {
                return request
            }
        }

        // 安全防护逻辑：如果刚创建的controller，没经过reset，
        // 则无论传入哪种刷新类型，都必须reset一下（否则会创建request失败）
        val finalRefreshForward = if (rootWidget.isMainContentEmpty()) {
            ListRefreshForward.RESET
        } else {
            refreshForward
        }

        val result = when (finalRefreshForward) {
            ListRefreshForward.RESET -> createResetRequest(requestEnv)              // 首刷（reset）
            ListRefreshForward.TOP_REFRESH -> createTopRefreshRequest(requestEnv)   // 顶刷（下拉刷新）
            ListRefreshForward.BOTTOM_REFRESH -> createBottomRefreshRequest(requestEnv) // 底刷（footer加载）
        }
        return result
    }

    private fun createAdCtrl(): IAdFeedsController? {
        val loid = getMajorAdLoid()
        if (loid == AdLoid.NONE) {
            return null
        }
        val adChannel = getAdChannelForNow()

        if (FrameworkServiceBridge.impl.isCloseAd(loid, adChannel)) {
            return null
        }

        return FrameworkServiceBridge.impl.createAdFeedsController(
            majorLoid = loid,
            adChannel = adChannel,
            adFeedsContext = adFeedsContext ?: FlexCtrlDefaultAdContext(this)
        )
    }

    private fun getAdChannelForNow(): String {
        return FrameworkServiceBridge.impl.exchangeAdRequestChannel(
            getMajorAdLoid(),
            pageItem?.invoke(),
            getDefaultChannelInfo()
        )
    }

    override fun getAdCtrl(): IAdFeedsController? = _adCtrl

    override fun checkAdCtrlMayReCreate() {
        val adCtrl = _adCtrl ?: return
        val curAdChannel = adCtrl.getAdScene().adChannel
        val targetAdChannel = getAdChannelForNow()
        if (curAdChannel != targetAdChannel) {
            _adCtrl = createAdCtrl()
        }
    }

    override fun onListScroll(isIdle: Boolean, lastVisibleItem: IKmmFeedsItem?, dx: Int, dy: Int) {
        getAdCtrl()?.onListScroll(isIdle, lastVisibleItem, dx, dy)
    }

    override fun onListShow(lastVisibleItem: IKmmFeedsItem) {
        getAdCtrl()?.onListShow(lastVisibleItem)
    }

    override fun onPageSelect(selectedItem: IKmmFeedsItem) {
        getAdCtrl()?.onPageSelect(selectedItem)
    }

    override fun onListAttach() {
        getAdCtrl()?.onListAttach()
    }

    override fun onListDetach() {
        getAdCtrl()?.onListDetach()
    }

    override fun insertFeedsItem(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Boolean {
        synchronized(curAllDataLock) {
            val insertIndex = curAllData.indexOfFirst(cursor)
            if (insertIndex >= 0) {
                curAllData.addAll(insertIndex, newData)
                notifyListDataChanged(ListRefreshReason.INSERT)
            }
        }
        return rootWidget.insertFeedsItem(newData, cursor)
    }

    override fun insertFeedsItemAfter(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Boolean {
        synchronized(curAllDataLock) {
            val insertIndex = curAllData.indexOfFirst(cursor)
            if (insertIndex >= 0) {
                // 横屏回插可直接锚定进入全屏时的内容item，避免调用方取展示列表下一个item命中广告。
                curAllData.addAll(insertIndex + 1, newData)
                notifyListDataChanged(ListRefreshReason.INSERT)
            }
        }
        return rootWidget.insertFeedsItemAfter(newData, cursor)
    }

    //  会追加到最后一个listWidget里
    override fun appendFeedsItem(newData: List<IKmmFeedsItem>): Boolean {
        synchronized(curAllDataLock) {
            curAllData.addAll(newData)
            notifyListDataChanged(ListRefreshReason.INSERT)
        }
        return rootWidget.appendFeedsItem(newData)
    }

    override fun removeFeedsItem(cursor: ItemCursor): List<IKmmFeedsItem> {
        synchronized(curAllDataLock) {
            val removed = curAllData.removeAll(cursor)
            if (removed) {
                notifyListDataChanged(ListRefreshReason.REMOVE)
            }
        }

        return rootWidget.removeFeedsItem(cursor)
    }

    override fun replaceFeedsItem(
        newData: List<IKmmFeedsItem>,
        cursor: ItemCursor,
    ): IKmmFeedsItem? {
        synchronized(curAllDataLock) {
            val oldItem = curAllData.find(cursor)
            if (oldItem != null) {
                curAllData.safeReplaceList(oldItem, newData)
                notifyListDataChanged(ListRefreshReason.REPLACE)
            }
        }

        return rootWidget.replaceFeedsItem(newData, cursor)
    }

    private fun notifyListDataChanged(reason: ListRefreshReason) {
        listRefreshListener?.onListRefresh(reason, curAllData)

        getAdCtrl()?.onListDataChanged(reason, curAllData)
    }

    override fun findFeedsItem(cursor: ItemCursor) = rootWidget.findFeedsItem(cursor)

    override fun getAllFeedsItemList() = curAllData

    private fun getMajorAdLoid(): Int =
        adFeedsContext?.getExchangeMajorLoid() ?: dataRepo().getMajorAdLoid()

    private fun createResetRequest(requestEnv: FeedsRequestEnv): INetworkBuilder<*> {
        val pageRequest = createResetDataRequest(requestEnv)

        return pageRequest.buildStructNetworkBuilder(requestEnv) { resp, newPageWidget ->
            if (checkFeedsResultSuccess(resp, newPageWidget)) {
                paramsHelper.resetPagingParams()
                paramsHelper.increasePagingParams(newPageWidget)
                extraAllData.clearAndAddAll(newPageWidget?.getExtraList())
            }

            processFeedsItemList(resp, requestEnv, newPageWidget) {
                rootWidget.replaceAllWidgets(newPageWidget)
            }
        }
    }

    private fun createTopRefreshRequest(requestEnv: FeedsRequestEnv): INetworkBuilder<*>? {
        // 分区列表，顶部自动加载（付费专栏有该场景）
        val topMoreListWidget = findTopAutoLoadListWidget()
        val topMoreRequest = topMoreListWidget?.pickAutoTopMoreRequest()
        if (topMoreRequest != null) {
            return topMoreRequest.buildStructNetworkBuilder(requestEnv) { resp, newPageWidget ->
                if (checkFeedsResultSuccess(resp, newPageWidget)) {
                    paramsHelper.increasePagingParams(newPageWidget)
                    topMoreListWidget.removeRequest(topMoreRequest) // 请求成功后移除该请求
                    extraAllData.safeAddAll(newPageWidget?.getExtraList())
                }
                processFeedsItemList(resp, requestEnv, newPageWidget) {
                    topMoreListWidget.topInsertNewWidgetData(it)
                }
            }
        }

        // 常规下拉刷新
        val refreshRequest = rootWidget.pager?.mainChannel.pickRefreshRequest()
        if (refreshRequest != null) {
            return refreshRequest.buildStructNetworkBuilder(requestEnv) { resp, newPageWidget ->
                if (checkFeedsResultSuccess(resp, newPageWidget)) {
                    if (ifResetPageNumWhenPullDown()) {
                        paramsHelper.resetPagingParams()
                    }
                    paramsHelper.increasePagingParams(newPageWidget)
                    extraAllData.safeAddAll(newPageWidget?.getExtraList())
                }
                processFeedsItemList(resp, requestEnv, newPageWidget) {
                    // 下拉刷新时，清理掉所有组件列表，完全替换为新的
                    rootWidget.localFixTopList = emptyList()
                    rootWidget.replaceMainContentWidgets(it)
                }
            }
        }

        errorLog("顶部刷新【失败】，没有找到 topMore 或 request 行为")
        return null
    }

    private fun createBottomRefreshRequest(requestEnv: FeedsRequestEnv): INetworkBuilder<*>? {
        val newsListWidget = findAutoLoadListWidget()
        val isCloudReplace = requestEnv.getRefreshAction().isCloudReplaceAction()

        val lazyInitRequest = newsListWidget?.pickLazyInitRequest()
        if (lazyInitRequest != null) {
            return lazyInitRequest.buildStructNetworkBuilder(requestEnv) { resp, newPageWidget ->
                if (isCloudReplace) {
                    processFeedsItemList(resp, requestEnv, newPageWidget) {
                        it.getMainContentFeedsItemWidgets().toFeedsItemList()
                    }
                    return@buildStructNetworkBuilder
                }
                if (checkFeedsResultSuccess(resp, newPageWidget)) {
                    paramsHelper.increasePagingParams(newPageWidget)
                    newsListWidget.removeRequest(lazyInitRequest) // 请求成功后移除该请求
                    extraAllData.safeAddAll(newPageWidget?.getExtraList())
                }
                processFeedsItemList(resp, requestEnv, newPageWidget) {
                    newsListWidget.appendNewWidgetData(it)
                }
            }
        }

        val loadMoreRequest = newsListWidget?.pickAutoLoadMoreRequest()
        if (loadMoreRequest != null) {
            return loadMoreRequest.buildStructNetworkBuilder(requestEnv) { resp, newPageWidget ->
                if (isCloudReplace) {
                    processFeedsItemList(resp, requestEnv, newPageWidget) {
                        it.getMainContentFeedsItemWidgets().toFeedsItemList()
                    }
                    return@buildStructNetworkBuilder
                }
                if (checkFeedsResultSuccess(resp, newPageWidget)) {
                    paramsHelper.increasePagingParams(newPageWidget)
                    newsListWidget.removeRequest(loadMoreRequest) // 请求成功后移除该请求
                    extraAllData.safeAddAll(newPageWidget?.getExtraList())
                }
                processFeedsItemList(resp, requestEnv, newPageWidget) {
                    newsListWidget.appendNewWidgetData(it)
                }
            }
        }

        errorLog("底部刷新【失败】，没有找到 lazyInit 或 loadMore 行为")
        return null
    }

    private fun createBottomClickRequest(
        requestEnv: FeedsRequestEnv,
        refreshActionData: ListRefreshActionData?,
    ): INetworkBuilder<*>? {
        val newsListWidget = findClickLoadListWidget(refreshActionData?.indexKey)

        val loadMoreRequest = newsListWidget?.pickClickRequest()
        if (loadMoreRequest != null) {
            return loadMoreRequest.buildStructNetworkBuilder(requestEnv) { resp, newPageWidget ->
                if (checkFeedsResultSuccess(resp, newPageWidget)) {
                    paramsHelper.increasePagingParams(newPageWidget)
                    newsListWidget.removeRequest(loadMoreRequest) // 请求成功后移除该请求
                }
                processFeedsItemList(resp, requestEnv, newPageWidget) {
                    newsListWidget.appendNewWidgetData(it)
                }
            }
        }

        errorLog("底部展开【失败】，没有找到 lazyInit 或 loadMore 行为")
        return null
    }

    private fun checkFeedsResultSuccess(
        resp: INetworkResponse<*>,
        newPageWidget: StructPageWidget?,
    ): Boolean {
        if (!resp.result.succeed) {
            return false
        }
        return pageConfig().dataInvalidator?.isDataValid(newPageWidget)
            ?: newPageWidget?.isFeedsDataValid().isTrue()
    }

    private fun DataRequest.buildStructNetworkBuilder(
        requestEnv: FeedsRequestEnv,
        onResponse: StructNetworkResponse,
    ): INetworkBuilder<*> {
        val dataRequest = this

        val finalResponse = hookOnResponse(onResponse)

        requestEnv.processor.onBeforeRequest(requestEnv, dataRequest)

        if (dataRequest.isValidLocalRequest()) {
            val result = LocalNetworkBuilder(
                localData = {
                    StructPageWidget().apply {
                        buildPageWithContent(
                            channel = ChannelWidget.createDefenseMainChannelWidget(),
                            content = dataRequest.local_data?.widget_list
                        )
                    }
                },
                onResponse = finalResponse
            )

            requestEnv.networkBuilder = result
            return result
        } else {
            // 首刷不需要请求接口，直接本地构造的情况：
            if (requestEnv.getRefreshForward() == ListRefreshForward.RESET) {
                val localPageWidget = dataRepo().createLocalResetPageWidget(requestEnv.dataEnv)
                if (localPageWidget != null) {
                    val result = LocalNetworkBuilder(
                        localData = { localPageWidget },
                        onResponse = finalResponse
                    )
                    requestEnv.networkBuilder = result
                    return result
                }
            }

            // 首刷正常发起网络请求：
            val request = buildCommonNetworkBuilder(paramsHelper, requestEnv, dataRequest)

            request.onResponse = { resp ->
                requestEnv.networkResponseTime = getCurTimeMillis()
                if (resp.isValid()) {
                    finalResponse.invoke(resp, resp.parserResult as? StructPageWidget)
                } else {
                    finalResponse.invoke(resp, null)
                }
            }

            // todo genesisli dev: 网络请求性能监控，channelModel.isEnableReusePreloadCache

            val logStr = listOf(
                "forward=${requestEnv.dataEnv.refreshForward}",
                "action=${requestEnv.dataEnv.refreshAction}",
                "url=${request.buildRequestLog()}"
            ).joinToString()
            fileLog("[Flex]发起列表刷新：${logStr}")

            requestEnv.networkBuilder = request
            return request
        }
    }

    private fun hookOnResponse(onResponse: StructNetworkResponse): StructNetworkResponse {
        return { resp, newPageWidget ->
            newPageWidget?.reBindRootWidget(rootWidget) // 这里重绑root是个最终兜底，这个时机晚于Processor处理

            onResponse.invoke(resp, newPageWidget)
        }
    }

    private fun processFeedsItemList(
        // 对应宿主以前的：StructPageNewsCache.processResult
        resp: INetworkResponse<*>,
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget?,
        newDataBuilder: (StructPageWidget) -> List<IKmmFeedsItem>?,
    ) {
        requestEnv.networkResponse = resp

        val processor = requestEnv.processor

        // todo genesisli dev 处理专题底层关闭广告，上报dp3 907，909

        if (checkFeedsResultSuccess(resp, newPageWidget) && newPageWidget != null) {

            FeedsLogHelper.printWidgetLog(getLogKey(), newPageWidget)

            // 后面很多数据处理可能依赖页面data数据，提前赋值一下
            if (requestEnv.getRefreshForward() == ListRefreshForward.RESET) {
                rootWidget.mergeWidgetData(newPageWidget)
            }

            // todo genesisli dev 派发 onBeforeServerResponse
            processor.onBeforeProcess(requestEnv, newPageWidget)

            newPageWidget.getMainContentListWidgets().forEach { widget ->
                processor.onPreProcessNewListWidget(requestEnv, widget)
            }

            // todo genesisli dev 宿主的extraList逻辑：
            //  channelModel.setExtraData(KEY_EXTRA_LIST, result?.getExtraList())

            val buildItemListStart = getCurTimeMillis()
            val originNewData = newDataBuilder(newPageWidget)
            val resultData = FeedsItemListBuilder.buildItemListResult(
                requestEnv, rootWidget, originNewData, getAdCtrl(), processor
            )

            bindCtxDtoData(resultData.allData, resultData.newData, requestEnv)

            requestEnv.buildItemListCost = getCurTimeMillis() - buildItemListStart
            synchronized(curAllDataLock) {
                curAllData.clearAndAddAll(resultData.allData)

                // 列表item也可以查询pageWidget（用于cell与页面互通信）
                FlexLogicContextHelper.bindPageWidget(curAllData, rootWidget)
            }

            val processCallbackStart = getCurTimeMillis()
            // todo genesisli dev 专题页 isCommentPage 相关处理（应该收敛到 dataRepo里？）
            processor.onAfterProcess(requestEnv, newPageWidget, resultData)

            // todo genesisli dev 绑定各种 ctxDto
            //  - isResetData 标记
            //  - cacheQueryType
            //  - dataInjectPageSkinMark
            //  - dataInjectCubeCommentTask

            // todo genesisli dev 派发 diapatchGlobalOnAfterServerResponse

            // todo genesisli dev 记录reset时间：ChannelResetHelper.saveChannelRefreshTime

            // todo genesisli dev 绑定articlePage bindArticlePage

            // todo genesisli dev 绑定网络traceId：ListContextInfoBinder.bindTraceId

            val feedsLog = FeedsLogHelper.buildItemLogStr(resultData.newData)

            if (resultData.newData.isNotEmpty()) {
                fileLog("[Flex]列表数据【刷新成功】：\n${feedsLog}")
                processor.onProcessSucceed(requestEnv, newPageWidget, resultData)
                notifyListDataChanged(requestEnv.getListRefreshReason())
            } else {
                val parseError = newPageWidget.parseError
                val retCode = newPageWidget.retCode

                if (parseError != null) {
                    errorLog("[Flex]列表数据【刷新失败】：发生解析异常", parseError)
                    val errorResult = ResultEx(
                        succeed = false,
                        msg = "解析异常",
                        error = parseError,
                        errorCode = retCode
                    )
                    processor.onProcessError(requestEnv, errorResult)
                } else if (dataRepo().checkRet() == true && retCode != 0) {
                    errorLog("[Flex]列表数据【刷新失败】：发生后台内部错误，ret=$retCode")
                    processor.onProcessError(requestEnv, resp.result.copy(errorCode = retCode))
                } else {
                    val errorForEmpty = getShiplySwitch("list_error_for_hasmore_empty", true)
                    if (errorForEmpty && newPageWidget.hasMore()) {
                        errorLog("[Flex]列表数据【刷新失败】：新一刷数据为空 或 全被排重 $feedsLog")
                        processor.onProcessError(
                            requestEnv,
                            resp.result.copy(errorCode = ResultCodeEx.LIST_EMPTY)
                        )
                    } else {
                        fileLog("[Flex]列表数据【刷新成功】：列表已刷空")
                        processor.onProcessSucceed(requestEnv, newPageWidget, resultData)
                        notifyListDataChanged(requestEnv.getListRefreshReason())
                    }
                }
            }

            requestEnv.processCallbackCost = getCurTimeMillis() - processCallbackStart

            NewsFeedsSLO.debugLog { "[Flex]列表(${dataRepo().getLogKey()})耗时：${requestEnv.logStr4SLO()}" }
        } else {
            val logStr = "widget=${newPageWidget}, ${resp.result.getLogStr()}"
            errorLog("[Flex]列表数据【刷新失败】：${logStr}", resp.result.error)

            processor.onProcessError(requestEnv, resp.result)
        }
    }

    private fun bindCtxDtoData(allData: List<IKmmFeedsItem>, newsData: List<IKmmFeedsItem>, requestEnv: FeedsRequestEnv) {
        val channelEnv = getDefaultChannelInfo().env

        val pageItem = channelEnv.pageItem
        if (pageItem != null) {
            bindPageArticleInfo(allData, pageItem)
        }

        allData.bindCtxDto { newsChannel = channelEnv.newsChannel }

        if (!requestEnv.getRefreshAction().isCloudReplaceAction()) {
            // currentRequestPage需要从0开始，这里-1保证正确
            paramsHelper.bindArticlePage(newsData)
            paramsHelper.bindCloudRerankArticlePage(newsData)
            bindArticleRealPos(newsData)
            bindArticleListPos(allData)
            bindRecTraceId(newsData, requestEnv.networkResponse?.headers?.get("traceid"))
        }
        bindArticleModulePos(newsData)
        bindArticleUUID(newsData, channelEnv.newsChannel)

        allData.bindCtxDto { pageBusinessType = rootWidget.data?.business_type }

        bindModArticleInfo(allData)

        // 交由广告处理，且时序有要求，必须业务侧先绑定
        getAdCtrl()?.bindCtxDtoData(allData, newsData)
    }

    /**
     * 下拉刷新是否重置页数
     */
    private fun ifResetPageNumWhenPullDown(): Boolean { // todo genesisli dev 主编精选模式不重置
        return false // channelModel.getEditorSelectionMode().isNotNullOrBlank()
    }

    override fun getAllList(): List<IListItem> = curAllData

    override fun getExtraList(): List<IListItem> = extraAllData

    override fun setListLayoutType(layoutType: Int) {
        getAdCtrl()?.setListLayoutType(layoutType)
    }

}
