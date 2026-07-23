package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

interface IAppVibration {
    fun triggerVibration()
}

val appVibration: IAppVibration by lazy {
    AppVibration(QnPlatformLogic.vibration)
}

private class AppVibration(private val platformVibration: IAppVibration?) : IAppVibration {
    override fun triggerVibration() {
        platformVibration?.triggerVibration()
    }
}