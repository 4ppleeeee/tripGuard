package com.tencent.kmm.demo.startup.sdk.kuikly

import android.content.Context
import android.graphics.Typeface
import com.tencent.kuikly.core.render.android.adapter.IKRFontAdapter

class KRFontAdapter(
    private val context: Context,
) : IKRFontAdapter {

    override fun getTypeface(fontFamily: String, result: (Typeface?) -> Unit) {
        if (fontFamily.isEmpty()) {
            result(null)
        } else {
            var tfe: Typeface? = null
            when (fontFamily) {
                "Qvideo Digit" -> {
                    tfe = Typeface.createFromAsset(
                        context.assets,
                        "fonts/$fontFamily.ttf"
                    )
                }
            }
            result(tfe)
        }
    }
}
