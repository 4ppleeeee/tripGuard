package com.tencent.news.core.tads.constants

import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.safeGet
import com.tencent.news.core.tads.click.AdClickRequest

enum class AdAndroidWaitSysWindowResult(val value: Int) {

    UNKNOWN(0),   // 未知结果
    NOT_SHOW(1),  // 系统拦截弹窗未出现
    CONFIRM(2),   // 系统拦截弹窗出现后，用户确认直达
    CANCEL(3),    // 系统拦截弹窗出现后，用户取消直达
    TIMEOUT(4)    //  系统弹窗检测超时（未检测到用户交互，CHECK_TIMEOUT_THRESHOLD 兜底超时，防止内存泄露）
    ;

    /**
     * 根据等待结果判断此次点击要不要执行下一个节点
     */
    fun enableContinue(): Boolean {
        return when (this) {
            NOT_SHOW, CONFIRM -> {
                false
            }

            CANCEL -> {
                true
            }

            else -> {
                false
            }
        }
    }

    companion object {
        fun fromValue(value: Int): AdAndroidWaitSysWindowResult {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}

// 是否能够使用Android激励Url
fun enableAndroidUseRewardUrl(request: AdClickRequest, rewardUrl: String?): Boolean {
    val canUseRewardUrl = canUseRewardUrl(request)
    val isValidRewardUrl = rewardUrl.isNotNullOrEmpty()
    return canUseRewardUrl && isValidRewardUrl
}

fun canUseRewardUrl(request: AdClickRequest): Boolean {
    val adOrder = request.adOrder ?: return false
    // 必须是延迟双开且用户取消了系统拦截弹窗
    val isAfterSysBlock = adOrder.action.jumpActions.safeGet(0)?.next ==
            AdJumpActionNext.AFTER_SYS_BLOCK

    val isCancel = adOrder.env.waitSysWindowResult == AdAndroidWaitSysWindowResult.CANCEL
    return isAfterSysBlock && isCancel
}
