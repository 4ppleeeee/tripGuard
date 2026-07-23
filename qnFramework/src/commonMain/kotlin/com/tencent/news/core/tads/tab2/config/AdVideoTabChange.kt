package com.tencent.news.core.tads.tab2.config

import kotlin.jvm.JvmStatic


object AdVideoTabChange {

    private var changeState = 0

    fun isHideByTabChange(): Boolean = changeState == 1

    @JvmStatic
    fun onHideByTabChange() {
        changeState = 1
    }

    fun resetTabChangeState() {
        changeState = 0
    }
}