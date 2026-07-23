package com.tencent.news.core.pop

data class KmmPopTask internal constructor(
    val id: String?,
    val priority: Int,
    val type: PopType?,
    val dialog: IPopUpView?,
    val dismissSelfByHigherPriority: Boolean, // 当出其他高优先级弹窗时，将本弹窗dismiss掉
    val viewLocation: Int, // 同一个位置出现的内容优先级相同
    val isIgnoreViewLocation: Boolean,
    val compare: ((currentTask: KmmPopTask, showingTask: KmmPopTask) -> Int)? = null,
    var popHelper: PopHelper? = null,
    val triggerType: TriggerType = TriggerType.DEFAULT,
    val canSameIdDialogShow: Boolean = false,
    val disableReCreate: Boolean = false,
    val lifecycleObserver: PopLifecycleObserver? = null,
    val deferredShowValidator: PopDeferredShowValidator? = null,
    val queueObserver: PopQueueObserver? = null
) : Comparable<KmmPopTask?> {

    override fun compareTo(other: KmmPopTask?): Int {
        other ?: return 1
        return compare?.invoke(this, other)
            ?: if (priority < other.priority) {
                -1
            } else {
                1
            }
    }

    fun isSamePosition(item: KmmPopTask): Boolean {
        return viewLocation == item.viewLocation ||
                isIgnoreViewLocation ||
                item.isIgnoreViewLocation
    }
}

interface PopLifecycleObserver {
    fun onDismiss(popTask: KmmPopTask) {
    }
}

interface PopDeferredShowValidator {
    fun canShow(popTask: KmmPopTask): Boolean
}

interface PopQueueObserver {
    fun onStateChanged(popTask: KmmPopTask, state: PopQueueState, reason: PopQueueReason) {
    }
}

enum class PopQueueState {
    /** 命中 N/Y 秒规则，任务已进入延迟队列，尚未真实上屏。 */
    QUEUED,

    /** showAction 执行成功，任务已经真实上屏。 */
    SHOWN,

    /** 因页面或业务条件失效而取消，不计入规则舍弃。 */
    CANCELLED,

    /** 因队列规则或补弹失败被最终舍弃，计入规则舍弃且不再重试。 */
    DISCARDED
}

enum class PopQueueReason {
    /** 当前状态没有额外原因。 */
    NONE,

    /** 距离上一受管广告真实 dismiss 未满 N 秒。 */
    FORM_INTERVAL,

    /** 上一受管广告仍在展示，需等待其 dismiss 后再开始 N 秒。 */
    FORM_SHOWING,

    /** 热启动后仍处于 Y 秒保护窗口。 */
    HOT_START,

    /** 同时命中形态 N 秒间隔与热启动 Y 秒保护。 */
    FORM_INTERVAL_AND_HOT_START,

    /** 同一逻辑形态的新任务入队，旧任务被替换并计规则舍弃。 */
    SAME_FORM_REPLACED,

    /** App 退后台，队列任务被清空并计规则舍弃。 */
    BACKGROUND,

    /** 任务所属页面已不活跃，任务取消且不计规则舍弃。 */
    PAGE_INACTIVE,

    /** 补弹前业务校验未通过或校验异常，任务取消且不计规则舍弃。 */
    VALIDATOR_FAILED,

    /** 补弹时与普通弹窗冲突，任务计规则舍弃且不再重试。 */
    POP_CONFLICT,

    /** 补弹时未通过业务频控，任务计规则舍弃且不再重试。 */
    FREQUENCY,

    /** showAction 已执行但弹窗自身展示失败，任务计规则舍弃且不再重试。 */
    SHOW_ACTION_FAILED,

    /** 补弹时其他展示条件未满足，任务计规则舍弃且不再重试。 */
    SHOW_CONDITION_FAILED
}

enum class TriggerType {
    DEFAULT,
    USER_OPERATE
}

sealed class PopImplType
class PopNativeImplType : PopImplType()

/**
 * Compose 弹窗的实现级别
 */
enum class ComposeImplLevel {
    /** 独立窗口层级，事件不透传 */
    WINDOW,
    /** 覆盖层级别(安卓View级别)，空白区域事件可透传 */
    OVERLAY
}

class PopComposeImplType(
    val pageName: String,
    val level: ComposeImplLevel = ComposeImplLevel.WINDOW
) : PopImplType()
