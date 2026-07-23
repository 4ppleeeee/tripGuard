package com.tencent.news.core.pop

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.annotation.OnlyHostInvokeApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.AppStateManager
import com.tencent.news.core.platform.PageLifeCycleManager
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.DefaultKmmActionResult
import com.tencent.news.core.platform.api.IAppConfig
import com.tencent.news.core.platform.api.IAppInitConfig
import com.tencent.news.core.platform.api.IKmmAction
import com.tencent.news.core.platform.api.IKmmActionResult
import com.tencent.news.core.platform.api.ITask
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 广告统一队列功能链路测试。
 *
 * 测试统一从 [KmmPopManager.showWithResult] 和真实 dismiss 入口驱动，
 * 不直接断言 [AdPopQueuePolicy] 的内部方法，避免测试与策略实现细节绑定。
 */
class AdPopQueuePolicyTest {

    private lateinit var context: IKmmContext
    private lateinit var manager: KmmPopManager

    @BeforeTest
    fun setUp() {
        context = object : IKmmContext {}
        QnPlatformLogic.appConfig = QueueTestAppConfig
        QueueTestTaskScheduler.clear()
        QnPlatformLogic.task = QueueTestTaskScheduler
        AppStateManager.onForeground()
        manager = KmmPopManager(context)
    }

    @AfterTest
    fun tearDown() {
        // 确保每个 Case 创建的页面队列注销生命周期监听，避免影响后续 Case。
        dispatchPageDestroy()
        QueueTestTaskScheduler.runLifecycleCallbacks()
        QueueTestTaskScheduler.clear()
        QnPlatformLogic.task = null
        QnPlatformLogic.appConfig = null
    }

    /**
     * 场景：OneShot 正在展示时，更高优先级的 CShot 到达。
     * 预期：CShot 被队列接收但不执行 showAction，等待 OneShot 真实 dismiss。
     */
    @Test
    fun higherPriorityAdWaitsForShowingAdToDismiss() {
        val oneShot = createTask(PopType.AD_ONESHOT)
        val cShot = createTask(PopType.AD_CSHOT)

        assertEquals(PopResult.SUCCESS, manager.showWithResult(oneShot.task))
        assertEquals(PopResult.QUEUED, manager.showWithResult(cShot.task))

        assertEquals(1, oneShot.view.showCount)
        assertEquals(0, cShot.view.showCount)
        assertTrue(cShot.observer.contains(PopQueueState.QUEUED, PopQueueReason.FORM_SHOWING))
    }

    /**
     * 场景：OneShot 真实 dismiss 后，N 秒窗口内到达更高优先级 CShot。
     * 预期：CShot 进入延迟队列，当前调用不会提前执行 showAction。
     */
    @Test
    fun higherPriorityAdStillWaitsDuringDismissInterval() {
        val oneShot = createTask(PopType.AD_ONESHOT)
        assertEquals(PopResult.SUCCESS, manager.showWithResult(oneShot.task))

        oneShot.view.dismissThroughPopHelper()
        val cShot = createTask(PopType.AD_CSHOT)

        assertEquals(PopResult.QUEUED, manager.showWithResult(cShot.task))
        assertEquals(0, cShot.view.showCount)
        assertTrue(cShot.observer.contains(PopQueueState.QUEUED, PopQueueReason.FORM_INTERVAL))
    }

    /**
     * 场景：OneShot dismiss 后仍处于 N 秒窗口，此时 FollowU 到达。
     * 预期：FollowU 豁免 N 秒规则，直接完成展示，不进入广告延迟队列。
     */
    @Test
    fun followUShowsDirectlyDuringDismissInterval() {
        val oneShot = createTask(PopType.AD_ONESHOT)
        assertEquals(PopResult.SUCCESS, manager.showWithResult(oneShot.task))
        oneShot.view.dismissThroughPopHelper()

        val followU = createTask(PopType.AD_FOLLOW_U)

        assertEquals(PopResult.SUCCESS, manager.showWithResult(followU.task))
        assertEquals(1, followU.view.showCount)
        assertTrue(followU.observer.contains(PopQueueState.SHOWN, PopQueueReason.NONE))
        assertFalse(followU.observer.hasState(PopQueueState.QUEUED))
    }

    /**
     * 场景：受管广告展示期间，原生和 Compose 超级蒙层先后进入队列。
     * 预期：两者视为同一逻辑形态，只保留后到任务，旧任务按规则舍弃。
     */
    @Test
    fun latestSuperMaskReplacesOlderQueuedImplementation() {
        val oneShot = createTask(PopType.AD_ONESHOT)
        assertEquals(PopResult.SUCCESS, manager.showWithResult(oneShot.task))

        val nativeSuperMask = createTask(PopType.AD_SUPER_DIALOG)
        val composeSuperMask = createTask(PopType.AD_COMPOSE_SUPER_DIALOG)

        assertEquals(PopResult.QUEUED, manager.showWithResult(nativeSuperMask.task))
        assertEquals(PopResult.QUEUED, manager.showWithResult(composeSuperMask.task))
        assertTrue(
            nativeSuperMask.observer.contains(
                PopQueueState.DISCARDED,
                PopQueueReason.SAME_FORM_REPLACED
            )
        )
        assertEquals(0, nativeSuperMask.view.showCount)
        assertEquals(0, composeSuperMask.view.showCount)
    }

    /**
     * 场景：页面消失时，一个广告已展示，另一个广告仍在 pending。
     * 预期：只取消 pending；已经展示的广告不归队列关闭，保持原展示状态。
     */
    @Test
    fun pageStopCancelsPendingTaskButKeepsShownAd() {
        val shownOneShot = createTask(PopType.AD_ONESHOT)
        val pendingCShot = createTask(PopType.AD_CSHOT)
        assertEquals(PopResult.SUCCESS, manager.showWithResult(shownOneShot.task))
        assertEquals(PopResult.QUEUED, manager.showWithResult(pendingCShot.task))

        dispatchPageStop()

        // 页面队列已同步终止，但业务 observer 要等生命周期桥接返回后再执行。
        assertFalse(pendingCShot.observer.hasState(PopQueueState.CANCELLED))
        QueueTestTaskScheduler.runLifecycleCallbacks()

        assertTrue(shownOneShot.view.isShowing)
        assertEquals(0, shownOneShot.view.dismissCount)
        assertEquals(0, pendingCShot.view.showCount)
        assertTrue(
            pendingCShot.observer.contains(
                PopQueueState.CANCELLED,
                PopQueueReason.PAGE_INACTIVE
            )
        )
    }

    /**
     * 场景：页面只展示普通业务弹窗，没有广告任务进入统一队列。
     * 预期：业务弹窗沿用原 KmmPopManager 直出链路，页面 stop 不触发广告队列清理。
     */
    @Test
    fun ordinaryBusinessPopupKeepsOriginalDirectShowLifecycle() {
        val businessPopup = createTask(PopType.UPDATE_DIALOG)

        assertEquals(PopResult.SUCCESS, manager.showWithResult(businessPopup.task))
        dispatchPageStop()
        QueueTestTaskScheduler.runLifecycleCallbacks()

        assertEquals(1, businessPopup.view.showCount)
        assertEquals(0, businessPopup.view.dismissCount)
        assertTrue(businessPopup.view.isShowing)
        assertTrue(businessPopup.observer.events.isEmpty())
    }

    /**
     * 场景：广告队列总开关关闭，受管广告通过原直出链路成功展示。
     * 预期：不创建延迟队列，但仍派发 SHOWN，业务可继续完成频控落盘和成功上报。
     */
    @Test
    fun managedAdStillReceivesShownCallbackWhenQueueDisabled() {
        val directManager = KmmPopManager(context, adQueueEnabledProvider = { false })
        val oneShot = createTask(PopType.AD_ONESHOT)

        assertEquals(PopResult.SUCCESS, directManager.showWithResult(oneShot.task))

        assertEquals(1, oneShot.view.showCount)
        assertTrue(oneShot.observer.contains(PopQueueState.SHOWN, PopQueueReason.NONE))
        assertFalse(oneShot.observer.hasState(PopQueueState.QUEUED))
    }

    /**
     * 场景：广告队列总开关关闭，受管广告的 showAction 执行失败。
     * 预期：沿用同步失败返回值，并派发终止状态，业务可继续执行清理和拦截上报。
     */
    @Test
    fun managedAdStillReceivesTerminalCallbackWhenQueueDisabled() {
        val directManager = KmmPopManager(context, adQueueEnabledProvider = { false })
        val oneShot = createTask(PopType.AD_ONESHOT, showSucceeds = false)

        assertEquals(PopResult.DIALOG_SELF_ERROR, directManager.showWithResult(oneShot.task))

        assertEquals(1, oneShot.view.showCount)
        assertTrue(
            oneShot.observer.contains(
                PopQueueState.CANCELLED,
                PopQueueReason.SHOW_ACTION_FAILED
            )
        )
        assertFalse(oneShot.observer.hasState(PopQueueState.SHOWN))
    }

    /**
     * 场景：受管广告仍在 N 秒队列中时 App 退到后台。
     * 预期：队列同步清空；生命周期返回后任务按规则舍弃并完成上报，不等待再次前台补弹。
     */
    @Test
    fun backgroundDiscardsPendingAdAfterLifecycleCallbackReturns() {
        val shownOneShot = createTask(PopType.AD_ONESHOT)
        val pendingCShot = createTask(PopType.AD_CSHOT)
        assertEquals(PopResult.SUCCESS, manager.showWithResult(shownOneShot.task))
        assertEquals(PopResult.QUEUED, manager.showWithResult(pendingCShot.task))

        AppStateManager.onBackground()

        assertFalse(pendingCShot.observer.hasState(PopQueueState.DISCARDED))
        QueueTestTaskScheduler.runLifecycleCallbacks()
        assertTrue(
            pendingCShot.observer.contains(
                PopQueueState.DISCARDED,
                PopQueueReason.BACKGROUND
            )
        )
        assertEquals(0, pendingCShot.view.showCount)
    }

    private fun createTask(type: PopType, showSucceeds: Boolean = true): TaskScenario {
        val view = RecordingPopUpView(showSucceeds)
        val observer = RecordingQueueObserver()
        val task = KmmPopTask(
            id = type.name,
            priority = type.getPriority(false),
            type = type,
            dialog = view,
            dismissSelfByHigherPriority = true,
            viewLocation = PopUpViewLocation.FULL,
            isIgnoreViewLocation = false,
            triggerType = type.triggerType,
            queueObserver = observer
        )
        return TaskScenario(task, view, observer)
    }

    /** 测试模拟宿主分发页面 stop，OptIn 仅收敛在该生命周期测试入口。 */
    @OptIn(OnlyHostInvokeApi::class)
    private fun dispatchPageStop() {
        PageLifeCycleManager.onStop(context)
    }

    /** 测试模拟宿主分发页面 destroy，确保每个 Case 注销页面队列监听。 */
    @OptIn(OnlyHostInvokeApi::class)
    private fun dispatchPageDestroy() {
        PageLifeCycleManager.onDestroy(context)
    }
}

private data class TaskScenario(
    val task: KmmPopTask,
    val view: RecordingPopUpView,
    val observer: RecordingQueueObserver
)

private data class QueueEvent(
    val state: PopQueueState,
    val reason: PopQueueReason
)

private class RecordingQueueObserver : PopQueueObserver {
    val events = mutableListOf<QueueEvent>()

    override fun onStateChanged(popTask: KmmPopTask, state: PopQueueState, reason: PopQueueReason) {
        events += QueueEvent(state, reason)
    }

    fun contains(state: PopQueueState, reason: PopQueueReason): Boolean {
        return events.any { it.state == state && it.reason == reason }
    }

    fun hasState(state: PopQueueState): Boolean = events.any { it.state == state }
}

/** 测试桩需要接收框架内部弹窗生命周期，OptIn 仅作用于该假 View。 */
@OptIn(KmmInternalApi::class)
private class RecordingPopUpView(
    private val showSucceeds: Boolean = true
) : IPopUpView {
    private var popHelper: PopHelper? = null

    var showCount = 0
        private set
    var dismissCount = 0
        private set
    var isShowing = false
        private set

    override fun onShowDialog(context: IKmmContext): Boolean {
        showCount += 1
        isShowing = showSucceeds
        return showSucceeds
    }

    override fun onDismissDialog() {
        dismissCount += 1
        isShowing = false
    }

    override fun isDialogShowing(): Boolean = isShowing

    override fun setPopHelper(popHelper: PopHelper) {
        this.popHelper = popHelper
    }

    override fun getPopHelper(): PopHelper? = popHelper

    fun dismissThroughPopHelper() {
        requireNotNull(popHelper).dismiss()
    }
}

/**
 * 测试配置固定开启队列，并把 N 设置为足够长的时间，确保 Case 只验证“是否进入延迟链路”，
 * 不依赖真实时间等待；Y 设为 0，避免热启动状态污染测试结果。
 */
private object QueueTestAppConfig : IAppConfig {
    override fun getShiplyConfig(key: String, defaultValue: String): String = when (key) {
        "pop_ad_form_interval_s" -> "60"
        "pop_ad_hot_start_protect_s" -> "0"
        else -> defaultValue
    }

    override fun getShiplySwitch(key: String, defaultValue: Boolean): Boolean = when (key) {
        "enable_ad_pop_task_queue" -> true
        else -> defaultValue
    }

    override fun getAppInitConfig(): IAppInitConfig? = null

    override fun getTabExpInt(key: String, defaultValue: Int): Int = defaultValue
}

/**
 * N/Y 延迟任务保持挂起；生命周期善后任务由 Case 在桥接调用返回后显式触发，
 * 用于验证 observer/上报不会嵌套在同步生命周期调用栈内。
 */
private object QueueTestTaskScheduler : ITask {
    private const val LIFECYCLE_DELAY_MAX_MS = 1L
    private val lifecycleCallbacks = mutableListOf<IKmmAction>()

    override fun postAction(action: IKmmAction, delayTime: Long): IKmmActionResult {
        if (delayTime <= LIFECYCLE_DELAY_MAX_MS) {
            lifecycleCallbacks += action
        }
        return DefaultKmmActionResult()
    }

    fun runLifecycleCallbacks() {
        val actions = lifecycleCallbacks.toList()
        lifecycleCallbacks.clear()
        actions.forEach { it() }
    }

    fun clear() {
        lifecycleCallbacks.clear()
    }

    override fun runIOAction(action: IKmmAction) {
        action()
    }

    override fun runMainAction(action: IKmmAction) {
        action()
    }

    override fun runCpuAction(action: IKmmAction) {
        action()
    }
}
