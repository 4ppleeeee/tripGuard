package com.tencent.news.core.view.extension

import android.view.View
import android.view.ViewGroup
import com.tencent.news.core.view.extension.DpEx.dpToPx
import kotlin.reflect.cast

object ViewSizeEx {

    fun View?.setWidthPx(widthPx: Int) {
        updateLp<ViewGroup.LayoutParams> {
            width = widthPx
        }
    }

    fun View?.setHeightPx(heightPx: Int) {
        updateLp<ViewGroup.LayoutParams> {
            height = heightPx
        }
    }

    fun View?.setViewMarginPx(left: Int, top: Int, right: Int, bottom: Int) {
        updateLp<ViewGroup.MarginLayoutParams> {
            setMargins(left, top, right, bottom)
        }
    }

    fun View?.setViewMarginDp(left: Int, top: Int, right: Int, bottom: Int) {
        updateLp<ViewGroup.MarginLayoutParams> {
            setMargins(left.dpToPx(), top.dpToPx(), right.dpToPx(), bottom.dpToPx())
        }
    }

    fun View?.setPaddingPx(left: Int, top: Int, right: Int, bottom: Int) {
        this?.setPadding(left, top, right, bottom)
    }

    fun View?.setPaddingDp(left: Int, top: Int, right: Int, bottom: Int) {
        this?.setPadding(left.dpToPx(), top.dpToPx(), right.dpToPx(), bottom.dpToPx())
    }

    private inline fun <reified T : ViewGroup.LayoutParams> View?.updateLp(action: T.() -> Unit) {
        this ?: return
        val lp = layoutParams ?: return
        if (!T::class.isInstance(lp)) {
            return
        }
        T::class.cast(lp).action()
        layoutParams = lp
    }

    // 宽高比
    fun View?.getAspectRatio(): Float {
        this ?: return 0f
        if (height <= 0) return 0f
        return width.toFloat() / height
    }

}