package com.tencent.kmm.demo

import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable

/**
 * 首页专用 Activity，继承 [KuiklyRenderActivity]。
 *
 * 在 AndroidManifest 中声明为 singleTop，保留桌面图标回前台时的任务栈：
 * - 从其他页面回到首页时，路由层显式添加 CLEAR_TOP/SINGLE_TOP 复用 MainTabActivity 并清除其上方的 Activity；
 * - 新的参数通过 [onNewIntent] → [ComposeEvent.OnPageNewIntent] 传递到 Compose 层。
 *
 * 其他非首页的 Compose 页面仍然使用 [KuiklyRenderActivity]（standard 模式）。
 */
class MainTabActivity : KuiklyRenderActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent.getStringExtra(KEY_PAGE_NAME).isNullOrBlank()) {
            intent.putExtra(KEY_PAGE_NAME, MAIN_TAB_PAGE)
        }
        super.onCreate(savedInstanceState)
        // 屏幕宽高发生变化时，Compose 层自动刷新没那么及时，可能漏出背景，所以页面启动后设置背景色
        window.setBackgroundDrawable(0xFF010001.toInt().toDrawable())
    }

    private companion object {
        const val KEY_PAGE_NAME = "pageName"
        const val MAIN_TAB_PAGE = "/page/demo_home"
    }
}
