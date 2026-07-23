@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.news.core.compose.scaffold.BuildStateViewOrElse
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.StructPage
import com.tencent.news.core.compose.scaffold.StructPageLoadingView
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.registry.LocalErrorImagePainterProvider
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.skin.rememberThemeChangeListener
import com.tencent.news.core.compose.scaffold.vm.StructPageViewModel
import com.tencent.news.core.compose.trace.ComposePageTrace
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.api.IStructDataLocalRepo
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.StructPageData
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.page.model.StructPageWidgetCache
import com.tencent.news.core.service.FrameworkService
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

/**
 * 控制 StructComposePage 是否延迟渲染内容区域。
 *
 * 默认值为 false（正常渲染）。
 * 当外层弹窗需要先完成弹出动画、再渲染内容时，可通过 CompositionLocalProvider 注入 true，
 * 动画完成后再切换为 false，避免数据加载与动画同帧竞争主线程导致卡顿。
 *
 * 使用示例：
 * ```kotlin
 * var delayRender by remember { mutableStateOf(true) }
 * LaunchedEffect(Unit) {
 *     delay(400)
 *     delayRender = false
 * }
 * CompositionLocalProvider(LocalDelayRender provides delayRender) {
 *     StructComposePage(...)
 * }
 * ```
 */
val LocalDelayRender: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }

// 结构化 页面、频道，都可以用这个
@Composable
fun StructComposePage(
    pageWidget: LazyImpl<StructPageWidget2>,                // 【重要】页面pageWidget，核心数据逻辑都在这里
    pageFlow: SharedFlow<PageLifecycleEvent>,               // 页面生命周期flow（resume-pause这些）
    uiCustomize: StructPageUICustomize? = null,             // 可以自定义一些ui的modifier（例如页面背景）
    pageCallbacks: IStructComposePageCallbacks? = null,     // 页面UI生命周期的回调
    dataSource: IStructComposeDataSource? = null,           // 目前主要用于widget缓存复用，后面考虑简化下
    key: Any? = null
) {
    val rootWidget = remember(key) { createOrGetCachePageWidget(dataSource, pageWidget) }
    val pageScope = rememberCoroutineScope()
    StructComposePage4VM(
        pageViewModel = { createPageViewModel(rootWidget, pageFlow, pageScope) },
        pageCallbacks = pageCallbacks,
        uiCustomize = uiCustomize,
        key = key
    )
}

@Composable
fun StructComposePage4VM(
    pageViewModel: LazyImpl<IStructPageViewModel>,          // 【重要】页面pageVM，核心数据逻辑都在这里
    uiCustomize: StructPageUICustomize? = null,             // 可以自定义一些ui的modifier（例如页面背景）
    pageCallbacks: IStructComposePageCallbacks? = null,
    forceDarkTheme: Boolean = false,                        // 是否强制使用夜间模式
    key: Any? = null
) {
    ComposePageTrace.record("onPageCreate")

    val viewModel = remember(key) {
        val vm = pageViewModel()
        pageCallbacks?.onPageViewModelCreated(vm)

        // 优化 localDataRepo 加载速度：创建vm后直接请求，不要等state刷新
        ComposePageTrace.record("startLoadPageData")
        loadPageData(vm.pageRootWidget, vm)

        vm
    }

    // 监听主题变化，触发 ViewModel 的 onThemeChanged 回调
    rememberThemeChangeListener { isDarkTheme ->
        viewModel.onThemeChanged(isDarkTheme)
    }

    // 改了时机，影响范围比较大，暂留一阵
//    LaunchedEffect(viewModel) {
//        ComposePageTrace.record("startLoadPageData")
//        loadPageData(viewModel.pageRootWidget, viewModel)
//    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onPageDisposed()
            // 页面销毁时，解绑 widget 上的 vm，防止内存泄漏（widget可能被全局缓存持有）
            viewModel.pageRootWidget.unbindStructPageVM()
        }
    }

    CompositionLocalProvider(
        LocalStructPageViewModel provides viewModel,
        LocalErrorImagePainterProvider provides uiCustomize?.errorImagePainterProvider
    ) {

        val loadingState = viewModel.loadingStateFlow.collectAsState()
        val uiState by remember { loadingState }

        StructPageDtReportContainer(
            pageWidget = viewModel.pageRootWidget,
            uiState = uiState
        ) {
            val errorImagePainter = uiCustomize?.errorImagePainterProvider?.invoke()
            StructPage<StructPageData>(
                uiState = uiState,
                loadStateWidget = viewModel.pageRootWidget,
                onRetryClick = { viewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET)) },
                forceDarkTheme = forceDarkTheme,
                errorImagePainter = errorImagePainter,
            ) { pageData ->
                // 返回true表示拦截展示UI（例如：专题不支持的 business_type 做降级）
                val interceptShowing = pageCallbacks?.onBeforeShowMainContent(pageData) ?: false
                val delayRender = LocalDelayRender.current
                if (!interceptShowing && !delayRender) {
                    StructComposeView(
                        uiCustomize = uiCustomize,
                        pageWidget = pageData.pageWidget,
                        feedsResult = pageData.feedsResult
                    )

                    viewModel.onAfterShowMainContent()
                    pageCallbacks?.onAfterShowMainContent(viewModel)
                } else {
                    viewModel.pageRootWidget.BuildStateViewOrElse({ loading }) {
                        StructPageLoadingView()
                    }
                }
            }
        }

    }

}

// 加载首屏数据
private fun loadPageData(rootWidget: StructPageWidget2, viewModel: IStructPageViewModel) {
    if (rootWidget.pageConfig.dataRepo is IStructDataLocalRepo) {
        viewModel.refresh(
            FeedsRefreshRequest(ListRefreshForward.RESET, ListRefreshAction.CACHE_AFTER_RESET)
        )
        return
    }
    if (StructPageWidgetCache.canUseCacheData(rootWidget)) {
        // 命中缓存时做一些事，比如上报
        rootWidget.pageConfig.cacheConfig?.onHitCache()
        viewModel.refresh(
            FeedsRefreshRequest(ListRefreshForward.RESET, ListRefreshAction.AUTO_CACHE)
        )
        // 取完cache 立即reset刷新更新数据，最多闪一下 可以接受
        viewModel.refresh(
            FeedsRefreshRequest(ListRefreshForward.RESET, ListRefreshAction.CACHE_AFTER_RESET)
        )
    } else {
        rootWidget.pageConfig.cacheConfig?.onNotHitCache()
        viewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET))
    }
}

// 优先查找缓存的pageWidget（如果页面打开了缓存复用），否则创建一个新的
private fun createOrGetCachePageWidget(
    dataSource: IStructComposeDataSource?,
    pageWidget: LazyImpl<StructPageWidget2>
): StructPageWidget2 {
    val cacheKey = dataSource?.getCacheKey()
    if (cacheKey.isNullOrEmpty()) {
        return pageWidget() // 不支持缓存
    }

    val cachedPageWidget = StructPageWidgetCache.getPageWidget(cacheKey)
    if (cachedPageWidget != null) {
        // 已缓存的pageWidget，更新pageItem信息
        // todo genesisli opt: 后面给pageWidget设计一个 onUpdatePageArgs 方法
        cachedPageWidget.pageConfig.defaultChannelInfo.env.pageItem = dataSource.getPageItem()
    }

    return StructPageWidgetCache.getOrCreatePageWidget(cacheKey) { pageWidget() }
}

// 创建页面viewModel
private fun createPageViewModel(
    rootWidget: StructPageWidget2,
    pageFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope
): StructPageViewModel = StructPageViewModel(
    controller = FrameworkService.createFlexFeedsController(
        rootWidget = rootWidget,
        pageItem = { rootWidget.findPageItem() }
    ),
    pageFlow = pageFlow,
    pageScope = pageScope
)