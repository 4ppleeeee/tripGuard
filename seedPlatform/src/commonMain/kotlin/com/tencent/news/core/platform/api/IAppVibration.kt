package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

interface IAppVibration {
    fun triggerVibration()
}

val appVibration: IAppVibration by lazy {
    AppVibration
}

private object AppVibration : IAppVibration {
    override fun triggerVibration() {
        val platformVibration = QnPlatformLogic.vibration
        if (platformVibration !== this) {
            platformVibration?.triggerVibration()
        }
    }
}
