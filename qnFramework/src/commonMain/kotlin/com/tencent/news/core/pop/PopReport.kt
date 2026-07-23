package com.tencent.news.core.pop

import com.tencent.news.core.platform.api.appReport


private const val REPORT_EVENT = "pop_dialog_report"
private const val PRIORITY_FUNNEL_EVENT = "pop_dialog_priority_funnel"
private const val ACTION_SHOW_RESULT = "1"
private const val ACTION_DISMISS_BY_HIGHER = "2"
private const val ACTION_REMOVE_TASK = "3"

internal object PopReport {
    fun reportPriorityFunnel(
        popTask: KmmPopTask,
        opportunityId: String,
        stage: String,
        logicalForm: String,
        rule: String,
        launchType: String,
        nSeconds: Long,
        ySeconds: Long,
        waitMs: Long,
        reason: PopQueueReason
    ) {
        appReport().reportBeacon(
            PRIORITY_FUNNEL_EVENT,
            mapOf(
                "opportunity_id" to opportunityId,
                "stage" to stage,
                "pop_type" to (popTask.type?.name ?: ""),
                "logical_form" to logicalForm,
                "priority" to popTask.priority.toString(),
                "rule" to rule,
                "hit_n" to if (rule == "form_interval" || rule == "both") "1" else "0",
                "hit_y" to if (rule == "hot_start" || rule == "both") "1" else "0",
                "launch_type" to launchType,
                "n_seconds" to nSeconds.toString(),
                "y_seconds" to ySeconds.toString(),
                "wait_ms" to waitMs.toString(),
                "reason" to reason.name.lowercase()
            )
        )
    }

    fun reportShowResultToBeacon(popTask: KmmPopTask, result: PopResult) {
        val popResult = result.name
        reportPopEvent(
            popTask, ACTION_SHOW_RESULT, mapOf(
                "result" to popResult
            )
        )
    }

    fun reportDismissByHigherToBeacon(popTask: KmmPopTask, higherTask: KmmPopTask) {
        val higherPopId = higherTask.id ?: ""
        val higherPriority = higherTask.priority
        reportPopEvent(
            popTask, ACTION_DISMISS_BY_HIGHER, mapOf(
                "higher_priority" to "$higherPriority",
                "higher_id" to higherPopId
            )
        )
    }

    fun reportRemoveTask(popTask: KmmPopTask) {
        reportPopEvent(popTask, ACTION_REMOVE_TASK)
    }

    private fun reportPopEvent(
        popTask: KmmPopTask,
        action: String,
        extraParams: Map<String, String>? = null,
    ) {
        val popId = popTask.id ?: ""
        val priority = popTask.priority
        val params = mutableMapOf(
            "action" to action,
            "id" to popId,
            "priority" to "$priority"
        )
        extraParams?.let {
            params.putAll(extraParams)
        }
        appReport().reportBeacon(REPORT_EVENT, params)
    }
}
