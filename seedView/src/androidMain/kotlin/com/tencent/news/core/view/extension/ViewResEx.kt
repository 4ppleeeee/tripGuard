package com.tencent.news.core.view.extension

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.tencent.news.core.extension.KColor
import com.tencent.news.core.platform.api.isDebug

object ViewResEx {

    fun View?.setDebugBorder(colorStr: String = "#ff0000", borderWidth: Int = 1) {
        if (!isDebug()) {
            return
        }
        this?.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setStroke(borderWidth, KColor.toColorInt(colorStr))
            setColor(Color.TRANSPARENT)
        }
    }

}