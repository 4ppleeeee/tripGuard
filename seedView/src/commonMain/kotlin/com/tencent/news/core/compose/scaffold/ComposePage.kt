package com.tencent.news.core.compose.scaffold

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.gestures.detectDragGestures
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.platform.GlobalTapManager
import com.tencent.kuikly.compose_dsl.kuikly.platform.TapEvent
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.platform.NTComposePageArgsPool
import com.tencent.news.core.compose.platform.emptyPageArgs
import com.tencent.news.core.compose.platform.parsePageArgs
import com.tencent.news.core.compose.debug.PageVisitReportDebugOverlay
import com.tencent.news.core.compose.scaffold.registry.LocalBubbleViewController
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageNewIntentFlow
import com.tencent.news.core.compose.scaffold.registry.LocalComposeSafeAreaInsetsFlow
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.compose.scaffold.registry.LocalFullScreenController
import com.tencent.news.core.compose.scaffold.registry.LocalScreenshot
import com.tencent.news.core.compose.scaffold.registry.LocalToastController
import com.tencent.news.core.compose.scaffold.theme.QNAppTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnScreenshot
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.ScreenshotState
import com.tencent.news.core.compose.view.bubble.BubbleViewController
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.compose.view.extension.preciseClickable
import com.tencent.news.core.compose.view.fullscreen.FullScreenController
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.api.appRouter
import com.tencent.news.core.platform.api.appReport
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.api.isRdm
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.platform.qnLogcat
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import com.tencent.news.core.util.lifecycle.PageLifecycleFlow
import com.tencent.news.core.util.lifecycle.PageNewIntentFlow
import com.tencent.news.core.util.lifecycle.PageSafeAreaInsetsFlow
import com.tencent.news.core.view.lifecycle.ComposeEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private const val DEVELOPER_DEBUG_PAGE = "page/developer/debug"
private const val PAGE_ARGS_KEY = "pageArgs"
private val DEBUG_FLOATING_INITIAL_START_OFFSET = 12.dp
private val DEBUG_FLOATING_SHAPE = RoundedCornerShape(18.dp)
private val DEBUG_FLOATING_BACKGROUND = Color(0xCC1F1F23)

/**
 * 安全区域配置
 * @param backgroundColor 安全区域背景色，默认为透明
 */
data class SafeAreaConfig(
    val lightColor: Color = Color.Transparent,
    val darkColor: Color = Color.Transparent
)

abstract class ComposePage : ComposeContainer() {

    var startTime = -1L

    @KmmInternalApi
    protected val pageArgs: MutableStateFlow<IComposePageArgs?> = MutableStateFlow(null)

    // 创建页面生命周期流
    protected val pageLifecycleFlow = PageLifecycleFlow()

    protected val pageFlow get() = pageLifecycleFlow.lifecycleFlow

    // 创建页面 NewIntent 事件流
    protected val pageNewIntentFlow = PageNewIntentFlow()

    // 创建页面安全区刷新事件流
    private val pageSafeAreaInsetsFlow = PageSafeAreaInsetsFlow()

    private val tapListener: (TapEvent) -> Unit = {
        if (isHarmonyPlatform()) {
            if (it.nativeView.getViewEvent().handlerWithEventName("click") == null) {
                registerTapListener(it)
            }
        } else {
            registerTapListener(it)
        }
    }

    fun registerTapListener(event: TapEvent) {
        getModule<PerformanceModule>("PerformanceModule")?.firePreClick(event)
    }

    // 是否需要截屏（不需要的话，能精简页面布局）
    open fun needScreenShot(): Boolean = true

    // 是否禁止iOS旋转重建页面
    open fun isForbidIOSRotationReBuildPage(): Boolean = false

    open fun enableGlobalDebugFloatingEntry(): Boolean = true

    // 禁用kuikly内置字体缩放能力
    override fun scaleFontSizeEnable(): Boolean = false

    override fun createExternalModules(): Map<String, Module>? {
        return mapOf(
            NewsComposeModule.Performance.moduleName to PerformanceModule(),
            NewsComposeModule.BusinessModule.moduleName to BusinessModule()
        )
    }

    /**
     * 在setContentView之前调用，目前用于性能统计
     */
    private fun beforeSetContentView() {
        // 增加获取当前时间戳，用于性能统计
        startTime = getCurTimeMillis()
        qnLogcat()?.logI("test-time", "startTime: $startTime")
    }

    /**
     * 场景名称
     */
    open fun sceneName(): String = "base_scene"

    /**
     * 是否开启性能统计 默认关闭
     */
    open fun enableReportPerformance(): Boolean {
        return sceneName() != "base_scene"
    }

    final override fun willInit() {
        super.willInit()
        setContentCompat {
            beforeSetContentView()
            OnSetContent()
        }
    }

    /**
     * 业务UI
     */
    @Composable
    abstract fun OnSetContent()

    // todo genesisli opt: 等 StructComposePage 下沉qnView后，再加这个工具
    @Composable
    protected inline fun <reified T : IComposePageArgs> SetContentWithArgs(
        content: @Composable (pageEnv: StructPageEnv<T>) -> Unit
    ) {
        val pageEnv = rememberedPageEnv<T>()
        if (pageEnv == null) {
            if (isDebug()) {
                QnText(
                    text = "页面加载失败",
                    color = QNTheme.colorScheme.redNormal,
                    fontSize = 14.sp
                )
            }
            return
        }
        content(pageEnv)
    }

    /**
     * 获取页面参数
     */
    @OptIn(KmmInternalApi::class)
    @Composable
    protected inline fun <reified T : IComposePageArgs> rememberedPageArgs(): T? {
        // 仅在 StateFlow 中没有有效值时才初始化（首次进入页面）
        // 后续 onNewIntent 更新 pageArgs 后，StateFlow 中已有新值，不应被旧值覆盖
        if (this.pageArgs.value == null) {
            val initialArgs = remember(pageData) { pageData.parsePageArgs<T>() }
            this.pageArgs.update { initialArgs }
        }
        val ret by this.pageArgs.collectAsState()
        return ret as? T
    }

    @Composable
    protected inline fun <reified T : IComposePageArgs> rememberedPageEnv(): StructPageEnv<T>? {
        val pageArgs = rememberedPageArgs<T>() ?: return null

        return StructPageEnv(
            pageArgs = pageArgs,
            pageFlow = pageLifecycleFlow.lifecycleFlow,
            pageScope = rememberCoroutineScope()
        )
    }

    /**
     * 页面首帧渲染完成，用于性能统计
     */
    fun onPageFirstFrameRendered() {
        if (enableReportPerformance() && startTime > -1L) {
            val endTime = getCurTimeMillis()
            val delta = endTime - startTime
            qnLogcat()?.logI("test-time", "delta: $delta")
            appReport().reportBeacon(
                "PAGE_FIRST_FRAME_FROM_CONTENT",
                mapOf(
                    "totalTime" to "$delta",
                    "biz_scene" to "${sceneName()}"
                )
            )
        }
        getModule<PerformanceModule>("PerformanceModule")?.onPageFirstFrameRendered()
    }

    @OptIn(KmmInternalApi::class)
    @CallSuper
    override fun onReceivePagerEvent(pagerEvent: String, eventData: JSONObject) {

        // 过滤掉配置变化事件，让字号缩放随density变化
        if (pagerEvent != PAGER_EVENT_CONFIGURATION_DID_CHANGED) {
            super.onReceivePagerEvent(pagerEvent, eventData)
        }

        ComposeViewLog.debug {
            "${this::class.simpleName}[${pageArgs.value?.identifier}] 收到事件 $pagerEvent ：$eventData"
        }
        when (pagerEvent) {
            ComposeEvent.OnPageNewIntent.eventName -> handlePageNewIntent(eventData)
            ComposeEvent.SafeArea.OnInsetsChanged.eventName -> handleSafeAreaInsetsChanged(eventData)
        }
    }

    @OptIn(KmmInternalApi::class)
    private fun handlePageNewIntent(eventData: JSONObject) {
        val eventMap = eventData.toMap()
        val hasPageArgs = eventMap.containsKey(NTComposePageArgsPool.PAGE_DATA_KEY) ||
                eventMap.containsKey(PAGE_ARGS_KEY)

        if (hasPageArgs) {
            parsePageArgs<IComposePageArgs>(eventData)?.let { newPageArgs ->
                this.pageArgs.update { newPageArgs }
                onPageNewIntent(newPageArgs)
            }
        }
        ComposeViewLog.debug { "${this::class.simpleName} onPageNewIntent data:$eventData" }
        pageNewIntentFlow.tryEmitNewIntent(eventMap)
    }

    private fun handleSafeAreaInsetsChanged(eventData: JSONObject) {
        ComposeViewLog.debug { "${this::class.simpleName} onSafeAreaInsetsChanged data:$eventData" }
        pageSafeAreaInsetsFlow.tryEmitSafeAreaInsets(eventData.toMap())
    }

    protected open fun onPageNewIntent(pageArgs: IComposePageArgs) {
    }

    override fun created() {
        super.created()
        pageLifecycleFlow.tryEmitEvent(PageLifecycleEvent.ON_CREATE)
        GlobalTapManager.enableTouchSlopForTap = true
        GlobalTapManager.addTapEventListener(tapListener)
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        pageLifecycleFlow.tryEmitEvent(PageLifecycleEvent.ON_RESUME)
    }

    override fun pageDidDisappear() {
        pageLifecycleFlow.tryEmitEvent(PageLifecycleEvent.ON_PAUSE)
        super.pageDidDisappear()
    }

    override fun pageWillDestroy() {
        pageLifecycleFlow.tryEmitEvent(PageLifecycleEvent.ON_DESTROY)
        super.pageWillDestroy()
        GlobalTapManager.removeTapEventListener(tapListener)
    }

    private fun setContentCompat(content: @Composable BoxScope.() -> Unit) {
        setContent {
            QNAppTheme { // 容器是个fillMaxSize的Box
                val bubbleViewController = remember { BubbleViewController() }
                val dialogController = remember { DialogController() }
                val fullScreenController = remember { FullScreenController() }
                val screenshotState = remember { ScreenshotState() }

                CompositionLocalProvider(
                    LocalBubbleViewController provides bubbleViewController,
                    LocalDialogController provides dialogController,
                    LocalToastController provides dialogController,
                    LocalFullScreenController provides fullScreenController,
                    LocalScreenshot provides screenshotState,
                    LocalComposePageLifecycleFlow provides pageLifecycleFlow.lifecycleFlow,
                    LocalComposePageNewIntentFlow provides pageNewIntentFlow.newIntentFlow,
                    LocalComposeSafeAreaInsetsFlow provides pageSafeAreaInsetsFlow.safeAreaInsetsFlow,
                ) {
                    if (needScreenShot()) {
                        // 截图只对内容生效，将dialog和气泡排除在外
                        QnScreenshot(modifier = Modifier.fillMaxSize(), screenshotState) {
                            // 截屏这个行为是个Column，得套一层Box
                            Box(modifier = Modifier.fillMaxSize()) {
                                content()
                            }
                        }
                    } else {
                        content()
                    }

                    PageVisitReportDebugOverlay()
                    if (enableGlobalDebugFloatingEntry()) {
                        GlobalDebugFloatingEntry()
                    }
                    fullScreenController.CollectFullScreenState()
                    bubbleViewController.CollectBubbleState()
                    dialogController.CollectDialogState()
                }
            }
        }
    }

    @Composable
    private fun GlobalDebugFloatingEntry() {
        if (!isDebug() && !isRdm()) {
            return
        }

        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        var offsetX by remember { mutableStateOf(DEBUG_FLOATING_INITIAL_START_OFFSET) }
        var offsetY by remember { mutableStateOf(0.dp) }
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = offsetX, y = offsetY)
                    .clip(DEBUG_FLOATING_SHAPE)
                    .background(DEBUG_FLOATING_BACKGROUND)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            offsetX += with(density) { dragAmount.x.toDp() }
                            offsetY += with(density) { dragAmount.y.toDp() }
                        }
                    }
                    .preciseClickable(onClickLabel = "打开调试页") {
                        scope.launch {
                            appRouter().toComposePage(
                                LocalKmmContext,
                                DEVELOPER_DEBUG_PAGE,
                                emptyPageArgs()
                            )
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                QnText(
                    text = "调试",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
    }

}

internal class PerformanceModule : Module() {

    internal fun onPageFirstFrameRendered() {
        toNative(
            keepCallbackAlive = false,
            methodName = "onPageFirstFrameRendered",
            param = null,
        )
    }

    override fun moduleName(): String = NewsComposeModule.Performance.moduleName

    internal fun firePreClick(event: TapEvent) {
        toNative(
            keepCallbackAlive = false,
            methodName = "firePreClick",
            param = JSONObject().apply {
                put("nativeRef", event.nativeView.nativeRef)
            }.toString(),
        )
    }
}

class BusinessModule : Module() {
    override fun moduleName(): String = NewsComposeModule.BusinessModule.moduleName
}
