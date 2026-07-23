package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic


interface IAppAlert {

    companion object {
        const val ALERT_SHORT_TIME = 1.5
        const val ALERT_LONG_TIME = 3.5
    }

    fun checkShowPushRemindDialog(type: String)

    /**
     * @param duration 单位秒
     */
    fun showToast(msg: String, duration: Double = ALERT_SHORT_TIME, debug: Boolean = false)

    fun showDialog(title: String, msg: String)

    fun showDecorDebugView(msg: String)

    fun hideCurrentToast()

}


fun appAlert(): IAppAlert = QnPlatformLogic.appAlert ?: emptyImpl

fun debugToast(msg: String) = runDebug {
    appAlert().showToast(msg)
}

private val emptyImpl by lazy { DefaultAppAlert() }

open class DefaultAppAlert : IAppAlert {
    override fun checkShowPushRemindDialog(type: String) {

    }

    override fun showToast(msg: String, duration: Double, debug: Boolean) {}
    override fun showDialog(title: String, msg: String) {}
    override fun showDecorDebugView(msg: String) {}
    override fun hideCurrentToast() {}
}
