package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

interface IStatusBarController {
    fun setWhiteBar()
    fun setBlackBar()
    fun resetStatusBar()
    fun setCustomBar(textColor: String, backgroundColor: String)
    fun setStatusBarVisibility(visible: Boolean)
}

enum class StatusBarStyle {
    LIGHT,
    DARK
}

val statusBarController
    get() = QnPlatformLogic.statusBarController ?: DefaultAppStatusBarController()

class DefaultAppStatusBarController : IStatusBarController {
    override fun setWhiteBar() {

    }

    override fun resetStatusBar() {

    }

    override fun setBlackBar() {
    }

    override fun setCustomBar(textColor: String, backgroundColor: String) {
    }

    override fun setStatusBarVisibility(visible: Boolean) {
    }

}
