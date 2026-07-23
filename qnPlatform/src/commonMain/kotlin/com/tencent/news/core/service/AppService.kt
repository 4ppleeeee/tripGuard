@file:OptIn(KmmInternalApi::class)

package com.tencent.news.core.service

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.api.IAppStatusService
import com.tencent.news.core.extension.IServiceDoc
import com.tencent.news.core.setup.get

object AppService : IServiceDoc {

    // 各种app及状态开关设置
    val status: IAppStatusService get() = IAppStatusService.get()

}