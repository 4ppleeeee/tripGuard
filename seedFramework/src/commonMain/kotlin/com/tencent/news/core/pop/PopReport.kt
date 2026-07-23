package com.tencent.news.core.pop

import com.tencent.news.core.platform.api.appReport


private const val REPORT_EVENT = "pop_dialog_report"
private const val ACTION_SHOW_RESULT = "1"
private const val ACTION_DISMISS_BY_HIGHER = "2"
private const val ACTION_REMOVE_TASK = "3"

internal object PopReport {
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