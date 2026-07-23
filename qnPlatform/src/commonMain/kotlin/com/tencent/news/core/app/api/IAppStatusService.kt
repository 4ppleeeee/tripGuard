package com.tencent.news.core.app.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.model.AppDevServer
import com.tencent.news.core.platform.api.export.IExportAppStatus
import com.tencent.news.core.setup.ILazyImplHolder
import com.tencent.news.core.setup.LazyImpl

// 注意：这个接口就先不单独拆分export，直接都使用基础数据类型，和鸿蒙统一一个接口
interface IAppStatusService : IAppStatusConsumer, IAppStatusProvider {

    companion object : ILazyImplHolder<IAppStatusService> {
        @KmmInternalApi
        override lateinit var instance: LazyImpl<IAppStatusService>
    }
}

interface IAppStatusProvider : IExportAppStatus {
    fun getDevServer(): AppDevServer?

    // 仅用于独立调试入口启动时恢复已持久化的 QnCore 网络环境；主端入口应同步宿主自身环境。
    fun restoreRequestDevEnvironment(): AppDevServer?
}

interface IAppStatusConsumer {

    fun setVersion(version: Int)

    // 【设备身份标识】：
    fun setDtSessionId(sessionId: String) {}                        // 大同公参：dt_ussn
    fun setQIMEI36(qimei: String)                                   // QIMEI36设备标识
    fun setOAID(oaid: String)                                       // OAID设备标识
    fun setTAID(taid: String)                                       // TAID设备标识
    fun setDevId(devId: String = "")                                // 设备ID

    // 【debug】相关：
    fun setDebug(isDebug: Boolean)                                  // 是否是debug包
    fun setIntegrationMode(isIntegrationMode: Boolean) {}           // 是否开启了集成测试模式（用于一些调试逻辑判断）

    // 【应用内各种模式切换】：
    fun setNightMode(isNightMode: Boolean)                          // 夜间模式
    fun setTextScaleLevel(level: Int)

    fun setPersonalizedSwitch(enabled: Boolean)                     // 个性化推荐开关（注意：默认是打开的）
    fun setInNewsTopManualMode(enabled: Boolean)                    // 要闻主编精选模式
    fun setInReviewMode(enabled: Boolean) {}                        // 审核模式

    // 【开发选项】接入层-开发环境
    fun setRequestDevEnvironment(
        name: String = "",                                          // 服务器名，例如：广告开发环境
        host: String = "",                                          // 请求的域名，例如：https://dev.inews.qq.com/
        domain: String = "",                                        // 对应Header里的 Request-Domain，例如：ad_dev_jinkuangyan.epc.webdev.com
        ip: String = "",                                            // 对应Header里的 Request-Ip，例如：9.*.*.27
        envName: String = "",                                       // 对应Header里的 env-name（后来新增的能力）
    )

}
