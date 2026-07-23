package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnFrameworkLogic


/**
 * @Author: junxiaoliao
 * @Date: 2025/1/20
 */

interface IPushSwitch {

    // 系统是否打开推送
    fun isSystemPushOpen(context: IKmmContext?, callback: (Boolean) -> Unit)

    // app内是否打开推送
    fun isAppPushOpen(): Boolean

    // 开启本地推送
    fun enableLocalPush(isShowDialog: Boolean) {}
}

@OptIn(KmmInternalApi::class)
fun pushSwitch(): IPushSwitch = QnFrameworkLogic.pushSwitch ?: defaultPushSwitch

fun IPushSwitch.isPushGuideOpen(context: IKmmContext?, callback: (Boolean) -> Unit) {
    if (!pushSwitch().isAppPushOpen()) {
        callback.invoke(false)
        return
    }
    isSystemPushOpen(context, callback)
}

private val defaultPushSwitch by lazy { DefaultPushSwitch() }

internal class DefaultPushSwitch : IPushSwitch {
    override fun isSystemPushOpen(context: IKmmContext?, callback: (Boolean) -> Unit) {
        callback(false)
    }

    override fun isAppPushOpen() = false

    override fun enableLocalPush(isShowDialog: Boolean) {

    }
}
