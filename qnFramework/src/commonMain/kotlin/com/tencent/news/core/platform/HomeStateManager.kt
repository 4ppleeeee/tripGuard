package com.tencent.news.core.platform

import com.tencent.news.core.annotation.OnlyHostInvokeApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.channel.constants.NewsChannel
import com.tencent.news.core.extension.ConcurrentList
import com.tencent.news.core.list.trace.LifecycleLog
import com.tencent.news.core.platform.api.IKmmHomePage
import com.tencent.news.core.platform.api.appPageStack
import com.tencent.news.core.platform.api.getShiplySwitch

/**
 * 维护首页的状态，分发四大tab的展示，tab1二级频道的展示隐藏
 */
object HomeStateManager {
    private val enableSnapshotDispatchListener by lazy {
        getShiplySwitch("enable_home_state_snapshot_dispatch_listener", true)
    }
    private var bottomTabId = ""
    private var tabOneSubPageId = ""
    private var bottomConfigList: List<BottomTabConfig>? = null
    private val listeners = ConcurrentList<IKmmHomeStateChangeListener>()

    @OnlyHostInvokeApi(msg = "由业务侧分发，不可调用")
    fun onBottomTabChange(context: IKmmContext, tabId: String) {
        log("onBottomTabChange $tabId")
        dispatchListeners {
            it.onBottomTabChange(context, tabId)
        }
    }

    @OnlyHostInvokeApi(msg = "由业务侧分发，不可调用")
    fun onBottomTabShow(context: IKmmContext, tabId: String) {
        this.bottomTabId = tabId
        log("onBottomTabShow $tabId")
        dispatchListeners {
            it.onBottomTabShow(context, tabId)
        }
    }

    @OnlyHostInvokeApi(msg = "由业务侧分发，不可调用")
    fun onBottomTabHide(context: IKmmContext, tabId: String) {
        log("onBottomTabHide $tabId")
        dispatchListeners {
            it.onBottomTabHide(context, tabId)
        }
    }

    @OnlyHostInvokeApi(msg = "由业务侧分发，不可调用")
    fun onTabOneSubPageShow(context: IKmmContext, channelId: String) {
        log("onTabOneSubPageShow $channelId")
        this.tabOneSubPageId = channelId
        dispatchListeners {
            it.onTabOneSubPageShow(context, channelId)
        }
    }

    @OnlyHostInvokeApi(msg = "由业务侧分发，不可调用")
    fun onTabOneSubPageHide(context: IKmmContext, channelId: String) {
        log("onTabOneSubPageHide $channelId")
        dispatchListeners {
            it.onTabOneSubPageHide(context, channelId)
        }
    }

    @OnlyHostInvokeApi(msg = "由业务侧分发，不可调用")
    fun onBottomTabConfigUpdate(configList: List<BottomTabConfig>) {
        bottomConfigList = configList
        log("onBottomTabConfigUpdate $configList")
        dispatchListeners {
            it.onBottomTabConfigUpdate(configList)
        }
    }

    fun addStateChangeListener(listener: IKmmHomeStateChangeListener) {
        log("addStateChangeListener $listener")
        listeners.add(listener)
    }

    fun removeStateChangeListener(listener: IKmmHomeStateChangeListener) {
        log("removeStateChangeListener $listener")
        listeners.remove(listener)
    }

    @OnlyHostInvokeApi(msg = "android业务侧对HomeStateManager进行了包装，需要操作list，因此暴露出去，其他地方禁止使用")
    fun getListeners() = listeners

    /**
     * 当前是否在tab1
     */
    fun isInTabOne(): Boolean = isHomePageActive() && bottomTabId == NewsChannel.NEWS_TOP

    /**
     * 获取到tab1当前的频道id，若当前不在tab1则返回null
     */
    fun getCurTabOneSubPageId(): String? {
        if (isInTabOne()) {
            return tabOneSubPageId
        }
        return null
    }

    /**
     * 获取到tab1当前的四大tabId，若当前不在首页返回null
     */
    fun geCurBottomTabId(): String? {
        if (isHomePageActive()) {
            return bottomTabId
        }
        return null
    }

    /**
     * 直接获取当前四大tabId（不校验首页是否在前台）
     */
    fun getCurrentSelectBottomTabId(): String {
        return bottomTabId
    }

    /**
     * 获取首页页面
     */
    fun getHomePage(): IKmmHomePage? {
        return appPageStack()?.getAllPages()?.find {
            it is IKmmHomePage
        } as? IKmmHomePage
    }

    /**
     * 首页页面是否在前台
     */
    fun isHomePageActive(): Boolean {
        val homePage = getHomePage() ?: return false
        return appPageStack()?.isPageActive(homePage) == true
    }

    private fun log(msg: String) {
        LifecycleLog.debug("HomeStateManager") { msg }
    }

    // 避免持有 ConcurrentList 锁执行外部 listener，部分 listener 会同步调用宿主/JS。
    private fun dispatchListeners(action: (IKmmHomeStateChangeListener) -> Unit) {
        if (enableSnapshotDispatchListener) {
            listeners.shallowCopyList().forEach { action(it) }
        } else {
            listeners.forEach { action(it) }
        }
    }
}


interface IKmmHomeStateChangeListener {
    fun onBottomTabChange(context: IKmmContext, tabId: String) { }

    fun onBottomTabShow(context: IKmmContext, tabId: String) { }

    fun onBottomTabHide(context: IKmmContext, tabId: String) { }

    fun onTabOneSubPageShow(context: IKmmContext, channelId: String) { }

    fun onTabOneSubPageHide(context: IKmmContext, channelId: String) { }

    fun onBottomTabConfigUpdate(configList: List<BottomTabConfig>) { }
}

data class BottomTabConfig(
    val channelId: String,
    val name: String
)
