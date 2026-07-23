package com.tencent.kmm.demo.setup

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.DisplayMetrics
import androidx.core.graphics.drawable.toDrawable
import com.squareup.picasso.Picasso
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.news.core.compose.AndroidLottieView
import com.tencent.news.core.compose.AndroidQrCodeView
import com.tencent.news.core.compose.DrawableCallback
import com.tencent.news.core.compose.IAndroidComposeBridge
import com.tencent.news.core.compose.KuiklyFontAdapter
import com.tencent.news.core.compose.andComposeBridge
import com.tencent.news.core.extension.safeDecodeStringMap
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.serializer.KtJson
import com.tencent.kmm.demo.KRApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


internal fun setupAndroidRes() {
    andComposeBridge = AndroidComposeBridge
    // 提前注册 KuiklyFontAdapter，防止 TypeFaceUtil 在 adapter 注册前缓存错误的系统字体
    // 若等到第一个 Compose 页面 onCreate 才注册，TypeFaceUtil 的 LruCache 可能已经缓存了
    // Typeface.create("iconfont", NORMAL) 返回的系统字体，导致 iconfont 在部分机器上显示为 "..."
    if (KuiklyRenderAdapterManager.krFontAdapter == null) {
        KuiklyRenderAdapterManager.krFontAdapter = KuiklyFontAdapter(KRApplication.application)
    }
}

private object AndroidComposeBridge : IAndroidComposeBridge {

    private const val TAG = "AndroidComposeBridge"
    private const val MIN_VALID_DENSITY = 0.01F
    private val app get() = KRApplication.application

    override fun loadDrawable(uri: String?, callback: DrawableCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 注意：不要用 into 方法通过 Target 接收回调，Picasso 是用弱引用持有的，匿名内部类可能收不到
                val bitmap = Picasso.get().load(uri).get()
                withContext(Dispatchers.Main) {
                    callback(bitmap.toDrawable(app.resources), null)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    callback(null, e)
                }
            }
        }
    }

    override fun createIconFontTypeface(): Typeface? {
        return try {
            val typeface = Typeface.createFromAsset(app.resources.assets, "iconfont/ws_iconfont_new_style.ttf")
            qnFileLog()?.logI(TAG, "createIconFontTypeface success, typeface=$typeface")
            typeface
        } catch (e: Throwable) {
            qnFileLog()?.logE(TAG, "createIconFontTypeface failed", e)
            null
        }
    }

    override fun getIconFontMapping(): Map<String, String> {
        return try {
            val result = app.resources.assets.open("iconfont/iconfont_new_style.json")
                .use {
                    KtJson.safeDecodeStringMap(String(it.readBytes()))
                } ?: emptyMap()
            qnFileLog()?.logI(TAG, "getIconFontMapping success, size=${result.size}")
            result
        } catch (e: Throwable) {
            qnFileLog()?.logE(TAG, "getIconFontMapping failed", e)
            emptyMap()
        }
    }

    override fun createLottieView(context: Context): AndroidLottieView {
        return WsAndroidLottieView(context)
    }

    override fun getDisplayMetrics(): DisplayMetrics {
        val systemMetrics = Resources.getSystem().displayMetrics
        // 字号缩放由 DensityAdapter 按“跟随系统”或“App 设置”二选一处理。
        // 这里保持基础 density 为系统物理密度，避免系统 fontScale 与 App 字号梯度叠乘。
        val adaptDensity = systemMetrics.density
        return DisplayMetrics().apply {
            setTo(app.resources.displayMetrics)
            if (adaptDensity > MIN_VALID_DENSITY) {
                density = adaptDensity
                scaledDensity = density
            }
        }
    }

    override fun createQrCodeView(context: Context): AndroidQrCodeView {
        return AndroidQrCodeViewImpl(context)
    }

    override fun createTypeface(name: String): Typeface? {
        if (name.isEmpty()) {
            return null
        }
        return try {
            val typeface = Typeface.createFromAsset(app.resources.assets, "fonts/$name.ttf")
            qnFileLog()?.logI(TAG, "createTypeface success, name=$name, typeface=$typeface")
            typeface
        } catch (e: Throwable) {
            qnFileLog()?.logE(TAG, "createTypeface failed, name=$name", e)
            null
        }
    }

}
