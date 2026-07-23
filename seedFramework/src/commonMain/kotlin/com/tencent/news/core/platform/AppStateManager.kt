package com.tencent.news.core.platform

import com.tencent.news.core.extension.ConcurrentList
import com.tencent.news.core.platform.api.BaseEvent
import com.tencent.news.core.platform.api.IAppNotifier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 接收并分发，业务侧的前后台切换
 */
object AppStateManager : IAppNotifier {

    private val lifeCycleListeners = ConcurrentList<IAppLifeCycleListener>()

    // Flow 派发相关
    private val _appStateFlow = MutableSharedFlow<AppStateChangeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val appStateFlow: SharedFlow<AppStateChangeEvent> = _appStateFlow.asSharedFlow()

    private val _loginStatusFlow = MutableSharedFlow<LoginStatusChangeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val loginStatusFlow: SharedFlow<LoginStatusChangeEvent> = _loginStatusFlow.asSharedFlow()

    private var isForeground = false
    private var isClodStart = true

    val appStartTime = getCurTimeMillis()

    // app冷启动，最早的时机（例如安卓 Application onCreate）
    // 一般仅用作记录启动时间戳，这里轻易不要加逻辑!!!
    fun onAppStart() {
    }

    fun onForeground() {
        isForeground = true
        safeForEach { it.onForeground() }
        _appStateFlow.tryEmit(AppStateChangeEvent.Foreground)
    }

    fun onBackground() {
        isClodStart = false
        isForeground = false
        safeForEach { it.onBackground() }
        _appStateFlow.tryEmit(AppStateChangeEvent.Background)
    }

    fun registerAppLifeCycleListener(listener: IAppLifeCycleListener) {
        lifeCycleListeners.add(listener)
    }

    fun removeAppLifeCycleListener(listener: IAppLifeCycleListener) {
        lifeCycleListeners.remove(listener)
    }

    private fun safeForEach(action: (listener: IAppLifeCycleListener) -> Unit) {
        lifeCycleListeners.forEach { action(it) }
    }

    // 是否在前台
    fun isForeground(): Boolean = isForeground

    // 是否冷启
    fun isClodStart(): Boolean = isClodStart

    // 登录态发生变化
    fun onLoginStatusChanged() {
        safeForEach { it.onLoginStatusChanged() }
        // 发射 Flow 事件
        _loginStatusFlow.tryEmit(LoginStatusChangeEvent)
    }
}


interface IAppLifeCycleListener {
    fun onBackground()

    fun onForeground()

    fun onLoginStatusChanged() {}
}

/**
 * 应用状态变化事件
 */
sealed class AppStateChangeEvent {
    object Foreground : AppStateChangeEvent()
    object Background : AppStateChangeEvent()
}

/**
 * 登录状态变化事件
 */
object LoginStatusChangeEvent

class AppStateEvent(val type: AppStateEventType) : BaseEvent()

enum class AppStateEventType {
    ColdStartLowPriority
}