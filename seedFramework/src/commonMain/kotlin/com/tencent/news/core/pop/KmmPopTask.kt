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
    val lifecycleObserver: PopLifecycleObserver? = null
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