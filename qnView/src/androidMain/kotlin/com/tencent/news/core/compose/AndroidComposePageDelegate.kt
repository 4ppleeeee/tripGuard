package com.tencent.news.core.compose

import android.app.Application
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.IKuiklyRenderView
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewDelegatorDelegate
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.scaffold.NewsComposeModule
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug

class AndroidComposePageDelegate : KuiklyRenderViewDelegatorDelegate, IComposePageDelegate {

    private var containerView: ViewGroup? = null
    private var pageArgs: IComposePageArgs? = null
    private var pageName: String = ""

    private val kuiklyRenderViewDelegator = KuiklyRenderViewDelegator(this)

    private var onFirstFrame: (() -> Unit)? = null

    private val exportModules = HashMap<String, KuiklyRenderBaseModule>()

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

    override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalRenderView(kuiklyRenderExport)
        val bridge = andComposeBridge ?: return
        with(kuiklyRenderExport) {
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

            renderViewExport("QnWebViewCell", { ctx ->
                bridge.createWebViewCell(ctx)
            })
        }
    }

    override fun registerViewExternalPropHandler(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerViewExternalPropHandler(kuiklyRenderExport)
        with(kuiklyRenderExport) {
            viewPropExternalHandlerExport(KuiklyRenderViewPropDispatcher())
        }
    }

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

}

