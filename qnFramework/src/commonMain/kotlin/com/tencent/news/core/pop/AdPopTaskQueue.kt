package com.tencent.news.core.pop

import com.tencent.news.core.app.CoreDataKey
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.IPageMap
import com.tencent.news.core.platform.AppLaunchType
import com.tencent.news.core.platform.AppStateManager
import com.tencent.news.core.platform.IAppLifeCycleListener
import com.tencent.news.core.platform.IPageLifeCycleListener
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.PageLifeCycleManager
import com.tencent.news.core.platform.api.appTask
import com.tencent.news.core.platform.api.getShiplyLong
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.getElapsedRealtime
import com.tencent.news.core.platform.synchronized
import kotlin.math.max

private const val DEFAULT_INTERVAL_SECONDS = 3L
// 总兜底开关：关闭后完全绕过广告队列，所有形态恢复 KmmPopManager 原直出流程。
private const val ENABLE_AD_POP_TASK_QUEUE_CONFIG_KEY = "enable_ad_pop_task_queue"
// N：任一受管广告真实 dismiss 后，下一受管广告至少间隔 N 秒；FollowU 完全豁免。
private const val FORM_INTERVAL_CONFIG_KEY = "pop_ad_form_interval_s"
// Y：热启动后超级蒙层与两类挂件的启动保护窗口。
private const val HOT_START_INTERVAL_CONFIG_KEY = "pop_ad_hot_start_protect_s"
private const val STAGE_CANCELLED = "cancelled"
private const val STAGE_DISCARDED = "discarded"
private const val STAGE_ELIGIBLE = "eligible"
private const val STAGE_QUEUED = "queued"
private const val STAGE_SHOWN = "shown"
// 确保业务 observer 和灯塔回调脱离同步的 JS -> Kotlin 生命周期调用栈。
private const val LIFECYCLE_CALLBACK_DELAY_MS = 1L

/**
 * 进程级只读配置缓存。配置不是队列状态，可由所有页面队列共享，
 * 避免每个页面首次展示时重复跨语言读取 Shiply。
 */
private object AdPopQueueConfig {
    val enabled by lazy(LazyThreadSafetyMode.PUBLICATION) {
        getShiplySwitch(ENABLE_AD_POP_TASK_QUEUE_CONFIG_KEY, true)
    }
    val formIntervalSeconds by lazy(LazyThreadSafetyMode.PUBLICATION) {
        getConfiguredSeconds(FORM_INTERVAL_CONFIG_KEY)
    }
    val hotStartIntervalSeconds by lazy(LazyThreadSafetyMode.PUBLICATION) {
        getConfiguredSeconds(HOT_START_INTERVAL_CONFIG_KEY)
    }

    private fun getConfiguredSeconds(key: String): Long {
        return getShiplyLong(key, DEFAULT_INTERVAL_SECONDS).takeIf { it >= 0L }
            ?: DEFAULT_INTERVAL_SECONDS
    }
}

/** 总开关关闭时，调用方不得创建队列或注册队列生命周期监听。 */
internal fun isAdPopTaskQueueEnabled(): Boolean = AdPopQueueConfig.enabled

/**
 * 漏斗机会 ID 需要在进程内唯一，因此独立于页面队列递增；它不持有任务、manager 或页面 Context。
 */
private object AdPopOpportunityIdGenerator {
    private val lock = Lock()
    private var sequence = 0L

    fun next(): String = synchronized(lock) {
        sequence += 1
        "${AppStateManager.appStartTime}_$sequence"
    }
}

/**
 * 单个 [KmmPopManager] 的广告弹窗队列 manager。队列与页面级 manager 严格 1:1，
 * 不同页面的 N/Y 计时、展示态和待补任务互不影响；线程安全排队和调度由通用队列控制器承接。
 */
internal class AdPopTaskQueueManager(
    private val manager: KmmPopManager
) : IAppLifeCycleListener, IPageLifeCycleListener {

    private data class QueueEntry(
        val task: KmmPopTask,
        val opportunityId: String,
        val logicalForm: String,
        val enqueueTime: Long,
        val launchType: AppLaunchType,
        var hitFormRule: Boolean,
        var queuedWhileFormShowing: Boolean,
        var hitHotStart: Boolean
    )

    private data class FormRuleSnapshot(
        val isBlocked: Boolean,
        val isFormShowing: Boolean
    )

    private val queueContext = PopTaskQueueContext(name = "ad_pop_task_queue")
    private val taskQueue = PopTaskQueueController(
        context = queueContext,
        config = PopTaskQueueConfig(
            keyProvider = { it.logicalForm },
            readyTimeProvider = ::effectiveReadyTime,
            readyComparator = compareByDescending<QueueEntry> { it.task.priority }
                .thenBy { it.enqueueTime },
            canSchedule = { AppStateManager.isForeground() },
            onReady = ::drainEntry
        )
    )
    // 只观察真实上屏且受 N 秒规则约束的广告，用于等待 dismiss 和计算 N 秒；队列不拥有其关闭权。
    // 普通业务弹窗和 FollowU 不进入此列表。
    private val showingCoveredAdTasks = mutableListOf<KmmPopTask>()
    // N 秒从受管广告真实 dismiss、移出 showingDialogList 的时刻开始；普通弹窗不会刷新。
    private var lastFormDismissTime = 0L

    private val formIntervalSeconds: Long
        get() = AdPopQueueConfig.formIntervalSeconds
    private val hotStartIntervalSeconds: Long
        get() = AdPopQueueConfig.hotStartIntervalSeconds

    init {
        AppStateManager.registerAppLifeCycleListener(this)
        PageLifeCycleManager.registerLifeCycleListener(this)
    }

    fun isDisposed(): Boolean = taskQueue.isDisposed()

    fun submit(task: KmmPopTask): PopResult {
        // Shiply 首次读取可能跨语言，确保不在队列锁内触发。
        val formInterval = formIntervalSeconds
        val hotStartInterval = hotStartIntervalSeconds
        manager.bindForQueue(task)
        if (taskQueue.isDisposed()) {
            // 外部若误持有已销毁页面的 manager，必须立即终止，不能重新挂起旧页面 Context。
            task.popHelper?.onShowResult(PopResult.RULE_DISCARDED)
            runCatching {
                task.queueObserver?.onStateChanged(task, PopQueueState.CANCELLED, PopQueueReason.PAGE_INACTIVE)
            }
            task.lifecycleObserver?.onDismiss(task)
            task.popHelper?.dismiss()
            return PopResult.RULE_DISCARDED
        }
        val now = getElapsedRealtime()
        val launchType = AppStateManager.getCurrentLaunchType()
        val formRule = getFormRuleSnapshot(task, now, formInterval)
        val hitHotStart = isBlockedByHotStart(task, now, hotStartInterval)
        // 只有已通过普通弹窗冲突、频控等前置检查的任务，才算“就绪且符合展示条件”。
        // 已有受管广告展示时，只忽略它造成的冲突，让新广告进入广告队列；普通业务弹窗冲突仍正常生效。
        if (manager.checkForQueue(task, ignoreShowingCoveredAds = formRule.isFormShowing) != PopResult.SUCCESS) {
            val result = manager.showDirect(task)
            if (result != PopResult.SUCCESS) {
                runCatching {
                    task.queueObserver?.onStateChanged(
                        task,
                        PopQueueState.CANCELLED,
                        result.toQueueReason()
                    )
                }
            }
            return result
        }

        val entry = QueueEntry(
            task = task,
            opportunityId = AdPopOpportunityIdGenerator.next(),
            logicalForm = AdPopQueuePolicy.logicalForm(task.type),
            enqueueTime = now,
            launchType = launchType,
            hitFormRule = formRule.isBlocked,
            queuedWhileFormShowing = formRule.isFormShowing,
            hitHotStart = hitHotStart
        )
        report(entry, STAGE_ELIGIBLE, PopQueueReason.NONE)

        if (!entry.hitFormRule && !entry.hitHotStart) {
            return showNow(entry)
        }

        val enqueueResult = taskQueue.enqueue(entry)
        if (!enqueueResult.enqueued) {
            entry.task.popHelper?.onShowResult(PopResult.RULE_DISCARDED)
            cancel(entry, PopQueueReason.PAGE_INACTIVE)
            return PopResult.RULE_DISCARDED
        }

        enqueueResult.replaced?.let { discard(it, PopQueueReason.SAME_FORM_REPLACED, PopResult.RULE_DISCARDED) }
        val reason = ruleReason(entry)
        notifyState(entry, PopQueueState.QUEUED, reason)
        report(entry, STAGE_QUEUED, reason)
        taskQueue.scheduleNext()
        return PopResult.QUEUED
    }

    private fun showNow(entry: QueueEntry): PopResult {
        if (cancelIfDisposed(entry)) return PopResult.RULE_DISCARDED
        val result = manager.showDirect(entry.task)
        if (result == PopResult.SUCCESS) {
            recordShown(entry)
        } else {
            cancel(entry, result.toQueueReason())
        }
        return result
    }

    private fun recordShown(entry: QueueEntry) {
        // 真实上屏后只标记“广告展示中”；N 秒必须等真实 dismiss 后才开始。
        if (AdPopQueuePolicy.isFormIntervalCovered(entry.task.type)) {
            synchronized(queueContext.lock) {
                if (showingCoveredAdTasks.none { it === entry.task }) {
                    showingCoveredAdTasks.add(entry.task)
                }
            }
        }
        notifyState(entry, PopQueueState.SHOWN, PopQueueReason.NONE)
        report(entry, STAGE_SHOWN, PopQueueReason.NONE)
        taskQueue.scheduleNext()
    }

    private fun drainEntry(entry: QueueEntry): PopTaskQueueDrainDecision {
        if (cancelIfDisposed(entry)) return PopTaskQueueDrainDecision.STOP

        val cancelReason = getCancelReason(entry)
        if (cancelReason != null) {
            if (cancelReason == PopQueueReason.BACKGROUND) {
                // drain 与退后台并发时，已从 pendingTasks 取出的任务也必须计规则舍弃。
                discard(entry, cancelReason, PopResult.RULE_DISCARDED)
            } else {
                cancel(entry, cancelReason)
            }
            return PopTaskQueueDrainDecision.CONTINUE
        }

        val checkResult = manager.checkForQueue(entry.task)
        if (checkResult != PopResult.SUCCESS) {
            // 补弹时遇到普通弹窗冲突或频控，终止并计舍弃，不再重试。
            discard(entry, checkResult.toQueueReason(), checkResult)
            return PopTaskQueueDrainDecision.CONTINUE
        }

        // 页面 stop/destroy 或 App 退后台可能与补弹检查并发；真正 showAction 前必须再确认一次。
        if (cancelIfDisposed(entry)) return PopTaskQueueDrainDecision.STOP
        val latestCancelReason = getCancelReason(entry)
        if (latestCancelReason != null) {
            if (latestCancelReason == PopQueueReason.BACKGROUND) {
                discard(entry, latestCancelReason, PopResult.RULE_DISCARDED)
            } else {
                cancel(entry, latestCancelReason)
            }
            return PopTaskQueueDrainDecision.CONTINUE
        }

        val showResult = manager.showDirect(entry.task)
        if (showResult == PopResult.SUCCESS) {
            recordShown(entry)
            return PopTaskQueueDrainDecision.STOP
        }
        // showAction 真实执行失败同样是最终舍弃；继续处理其他已到期形态。
        discard(entry, showResult.toQueueReason())
        return PopTaskQueueDrainDecision.CONTINUE
    }

    private fun getCancelReason(entry: QueueEntry): PopQueueReason? {
        if (!AppStateManager.isForeground()) return PopQueueReason.BACKGROUND
        val canShow = runCatching {
            entry.task.deferredShowValidator?.canShow(entry.task) ?: true
        }.getOrDefault(false)
        if (!canShow) return PopQueueReason.VALIDATOR_FAILED
        return null
    }

    /** 页面消失后队列直接终止；已从 pending 取出的并发任务也必须取消，不能等待 resume。 */
    private fun cancelIfDisposed(entry: QueueEntry): Boolean {
        if (!taskQueue.isDisposed()) return false
        entry.task.popHelper?.onShowResult(PopResult.RULE_DISCARDED)
        cancel(entry, PopQueueReason.PAGE_INACTIVE)
        return true
    }

    private fun cancel(entry: QueueEntry, reason: PopQueueReason) {
        notifyState(entry, PopQueueState.CANCELLED, reason)
        report(entry, STAGE_CANCELLED, reason)
    }

    private fun discard(entry: QueueEntry, reason: PopQueueReason, showResult: PopResult? = null) {
        showResult?.let { entry.task.popHelper?.onShowResult(it) }
        notifyState(entry, PopQueueState.DISCARDED, reason)
        report(entry, STAGE_DISCARDED, reason)
    }

    private fun effectiveReadyTime(entry: QueueEntry, now: Long): Long? {
        var readyTime = now
        if (AdPopQueuePolicy.isFormIntervalCovered(entry.task.type)) {
            // 广告展示期间先无限期等待；dismiss 后才产生可计算的 N 秒截止时间。
            if (showingCoveredAdTasks.isNotEmpty()) {
                entry.hitFormRule = true
                entry.queuedWhileFormShowing = true
                return null
            }
            // N 秒与任务优先级无关；更高、相同、更低优先级都使用同一 dismiss 截止时间。
            if (lastFormDismissTime > 0L && formIntervalSeconds > 0L) {
                entry.hitFormRule = true
                readyTime = max(readyTime, lastFormDismissTime + formIntervalSeconds * 1000L)
            }
        }
        if (AdPopQueuePolicy.isHotStartProtected(entry.task.type) &&
            entry.launchType == AppLaunchType.HOT && hotStartIntervalSeconds > 0L) {
            entry.hitHotStart = true
            readyTime = max(
                readyTime,
                AppStateManager.getForegroundElapsedRealtime() + hotStartIntervalSeconds * 1000L
            )
        }
        return readyTime
    }

    private fun getFormRuleSnapshot(
        task: KmmPopTask,
        now: Long,
        intervalSeconds: Long
    ): FormRuleSnapshot = synchronized(queueContext.lock) {
        val isFormShowing = AdPopQueuePolicy.isFormIntervalCovered(task.type) &&
            showingCoveredAdTasks.isNotEmpty()
        FormRuleSnapshot(
            isBlocked = AdPopQueuePolicy.shouldBlockByFormInterval(
                type = task.type,
                hasShowingCoveredAd = isFormShowing,
                lastDismissTime = lastFormDismissTime,
                now = now,
                intervalMs = intervalSeconds * 1000L
            ),
            isFormShowing = isFormShowing
        )
    }

    private fun isBlockedByHotStart(task: KmmPopTask, now: Long, intervalSeconds: Long): Boolean {
        return AdPopQueuePolicy.isHotStartProtected(task.type) &&
            AppStateManager.getCurrentLaunchType() == AppLaunchType.HOT &&
            intervalSeconds > 0L &&
            now < AppStateManager.getForegroundElapsedRealtime() + intervalSeconds * 1000L
    }

    private fun notifyState(entry: QueueEntry, state: PopQueueState, reason: PopQueueReason) {
        runCatching {
            entry.task.queueObserver?.onStateChanged(entry.task, state, reason)
        }
        if (state == PopQueueState.CANCELLED || state == PopQueueState.DISCARDED) {
            entry.task.lifecycleObserver?.onDismiss(entry.task)
            entry.task.popHelper?.dismiss()
        }
    }

    private fun report(entry: QueueEntry, stage: String, reason: PopQueueReason) {
        PopReport.reportPriorityFunnel(
            popTask = entry.task,
            opportunityId = entry.opportunityId,
            stage = stage,
            logicalForm = entry.logicalForm,
            rule = ruleName(entry),
            launchType = entry.launchType.name.lowercase(),
            nSeconds = formIntervalSeconds,
            ySeconds = hotStartIntervalSeconds,
            waitMs = max(0L, getElapsedRealtime() - entry.enqueueTime),
            reason = reason
        )
    }

    private fun ruleName(entry: QueueEntry): String = when {
        entry.hitFormRule && entry.hitHotStart -> "both"
        entry.hitFormRule -> "form_interval"
        entry.hitHotStart -> "hot_start"
        else -> "none"
    }

    private fun ruleReason(entry: QueueEntry): PopQueueReason = when {
        entry.queuedWhileFormShowing -> PopQueueReason.FORM_SHOWING
        entry.hitFormRule && entry.hitHotStart -> PopQueueReason.FORM_INTERVAL_AND_HOT_START
        entry.hitFormRule -> PopQueueReason.FORM_INTERVAL
        else -> PopQueueReason.HOT_START
    }

    internal fun onTaskDismissed(task: KmmPopTask) {
        if (!AdPopQueuePolicy.isFormIntervalCovered(task.type)) return
        val removed = synchronized(queueContext.lock) {
            if (taskQueue.isDisposed()) return@synchronized false
            val index = showingCoveredAdTasks.indexOfFirst { it === task }
            if (index < 0) {
                false
            } else {
                showingCoveredAdTasks.removeAt(index)
                // 只有真实展示过、且从 showingDialogList 成功移除的受管广告才能开启 N 秒窗口。
                lastFormDismissTime = getElapsedRealtime()
                true
            }
        }
        if (removed) taskQueue.scheduleNext()
    }

    override fun onBackground() {
        val entries = synchronized(queueContext.lock) {
            if (taskQueue.isDisposed()) return@synchronized emptyList()
            lastFormDismissTime = 0L
            showingCoveredAdTasks.clear()
            taskQueue.clear()
        }
        // 已进入 N/Y 规则队列后因退后台未能展示，属于最终规则舍弃；桥接返回后逐任务上报。
        dispatchAfterLifecycle {
            entries.forEach {
                discard(it, PopQueueReason.BACKGROUND, PopResult.RULE_DISCARDED)
            }
        }
    }

    override fun onForeground() {
        taskQueue.scheduleNext()
    }

    override fun onStop(context: IKmmContext) {
        if (context !== manager.context) return
        disposePageQueue(clearManager = false)
    }

    override fun onDestroy(context: IKmmContext) {
        if (context !== manager.context) return
        disposePageQueue(clearManager = true)
    }

    private fun disposePageQueue(clearManager: Boolean) {
        val entries = synchronized(queueContext.lock) {
            if (taskQueue.isDisposed()) return
            lastFormDismissTime = 0L
            showingCoveredAdTasks.clear()
            taskQueue.dispose()
        }

        // 页面消失即是本次页面队列的终点，不保留任务等待 resume；同时解除全局监听和 Context 引用。
        AppStateManager.removeAppLifeCycleListener(this)
        PageLifeCycleManager.removeLifeCycleListener(this)
        if (clearManager) {
            val pageMap = manager.context as? IPageMap
            if (pageMap?.getValue(CoreDataKey.POPUP_MANAGER) === manager) {
                pageMap.setValue(CoreDataKey.POPUP_MANAGER, null)
            }
        }

        dispatchAfterLifecycle {
            entries.forEach { cancel(it, PopQueueReason.PAGE_INACTIVE) }
        }
    }

    private fun dispatchAfterLifecycle(action: () -> Unit) {
        appTask().postAction(action, LIFECYCLE_CALLBACK_DELAY_MS)
    }

}

internal fun PopResult.toQueueReason(): PopQueueReason = when (this) {
    PopResult.FREQUENCY -> PopQueueReason.FREQUENCY
    PopResult.SHOWING, PopResult.HAS_HIGHER -> PopQueueReason.POP_CONFLICT
    PopResult.DIALOG_SELF_ERROR -> PopQueueReason.SHOW_ACTION_FAILED
    else -> PopQueueReason.SHOW_CONDITION_FAILED
}

internal object AdPopQueuePolicy {
    // 统一接管的合约广告形态；FollowU 仍走统一漏斗，但不参与 N/Y 延迟。
    private val managedTypes = setOf(
        PopType.AD_ONESHOT_BROKEN,
        PopType.AD_CSHOT,
        PopType.AD_ONESHOT,
        PopType.AD_SUPER_DIALOG,
        PopType.AD_COMPOSE_SUPER_DIALOG,
        PopType.AD_BRAND_GIFT,
        PopType.AD_BOTTOM_FLOAT,
        PopType.AD_FOLLOW_U,
        PopType.AD_HIGHLIGHT_PENDANT,
        PopType.AD_OLYMPIC_PENDANT
    )

    private val hotStartProtectedTypes = setOf(
        PopType.AD_SUPER_DIALOG,
        PopType.AD_COMPOSE_SUPER_DIALOG,
        PopType.AD_HIGHLIGHT_PENDANT,
        PopType.AD_OLYMPIC_PENDANT
    )

    fun isManaged(type: PopType?): Boolean = type in managedTypes

    // “不覆盖 FollowU”同时表示：FollowU 不等待 N 秒，且其 dismiss 不产生新的 N 秒窗口。
    fun isFormIntervalCovered(type: PopType?): Boolean = isManaged(type) && type != PopType.AD_FOLLOW_U

    fun isHotStartProtected(type: PopType?): Boolean = type in hotStartProtectedTypes

    fun logicalForm(type: PopType?): String = when (type) {
        PopType.AD_SUPER_DIALOG, PopType.AD_COMPOSE_SUPER_DIALOG -> "super_mask"
        else -> type?.name?.lowercase() ?: "unknown"
    }

    fun shouldBlockByFormInterval(
        type: PopType?,
        hasShowingCoveredAd: Boolean,
        lastDismissTime: Long,
        now: Long,
        intervalMs: Long
    ): Boolean {
        if (!isFormIntervalCovered(type)) return false
        if (hasShowingCoveredAd) return true
        return intervalMs > 0L && lastDismissTime > 0L && now < lastDismissTime + intervalMs
    }
}
