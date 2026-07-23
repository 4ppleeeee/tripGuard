package com.tencent.news.core.view.extension

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

fun View?.setRoundCornerInPx(cornerInPx: Int) {
    this ?: return
    if (!clipToOutline) {
        clipToOutline = true
    }

    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            view ?: return
            outline?.setRoundRect(0, 0, view.width, view.height, cornerInPx.toFloat())
        }
    }

    invalidateOutline()
}