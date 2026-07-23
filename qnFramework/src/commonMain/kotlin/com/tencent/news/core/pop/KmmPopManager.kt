package com.tencent.news.core.pop

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.extension.safeForEach
import com.tencent.news.core.list.trace.PopLog
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.api.appTask
import com.tencent.news.core.platform.api.isTalkbackEnabled
import com.tencent.news.core.platform.synchronized
import com.tencent.news.core.pop.api.IPopUpManager

/**
 * 弹窗Debug日志工具
 */
private object PopDebugLog {
    /** 构建弹窗任务信息字符串 */
    fun buildPopTaskInfo(popTask: KmmPopTask?): String {
        return popTask?.let { task ->
            "弹窗[${task.id}] 类型=${task.type} 优先级=${task.priority} " +
                    "触发=${if (task.triggerType == TriggerType.USER_OPERATE) "用户" else "自动"} " +
                    "高优关闭=${task.dismissSelfByHigherPriority} 允许同ID=${task.canSameIdDialogShow}"
        } ?: "弹窗信息为空"
    }

    /** 构建当前显示弹窗列表信息 */
    fun buildShowingListInfo(showingList: List<KmmPopTask>): String {
        return if (showingList.isEmpty()) "当前无弹窗显示"
        else "显示中: ${showingList.joinToString(", ") { "${it.id}(${it.type}:${it.priority})" }}"
    }

    /** 获取PopResult的中文描述 */
    fun getResultDescription(result: PopResult): String = when (result) {
        PopResult.SUCCESS -> "成功显示"
        PopResult.ILLEGAL_DATA -> "参数非法"
        PopResult.FREQUENCY -> "频次控制拦截"
        PopResult.SHOWING -> "已在显示中"
        PopResult.HAS_HIGHER -> "有更高优先级弹窗"
        PopResult.TALKBACK -> "无障碍模式拦截"
        PopResult.DIALOG_SELF_ERROR -> "弹窗自身错误"
        PopResult.QUEUED -> "已进入形态队列"
        PopResult.RULE_DISCARDED -> "被形态规则舍弃"
    }
}


/** 弹窗任务简要信息 - 用于日志 */
private fun KmmPopTask.briefInfo(): String = PopDebugLog.buildPopTaskInfo(this)

/** PopResult描述 */
private fun PopResult.desc(): String = PopDebugLog.getResultDescription(this)

internal class KmmPopManager(
    internal val context: IKmmContext,
    private val adQueueEnabledProvider: () -> Boolean = ::isAdPopTaskQueueEnabled
) : IPopUpManager {

    private val showingDialogList: ArrayList<KmmPopTask> = ArrayList()
    private val lock = Lock()
    // 页面级 manager 与广告队列严格 1:1；N/Y 计时和待补任务不会跨页面共享。
    // 页面消失后旧队列终止；下次新的广告机会按需创建新队列，不依赖 page resume。
    // 总开关关闭时保持 null，不注册队列监听；广告仍走原直出流程并保留结果 observer。
    private var adPopTaskQueueManager: AdPopTaskQueueManager? = null
    private val logger = PopLog

    /** 获取显示列表快照字符串 - 用于日志，避免并发修改异常 */
    private fun showingListSnapshot(): String =
        PopDebugLog.buildShowingListInfo(ArrayList(showingDialogList))

    override fun show(popTask: KmmPopTask): Boolean {
        val result = showWithResult(popTask)
        logger.fileLog("PopManager", "${popTask.id} showWithResult: $result")
        logger.debug("Show") { "${popTask.briefInfo()} 结果=${result.desc()}" }
        // Boolean 表示任务是否被接收；业务展示成功必须以 SHOWN 回调为准。
        return result == PopResult.SUCCESS || result == PopResult.QUEUED
    }

    override fun showWithResult(popTask: KmmPopTask): PopResult {
        if (!AdPopQueuePolicy.isManaged(popTask.type)) {
            return showDirect(popTask)
        }
        if (adQueueEnabledProvider()) {
            return getAdPopTaskQueueManager().submit(popTask)
        }
        // 总开关只关闭排队规则，不能关闭业务对真实展示结果的监听。
        // 三类广告入口都依赖该回调完成蒙层清理、频控落盘和成功/拦截上报。
        val result = showDirect(popTask)
        notifyManagedDirectResult(popTask, result)
        return result
    }

    private fun notifyManagedDirectResult(popTask: KmmPopTask, result: PopResult) {
        val state = if (result == PopResult.SUCCESS) {
            PopQueueState.SHOWN
        } else {
            PopQueueState.CANCELLED
        }
        val reason = if (result == PopResult.SUCCESS) {
            PopQueueReason.NONE
        } else {
            result.toQueueReason()
        }
        runCatching {
            popTask.queueObserver?.onStateChanged(popTask, state, reason)
        }
    }

    private fun getAdPopTaskQueueManager(): AdPopTaskQueueManager = synchronized(lock) {
        if (adPopTaskQueueManager == null || adPopTaskQueueManager?.isDisposed() == true) {
            adPopTaskQueueManager = AdPopTaskQueueManager(this)
        }
        requireNotNull(adPopTaskQueueManager)
    }

    internal fun showDirect(popTask: KmmPopTask): PopResult {
        logger.debug("ShowFlow") { "开始显示 ${popTask.briefInfo()}" }
        bindPopHelper(popTask)

        val result = synchronized(lock) {
            val listSnapshot = showingListSnapshot()
            dispatchOtherDialogTryShow(popTask)
            val showResult = tryShowDialogWithResult(popTask)

            if (showResult == PopResult.SUCCESS) {
                tryDismissLowDialog(popTask)
                showingDialogList.add(popTask)
                logger.debug("ShowFlow") { "显示成功 ${popTask.briefInfo()} | ${showingListSnapshot()}" }
            } else {
                logger.fileLog(
                    "ShowFlow",
                    "显示失败 ${popTask.briefInfo()} 原因=${showResult.desc()} | $listSnapshot"
                )
            }
            showResult
        }
        return result
    }

    internal fun bindForQueue(popTask: KmmPopTask) {
        bindPopHelper(popTask)
    }

    internal fun checkForQueue(
        popTask: KmmPopTask,
        ignoreShowingCoveredAds: Boolean = false
    ): PopResult = checkShow(popTask, ignoreShowingCoveredAds)

    private fun dispatchOtherDialogTryShow(popTask: KmmPopTask) {
        showingDialogList.safeForEach {
            // 需要在高优先级弹窗展示时将自己dismiss掉
            it.popHelper?.onOtherDialogTryShow(popTask)
        }
    }

    private fun bindPopHelper(popTask: KmmPopTask) {
        // 当前弹窗正在展示则不绑定helper，防止多次调用show后移除时导致的移除失败
        if (findPopTask { it.dialog == popTask.dialog } != null) {
            return
        }
        PopHelper(popTask, this).bindPopHelper()
    }

    private fun tryShowDialogWithResult(popTask: KmmPopTask): PopResult {
        var result = checkShow(popTask)
        logger.debug("Check") { "${popTask.briefInfo()} 检查结果=${result.desc()}" }

        if (result == PopResult.SUCCESS) {
            val isSuccess = popTask.popHelper?.showPopView(context) ?: false
            if (!isSuccess) {
                result = PopResult.DIALOG_SELF_ERROR
                logger.error("Show", "${popTask.briefInfo()} 自身显示失败")
            }
        }
        popTask.popHelper?.onShowResult(result)
        return result
    }

    private fun tryDismissLowDialog(popTask: KmmPopTask) {
        showingDialogList.safeForEach { showingTask ->
            val needDismiss = showingTask.dismissSelfByHigherPriority &&
                    showingTask.isSamePosition(popTask) && showingTask < popTask
            if (needDismiss) {
                logger.debug("Priority") { "高优关闭低优 高=${popTask.briefInfo()} 低=${showingTask.briefInfo()}" }
                appTask().postAction({ showingTask.popHelper?.dismiss() })
                showingTask.popHelper?.onDismissByHigher(popTask)
            } else {
                logger.debug("Priority") { "暂停显示 新=${popTask.briefInfo()} 暂停=${showingTask.briefInfo()}" }
                appTask().postAction({ showingTask.popHelper?.onPauseByItem(popTask) })
            }
        }
    }

    override fun checkShowCondition(popTask: KmmPopTask?): Boolean =
        checkShow(popTask) == PopResult.SUCCESS

    private fun checkShow(
        popTask: KmmPopTask?,
        ignoreShowingCoveredAds: Boolean = false
    ): PopResult {
        if (popTask == null) {
            logger.error("Check", "弹窗任务为空")
            return PopResult.ILLEGAL_DATA
        }
        if (isInterceptByTalkback(popTask)) {
            logger.fileLog("Talkback", "无障碍拦截 ${popTask.briefInfo()}")
            return PopResult.TALKBACK
        }
        if (popTask.popHelper?.checkBeforeRealShow() != true) {
            logger.fileLog("Frequency", "频次拦截 ${popTask.briefInfo()}")
            return PopResult.FREQUENCY
        }

        val result = synchronized(lock) {
            when {
                showingDialogList.contains(popTask) -> {
                    logger.fileLog("Duplicate", "重复显示拦截 ${popTask.briefInfo()}")
                    PopResult.SHOWING
                }

                popTask.triggerType == TriggerType.USER_OPERATE -> {
                    logger.debug("UserOperate") { "用户操作直接通过 ${popTask.briefInfo()}" }
                    PopResult.SUCCESS
                }

                popTask.id == null -> {
                    logger.error("Check", "弹窗ID为空 ${popTask.briefInfo()}")
                    PopResult.ILLEGAL_DATA
                }

                else -> checkPriorityConflict(popTask, ignoreShowingCoveredAds)
            }
        }
        return result
    }

    /** 检查优先级冲突 - 必须在锁内调用 */
    private fun checkPriorityConflict(
        popTask: KmmPopTask,
        ignoreShowingCoveredAds: Boolean
    ): PopResult {
        // 普通业务弹窗仍直接使用完整 showingList；只有广告队列预检才忽略正在展示的 N 秒受管广告。
        val conflictCandidates = if (ignoreShowingCoveredAds) {
            showingDialogList.filterNot { AdPopQueuePolicy.isFormIntervalCovered(it.type) }
        } else {
            showingDialogList
        }
        // 检查同ID弹窗
        if (!popTask.canSameIdDialogShow) {
            val sameIdTask = conflictCandidates.find { it.id == popTask.id }
            if (sameIdTask != null) {
                logger.fileLog(
                    "SameId",
                    "同ID拦截 当前=${popTask.briefInfo()} 已显示=${sameIdTask.briefInfo()}"
                )
                return PopResult.SHOWING
            }
        }
        // 检查高优先级冲突
        val higherTask = conflictCandidates.find { popTask < it && popTask.isSamePosition(it) }
        if (higherTask != null) {
            logger.fileLog(
                "Priority",
                "优先级拦截 当前=${popTask.briefInfo()} 高优=${higherTask.briefInfo()}"
            )
            return PopResult.HAS_HIGHER
        }
        logger.debug("Check") { "检查通过 ${popTask.briefInfo()}" }
        return PopResult.SUCCESS
    }

    private fun isInterceptByTalkback(popTask: KmmPopTask): Boolean {
        if (isTalkbackEnabled().not()) return false
        popTask.type ?: return false
        val isIntercepted = popTask.type in TALKBACK_INTERCEPT_TYPES
        if (isIntercepted) {
            logger.debug("Talkback") { "无障碍拦截 ${popTask.briefInfo()}" }
        }
        return isIntercepted
    }

    override fun dismiss(popTask: KmmPopTask?) {
        synchronized(lock) {
            popTask ?: return
            if (showingDialogList.isEmpty()) {
                logger.debug("Dismiss") { "关闭调用但无弹窗显示" }
                return@synchronized
            }
            if (showingDialogList.contains(popTask)) {
                val snapshot = showingListSnapshot()
                popTask.popHelper?.dismiss()
                showingDialogList.remove(popTask)
                logger.debug("Dismiss") { "关闭成功 ${popTask.briefInfo()} | $snapshot" }
            } else {
                logger.fileLog(
                    "Dismiss",
                    "关闭失败 ${popTask.briefInfo()} 不在显示列表 | ${showingListSnapshot()}"
                )
            }
        }
    }

    override fun removeItem(popTask: KmmPopTask?): Boolean {
        val removedTask = synchronized(lock) {
            if (showingDialogList.contains(popTask)) {
                val removed = showingDialogList.remove(popTask)
                logger.debug("Remove") { "移除成功 ${popTask?.briefInfo()}" }
                if (removed) popTask else null
            } else {
                logger.fileLog("Remove", "移除失败 ${popTask?.briefInfo()} 不在显示列表")
                null
            }
        }
        if (removedTask != null) {
            // 只有 N 秒覆盖的广告才通知队列；普通业务弹窗和 FollowU 不参与 dismiss 计时。
            if (AdPopQueuePolicy.isFormIntervalCovered(removedTask.type)) {
                adPopTaskQueueManager?.onTaskDismissed(removedTask)
            }
            removedTask.lifecycleObserver?.onDismiss(removedTask)
        }
        return removedTask != null
    }

    override fun clear() {
        synchronized(lock) {
            if (showingDialogList.isEmpty()) {
                logger.debug("Clear") { "清空调用但无弹窗显示" }
                return@synchronized
            }
            val size = showingDialogList.size
            val snapshot = showingListSnapshot()
            showingDialogList.safeForEach { dismiss(it) }
            logger.debug("Clear") { "清空完成 数量=$size | $snapshot" }
        }
    }

    override fun findPopTask(condition: (popTask: KmmPopTask) -> Boolean): KmmPopTask? {
        synchronized(lock) {
            return showingDialogList.find {
                condition.invoke(it)
            }
        }
    }

    override fun findPopTaskWithType(type: PopType): KmmPopTask? {
        return findPopTask {
            it.type == type
        }
    }

    companion object {
        /** 无障碍模式下需要拦截的弹窗类型 */
        private val TALKBACK_INTERCEPT_TYPES = setOf(
            PopType.AD_APPOINTMENT_DIALOG,
            PopType.RED_FLOWER_AVATAR_FRAME_DIALOG,
            PopType.DESKTOP_SHORT_CUT_DIALOG,
            PopType.CHANNEL_DESKTOP_SHORT_CUT_DIALOG,
            PopType.CHANNEL_SHARE_DIALOG,
            PopType.REC_CHANNEL_DIALOG,
            PopType.GOT_RED_FLOWER_DIALOG,
            PopType.RED_FLOWER_DIALOG,
            PopType.AD_SHORT_CUT_DIALOG,
            PopType.AD_APPOINTMENT_CARD,
            PopType.AD_APPOINTMENT_BOTTOM_BAR,
            PopType.AD_GAME_DOWNLOAD_BOTTOM_BAR,
            PopType.PRAISE_DIALOG,
            PopType.IP_VIDEO_APPOINTMENT,
            PopType.HOT_DIALOG,
            PopType.VIP_MOVE_CHANNEL_DIALOG_BOTTOM,
            PopType.LAST_READ_TIP,
            PopType.LIVE_START_TIP,
            PopType.USER_GROWTH_MIDDLE,
            PopType.USER_GROWTH_FULLSCREEN,
            PopType.USER_SURVEY,
            PopType.USER_GROWTH_BOTTOM_MINI_BAR,
            PopType.LANDING_COIN_MIX_VIDEO_POP,
            PopType.LANDING_COIN_MIX_VIDEO_RIGHT_BOTTOM,
            PopType.LANDING_COIN_MAIN_BOTTOM,
            PopType.USER_GROWTH_BOTTOM,
            PopType.AD_BRAND_GIFT,
            PopType.AD_OLYMPIC_PENDANT,
            PopType.AD_HIGHLIGHT_PENDANT,
            PopType.AD_FOLLOW_U,
            PopType.AD_BOTTOM_FLOAT,
            PopType.AD_SUPER_DIALOG,
            PopType.AD_ONESHOT,
            PopType.AD_CSHOT,
            PopType.AD_ONESHOT_BROKEN,
            PopType.USER_GROWTH_KOULING_MIDDLE,
            PopType.USER_GROWTH_KOULING_FULLSCREEN,
            PopType.CHANGE_LOCAL_CHANNEL_DIALOG,
            PopType.AD_PAUSE_DIALOG,
            PopType.ACTIVITY_DIALOG,
            PopType.LIVE_BACKGROUND_PLAY_DIALOG,
            PopType.REDPACKET_DIALOG,
            PopType.AD_GAME_DIALOG,
            PopType.UPDATE_DIALOG,
            PopType.AD_GAME_RESERVE_DIALOG,
            PopType.AD_COMPOSE_SUPER_DIALOG
        )
    }
}
