package com.tencent.kmm.demo.setup

import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.tencent.news.core.platform.api.appStatus

/**
 * 通知权限「引导弹窗」（原生 Dialog，全局可弹）。
 *
 * 设计稿（node 6201-57101 浅色 / 6201-57116 深色）：居中圆角卡片 + 右上圆形关闭 × + 顶部 3D 渐变铃铛
 * + 标题 + 副标题 + 橙红渐变「开启通知」按钮，支持浅色 / 深色两版。
 *
 * 由 [AndroidAppStatus.getNotificationAuthorizationStatus] 在「通知未开启 + 业务传入引导配置」时弹出，
 * 用户点「开启通知」后跳系统通知设置页（targetSdk<33，运行时 requestPermissions 无效，只能跳设置页）。
 *
 * 纯代码构建视图（wsPush / androidApp 资源合并不确定，避免 R 依赖）；
 * 铃铛走 assets 加载：浅 / 深两版图各自背景色与对应卡片色一致、叠卡正好融合（见 [bellView]）。
 */
internal object NotificationGuideDialog {

    // 「开启通知」按钮橙红渐变（对齐设计稿）。
    private val BTN_GRADIENT = intArrayOf(0xFFFF7A5C.toInt(), 0xFFFF4D4D.toInt())

    // 铃铛图标资源（从 Figma 提取的完整铃铛图）：浅色版白底(node 6162-54071)、深色版 #1F1F23 底(node 6162-54126)，
    // 各自背景色与对应弹窗卡片色一致 → 叠在卡片上正好融合。
    private const val BELL_ASSET_LIGHT = "ws_noti_guide_bell.png"
    private const val BELL_ASSET_DARK = "ws_noti_guide_bell_dark.png"

    fun show(
        activity: Activity,
        title: String,
        content: String,
        confirmText: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ) {
        if (activity.isFinishing) {
            onCancel()
            return
        }
        val dark = isNightMode(activity)
        val cardBg = if (dark) 0xFF1F1F23.toInt() else Color.WHITE
        val titleColor = if (dark) Color.WHITE else 0xFF1A1A1A.toInt()
        val descColor = if (dark) 0x99FFFFFF.toInt() else 0x99000000.toInt()
        val closeColor = if (dark) 0x80FFFFFF.toInt() else 0x4D000000.toInt()

        val dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        // 单次结算：点确认 / 取消 / 关闭只触发一次对应回调。
        var settled = false
        fun settle(confirmed: Boolean) {
            if (settled) return
            settled = true
            runCatching { dialog.dismiss() }
            if (confirmed) onConfirm() else onCancel()
        }

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16f).toFloat()
                setColor(cardBg)
            }
            setPadding(dp(activity, 20f), dp(activity, 16f), dp(activity, 20f), dp(activity, 20f))
        }

        // 右上角关闭 ×
        card.addView(
            TextView(activity).apply {
                text = "✕"
                setTextColor(closeColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.END
                setOnClickListener { settle(false) }
            },
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT),
        )

        // 顶部铃铛图标
        card.addView(
            bellView(activity, dark),
            LinearLayout.LayoutParams(dp(activity, 76f), dp(activity, 76f)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = dp(activity, 4f)
            },
        )

        // 标题
        card.addView(
            TextView(activity).apply {
                text = title
                setTextColor(titleColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                gravity = Gravity.CENTER
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            },
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(activity, 16f) },
        )

        // 副标题
        card.addView(
            TextView(activity).apply {
                text = content
                setTextColor(descColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(activity, 8f) },
        )

        // 「开启通知」按钮（橙红渐变圆角）
        card.addView(
            TextView(activity).apply {
                text = confirmText
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, BTN_GRADIENT)
                    .apply { cornerRadius = dp(activity, 24f).toFloat() }
                setOnClickListener { settle(true) }
            },
            LinearLayout.LayoutParams(MATCH_PARENT, dp(activity, 48f)).apply { topMargin = dp(activity, 20f) },
        )

        dialog.setContentView(card)
        dialog.window?.let {
            val attrs = it.attributes
            attrs.gravity = Gravity.CENTER
            attrs.width = dp(activity, 290f)
            it.attributes = attrs
        }
        runCatching { dialog.show() }.onFailure { onCancel() }
    }

    /**
     * 铃铛图标：按日夜间加载对应的完整铃铛图（assets）。
     * - 浅色：[BELL_ASSET_LIGHT]（白底，叠白色卡片融合）；
     * - 深色：[BELL_ASSET_DARK]（#1F1F23 底，叠深色卡片 #1F1F23 融合）。
     * 加载失败时回退粉红渐变圆兜底。
     */
    private fun bellView(activity: Activity, dark: Boolean): View {
        val asset = if (dark) BELL_ASSET_DARK else BELL_ASSET_LIGHT
        runCatching {
            val bitmap = activity.assets.open(asset).use { BitmapFactory.decodeStream(it) }
            return ImageView(activity).apply { setImageBitmap(bitmap) }
        }
        // 加载失败兜底：粉红渐变圆
        return View(activity).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TL_BR
                colors = intArrayOf(0xFFFFD6CC.toInt(), 0xFFFF6F61.toInt())
            }
        }
    }

    // 取 app 实际日夜间（含用户在设置里强制的浅/深色），与 AndroidAppStatus.isNightMode() 一致——
    // app 的日夜间由 isDarkMode 标志维护、不一定改系统 Configuration，故不能只看 Configuration.uiMode。
    // appStatus 异常时兜底读系统 Configuration。
    private fun isNightMode(activity: Activity): Boolean =
        runCatching { appStatus().isNightMode() }.getOrElse {
            (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        }

    private fun dp(activity: Activity, value: Float): Int =
        (value * activity.resources.displayMetrics.density + 0.5f).toInt()
}
