package com.tencent.news.core.tads.model.ai

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getCurTimestampMillis
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.core.tads.model.getAdSeq


private const val IGNORE = -1

typealias AiPolicy = KmmCARInvokeMechanism
typealias AiPolicyList = List<KmmCARInvokeMechanism>?

/**
 * CARInvokeMechanism 重拉策略- 每种触发机制，有一套重拉策略
 */

data class KmmCARInvokeMechanism(
    val offset: Int,            // 触发的偏移量, 假设广告位置在A，则触发点位为A-invoke_offset; 值为-1时, 表示忽略offset
    val minTimeGap: Int,        // 触发重拉的最小时间间隔, 单位秒, 在该时间内第二次触发会忽略; 值为-1时, 表示忽略min_time_gap（全局）
    @KmmCheckInvokeTiming
    val invokeTiming: Int,      // 触发重拉检测的机制, 终端根据枚举值决定何时检测是否要重拉
    val eachAdMinTimeGap: Int,  // 触发每条广告重拉的最小时间间隔，单位秒，对于每个trace_id，在该时间内的第二次触发会被忽略，值为-1时，表示忽略该time_gap （每条广告）
) : IKmmKeep {

    fun canTrigger(
        order: IKmmAdOrder,
        invokeTiming: Int,
        lastSeq: Int,
        lastTriggerTime: Long,
    ): Boolean {
        val invokeTimingValid = isInvokeTimingMatch(invokeTiming, this.invokeTiming)

        val offsetValid = offset == IGNORE ||
                (order.getAdSeq() - lastSeq <= offset)

        val curTime = getCurTimestampMillis()
        val minTimeGapValid = minTimeGap == IGNORE ||
                (curTime - lastTriggerTime >= minTimeGap * 1000)

        val eachAdMinTimeGapValid = eachAdMinTimeGap == IGNORE ||
                (curTime - order.env.aiState.lastRePullTime >= eachAdMinTimeGap * 1000)

        return invokeTimingValid && offsetValid && minTimeGapValid && eachAdMinTimeGapValid
    }

    fun getDistinctKey(): String =
        listOf(offset, minTimeGap, invokeTiming, eachAdMinTimeGap).joinToString("_")

    private fun isInvokeTimingMatch(appInvoke: Int, policyInvoke: Int): Boolean {
        if (appInvoke == policyInvoke) {
            return true
        }

        // 新闻将这3种视为同一个类型
        if (appInvoke == KmmCheckInvokeTiming.CHECK_PAGE_LOADED_WHEN_TAB_SWITCH) {
            return setOf(
                KmmCheckInvokeTiming.CHECK_PAGE_LOADED_WHEN_TAB_SWITCH,
                KmmCheckInvokeTiming.CHECK_PAGE_LOADED_RETURN_FROM_DETAIL_PAGE,
                KmmCheckInvokeTiming.CHECK_PAGE_LOADED_RETURN_FROM_BACKEND,
            ).contains(policyInvoke)
        }

        return false
    }

    override fun toString(): String =
        "${invokeStr(invokeTiming)}_偏移${offset}_频控${minTimeGap}|${eachAdMinTimeGap}"

    companion object {
        fun invokeStr(invokeTiming: Int): String {
            return when (invokeTiming) {
                KmmCheckInvokeTiming.CHECK_PAGE_SCROLLING -> "滑动"
                KmmCheckInvokeTiming.CHECK_PAGE_SCROLL_STOP -> "Idle"
                KmmCheckInvokeTiming.CHECK_PAGE_LOADED_WHEN_TAB_SWITCH -> "Show"

                else -> invokeTiming.toString()
            }
        }
    }

}


/**
 * CheckInvokeTiming 触发机制枚举
 */
annotation class KmmCheckInvokeTiming {
    companion object {
        const val CHECK_UNKNOWN = 0                             // 未知触发机制
        const val CHECK_PAGE_SCROLLING = 1                      // 滑动中触发
        const val CHECK_PAGE_SCROLL_STOP = 2                    // 滑动停止触发
        const val CHECK_PAGE_LOADED_WHEN_TAB_SWITCH = 3         // TAB切换导致页面加载触发
        const val CHECK_PAGE_LOADED_RETURN_FROM_DETAIL_PAGE = 4 // 详情页返回导致页面加载触发
        const val CHECK_PAGE_LOADED_RETURN_FROM_BACKEND = 5     // APP从后台进程切换到前台导致页面加载触发
        const val CHECK_FRAME_SWITCHED = 6                      // 帧切换触发，适用于横版（如焦点图）及竖版（如短视频）
    }
}