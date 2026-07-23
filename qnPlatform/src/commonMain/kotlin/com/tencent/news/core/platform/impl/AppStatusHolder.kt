package com.tencent.news.core.platform.impl

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.NetStateChangeListener


// 懒加载静态持有下，防止每次调用都创建
// 同时兼顾了宿主 QnPlatformLogic.appStatus 注入的时序问题，即使注入的晚也能切换过来
@KmmInternalApi
internal object AppStatusHolder {

    private var instance: IAppStatus? = null
    private var hostInstance: IAppStatus? = null

    private val defaultImpl by lazy { AppStatusInterceptor(DefaultAppStatus()) }

    fun get(): IAppStatus {
        val hostImpl = QnPlatformLogic.appStatus
        if (hostImpl != null) {
            // 宿主实现可能在初始化或测试中被替换，缓存需要跟随最新实现刷新。
            if (hostImpl !== hostInstance) {
                hostInstance = hostImpl
                instance = AppStatusInterceptor(hostImpl)
            }
            return instance ?: defaultImpl
        }
        hostInstance = null
        instance = null
        return defaultImpl
    }

}

// 这个类主要用于拦截一些app不会变化的值：
// 尤其针对鸿蒙平台，从target读取数据会跨语言环境，影响性能
private class AppStatusInterceptor(val target: IAppStatus) : IAppStatus by target {

    private var _isDebug: Boolean? = null
    private var _isRdmDebug: Boolean? = null
    private var _isGrey: Boolean? = null

    override fun isDebug(): Boolean {
        val result = _isDebug ?: target.isDebug()
        _isDebug = result
        return result
    }

    override fun isRdmDebug(): Boolean {
        val result = _isRdmDebug ?: target.isRdmDebug()
        _isRdmDebug = result
        return result
    }

    override fun isGrey(): Boolean {
        val result = _isGrey ?: target.isGrey()
        _isGrey = result
        return result
    }

}

private class DefaultAppStatus : IAppStatus {
    override fun getDtSessionId() = ""
    override fun getQIMEI36() = "fcaa0d9bcd98f0edb484464910001aa18810"
    override fun getOAID() = ""
    override fun getTAID() = ""
    override fun getDevId(): String = ""

    override fun getVersion(): Int = 0

    override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
    }

    override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
    }
}
