package com.tencent.news.core.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.util.Log
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.news.core.compose.view.SCREENSHOT_PATH
import java.io.File

open class AndroidScreenshotView(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {

    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            "take" -> {
                take(callback)
            }

            else -> super.call(method, params, callback)
        }
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            "take" -> {
                take(callback)
            }

            else -> super.call(method, params, callback)
        }
    }

    open fun take(callback: KuiklyRenderCallback?) {
        callback ?: return

        if (width <= 0 || height <= 0) {
            return
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)

        val fileDir = File(context.externalCacheDir, "data/screenshot")
        val file = File(fileDir, "${System.currentTimeMillis()}.jpg")
        saveBitmap(bitmap, file)
        Log.d("AndroidScreenshot", file.absolutePath)
        callback.invoke(mapOf(SCREENSHOT_PATH to file.absolutePath))
    }

    open fun saveBitmap(bitmap: Bitmap, file: File) {
        val dir = file.parentFile ?: return
        if (!dir.exists()) {
            dir.mkdirs()
        }

        file.outputStream().use {
            bitmap.compress(CompressFormat.JPEG, 100, it)
            it.flush()
        }
    }

}