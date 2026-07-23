package com.tencent.news.core.compose

import android.app.Application
import android.graphics.Typeface
import android.util.DisplayMetrics
import com.tencent.kuikly.core.render.android.adapter.IKRFontAdapter
import com.tencent.news.core.compose.scaffold.theme.Fonts

class KuiklyFontAdapter(val app: Application) : IKRFontAdapter {

    override fun getDisplayMetrics(useHostDisplayMetrics: Boolean?): DisplayMetrics? {
        return andComposeBridge?.getDisplayMetrics()
    }

    override fun getTypeface(fontFamily: String, result: (Typeface?) -> Unit) {
        val typeface: Typeface? = when (fontFamily) {
            Fonts.ICONFONT.family -> andComposeBridge?.createIconFontTypeface()
            Fonts.DEFAULT.family -> Typeface.DEFAULT
            else -> andComposeBridge?.createTypeface(fontFamily)
        }
        result(typeface)
    }

    override fun scaleFontSize(fontSize: Float): Float {
        return 1F
    }
}