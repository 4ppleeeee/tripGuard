package com.tencent.news.core.compose

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.IKuiklyRenderView
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewDelegatorDelegate
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.scaffold.NewsComposeModule
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.view.lifecycle.ComposeEvent

open class AndroidComposePageDelegate : KuiklyRenderViewDelegatorDelegate, IComposePageDelegate {

    companion object {
        private const val SAFE_AREA_LOG_SUB_TAG = "SafeArea"
        private const val MODEL_V1816A = "V1816A"
        private const val SAFE_AREA_RETRY_DELAY_MS = 50L
        private const val SAFE_BOTTOM_TOLERANCE_DP = 16F

        /**
         * 自定义 delegate 工厂，由宿主 App 注册。
         * ComposeContainerView 等内部组件通过此工厂创建 delegate 实例，
         * 从而获得宿主注册的自定义 Module 和 RenderView。
         */
        @Volatile
        private var factory: (() -> AndroidComposePageDelegate)? = null

        /**
         * 注册自定义 delegate 工厂。
         * 应在 Application.onCreate 中调用。
         */
        fun registerFactory(factory: () -> AndroidComposePageDelegate) {
            this.factory = factory
        }

        /**
         * 创建 delegate 实例。
         * 如果已注册工厂则使用工厂创建，否则使用默认的 AndroidComposePageDelegate。
         */
        fun create(): AndroidComposePageDelegate {
            return factory?.invoke() ?: AndroidComposePageDelegate()
        }
    }

    private var containerView: ViewGroup? = null
    override var pageArgs: IComposePageArgs? = null
    override var pageName: String? = ""

    private val kuiklyRenderViewDelegator = KuiklyRenderViewDelegator(this)

    private var onFirstFrame: (() -> Unit)? = null

    private val exportModules = HashMap<String, KuiklyRenderBaseModule>()

    private var lastBottomInsetPx = 0

    override fun onCreate(
        app: Application,
        rootView: ViewGroup,
        pageArgs: IComposePageArgs,
        pageName: String,
        modules: Map<String, KuiklyRenderBaseModule>,
        onFirstFrame: (() -> Unit)?,
        size: Size?
    ) {
        if (isDebug() && this.pageArgs != null) {
            debugToast("【警告】！！！ 重复调用compose的 onCreate 会导致 context 内存泄露，不能直接复用")
        }

        this.containerView = rootView
        this.pageArgs = pageArgs
        this.pageName = pageName

        if (KuiklyRenderAdapterManager.krRouterAdapter == null) {
            KuiklyRenderAdapterManager.krRouterAdapter = KuiklyRouterAdapter()
        }

        if (KuiklyRenderAdapterManager.krFontAdapter == null) {
            KuiklyRenderAdapterManager.krFontAdapter = KuiklyFontAdapter(app)
        }

        if (KuiklyRenderAdapterManager.krImageAdapter == null) {
            KuiklyRenderAdapterManager.krImageAdapter = KuiklyImageAdapter()
        }

        if (KuiklyRenderAdapterManager.krThreadAdapter == null) {
            KuiklyRenderAdapterManager.krThreadAdapter = KuiklyThreadAdapter()
        }

        exportModules.putAll(modules)
        this.onFirstFrame = onFirstFrame

        kuiklyRenderViewDelegator.onAttach(     // 对应iOS的 initWithPageName
            container = rootView,
            contextCode = "",
            pageName = pageName,
            pageData = pageArgs.pushPageArgsToMap,
            size = size
        )

    }

    override fun onNewIntent(args: IComposePageArgs) {
        // 将新参数推入 ArgsPool，通过 identifier 传递给 Compose 层
        this.pageArgs = args
        sendEvent(ComposeEvent.OnPageNewIntent.eventName, args.pushPageArgsToMap)
    }

    /**
     * 重新读取并派发底部安全区。
     *
     * 退出横屏后 Android 的 rootWindowInsets 会在下一帧更新，先 requestApplyInsets，
     * 再通过独立 safeAreaInsets 事件带上最新安全区，驱动 Kuikly 侧刷新底部安全区。
     */
    fun refreshSafeAreaInsets() {
        val view = containerView ?: return
        ComposeViewLog.fileLog(SAFE_AREA_LOG_SUB_TAG, "refreshSafeAreaInsets start page=$pageName")
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "refreshSafeAreaInsets start page=$pageName lastBottomInsetPx=$lastBottomInsetPx"
        }
        ViewCompat.requestApplyInsets(view)
        view.post {
            dispatchSafeAreaInsetsWhenReady(view, retryCount = 3, forceUseCurrentInset = true)
        }
    }

    private fun dispatchSafeAreaInsetsWhenReady(
        view: View,
        retryCount: Int,
        forceUseCurrentInset: Boolean = false,
    ) {
        ViewCompat.requestApplyInsets(view)
        val context = view.context ?: return
        val bottomInsetPx = resolveBottomInsetPx(
            view = view,
            context = context,
            useFallback = !forceUseCurrentInset,
        )

        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "dispatchSafeAreaInsetsWhenReady page=$pageName bottomInsetPx=$bottomInsetPx " +
                "lastBottomInsetPx=$lastBottomInsetPx retryCount=$retryCount " +
                "forceUseCurrentInset=$forceUseCurrentInset"
        }
        if (bottomInsetPx <= 0 && retryCount > 0) {
            val shouldRetry = forceUseCurrentInset || lastBottomInsetPx > 0
            if (shouldRetry) {
                ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
                    "retry safe area dispatch page=$pageName because bottom inset is zero, " +
                        "nextRetryCount=${retryCount - 1} forceUseCurrentInset=$forceUseCurrentInset"
                }
                view.postDelayed({
                    dispatchSafeAreaInsetsWhenReady(view, retryCount - 1, forceUseCurrentInset)
                }, SAFE_AREA_RETRY_DELAY_MS)
                return
            }
        }
        val safeAreaData = externalPageData(bottomInsetPx, forceUseCurrentInset)
        ComposeViewLog.fileLog(
            SAFE_AREA_LOG_SUB_TAG,
            "dispatch safe area page=$pageName bottomInsetPx=$bottomInsetPx " +
                "lastBottomInsetPx=$lastBottomInsetPx forceUseCurrentInset=$forceUseCurrentInset"
        )
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "send safe area page=$pageName data=$safeAreaData"
        }
        sendEvent(ComposeEvent.SafeArea.OnInsetsChanged.eventName, safeAreaData)
    }

    override fun onResume() {
        kuiklyRenderViewDelegator.onResume()    // 对应iOS的 didAppear
    }

    override fun onPause() {
        kuiklyRenderViewDelegator.onPause()     // 对应iOS的 didDisappear
    }

    override fun onDestroy() {
        kuiklyRenderViewDelegator.onDetach()    // 对应iOS的 dealloc
        this.pageArgs = null
    }

    override fun onDispatchBackEvent(): Boolean {
        return kuiklyRenderViewDelegator.onBackPressed()
    }

    override fun onDispatchTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    // 从宿主向kuikly发送事件
    override fun sendEvent(event: String, data: Map<String, Any>) =
        kuiklyRenderViewDelegator.sendEvent(event, data)

    override fun findNativeView(nativeViewRef: Int): View? {
        val renderView = containerView?.getChildAt(0) as? IKuiklyRenderView
        return renderView?.getView(nativeViewRef)
    }

    @CallSuper
    override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalRenderView(kuiklyRenderExport)
        val bridge = andComposeBridge ?: return
        with(kuiklyRenderExport) {

            // todo genesisli fix: 检查一下这些组件是否要注册

            renderViewExport("QnLottieView", { context ->
                bridge.createLottieView(context)
            })

            renderViewExport("QnScreenshotView", { context ->
                bridge.createScreenshotView(context)
            })

            renderViewExport("QnQrCodeView", { context ->
                bridge.createQrCodeView(context)
            })

            renderViewExport("QnVideoView", { context ->
                AndroidVideoView(context)
            })

            renderViewExport("QnStreamVideoView", { context ->
                AndroidStreamVideoView(context)
            })

            renderViewExport("QnAlphaVideoView", { context ->
                AndroidAlphaVideoView(context)
            })

        }
    }

    @CallSuper
    override fun registerViewExternalPropHandler(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerViewExternalPropHandler(kuiklyRenderExport)
        with(kuiklyRenderExport) {
            viewPropExternalHandlerExport(KuiklyRenderViewPropDispatcher())
        }
    }

    @CallSuper
    override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalModule(kuiklyRenderExport)
        with(kuiklyRenderExport) {
            moduleExport(NewsComposeModule.Performance.moduleName) {
                AndroidPerformanceModule(onFirstFrame)
            }
            exportModules.forEach {
                moduleExport(it.key) { it.value }
            }
            // 需要clear么?
            exportModules.clear()
        }
    }

    /**
     * 注入底部安全区（导航栏 / 手势条高度）到 Kuikly 的 safeAreaInsets。
     *
     * Kuikly Android 默认的 safeAreaInsets 只设置了 top=状态栏高度，left/bottom/right=0，
     * 导致 `ComposeUtils.rememberSafeAreaBottomHeight()` 在 Android 上永远是 0，
     * 页面底部无法识别到导航栏 / 手势条。这里通过 externalPageData 覆盖 safeAreaInsets，
     * 让业务侧可以统一使用 `safeAreaInsets.bottom` 获取底部安全区高度。
     *
     * 注意：该方法由 KuiklyRenderView 在 initRenderCore 阶段调用（onAttach 同步流程内），
     * 调用时 containerView 已被 onCreate 赋值，且 Activity 的 window 已 attach，
     * rootWindowInsets 可用。
     */
    override fun externalPageData(): Map<String, Any> {
        val view = containerView ?: return emptyMap()
        val context = view.context ?: return emptyMap()
        val navBarPx = resolveBottomInsetPx(view, context)
        return externalPageData(navBarPx, forceUseCurrentInset = false)
    }

    private fun externalPageData(navBarPx: Int, forceUseCurrentInset: Boolean): Map<String, Any> {
        val view = containerView ?: return emptyMap()
        val context = view.context ?: return emptyMap()

        val statusBarPx = getStatusBarHeightPx(context)
        val normalizedNavBarPx = normalizeSafeBottomInsetPx(
            view = view,
            context = context,
            newSafeBottomPx = navBarPx,
            forceUseCurrentInset = forceUseCurrentInset,
        )
        val safeNavBarPx = when {
            forceUseCurrentInset -> {
                lastBottomInsetPx = normalizedNavBarPx
                normalizedNavBarPx
            }
            normalizedNavBarPx > 0 -> {
                lastBottomInsetPx = normalizedNavBarPx
                normalizedNavBarPx
            }
            navBarPx > 0 -> {
                lastBottomInsetPx = normalizedNavBarPx
                normalizedNavBarPx
            }
            else -> {
                if (lastBottomInsetPx > 0) {
                    ComposeViewLog.fileLog(
                        SAFE_AREA_LOG_SUB_TAG,
                        "use cached bottom inset page=$pageName navBarPx=$navBarPx " +
                            "normalizedNavBarPx=$normalizedNavBarPx cached=$lastBottomInsetPx"
                    )
                }
                lastBottomInsetPx
            }
        }

        val topDp = pxToDp(context, statusBarPx)
        val bottomDp = pxToDp(context, safeNavBarPx)

        // 格式与 Kuikly 默认一致："top left bottom right"（均为 dp）
        val safeAreaInsetsStr = "$topDp 0 $bottomDp 0"
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "build externalPageData page=$pageName statusBarPx=$statusBarPx navBarPx=$navBarPx " +
                "normalizedNavBarPx=$normalizedNavBarPx safeNavBarPx=$safeNavBarPx " +
                "safeAreaInsets=$safeAreaInsetsStr ${buildSafeBottomDiagnostic(view, context)}"
        }
        return mapOf("safeAreaInsets" to safeAreaInsetsStr)
    }

    private fun normalizeSafeBottomInsetPx(
        view: View,
        context: Context,
        newSafeBottomPx: Int,
        forceUseCurrentInset: Boolean,
    ): Int {
        val navBarHeightPx = getNavigationBarHeightPx(context)
        val isImmersiveMode = isImmersiveMode(view)
        val sdkInt = android.os.Build.VERSION.SDK_INT

        if (!isImmersiveMode && sdkInt < android.os.Build.VERSION_CODES.R) {
            val stableBottom = stableLastValidBottomPx(navBarHeightPx)
            logDropUnsafeBottomInset(
                reason = "lowVersionNonImmersive",
                view = view,
                context = context,
                newSafeBottomPx = newSafeBottomPx,
                navBarHeightPx = navBarHeightPx,
                thresholdPx = navBarHeightPx + dpToPx(context, SAFE_BOTTOM_TOLERANCE_DP),
                isImmersiveMode = false,
                forceUseCurrentInset = forceUseCurrentInset,
                fallbackBottomPx = stableBottom,
            )
            return stableBottom
        }

        if (newSafeBottomPx <= 0) return 0

        val tolerancePx = dpToPx(context, SAFE_BOTTOM_TOLERANCE_DP)
        val thresholdPx = if (navBarHeightPx > 0) {
            navBarHeightPx + tolerancePx
        } else {
            tolerancePx
        }
        if (newSafeBottomPx > thresholdPx) {
            val stableBottom = stableLastValidBottomPx(navBarHeightPx)
            logDropUnsafeBottomInset(
                reason = "exceedNavBarThreshold",
                view = view,
                context = context,
                newSafeBottomPx = newSafeBottomPx,
                navBarHeightPx = navBarHeightPx,
                thresholdPx = thresholdPx,
                isImmersiveMode = isImmersiveMode,
                forceUseCurrentInset = forceUseCurrentInset,
                fallbackBottomPx = stableBottom,
            )
            return stableBottom
        }

        return newSafeBottomPx
    }

    private fun stableLastValidBottomPx(navBarHeightPx: Int): Int {
        if (lastBottomInsetPx <= 0) return 0
        if (navBarHeightPx <= 0) return lastBottomInsetPx
        val thresholdPx = navBarHeightPx + dpToPx(
            containerView?.context ?: return 0,
            SAFE_BOTTOM_TOLERANCE_DP,
        )
        return if (lastBottomInsetPx <= thresholdPx) lastBottomInsetPx else 0
    }

    @Suppress("DEPRECATION")
    private fun isImmersiveMode(view: View): Boolean {
        val decorView = findActivity(view.context)?.window?.decorView ?: view
        return decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION != 0
    }

    private fun logDropUnsafeBottomInset(
        reason: String,
        view: View,
        context: Context,
        newSafeBottomPx: Int,
        navBarHeightPx: Int,
        thresholdPx: Int,
        isImmersiveMode: Boolean,
        forceUseCurrentInset: Boolean,
        fallbackBottomPx: Int,
    ) {
        ComposeViewLog.fileLog(
            SAFE_AREA_LOG_SUB_TAG,
            "drop unsafe bottom inset reason=$reason page=$pageName " +
                "newSafeBottomPx=$newSafeBottomPx navBarHeightPx=$navBarHeightPx " +
                "thresholdPx=$thresholdPx fallbackBottomPx=$fallbackBottomPx " +
                "isImmersiveMode=$isImmersiveMode sdk=${android.os.Build.VERSION.SDK_INT} " +
                "forceUseCurrentInset=$forceUseCurrentInset ${buildSafeBottomDiagnostic(view, context)}"
        )
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "drop unsafe bottom inset reason=$reason page=$pageName " +
                "newSafeBottomPx=$newSafeBottomPx navBarHeightPx=$navBarHeightPx " +
                "thresholdPx=$thresholdPx fallbackBottomPx=$fallbackBottomPx " +
                "lastBottomInsetPx=$lastBottomInsetPx"
        }
    }

    private fun buildSafeBottomDiagnostic(view: View, context: Context): String {
        val rootViewHeight = view.height
        val composeViewHeight = (view as? ViewGroup)?.getChildAt(0)?.height ?: 0
        val screenHeight = context.resources.displayMetrics.heightPixels
        val navBarHeightPx = getNavigationBarHeightPx(context)
        return "diagnostic{safeBottomBarHeightPx=$lastBottomInsetPx " +
            "navigationBarHeightPx=$navBarHeightPx " +
            "isImmersiveMode=${isImmersiveMode(view)} " +
            "sdk=${android.os.Build.VERSION.SDK_INT} " +
            "rootViewHeight=$rootViewHeight composeViewHeight=$composeViewHeight " +
            "screenHeight=$screenHeight}"
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        if (density <= 0f) return 0
        return (dp * density + 0.5f).toInt()
    }

    /**
     * 取底部导航栏 / 手势条高度（单位：px）。
     *
     * 策略（按优先级）：
     * 1. API 30+：通过 WindowInsets.Type.navigationBars() 精确获取，能正确区分三键导航与手势条。
     * 2. API 21-29：通过 systemWindowInsetBottom 获取。
     *    注意：在全屏沉浸模式（SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION）下，
     *    systemWindowInsetBottom 仍会包含导航栏高度；但在部分厂商 ROM 或导航栏
     *    被完全隐藏时可能返回 0，此时回退到策略 3。
     * 3. 兜底：通过系统资源 navigation_bar_height 查询，并结合 hasNavigationBar
     *    配置项判断设备是否真实存在导航栏，避免在无导航栏设备上误注入高度。
     */
    private fun resolveBottomInsetPx(
        view: View,
        context: Context,
        useFallback: Boolean = true,
    ): Int {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val windowInsets = view.rootWindowInsets
            if (windowInsets != null) {
                val bottom = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    windowInsets.getInsets(
                        android.view.WindowInsets.Type.navigationBars() or
                            android.view.WindowInsets.Type.displayCutout()
                    ).bottom
                } else {
                    @Suppress("DEPRECATION")
                    windowInsets.systemWindowInsetBottom
                }
                ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
                    "resolveBottomInsetPx by rootWindowInsets page=$pageName bottom=$bottom " +
                        "useFallback=$useFallback"
                }
                if (bottom > 0) {
                    return adjustBottomInsetForDevice(bottom)
                }
                val visibleNavBarHeight = resolveVisibleNavBarHeightByDecorView(context)
                if (visibleNavBarHeight > 0) {
                    ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
                        "resolveBottomInsetPx use visible nav bar height page=$pageName " +
                            "visibleNavBarHeight=$visibleNavBarHeight"
                    }
                    return adjustBottomInsetForDevice(visibleNavBarHeight)
                }
                if (!useFallback) {
                    return 0
                }
                val stableBottom = getStableBottomInsetPx(view)
                if (stableBottom > 0) {
                    ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
                        "resolveBottomInsetPx use stable bottom page=$pageName stableBottom=$stableBottom"
                    }
                    return adjustBottomInsetForDevice(stableBottom)
                }
            } else {
                ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
                    "resolveBottomInsetPx rootWindowInsets is null page=$pageName useFallback=$useFallback"
                }
            }
        }
        if (!useFallback) {
            return 0
        }
        // 兜底：通过系统资源 + stableInsetBottom 多维校验，避免误注入
        val resourceBottom = resolveNavBarHeightFromResource(
            view = view,
            context = context,
            checkStableInset = useFallback,
        )
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "resolveBottomInsetPx by resource page=$pageName bottom=$resourceBottom"
        }
        return adjustBottomInsetForDevice(resourceBottom)
    }

    private fun adjustBottomInsetForDevice(bottom: Int): Int {
        val isV1816A = android.os.Build.MODEL.equals(MODEL_V1816A, ignoreCase = true)
        if (bottom <= 0 || !isV1816A) return bottom

        val adjustedBottom = bottom * 2 / 3
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "adjust bottom inset for V1816A page=$pageName bottom=$bottom adjustedBottom=$adjustedBottom"
        }
        return adjustedBottom
    }

    /**
     * 兜底策略：通过系统资源 + DecorView 可用高度校验导航栏高度。
     *
     * 仅靠 config_showNavigationBar 不够准确——部分厂商 ROM（小米、华为、OPPO 等）在
     * 用户切换到手势导航模式后，该值仍为 true，但实际导航栏已不存在。
     *
     * 校验逻辑（三重过滤，任一不满足则返回 0）：
     * 1. config_showNavigationBar == true：设备声明存在导航栏（必要非充分条件）。
     * 2. navigation_bar_height > 0：资源高度大于 0，排除无导航栏设备。
     * 3. DecorView 底部存在不可见区域：说明 NavigationBar 当前真实显示并占用窗口底部区域。
     */
    private fun resolveNavBarHeightFromResource(
        view: View,
        context: Context,
        checkStableInset: Boolean = true,
    ): Int {
        // 第一重：config_showNavigationBar
        val showNavBar = try {
            val resId = context.resources.getIdentifier(
                "config_showNavigationBar", "bool", "android"
            )
            if (resId > 0) context.resources.getBoolean(resId) else true
        } catch (e: Exception) {
            ComposeViewLog.error(SAFE_AREA_LOG_SUB_TAG, "Failed to read config_showNavigationBar", e)
            true
        }
        if (!showNavBar) return 0

        // 第二重：navigation_bar_height > 0
        val navBarHeight = getNavigationBarHeightPx(context)
        if (navBarHeight <= 0) return 0

        if (checkStableInset && resolveVisibleNavBarHeightByDecorView(context) <= 0) {
            return 0
        }

        return navBarHeight
    }

    private fun getStableBottomInsetPx(view: View): Int {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return 0

        val windowInsets = view.rootWindowInsets ?: return 0
        @Suppress("DEPRECATION")
        return windowInsets.stableInsetBottom
    }

    private fun resolveVisibleNavBarHeightByDecorView(context: Context): Int {
        val decorView = findActivity(context)?.window?.decorView ?: return 0
        val decorViewHeight = decorView.height
        if (decorViewHeight <= 0) return 0

        val visibleFrame = Rect()
        decorView.getWindowVisibleDisplayFrame(visibleFrame)
        if (visibleFrame.isEmpty) return 0

        val decorLocation = IntArray(2)
        decorView.getLocationOnScreen(decorLocation)
        val decorBottomOnScreen = decorLocation[1] + decorViewHeight
        val bottomHiddenHeight = decorBottomOnScreen - visibleFrame.bottom
        if (bottomHiddenHeight <= 0) return 0

        val navBarHeight = getNavigationBarHeightPx(context)
        ComposeViewLog.debug(SAFE_AREA_LOG_SUB_TAG) {
            "resolveVisibleNavBarHeightByDecorView page=$pageName decorViewHeight=$decorViewHeight " +
                "visibleFrame=$visibleFrame bottomHiddenHeight=$bottomHiddenHeight navBarHeight=$navBarHeight"
        }
        return navBarHeight
    }

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    private fun getStatusBarHeightPx(context: Context): Int {
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else 0
    }

    private fun getNavigationBarHeightPx(context: Context): Int {
        val resId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else 0
    }

    private fun pxToDp(context: Context, px: Int): Float {
        if (px <= 0) return 0f
        val density = context.resources.displayMetrics.density
        if (density <= 0f) return 0f
        return px / density
    }

}
