package com.tencent.kmm.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import com.tencent.news.core.compose.platform.ComposePageLaunchType
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.platform.NTComposePageArgsPool
import com.tencent.news.core.compose.platform.PageTransition
import com.tencent.news.core.compose.platform.emptyPageArgs
import com.tencent.news.core.extension.safeForEach
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.platform.qnLogcat
import com.tencent.news.core.view.lifecycle.ComposeEvent
import com.tencent.kmm.demo.KuiklyRenderActivity.Companion.start
import com.tencent.kmm.demo.startup.sdk.tasks.AndroidQQLoginRuntime
import com.tencent.kmm.demo.view.AndroidComposePageActivity
import com.tencent.kmm.demo.view.webview.AndroidWebChromeClient

open class KuiklyRenderActivity : AndroidComposePageActivity() {

    /**
     * 由 [start] 写入，退出时 [onPause]/[finish] 读取以反向应用转场动画。
     */
    private var pendingTransition: PageTransition = PageTransition.DEFAULT
    private var currentPageName: String? = null
    private var restoredPageArgsBundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        restoredPageArgsBundle = savedInstanceState?.getBundle(KEY_PAGE_DATA_BUNDLE)
        super.onCreate(savedInstanceState)
    }

    override fun createComposePage() {
        val start = System.currentTimeMillis()

        val pageName = intent.getStringExtra(KEY_PAGE_NAME).takeIfNotBlank()
        if (pageName == null) {
            qnLogcat()?.logE("ComposePage", "missing pageName, finish")
            finish()
            return
        }
        val pageArgs = parsePageArgs(restoredPageArgsBundle)
            ?: parsePageArgs(intent)
            ?: parseDslPageData(intent)
            ?: emptyPageArgs()

        currentPageName = pageName
        pendingTransition = pageArgs.transition

        compose.onCreate(
            this.application,
            findViewById(android.R.id.content),
            pageArgs,
            pageName,
            onFirstFrame = {
                val cost = System.currentTimeMillis() - start
                qnLogcat()?.logD("ComposePage", "onFirstFrame: $cost")
            }
        )
    }

    /**
     * 从 KRRouterAdapter.openPage 传入的 Intent 中解析 pageData JSON 字符串。
     * 当 [parsePageArgs] 返回 null（即不是通过 [start] 标准路径启动）时，
     * 尝试从 "pageData" extra 中读取 JSON，构造 [DslPageDataArgs] 使 Kuikly DSL 页面
     * 能通过 pagerData.params 获取到完整的页面参数。
     */
    private fun parseDslPageData(intent: Intent): IComposePageArgs? {
        val jsonStr = intent.getStringExtra(KEY_DSL_PAGE_DATA).takeIfNotBlank() ?: return null
        return try {
            DslPageDataArgs(jsonStr)
        } catch (e: Exception) {
            qnLogcat()?.logE("ComposePage", "parseDslPageData error: ${e.message}")
            null
        }
    }

    override fun finish() {
        super.finish()
        // 反向应用转场：根据进入方式决定退出动画。
        when (pendingTransition) {
            PageTransition.MODAL_BOTTOM -> {
                overridePendingTransition(R.anim.no_change, R.anim.slide_out_down)
            }

            else -> {
                // 默认退出动画：当前页面向右滑出，底部页面从左滑入
                overridePendingTransition(R.anim.scale_out, R.anim.slide_right_out)
            }
        }
    }

    /**
     * 处理 SingleTop 模式下的页面重复拉起
     *
     * 当主页面（MainTab）已存在时，由于使用了 FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP，
     * 系统不会重建 Activity 而是调用 onNewIntent()。
     * 此处将新的 pageArgs 通过 [ComposeEvent.OnPageNewIntent] 事件传递到 Compose 层，
     * 由 [ComposePage.onPageNewIntent] → [PageNewIntentFlow] 分发给子组件消费。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val intentPageName = intent.getStringExtra(KEY_PAGE_NAME).takeIfNotBlank()
        val newPageArgs = parsePageArgs(intent)
            ?: return

        if (intentPageName != null && intentPageName != currentPageName) {
            qnLogcat()?.logE(
                "ComposePage",
                "mismatched new intent, restart target page current=$currentPageName " +
                    "intentPage=$intentPageName args=${newPageArgs::class.simpleName}",
            )
            start(this, intentPageName, newPageArgs)
            return
        }

        setIntent(intent)
        compose.onNewIntent(newPageArgs)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        compose.pageArgs?.pushPageArgsToMap?.toBundle()?.let { pageArgsBundle ->
            outState.putBundle(KEY_PAGE_DATA_BUNDLE, pageArgsBundle)
        }
        super.onSaveInstanceState(outState)
    }

    private fun parsePageArgs(intent: Intent): IComposePageArgs? {
        val bundle = intent.getBundleExtra(KEY_PAGE_DATA_BUNDLE)
        return parsePageArgs(bundle)
    }

    private fun parsePageArgs(bundle: Bundle?): IComposePageArgs? {
        val identifier = bundle?.getInt(NTComposePageArgsPool.PAGE_DATA_KEY) ?: return null
        return NTComposePageArgsPool.popPageArgs(identifier)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (AndroidWebChromeClient.handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        AndroidQQLoginRuntime.handleActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AndroidWebChromeClient.handleRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    internal fun isRenderingPage(pageName: String): Boolean = currentPageName == pageName

    private fun canHandleSingleTopPageIntent(): Boolean {
        return !isFinishing &&
            !isDestroyed &&
            lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    companion object {

        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA_BUNDLE = "pageArgsBundle"
        /** KRRouterAdapter.openPage 传入的原始 pageData JSON 字符串 */
        private const val KEY_DSL_PAGE_DATA = "pageData"

        @Suppress("UNCHECKED_CAST")
        private fun Map<String, Int>.toBundle(): Bundle {
            return Bundle().also { bundle ->
                // pushPageArgsToMap 的实际类型可能是 Map<String, Any>（如 DslPageDataArgs），
                // 通过类型擦除绕过了编译检查，因此这里需要安全处理各种 value 类型，
                // 避免 String cannot be cast to Number 的 ClassCastException。
                (this as Map<String, Any>).safeForEach { e ->
                    when (val value = e.value) {
                        is Int -> bundle.putInt(e.key, value)
                        is Long -> bundle.putLong(e.key, value)
                        is String -> bundle.putString(e.key, value)
                        is Boolean -> bundle.putBoolean(e.key, value)
                        is Float -> bundle.putFloat(e.key, value)
                        is Double -> bundle.putDouble(e.key, value)
                        else -> bundle.putString(e.key, value.toString())
                    }
                }
            }
        }

        fun start(context: Context, pageName: String, pageArgs: IComposePageArgs) {
            val targetClass = KuiklyRenderActivity::class.java

            val starter = Intent(context, targetClass)
            if (context !is Activity) {
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // 通用 KuiklyRenderActivity 只在重复打开同一个 pageName 时才应用 SINGLE_TOP，
            // 避免把不同 Compose 页面复用到当前 Activity 上。
            val sourceActivity = context as? KuiklyRenderActivity
            val isSameKuiklyPage = targetClass == KuiklyRenderActivity::class.java &&
                sourceActivity?.isRenderingPage(pageName) == true
            val canReuseSameKuiklyPage = isSameKuiklyPage &&
                sourceActivity?.canHandleSingleTopPageIntent() == true
            when {
                canReuseSameKuiklyPage && pageArgs.launchType == ComposePageLaunchType.SINGLE_TOP -> {
                    starter.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
            }

            val extras: Bundle = pageArgs.pushPageArgsToMap.toBundle()
            starter.putExtra(KEY_PAGE_NAME, pageName)
            starter.putExtra(KEY_PAGE_DATA_BUNDLE, extras)
            context.startActivity(starter)

            // 页面进入动画：MODAL_BOTTOM 时底部向上滑入；DEFAULT 从右向左滑入。
            // 新 Activity 自己在 `finish()` 里做退出动画；这里只管进入。
            if (context is Activity) {
                when (pageArgs.transition) {
                    PageTransition.MODAL_BOTTOM -> {
                        context.overridePendingTransition(R.anim.slide_in_up, R.anim.no_change)
                    }

                    else -> {
                        // 默认入场动画：新页面从右滑入，旧页面向左滑出
                        context.overridePendingTransition(R.anim.slide_left_in, R.anim.scale_in)
                    }
                }
            }
        }
    }

}
