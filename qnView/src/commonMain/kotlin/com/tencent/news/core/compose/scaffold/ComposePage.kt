package com.tencent.news.core.compose.scaffold

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.platform.GlobalTapManager
import com.tencent.kuikly.compose_dsl.kuikly.platform.TapEvent
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.platform.parsePageArgs
import com.tencent.news.core.compose.scaffold.registry.LocalBubbleViewController
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageNewIntentFlow
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.compose.scaffold.registry.LocalScreenshot
import com.tencent.news.core.compose.scaffold.theme.QNAppTheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnScreenshot
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.ScreenshotState
import com.tencent.news.core.compose.view.bubble.BubbleViewController
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.extension.safeEncodeByKClass
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.api.appReport
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import com.tencent.news.core.util.lifecycle.PageLifecycleFlow
import com.tencent.news.core.util.lifecycle.PageNewIntentFlow
import com.tencent.news.core.view.lifecycle.ComposeEvent


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
    protected var pageArgs: IComposePageArgs? = null

    // 创建页面生命周期流
    protected val pageLifecycleFlow = PageLifecycleFlow()

    // 创建页面 NewIntent 事件流
    protected val pageNewIntentFlow = PageNewIntentFlow()

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
    @CallSuper
    @Composable
    protected open fun OnSetContent() {

    }

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
        // parsePageArgs 解析后，缓存的pageArgs就会被清楚，此处要把pageArgs保存；
        // 否则重组情况可能丢失（目前kuikly有bug，折叠屏切换时remember会丢）
        val args = this.pageArgs as? T ?: remember { pageData.parsePageArgs<T>() }
        this.pageArgs = args
        return args
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
            "${this::class.simpleName}[${this.hashCode().toString(16)}][${pageArgs?.identifier}] 收到事件 $pagerEvent ：$eventData"
        }
        when (pagerEvent) {
            ComposeEvent.OnPageNewIntent.eventName -> handlePageNewIntent(eventData)
        }
    }

    private fun handlePageNewIntent(eventData: JSONObject) {
        onPageNewIntent(eventData.toMap())
    }

    protected open fun onPageNewIntent(dataMap: Map<String, Any>) {
        ComposeViewLog.debug { "${this::class.simpleName}[${
            this.hashCode().toString(16)
        }] onPageNewIntent data:$dataMap" }
        pageNewIntentFlow.tryEmitNewIntent(dataMap)
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
                val screenshotState = remember { ScreenshotState() }

                CompositionLocalProvider(
                    LocalBubbleViewController provides bubbleViewController,
                    LocalDialogController provides dialogController,
                    LocalScreenshot provides screenshotState,
                    LocalComposePageLifecycleFlow provides pageLifecycleFlow.lifecycleFlow,
                    LocalComposePageNewIntentFlow provides pageNewIntentFlow.newIntentFlow,
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

                    bubbleViewController.CollectBubbleState()
                    dialogController.CollectDialogState()
                }
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

    fun callNative(
        keepCallbackAlive: Boolean,
        methodName: String,
        param: Any?,
    ) {
        val finalParam = if (isHarmonyPlatform()) {
            formatParamForOhos(param)
        } else {
            param
        }
        toNative(keepCallbackAlive, methodName, finalParam)
    }

    /**
     * 鸿蒙平台对 param 进行 JSON 字符串格式化：
     * 1. 字符串类型：直接返回
     * 2. Map 类型：递归处理所有 value，整体转为 JSON 字符串
     * 3. 其他类型：尝试解析为 JSONObject，失败则直接 toString()
     */
    private fun formatParamForOhos(param: Any?): String? {
        return when (param) {
            null -> null
            is String -> param
            is Map<*, *> -> mapToJsonObject(param).toString()
            else -> {
                // 复杂对象：用 KtJson 序列化为 JSON 字符串，解析失败则 toString()
                runCatching { KtJson.safeEncodeByKClass(param) }
                    .getOrElse { param.toString() }
            }
        }
    }

    /**
     * 将 Map 递归转换为 JSONObject：
     * - 基础类型（String/Int/Long/Double/Float/Boolean）直接 put
     * - 嵌套 Map 递归转为 JSONObject
     * - 其他复杂对象尝试解析为 JSONObject，失败则 toString()
     */
    private fun mapToJsonObject(map: Map<*, *>): JSONObject {
        return JSONObject().apply {
            map.forEach { (key, value) ->
                val keyStr = key?.toString() ?: return@forEach
                when (value) {
                    null -> put(keyStr, "")
                    is String -> put(keyStr, value)
                    is Int -> put(keyStr, value)
                    is Long -> put(keyStr, value)
                    is Double -> put(keyStr, value)
                    is Float -> put(keyStr, value)
                    is Boolean -> put(keyStr, value)
                    is Map<*, *> -> put(keyStr, mapToJsonObject(value))
                    else -> {
                        // 复杂对象：用 KtJson 序列化为 JSON 字符串，再包装为 JSONObject
                        val jsonStr = runCatching { KtJson.safeEncodeByKClass(value) }
                            .getOrElse { value.toString() }
                        val jsonObject = runCatching { JSONObject(jsonStr) }.getOrNull()
                        if (jsonObject != null) {
                            put(keyStr, jsonObject)
                        } else {
                            put(keyStr, jsonStr)
                        }
                    }
                }
            }
        }
    }
}
