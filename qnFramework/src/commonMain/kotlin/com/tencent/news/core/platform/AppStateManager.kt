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
    private var currentLaunchType = AppLaunchType.COLD
    private var foregroundElapsedRealtime = getElapsedRealtime()

    val appStartTime = getCurTimeMillis()

    // app冷启动，最早的时机（例如安卓 Application onCreate）
    // 一般仅用作记录启动时间戳，这里轻易不要加逻辑!!!
    fun onAppStart() {
    }

    fun onForeground() {
        // 必须在通知监听方前落盘，队列收到新任务时才能准确判断本次冷/热启动及 Y 秒起点。
        currentLaunchType = if (isClodStart) AppLaunchType.COLD else AppLaunchType.HOT
        foregroundElapsedRealtime = getElapsedRealtime()
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
        // 只在锁内获取快照，业务生命周期回调必须在监听器锁外执行。
        lifeCycleListeners.shallowCopyList().forEach { action(it) }
    }

    // 是否在前台
    fun isForeground(): Boolean = isForeground

    // 是否冷启
    fun isClodStart(): Boolean = isClodStart

    fun getCurrentLaunchType(): AppLaunchType = currentLaunchType

    fun getForegroundElapsedRealtime(): Long = foregroundElapsedRealtime

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

enum class AppLaunchType {
    /** 当前进程首次进入前台，用于区分冷启动场景。 */
    COLD,

    /** 当前进程从后台再次进入前台，用于计算热启动 Y 秒保护窗口。 */
    HOT
}
