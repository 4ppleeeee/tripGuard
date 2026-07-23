package com.tencent.kmm.demo.view

import android.R
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.tencent.news.core.compose.AndroidComposePageDelegate
import com.tencent.news.core.compose.IComposePage
import com.tencent.news.core.compose.IComposePageDelegate
import com.tencent.news.core.compose.platform.IComposeDemoPage
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IStatusBarController
import com.tencent.news.core.platform.qnLogcat
import com.tencent.kmm.demo.module.KRCalendarReminderModule
import com.tencent.kmm.demo.setup.handleAndroidAppLocationPermissionResult
import com.tencent.kmm.demo.setup.handleSelectImageActivityResult
import com.tencent.kmm.demo.setup.notifySystemConfigurationChanged
import java.lang.ref.WeakReference

open class AndroidComposePageActivity : FragmentActivity(), IComposePage {

    companion object {
        private var windowInsetsListenerOwnerRef: WeakReference<AndroidComposePageActivity>? = null
    }

    override val compose: IComposePageDelegate = AndroidComposePageDelegate()

    private var composePageCreated = false
    private var resumed = false
    private var lastOrientation = Configuration.ORIENTATION_UNDEFINED
    private var lastNavigationBarBottomInset = Int.MIN_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastOrientation = resources.configuration.orientation
        setContentView(FrameLayout(this))
        qnLogcat()?.logD("ComposePage", "onCreate")

        // 开启沉浸模式：全屏透明状态栏 + 深色字体 + 透明导航栏
        ImmersiveHelper.enableImmersiveMode(
            window = window,
            context = this,
            isFullScreen = true,
            isLightStatusBar = true,
            statusBarColor = Color.TRANSPARENT,
            navigationBarColor = Color.TRANSPARENT
        )
        setupWindowInsetsListener()

        // 注入 IStatusBarController，将 ImmersiveHelper 能力桥接到 KMM 公共层
        // setWhiteBar = 白色图标（适合深色背景），setBlackBar = 深色图标（适合浅色背景）
        // 注意：setSystemUiVisibility 必须在主线程调用，用 runOnUiThread 保证线程安全
        // 注意：不能直接持有 window 引用，因为 statusBarController 是全局单例，
        //       每次打开新页面都会覆盖，从详情页返回后持有的是已销毁 Activity 的 window。
        //       改为动态获取当前前台 Activity 的 window，确保始终操作正确的窗口。
        QnPlatformLogic.statusBarController = object : IStatusBarController {
            private fun applyOnCurrentWindow(isLight: Boolean) {
                val currentActivity = QnPlatformLogic.appPageStack
                    ?.getTopValidPage() as? FragmentActivity
                    ?: this@AndroidComposePageActivity
                currentActivity.runOnUiThread {
                    ImmersiveHelper.setStatusBarLightMode(currentActivity.window, isLight = isLight)
                }
            }
            override fun setWhiteBar() { applyOnCurrentWindow(isLight = false) }
            override fun setBlackBar() { applyOnCurrentWindow(isLight = true) }
            override fun resetStatusBar() { applyOnCurrentWindow(isLight = true) }
            override fun setCustomBar(textColor: String, backgroundColor: String) {}
            override fun setStatusBarVisibility(visible: Boolean) {
                val currentActivity = QnPlatformLogic.appPageStack
                    ?.getTopValidPage() as? FragmentActivity
                    ?: this@AndroidComposePageActivity
                currentActivity.runOnUiThread {
                    val controller = WindowInsetsControllerCompat(
                        currentActivity.window,
                        currentActivity.window.decorView
                    )
                    if (visible) {
                        controller.show(WindowInsetsCompat.Type.statusBars())
                    } else {
                        controller.hide(WindowInsetsCompat.Type.statusBars())
                    }
                }
            }
        }

        createComposePageIfNeeded()
    }

    private fun setupWindowInsetsListener() {
        val owner = windowInsetsListenerOwnerRef?.get()
        if (owner === this) {
            return
        }
        owner?.window?.decorView?.let { decorView ->
            ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
        }
        windowInsetsListenerOwnerRef = WeakReference(this)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout()
            ).bottom
            if (bottomInset != lastNavigationBarBottomInset) {
                lastNavigationBarBottomInset = bottomInset
                (compose as? AndroidComposePageDelegate)?.refreshSafeAreaInsets()
            }
            insets
        }
    }

    open fun createComposePage() {
        val start = System.currentTimeMillis()
        val pageArgs = intent.getSerializableExtra("pageArgs") as? IComposePageArgs
        require(pageArgs != null) {
            "传参中没有'pageArgs'，请检查！"
        }

        (pageArgs as? IComposeDemoPage)?.runInDemo = true

        val pageName = intent.getStringExtra("pageName")
        require(!pageName.isNullOrEmpty()) {
            "传参中没有'pageName'，请检查！"
        }

        compose.onCreate(
            this.application,
            findViewById(R.id.content),
            pageArgs,
            pageName,
            onFirstFrame = {
                val cost = System.currentTimeMillis() - start
                qnLogcat()?.logD("ComposePage", "onFirstFrame: $cost")
            }
        )
    }

    private fun createComposePageIfNeeded() {
        if (composePageCreated) {
            return
        }
        composePageCreated = true
        createComposePage()
        QnPlatformLogic.appPageStack?.onPageCreated(null, this)
        if (resumed) {
            compose.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        setupWindowInsetsListener()
        if (composePageCreated) {
            compose.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        resumed = false
        if (composePageCreated) {
            compose.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (composePageCreated) {
            compose.onDestroy()
            QnPlatformLogic.appPageStack?.onPageDestroyed(null, this)
        }
        if (windowInsetsListenerOwnerRef?.get() === this) {
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)
            windowInsetsListenerOwnerRef = null
        }

//        pageStack.pop()

    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_BACK || event.action != MotionEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event)
        }
        if (composePageCreated && compose.onDispatchBackEvent()) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (composePageCreated) {
            compose.onDispatchTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        KRCalendarReminderModule.handlePermissionsResult(requestCode, grantResults)
        handleAndroidAppLocationPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleSelectImageActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        notifySystemConfigurationChanged(newConfig)
        qnLogcat()?.logD("ComposePage", "onConfigurationChanged: ${newConfig}")
    }

}
