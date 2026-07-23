package com.tencent.kmm.demo.view

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.WindowCompat
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * 沉浸式体验工具类
 *
 * 参考腾讯新闻 ImmersiveHelper 实现，提供以下能力：
 * - 状态栏透明 / 全屏沉浸模式
 * - 状态栏字体深色 / 浅色切换
 * - 小米 MIUI、魅族 Flyme 等特殊机型适配
 * - 导航栏颜色与深色模式控制
 * - View 高度适配（paddingTop / marginTop / fitsSystemWindows）
 */
object ImmersiveHelper {

    private const val TAG = "ImmersiveHelper"

    /** 状态栏深色字体标记（API 23 以下使用） */
    const val SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 0x00002000

    /** 适配模式：fitsSystemWindows */
    const val FIT_SYSTEM = 1

    /** 适配模式：增加 paddingTop */
    const val PADDING_TOP = 2

    /** 适配模式：增加 marginTop */
    const val MARGIN_TOP = 3

    private const val MODE_TRANSPARENT = 0
    private const val MODE_TRANSPARENT_DARK_TEXT = 1
    private const val MODE_CLEAR_DARK_TEXT = 2
    private const val MODE_DARK_TEXT = 3

    /** 是否为有效的 MIUI 版本 */
    var isValidMiuiVersion: Boolean = false
        private set

    /** 是否为魅族系统 */
    var isMeizuOS: Boolean = false
        private set

    /** 系统状态栏高度（沉浸模式开启后记录） */
    var statusBarHeight: Int = 0

    init {
        detectDeviceBrand()
    }

    /**
     * 检测设备品牌，判断是否为 MIUI 或魅族
     */
    private fun detectDeviceBrand() {
        try {
            val brand = Build.BRAND
            if (!TextUtils.isEmpty(brand) && brand.equals("meizu", ignoreCase = true)) {
                isMeizuOS = true
            }
            val sysClass = Class.forName("android.os.SystemProperties")
            val methodGetter = sysClass.getDeclaredMethod("get", String::class.java)
            val miuiVerName = methodGetter.invoke(sysClass, "ro.miui.ui.version.name") as? String
            Log.i(TAG, "ro.miui.ui.version.name = $miuiVerName")
            isValidMiuiVersion = !TextUtils.isEmpty(miuiVerName)
        } catch (e: Exception) {
            Log.i(TAG, "ro.miui.ui.version.name = null")
            isValidMiuiVersion = false
        }
    }

    // ==================== 沉浸模式开启 ====================

    /**
     * 开启沉浸模式
     *
     * @param window 当前 Activity 的 Window
     * @param context 当前 Context，用于获取状态栏高度
     * @param isFullScreen 是否全屏（拉伸到状态栏下方）
     * @param isLightStatusBar 状态栏是否使用深色字体（亮色背景时为 true）
     * @param statusBarColor 状态栏背景色，默认透明
     * @param navigationBarColor 导航栏背景色，默认透明
     * @return 是否成功开启沉浸模式
     */
    fun enableImmersiveMode(
        window: Window,
        context: Context,
        isFullScreen: Boolean = true,
        isLightStatusBar: Boolean = true,
        statusBarColor: Int = Color.TRANSPARENT,
        navigationBarColor: Int = Color.TRANSPARENT
    ): Boolean {

        // 清除半透明状态栏标记，否则 setStatusBarColor 失效
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (shouldKeepSystemBarsVisible()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        // 开启绘制系统栏背景标记
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // 设置全屏与状态栏字体模式
        applyImmersiveFlags(window, isFullScreen, isLightStatusBar)

        // 设置状态栏颜色
        window.statusBarColor = statusBarColor
        // Android 7-9 保留系统栏默认表现，不主动把内容延伸到系统栏区域。
        if (shouldKeepSystemBarsVisible()) {
            if (shouldForceOpaqueNavigationBar()) {
                window.navigationBarColor = Color.BLACK
            }
        } else {
            window.navigationBarColor = navigationBarColor
        }
        disableNavigationBarContrastEnforced(window)

        // 针对 MIUI 设备额外处理状态栏字体颜色
        if (isValidMiuiVersion) {
            val mode = if (isLightStatusBar) MODE_DARK_TEXT else MODE_CLEAR_DARK_TEXT
            if (!setMiuiStatusBarFontColor(window, mode)) {
                Log.i(TAG, "setMiuiStatusBarFontColor failed")
            }
        }

        // 针对魅族设备额外处理状态栏字体颜色
        if (isMeizuOS) {
            setMeizuStatusBarFontColor(window, isFullScreen, isLightStatusBar)
        }

        statusBarHeight = getStatusBarHeight(context)
        Log.i(TAG, "enableImmersiveMode success, statusBarHeight=$statusBarHeight")
        return true
    }

    /**
     * 设置沉浸式 SystemUI 标记
     *
     * Android 11+ 使用 [WindowCompat.setDecorFitsSystemWindows] 新 API，
     * 使内容区域延伸到状态栏和导航栏下方（edge-to-edge）。
     * 低版本兼容旧的 systemUiVisibility 方式。Android 7-9 保留系统栏，
     * 避免部分 ROM（如 Flyme Android 7）把系统栏区域绘制成内容背景。
     */
    private fun applyImmersiveFlags(
        window: Window,
        isFullScreen: Boolean,
        isLightStatusBar: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用新 API：内容延伸到系统栏下方
            WindowCompat.setDecorFitsSystemWindows(window, !isFullScreen)
        } else {
            // 低版本兼容旧方式
            window.decorView.applySystemUiFlag(isFullScreen, View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            // Android 7-9 保留系统状态栏，不让内容延伸到状态栏下方。
            window.decorView.applySystemUiFlag(
                isFullScreen && !shouldKeepSystemBarsVisible(),
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
            // Android 7-9 保留系统导航栏，不让内容延伸到导航栏下方。
            window.decorView.applySystemUiFlag(
                isFullScreen && !shouldKeepSystemBarsVisible(),
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
        window.decorView.applySystemUiFlag(isLightStatusBar, SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }

    private fun shouldKeepSystemBarsVisible(): Boolean {
        return Build.VERSION.SDK_INT in Build.VERSION_CODES.N..Build.VERSION_CODES.P
    }

    private fun shouldForceOpaqueNavigationBar(): Boolean {
        return Build.VERSION.SDK_INT in Build.VERSION_CODES.N..Build.VERSION_CODES.N_MR1
    }

    /**
     * 关闭 Android 10+ 系统为透明导航栏自动叠加的对比度蒙层。
     */
    private fun disableNavigationBarContrastEnforced(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    // ==================== 状态栏字体颜色 ====================

    /**
     * 设置状态栏字体为深色或浅色
     *
     * @param window 当前 Window
     * @param isLight true 为深色字体（亮色背景），false 为浅色字体（暗色背景）
     */
    fun setStatusBarLightMode(window: Window, isLight: Boolean) {
        window.decorView.applySystemUiFlag(isLight, SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        if (isValidMiuiVersion) {
            val mode = if (isLight) MODE_DARK_TEXT else MODE_CLEAR_DARK_TEXT
            setMiuiStatusBarFontColor(window, mode)
        }
        if (isMeizuOS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 魅族 7.0 以上走标准方式即可
        } else if (isMeizuOS) {
            setMeizuStatusBarFontColorLegacy(window, isLight)
        }
    }

    /**
     * 设置导航栏深色模式
     *
     * @param window 当前 Window
     * @param isDark true 为深色图标（亮色背景）
     * @param color 导航栏背景色，不传则根据 isDark 自动选择黑/白
     */
    fun setNavigationBarMode(window: Window, isDark: Boolean, color: Int? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || shouldKeepSystemBarsVisible()) return
        val vis = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = if (isDark) {
            vis or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            vis and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        window.navigationBarColor = color ?: if (isDark) Color.WHITE else Color.BLACK
    }

    // ==================== MIUI 适配 ====================

    /**
     * 设置小米 MIUI 状态栏字体颜色
     */
    private fun setMiuiStatusBarFontColor(window: Window, mode: Int): Boolean {
        if (!isValidMiuiVersion) return false
        // MIUI 的 setExtraFlags 在 Android Q 以上被列入黑名单
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return false

        return try {
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val tranceFlag = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT").getInt(layoutParams)
            val darkModeFlag = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE").getInt(layoutParams)
            val extraFlagField = window.javaClass.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            when (mode) {
                MODE_TRANSPARENT -> extraFlagField.invoke(window, tranceFlag, tranceFlag)
                MODE_TRANSPARENT_DARK_TEXT -> extraFlagField.invoke(window, tranceFlag or darkModeFlag, tranceFlag or darkModeFlag)
                MODE_CLEAR_DARK_TEXT -> extraFlagField.invoke(window, 0, darkModeFlag)
                MODE_DARK_TEXT -> extraFlagField.invoke(window, darkModeFlag, darkModeFlag)
                else -> return false
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "setMiuiStatusBarFontColor failed: ${e.message}")
            false
        }
    }

    // ==================== 魅族适配 ====================

    /**
     * 设置魅族状态栏字体颜色
     */
    private fun setMeizuStatusBarFontColor(
        window: Window,
        isFullScreen: Boolean,
        isLight: Boolean
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.decorView.applySystemUiFlag(
                isFullScreen && !shouldKeepSystemBarsVisible(),
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
            window.decorView.applySystemUiFlag(isLight, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            true
        } else {
            setMeizuStatusBarFontColorLegacy(window, isLight)
        }
    }

    /**
     * 魅族旧版本（< Android N）设置状态栏字体颜色
     */
    private fun setMeizuStatusBarFontColorLegacy(window: Window, isDark: Boolean): Boolean {
        return try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java
                .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java
                .getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            val value = meizuFlags.getInt(lp)
            meizuFlags.setInt(lp, if (isDark) value or bit else value and bit.inv())
            window.attributes = lp
            true
        } catch (e: Exception) {
            Log.e(TAG, "Meizu set status bar font color failed: $e")
            false
        }
    }

    // ==================== View 适配 ====================

    /**
     * 为指定 View 设置沉浸式适配（增加状态栏高度的偏移）
     *
     * @param view 需要适配的 View
     * @param mode 适配模式：[FIT_SYSTEM] / [PADDING_TOP] / [MARGIN_TOP]
     */
    fun setImmersiveAdaptation(view: View?, mode: Int) {
        view ?: return
        when (mode) {
            FIT_SYSTEM -> view.fitsSystemWindows = true
            PADDING_TOP -> adjustPaddingTop(view, true)
            MARGIN_TOP -> adjustMarginTop(view, true)
        }
    }

    /**
     * 取消沉浸式适配（减去状态栏高度的偏移）
     *
     * @param view 需要取消适配的 View
     * @param mode 适配模式：[PADDING_TOP] / [MARGIN_TOP]
     */
    fun clearImmersiveAdaptation(view: View?, mode: Int) {
        view ?: return
        when (mode) {
            PADDING_TOP -> adjustPaddingTop(view, false)
            MARGIN_TOP -> adjustMarginTop(view, false)
        }
    }

    private fun adjustPaddingTop(view: View, increase: Boolean) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val paddingTop = if (increase) {
                    view.paddingTop + statusBarHeight
                } else {
                    view.paddingTop - statusBarHeight
                }
                view.setPadding(view.paddingLeft, paddingTop, view.paddingRight, view.paddingBottom)
                view.requestLayout()
                return true
            }
        })
    }

    private fun adjustMarginTop(view: View, increase: Boolean) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val lp = view.layoutParams
                if (lp is ViewGroup.MarginLayoutParams) {
                    val marginTop = if (increase) {
                        lp.topMargin + statusBarHeight
                    } else {
                        lp.topMargin - statusBarHeight
                    }
                    lp.setMargins(lp.leftMargin, marginTop, lp.rightMargin, lp.bottomMargin)
                    view.layoutParams = lp
                }
                view.requestLayout()
                return true
            }
        })
    }

    // ==================== 工具方法 ====================

    /**
     * 获取系统状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 判断当前是否为全屏透明沉浸模式
     */
    fun isFullScreenTransparentImmersive(activity: Activity): Boolean {
        return activity.window.statusBarColor == Color.TRANSPARENT
    }
}

/**
 * View 扩展：追加或移除 SystemUI 标记
 *
 * @param append true 追加标记，false 移除标记
 * @param flag 要操作的 SystemUI 标记
 */
fun View?.applySystemUiFlag(append: Boolean, flag: Int) {
    this ?: return
    systemUiVisibility = if (append) {
        systemUiVisibility or flag
    } else {
        systemUiVisibility and flag.inv()
    }
}
