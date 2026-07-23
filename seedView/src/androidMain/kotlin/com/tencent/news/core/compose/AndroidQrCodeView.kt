package com.tencent.news.core.compose

import android.content.Context
import android.widget.ImageView
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport

abstract class AndroidQrCodeView(context: Context) : ImageView(context), IKuiklyRenderViewExport {

    override fun setProp(propKey: String, propValue: Any): Boolean {
        when (propKey) {
            "content" -> setContent(propValue as String)
            "foregroundColor" -> setColor(propValue as String)
            "backgroundColor" -> {
                setQrBackgroundColor(propValue as String)
            }

            else -> super.setProp(propKey, propValue)
        }
        return true
    }

    abstract fun setContent(content: String)

    abstract fun setColor(color: String)

    abstract fun setQrBackgroundColor(color: String)
    
    
}