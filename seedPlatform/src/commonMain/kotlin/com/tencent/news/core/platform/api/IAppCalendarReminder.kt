package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

fun appCalendarReminder(): IAppCalendarReminder =
    QnPlatformLogic.calendarReminder ?: DefaultAppCalendarReminder

data class AppCalendarReminderRequest(
    val eventKey: String,
    val title: String,
    val desc: String,
    val startTimeSeconds: Long,
    val endTimeSeconds: Long,
    val reminderMinutesBefore: Int,
    val repeatDays: Int,
    val source: String,
)

data class AppCalendarReminderResult(
    val status: AppCalendarReminderStatus,
    val message: String = "",
)

enum class AppCalendarReminderStatus {
    Added,
    Removed,
    Exists,
    Missing,
    PermissionDenied,
    InvalidParam,
    Unsupported,
    Failed,
}

interface IAppCalendarReminder {
    fun addReminder(
        request: AppCalendarReminderRequest,
        callback: (AppCalendarReminderResult) -> Unit,
    )

    fun removeReminder(
        eventKey: String,
        callback: (AppCalendarReminderResult) -> Unit,
    )

    fun queryReminder(
        eventKey: String,
        callback: (AppCalendarReminderResult) -> Unit,
    )
}

private object DefaultAppCalendarReminder : IAppCalendarReminder {
    override fun addReminder(
        request: AppCalendarReminderRequest,
        callback: (AppCalendarReminderResult) -> Unit,
    ) {
        callback(AppCalendarReminderResult(AppCalendarReminderStatus.Unsupported))
    }

    override fun removeReminder(
        eventKey: String,
        callback: (AppCalendarReminderResult) -> Unit,
    ) {
        callback(AppCalendarReminderResult(AppCalendarReminderStatus.Unsupported))
    }

    override fun queryReminder(
        eventKey: String,
        callback: (AppCalendarReminderResult) -> Unit,
    ) {
        callback(AppCalendarReminderResult(AppCalendarReminderStatus.Unsupported))
    }
}
