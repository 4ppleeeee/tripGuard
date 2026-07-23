package com.tencent.news.core.compose

import android.graphics.drawable.Drawable
import android.net.Uri
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.api.appStatus
import java.io.File

private typealias KuiklyCallback = (drawable: Drawable?) -> Unit

internal class KuiklyImageAdapter : IKRImageAdapter {

    override fun fetchDrawable(imageLoadOption: HRImageLoadOption, callback: KuiklyCallback) {
        fetchDrawableImpl(imageLoadOption, callback)
    }

    private fun fetchDrawableImpl(imageLoadOption: HRImageLoadOption, callback: KuiklyCallback) {
        val bridget = andComposeBridge
        if (bridget == null) {
            callback(null)
            return
        }

        var fallbackUri: String? = null
        val uri = when {
            imageLoadOption.isAssets() -> {
                val assetPath =
                    imageLoadOption.src.substring(HRImageLoadOption.SCHEME_ASSETS.length)
                // 有些包内置资源没有对应的夜间资源，这种case下兜底加载日间资源
                if (appStatus().isNightMode()) {
                    val light = assetPath.substring("dark-".length)
                    fallbackUri = Uri.parse("file:///android_asset/$light").toString()
                }

                Uri.parse("file:///android_asset/$assetPath").toString()
            }

            imageLoadOption.isFile() -> imageLoadOption.src

            imageLoadOption.isWebUrl() -> imageLoadOption.src

            else -> Uri.fromFile(File(imageLoadOption.src)).toString()
        }

        val callbackWrapper = CallbackWrapper(imageLoadOption.src, callback)

        bridget.loadDrawable(uri) { drawable, error ->
            if (drawable == null && !fallbackUri.isNullOrEmpty()) {
                bridget.loadDrawable(fallbackUri) { drawable, error ->
                    callbackWrapper.performCallback(drawable, error)
                }
            } else {
                callbackWrapper.performCallback(drawable, error)
            }
        }
    }

    // 用于限定：一次图片加载，只能回调一次；防止宿主多次回调导致图片序列乱套
    private class CallbackWrapper(val uri: String, val originCallback: KuiklyCallback) {

        private val debugPicLog = false // 需要时再打开
        private var hasCallback = false

        fun performCallback(drawable: Drawable?, error: Throwable?) {
            if (!hasCallback) {
                hasCallback = true

                originCallback(drawable)

                if (debugPicLog) {
                    if (drawable == null) {
                        ComposeViewLog.warn("Pic") { "图片加载失败：${uri}, e=$error" }
                    } else {
                        ComposeViewLog.debug("Pic") { "图片加载成功：${uri}" }
                    }
                }
            }
        }

    }

}