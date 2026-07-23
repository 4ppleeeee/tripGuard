package com.tencent.news.core.ohos.setup

import com.tencent.news.core.app.asIKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.tmm.knoi.annotation.ServiceProvider
import com.tencent.tmm.knoi.type.JSValue

@ServiceProvider(singleton = true)
open class HarmonyPageStackManager {

    fun onPageCreated(contxt: JSValue) {
        QnPlatformLogic.appPageStack?.onPageCreated(null, contxt.asIKmmContext())
    }

    fun onPageDestroyed(contxt: JSValue) {
        QnPlatformLogic.appPageStack?.onPageDestroyed(null, contxt.asIKmmContext())
    }

}
