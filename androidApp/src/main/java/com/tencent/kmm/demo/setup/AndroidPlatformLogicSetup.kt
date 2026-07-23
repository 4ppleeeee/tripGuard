package com.tencent.kmm.demo.setup

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.platform.setupAndroidAppFileManager
import com.tencent.kmm.demo.KRApplication

object AndroidPlatformLogicSetup {

    @KmmInternalApi
    fun setup() {
        qnFileLog()?.logI("AndroidPlatformInit", "setup called")
        setupAndroidRes()
        setupAndroidResManager()
        setupAndroidAppRouter()
        setupAndroidAppStatus()
        setupAndroidAppFileManager(KRApplication.application)
    }
}
