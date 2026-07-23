package com.tencent.news.core.view.extension

import com.tencent.news.core.platform.api.androidViewLogic

object DpEx {

    fun Number.dpToPx(): Int = androidViewLogic().dpToPx(this.toFloat())
    fun Number.dpToPxNoScale(): Int = androidViewLogic().dpToPxNoScale(this.toFloat())
//    fun Number.pxToDp(): Int = androidViewLogic().pxToDp(this.toInt())

}