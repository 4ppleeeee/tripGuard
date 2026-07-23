package com.tencent.news.core.platform

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.provider.Settings
import android.view.OrientationEventListener
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.platform.api.IAppWindow
import com.tencent.news.core.platform.api.IAppWindowOrientationSensor
import com.tencent.news.core.platform.api.IDeviceOrientationListener
import com.tencent.news.core.platform.api.INavigationBarWindow
import com.tencent.news.core.platform.api.ScreenOrientation
import com.tencent.news.core.platform.api.appTask

/**
 * Android 平台的 IAppWindow 实现
 * 通过 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON 控制屏幕常亮
 * 通过 Activity.requestedOrientation 控制屏幕方向
 * 通过 SystemUI flags / WindowInsetsController 控制全屏
 */
class AndroidAppWindow : IAppWindow, INavigationBarWindow, IAppWindowOrientationSensor {

    private val deviceOrientationListeners = mutableSetOf<IDeviceOrientationListener>()
    private var orientationEventListener: OrientationEventListener? = null
    private var latestDeviceOrientation: ScreenOrientation? = null

    companion object {
        private const val HOME_NAVIGATION_BAR_COLOR = 0xFF232327.toInt()

        private fun applyScreenOrientation(activity: Activity, orientation: ScreenOrientation) {
            val targetOrientation = when (orientation) {
                ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                ScreenOrientation.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            activity.requestedOrientation = targetOrientation
        }

        @Suppress("DEPRECATION")
        private fun enterFullScreen(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.hide(
                    android.view.WindowInsets.Type.statusBars() or
                            android.view.WindowInsets.Type.navigationBars()
                )
                activity.window.insetsController?.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else if (isAndroidOreo()) {
                activity.window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_VISIBLE
                        );
                activity.window.navigationBarColor = HOME_NAVIGATION_BAR_COLOR
            } else {
                activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
            }
        }

        @Suppress("DEPRECATION")
        private fun exitFullScreen(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.setDecorFitsSystemWindows(false)
                activity.window.insetsController?.show(
                    android.view.WindowInsets.Type.statusBars() or
                    android.view.WindowInsets.Type.navigationBars()
                )
            } else if (shouldKeepSystemBarsVisible()) {
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                activity.window.navigationBarColor = HOME_NAVIGATION_BAR_COLOR
            } else {
                activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        )
            }
        }

        @Suppress("DEPRECATION")
        private fun clearNavigationBarHiddenFlags(activity: Activity) {
            activity.window.decorView.systemUiVisibility = activity.window.decorView.systemUiVisibility and
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv() and
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv() and
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION.inv()
        }

        private fun isAndroidOreo(): Boolean {
            return Build.VERSION.SDK_INT == Build.VERSION_CODES.O ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1
        }

        private fun shouldKeepSystemBarsVisible(): Boolean {
            return Build.VERSION.SDK_INT in Build.VERSION_CODES.N..Build.VERSION_CODES.P
        }

        @Suppress("DEPRECATION")
        private fun setNavigationBarVisibility(activity: Activity, visible: Boolean) {
            if (isAndroidOreo() && !visible) {
                activity.window.navigationBarColor = HOME_NAVIGATION_BAR_COLOR
                clearNavigationBarHiddenFlags(activity)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val type = android.view.WindowInsets.Type.navigationBars()
                if (visible) {
                    activity.window.insetsController?.show(type)
                } else {
                    activity.window.insetsController?.hide(type)
                    activity.window.insetsController?.systemBarsBehavior =
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                if (visible) {
                    activity.window.decorView.systemUiVisibility =
                        activity.window.decorView.systemUiVisibility and
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv() and
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv() and
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION.inv()
                } else {
                    activity.window.decorView.systemUiVisibility =
                        activity.window.decorView.systemUiVisibility or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            }
        }
    }

    override fun keepScreenOn() {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun cancelScreenOn() {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun setScreenOrientation(orientation: ScreenOrientation) {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            applyScreenOrientation(activity, orientation)
        }
    }

    override fun getScreenOrientation(): ScreenOrientation {
        val activity = LocalKmmContext as? Activity ?: return ScreenOrientation.PORTRAIT
        val rotation = activity.windowManager.defaultDisplay.rotation
        return when (rotation) {
            android.view.Surface.ROTATION_0, android.view.Surface.ROTATION_180 -> ScreenOrientation.PORTRAIT
            android.view.Surface.ROTATION_90, android.view.Surface.ROTATION_270 -> ScreenOrientation.LANDSCAPE
            else -> ScreenOrientation.PORTRAIT
        }
    }

    override fun enterFullScreen() {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            enterFullScreen(activity)
        }
    }

    override fun exitFullScreen() {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            exitFullScreen(activity)
        }
    }

    override fun registerDeviceOrientationListener(listener: IDeviceOrientationListener): Boolean {
        val activity = LocalKmmContext as? Activity ?: return false
        deviceOrientationListeners.add(listener)
        val eventListener = orientationEventListener ?: object :
            OrientationEventListener(activity.applicationContext) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val nextOrientation =
                    orientation.toScreenOrientation(latestDeviceOrientation) ?: return

                if (latestDeviceOrientation == nextOrientation) return
                latestDeviceOrientation = nextOrientation
                deviceOrientationListeners.toList().forEach {
                    it.onDeviceOrientationChanged(nextOrientation)
                }
            }
        }.also {
            if (!it.canDetectOrientation()) {
                deviceOrientationListeners.remove(listener)
                return false
            }
            orientationEventListener = it
        }
        eventListener.enable()
        return true
    }

    override fun unregisterDeviceOrientationListener(listener: IDeviceOrientationListener) {
        if (!deviceOrientationListeners.remove(listener)) return
        if (deviceOrientationListeners.isEmpty()) {
            orientationEventListener?.disable()
            latestDeviceOrientation = null
        }
    }

    override fun unregisterAllDeviceOrientationListeners() {
        if (deviceOrientationListeners.isEmpty()) return
        deviceOrientationListeners.clear()
        orientationEventListener?.disable()
        latestDeviceOrientation = null
    }

    override fun isAutoRotationEnabled(): Boolean {
        val activity = LocalKmmContext as? Activity ?: return true
        return try {
            Settings.System.getInt(
                activity.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION
            ) == 1
        } catch (_: Settings.SettingNotFoundException) {
            true // 获取失败时默认允许旋转
        }
    }

    private fun Int.toScreenOrientation(currentOrientation: ScreenOrientation?): ScreenOrientation? {
        return when (currentOrientation) {
            ScreenOrientation.LANDSCAPE -> {
                if (isStablePortraitOrientation()) ScreenOrientation.PORTRAIT else ScreenOrientation.LANDSCAPE
            }

            ScreenOrientation.PORTRAIT -> {
                if (isStableLandscapeOrientation()) ScreenOrientation.LANDSCAPE else ScreenOrientation.PORTRAIT
            }

            ScreenOrientation.AUTO, null -> when {
                isStableLandscapeOrientation() -> ScreenOrientation.LANDSCAPE
                isStablePortraitOrientation() -> ScreenOrientation.PORTRAIT
                else -> null
            }
        }
    }

    private fun Int.isStableLandscapeOrientation(): Boolean {
        return this in 60..120 || this in 240..300
    }

    private fun Int.isStablePortraitOrientation(): Boolean {
        return this in 0..30 || this in 150..210 || this in 330..359
    }

    override fun setNavigationBarVisibility(visible: Boolean) {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            setNavigationBarVisibility(activity, visible)
        }
    }

    @Suppress("DEPRECATION")
    override fun setNavigationBarDarkButtons(isDark: Boolean) {
        appTask().runMainAction {
            val activity = LocalKmmContext as? Activity ?: return@runMainAction
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return@runMainAction
            }
            val decorView = activity.window.decorView
            decorView.systemUiVisibility = if (isDark) {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
    }

    override fun getNavigationBarHeight(): Int {
        val activity = LocalKmmContext as? Activity ?: return 0
        val resourceId = activity.resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            activity.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}
