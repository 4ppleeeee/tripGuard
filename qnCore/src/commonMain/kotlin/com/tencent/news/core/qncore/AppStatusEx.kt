package com.tencent.news.core.qncore

import com.tencent.news.core.app.api.IAppStatusService

object QnCoreAppStatusEx {

    // 新闻业务默认开发环境；业务侧负责决定是否注入。
    fun IAppStatusService.setRequestDefaultDev() {
        setRequestDevEnvironment(
            name = "默认开发环境",
            host = AppHost.DEV_HOST,
            domain = "dev.inews.qq.com"
        )
    }
}
