package com.tencent.news.core.app.extension

import com.tencent.news.core.app.api.IAppStatusService
import com.tencent.news.core.platform.api.IAppStatus

object AppStatusEx {

    // 将app版本号，4位数字格式的，例如：7730，转换成字符串格式的，例如：7.7.30
    fun IAppStatus.getAppVersionName(): String {
        val appVersion = getVersion().toString()
        if (appVersion.length != 4) {
            return appVersion
        }
        return "${appVersion[0]}.${appVersion[1]}.${appVersion.substring(2)}"
    }

    // 切换默认开发环境
    fun IAppStatusService.setRequestDefaultDev() {
        setRequestDevEnvironment(
            name = "默认开发环境",
            host = "https://dev.inews.qq.com/",
            domain = "dev.inews.qq.com"
        )
    }

}