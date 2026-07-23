package com.tencent.news.core.compose

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics

var andComposeBridge: IAndroidComposeBridge? = null
    get() {
        return field ?: throw IllegalStateException(
            "请先初始化'IAndroidComposeBridge.andComposeBridge'"
        )
    }


typealias DrawableCallback = (Drawable?, Throwable?) -> Unit

interface IAndroidComposeBridge {

    /**
     * 通过uri加载图片，uri可能是网图也可能是assets图片
     */
    fun loadDrawable(uri: String?, callback: DrawableCallback)

    /**
     * 创建iconFont字体
     */
    fun createIconFontTypeface(): Typeface?

    fun createTypeface(name: String): Typeface?

    /**
     * 获取iconFont字体名字和字的映射关系
     */
    fun getIconFontMapping(): Map<String, String>

    /**
     * 创建LottieView
     */
    fun createLottieView(context: Context): AndroidLottieView

    fun getDisplayMetrics(): DisplayMetrics? = null

    /**
     * 创建截屏组件
     */
    fun createScreenshotView(context: Context) = AndroidScreenshotView(context)

    fun createQrCodeView(context: Context): AndroidQrCodeView

}

