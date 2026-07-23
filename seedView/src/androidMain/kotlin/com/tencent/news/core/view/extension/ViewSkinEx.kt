package com.tencent.news.core.view.extension

import android.graphics.drawable.Drawable
import android.view.View
import com.tencent.news.core.platform.api.androidSkinLogic

object ViewSkinEx {

    fun View?.setSkinBackgroundColor(day: Int, night: Int) {
        this ?: return
        androidSkinLogic().setBackgroundColor(this, day, night)
    }

    fun View?.setSkinBackgroundDrawable(day: Drawable, night: Drawable?) {
        this ?: return
        androidSkinLogic().setBackgroundDrawable(this, day, night ?: day)
    }

}