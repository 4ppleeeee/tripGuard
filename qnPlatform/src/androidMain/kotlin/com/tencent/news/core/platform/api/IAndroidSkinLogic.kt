package com.tencent.news.core.platform.api

import android.graphics.drawable.Drawable
import android.view.View
import com.tencent.news.core.annotation.KmmInternalApi

interface IAndroidSkinLogic {

    fun setBackgroundColor(view: View, day: Int, night: Int)
    fun setBackgroundDrawable(view: View, day: Drawable, night: Drawable)

    companion object {
        @KmmInternalApi
        var impl: IAndroidSkinLogic? = null
    }
}

@OptIn(KmmInternalApi::class)
fun androidSkinLogic(): IAndroidSkinLogic = IAndroidSkinLogic.impl ?: defaultAndroidSkinLogic

private val defaultAndroidSkinLogic = object : IAndroidSkinLogic {
    override fun setBackgroundColor(view: View, day: Int, night: Int) {
        view.setBackgroundColor(day)
    }

    override fun setBackgroundDrawable(view: View, day: Drawable, night: Drawable) {
        view.setBackgroundDrawable(day)
    }
}