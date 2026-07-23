package com.tencent.news.core.compose

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.platform.IComposePageSize
import com.tencent.news.core.compose.scaffold.NewsComposeModule
import com.tencent.news.core.compose.view.IComposeContainerView
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.view.extension.DpEx.dpToPx
import com.tencent.news.core.view.extension.DpEx.dpToPxNoScale
import com.tencent.news.core.view.lifecycle.ComposeEvent
import kotlin.collections.mapOf
import kotlin.math.max

/**
 * 尽量不要放开view的继承，可以外面套一层自己的view使用
 */
class ComposeContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), IComposeContainerView {

    var autoDestroyOnDetach = false         // detach时自动回收compose对象

    /**
     * 宽度变化时是否重建 compose（解决横竖屏切换 / 平行视界 / 折叠屏展开收起后
     * KuiklyRenderView 的 LayoutParams 仍按旧宽度的 px 写死、宽度不自适应的问题）。
     *
     * 默认关闭。仅按需打开（如目前的高考卡 picShowType=816/817/818），避免
     * 影响其他 ComposeCell 在宽度无关变化场景下的体验与性能。
     */
    var enableRecreateOnWidthChange = false

    var compose: IComposePageDelegate? = null
    var callback: AndroidViewModuleCallback? = null

    private var pageArgs: IComposePageArgs? = null
    private var composeViewKey: String? = ""

    private val lifecycleObserver = LifecycleEventObserver { source, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            // compose 会持有当前 context 引用，需要在 destroy 时回收防止泄露：
            // https://bugly.woa.com/v2/memory/java-leak/detail?productId=0d8bed2efe&pid=1
            // &token=55e814e99ce59fa6f418a0ace00cef46&feature=0b24b0e587a788d47b0c40035a3e8d62
            // &cId=a0f8f0470a71e4efc451708712be02ce
            innerDestroy()
        }
    }

    init {
        (context as? LifecycleOwner)?.lifecycle?.addObserver(lifecycleObserver)
    }

    override fun onCreate(composeViewKey: String, pageArgs: IComposePageArgs) {
        if (this.composeViewKey == composeViewKey &&
            this.pageArgs?.identifier == pageArgs.identifier &&
            this.compose != null
        ) {
            debugLog { "onCreate notChange $pageArgs" }
            return // 数据没变，且compose已存在；避免重复创建
        }
        if (!isActivityActive()) {
            val msg = "【警告】创建Compose[${composeViewKey}时，Activity已销毁！]"
            ComposeViewLog.fileLog(msg)
            debugToast(msg)
            return
        }

        // 注意，同一个compose对象不能onCreate 2次，否则会有内存泄露；
        // view复用时，要么彻底回收旧的compose；
        // 要么改为发送自定义事件，让compose侧自己处理刷新。
        if (compose != null) {
            debugLog { "view被复用，重建compose对象：${this.pageArgs}->${pageArgs}" }
            innerDestroy()
        }

        this.composeViewKey = composeViewKey
        this.pageArgs = pageArgs

        debugLog { "onCreate $pageArgs" }

        realCreateComposeView()
    }

    private fun isActivityActive(): Boolean {
        val activity = context as? Activity ?: return true // 没有Activity时不做校验
        return !activity.isFinishing && !activity.isDestroyed
    }

    private fun realCreateComposeView() {
        val composeViewKey = this.composeViewKey.takeIfNotEmpty() ?: return
        val pageArgs = this.pageArgs ?: return

        val createTime = getCurTimeMillis()

        val compose = AndroidComposePageDelegate()
        this.compose = compose

        removeAllViews()    // 【注意】每次onCreate都会添加一个 KuiklyRenderView，要注意清理

        compose.onCreate(
            app = context.applicationContext as Application,
            rootView = this,
            pageArgs = pageArgs,
            pageName = composeViewKey,
            onFirstFrame = {
                debugLog { "$composeViewKey onCreate耗时：${getCurTimeMillis() - createTime}" }
            },
            size = pageArgs.getViewSize(),
            modules = mapOf(
                NewsComposeModule.Dialog.moduleName to AndroidViewModule(callback)
            ),
        )
    }

    // 【重要】列表上下滑动重新attach时，如果不传递size，compose布局尺寸会为0
    private fun IComposePageArgs.getViewSize(): Size? {
        if (this !is IComposePageSize) {
            return null
        }
        if (isForceRatio) {
            val cellWidth = safeGetParentWidth()
            if (cellWidth <= 0) {
                debugToast("父容器宽度为0，compsoeView forceRatio展示失败")
            }
            return Size(cellWidth, (cellWidth / viewAspectRatio).toInt())
        }
        if (!needHostViewSize) {
            return null
        }
        val heightPx = if (scaleHostInitHeightWithFont) {
            initHeightInDp.dpToPx()
        } else {
            initHeightInDp.dpToPxNoScale()
        }
        return if (width > 0 && height > 0) {
            Size(width, max(height, heightPx))
        } else {
            val cellWidth = safeGetParentWidth()
            if (cellWidth > 0 && viewAspectRatio > 0) {
                val cellHeight = max((cellWidth / viewAspectRatio).toInt(), heightPx)
                Size(cellWidth, cellHeight)
            } else if (cellWidth > 0 && heightPx > 0) {
                Size(cellWidth, heightPx)
            } else {
                null
            }
        }
    }

    // 列表cell首次创建时，view measure还没执行，此时递归找到合法的view宽度
    // （绝大多数情况，应该是 RecyclerView 的宽度）
    private fun View.safeGetParentWidth(): Int {
        val parentView = parent as? View
        if (parentView != null) {
            return if (parentView.width > 0) {
                parentView.width
            } else {
                parentView.safeGetParentWidth()
            }
        }
        return 0
    }

    override fun onPause() {
        compose?.onPause()
    }

    override fun onResume() {
        compose?.onResume()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        debugLog { "onAttach $pageArgs" }

        if (autoDestroyOnDetach &&
            pageArgs != null &&
            composeViewKey.isNotNullOrEmpty() &&
            // 有数据但compose为空，可能是生命周期不配对：
            // 例如detach回收后，没有重新走onCreate。
            compose == null
        ) {
            debugLog { "onAttach reCreate $pageArgs" }

            realCreateComposeView()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        debugLog { "onDetach $pageArgs" }

        if (autoDestroyOnDetach) {
            innerDestroy()
        }
    }

    /**
     * 宽度变化时重建 compose，解决横竖屏切换 / 平行视界 / 折叠屏展开收起后，
     * KuiklyRenderView 的 LayoutParams 仍按旧宽度的 px 写死、宽度不自适应的问题。
     *
     * 仅判断宽度变化：高度变化由 KMM 侧 Compose cell size 回调反推刷上来，
     * 不在此触发，避免与 KMM 高度回报形成循环。
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!enableRecreateOnWidthChange) {
            return
        }
        if (oldw <= 0 || w <= 0 || w == oldw) {
            return
        }
        if (compose == null || pageArgs == null || composeViewKey.isNullOrEmpty()) {
            return
        }
        if (!isActivityActive()) {
            return
        }
        debugLog { "onSizeChanged width $oldw -> $w, recreate compose" }
        // innerDestroy 仅清 compose / 子 view，不会清空 composeViewKey 与 pageArgs，
        // 因此可以直接调用 realCreateComposeView 用新宽度重建。
        innerDestroy()
        realCreateComposeView()
    }

    override fun onDestroy() {
        innerDestroy()
    }

    private fun innerDestroy() {
        compose?.onDestroy()
        compose = null
        removeAllViews()
    }

    override fun sendEvent(event: ComposeEvent, params: Map<String, Any>) {
        compose?.sendEvent(event.eventName, params)
    }

    private inline fun debugLog(msg: () -> String) {
        ComposeViewLog.debug(subTag = "$composeViewKey") {
            "[${Thread.currentThread().name}]${msg()}"
        }
    }

}
