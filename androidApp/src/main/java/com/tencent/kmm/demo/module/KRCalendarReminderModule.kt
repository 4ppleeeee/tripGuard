package com.tencent.kmm.demo.module

import android.app.Activity
import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * 日历提醒模块 Android 端实现
 * 日历提醒 Kuikly 模块。
 * 提供日历权限检查、权限请求、写日历事件、删日历事件、查询日历事件状态等能力
 */
class KRCalendarReminderModule : KuiklyRenderBaseModule() {

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        Log.i(TAG, "[DEBUG] call: method=$method, params=$params, callback=$callback")
        return when (method) {
            METHOD_IS_CALENDAR_ENABLED -> isCalendarEnabled(callback)
            METHOD_REQUEST_CALENDAR_PERMISSION -> requestCalendarPermission(callback)
            METHOD_SAVE_EVENT -> saveEvent(params, callback)
            METHOD_DELETE_EVENT -> deleteEvent(params, callback)
            METHOD_QUERY_EVENT_STATE -> queryEventState(params, callback)
            else -> {
                callback?.invoke(mapOf("code" to -1, "msg" to "method not found: $method"))
                null
            }
        }
    }

    /**
     * 检查日历权限是否已开启
     */
    private fun isCalendarEnabled(callback: KuiklyRenderCallback?) {
        val ctx = context ?: run {
            callback?.invoke(mapOf("code" to -1, "msg" to "context is null"))
            return
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val data = mapOf("isEnable" to if (hasPermission) 1 else 0)
        callback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
    }

    /**
     * 请求日历权限
     */
    private fun requestCalendarPermission(callback: KuiklyRenderCallback?) {
        Log.i(TAG, "requestCalendarPermission: activity=$activity, context=$context")
        val act = activity ?: findActivity(context) ?: run {
            Log.e(TAG, "requestCalendarPermission: activity is null, cannot request permission")
            callback?.invoke(mapOf("code" to -1, "msg" to "activity is null"))
            return
        }
        Log.i(TAG, "requestCalendarPermission: resolved activity=$act")

        // 已有权限，直接回调
        if (ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_CALENDAR)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "requestCalendarPermission: already granted")
            val data = mapOf("isAuth" to 1)
            callback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
            return
        }

        // 请求权限
        Log.i(TAG, "requestCalendarPermission: requesting permissions")
        pendingPermissionCallback = callback
        ActivityCompat.requestPermissions(
            act,
            CALENDAR_PERMISSIONS,
            REQUEST_CODE_CALENDAR
        )
    }

    /**
     * 从 Context 中尝试提取 Activity（兜底方案）
     */
    private fun findActivity(context: Context?): Activity? {
        var ctx = context
        while (ctx != null) {
            if (ctx is Activity) return ctx
            ctx = if (ctx is ContextWrapper) ctx.baseContext else null
        }
        return null
    }

    /**
     * 写日历提醒事件
     */
    private fun saveEvent(params: String?, callback: KuiklyRenderCallback?) {
        val ctx = context ?: run {
            callback?.invoke(mapOf("code" to -1, "msg" to "context is null"))
            return
        }
        if (params.isNullOrEmpty()) {
            callback?.invoke(mapOf("code" to -1, "msg" to "params is empty"))
            return
        }

        try {
            val json = JSONObject(params)
            val activityInfo = CalendarActivityInfo.fromJson(json)
            if (activityInfo.isInvalid()) {
                callback?.invoke(mapOf("code" to -1, "msg" to "invalid activityInfo"))
                return
            }

            // 如果已存在，先删后写
            if (isActivityEventExist(ctx, activityInfo.eventID)) {
                deleteCalendarEvent(ctx, activityInfo.eventID)
            }

            val success = addCalendarEvent(ctx, activityInfo)
            val data = mapOf("isSave" to if (success) 1 else 0)
            callback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
        } catch (e: Exception) {
            Log.e(TAG, "saveEvent error", e)
            val data = mapOf("isSave" to 0)
            callback?.invoke(mapOf<String, Any>("code" to -1, "msg" to (e.message ?: "unknown error"), "data" to data))
        }
    }

    /**
     * 删除日历提醒事件
     */
    private fun deleteEvent(params: String?, callback: KuiklyRenderCallback?) {
        val ctx = context ?: run {
            callback?.invoke(mapOf("code" to -1, "msg" to "context is null"))
            return
        }
        if (params.isNullOrEmpty()) {
            callback?.invoke(mapOf("code" to -1, "msg" to "params is empty"))
            return
        }

        try {
            val json = JSONObject(params)
            val eventID = json.optString("eventID", "")
            if (eventID.isEmpty()) {
                callback?.invoke(mapOf("code" to -1, "msg" to "eventID is empty"))
                return
            }

            val success = deleteCalendarEvent(ctx, eventID)
            val data = mapOf("isDelete" to if (success) 1 else 0)
            callback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
        } catch (e: Exception) {
            Log.e(TAG, "deleteEvent error", e)
            val data = mapOf("isDelete" to 0)
            callback?.invoke(mapOf<String, Any>("code" to -1, "msg" to (e.message ?: "unknown error"), "data" to data))
        }
    }

    /**
     * 查询日历提醒事件是否存在
     */
    private fun queryEventState(params: String?, callback: KuiklyRenderCallback?) {
        val ctx = context ?: run {
            callback?.invoke(mapOf("code" to -1, "msg" to "context is null"))
            return
        }
        if (params.isNullOrEmpty()) {
            callback?.invoke(mapOf("code" to -1, "msg" to "params is empty"))
            return
        }

        try {
            val json = JSONObject(params)
            val eventID = json.optString("eventID", "")
            if (eventID.isEmpty()) {
                callback?.invoke(mapOf("code" to -1, "msg" to "eventID is empty"))
                return
            }

            val exists = isActivityEventExist(ctx, eventID)
            val data = mapOf("isExist" to if (exists) 1 else 0)
            callback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
        } catch (e: Exception) {
            Log.e(TAG, "queryEventState error", e)
            val data = mapOf("isExist" to 0)
            callback?.invoke(mapOf<String, Any>("code" to -1, "msg" to (e.message ?: "unknown error"), "data" to data))
        }
    }

    // ==================== 日历操作核心逻辑 ====================

    /**
     * 添加日历事件
     */
    private fun addCalendarEvent(context: Context, activityInfo: CalendarActivityInfo): Boolean {
        val mCalendar = Calendar.getInstance()
        mCalendar.timeInMillis = activityInfo.startTime * 1000
        val start = mCalendar.time.time
        mCalendar.timeInMillis = activityInfo.endTime * 1000
        val end = mCalendar.time.time

        val event = ContentValues().apply {
            put(CalendarContract.Events.TITLE, activityInfo.eventTitle)
            put(CalendarContract.Events.DESCRIPTION, buildEventNote(activityInfo.eventNote))
            put(CalendarContract.Events.CALENDAR_ID, DEFAULT_CALENDAR_ID)
            put(CalendarContract.Events.CUSTOM_APP_URI, activityInfo.eventID)
            put(CalendarContract.Events.DTSTART, start)
            put(CalendarContract.Events.DTEND, end)
            put(CalendarContract.Events.HAS_ALARM, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai")
        }

        // 设置重复规则
        if (activityInfo.endDate > 0 && activityInfo.recurrentInterval > 0) {
            val finishDate = getRFC5545Time(activityInfo.endDate * 1000)
            val rrule = "FREQ=DAILY;INTERVAL=${activityInfo.recurrentInterval};UNTIL=$finishDate"
            Log.i(TAG, "add event rrule = $rrule")
            event.put(CalendarContract.Events.RRULE, rrule)
        }

        val newEvent = try {
            context.contentResolver.insert(Uri.parse(CALENDAR_EVENT_URL), event)
        } catch (e: Exception) {
            Log.e(TAG, "insert event failed", e)
            null
        }

        if (newEvent == null) {
            Log.e(TAG, "insert event failed, uri is null")
            return false
        }

        // 设置提醒
        var alarmOffset = activityInfo.alarmOffset / 60
        if (alarmOffset < 0) alarmOffset = 0

        val calendarEventId = ContentUris.parseId(newEvent)
        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, calendarEventId)
            put(CalendarContract.Reminders.MINUTES, alarmOffset)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }

        val reminderUri = try {
            context.contentResolver.insert(Uri.parse(CALENDAR_REMINDER_URL), reminderValues)
        } catch (e: Exception) {
            Log.e(TAG, "insert reminder failed", e)
            null
        }

        if (reminderUri == null) {
            Log.e(TAG, "insert reminder failed, uri is null")
            return false
        }

        // 保存事件标记
        saveActivityExistFlag(context, activityInfo.eventID)
        saveCalendarEventId(context, activityInfo.eventID, calendarEventId)
        return true
    }

    /**
     * 删除日历事件
     */
    private fun deleteCalendarEvent(context: Context, activityID: String): Boolean {
        val calendarEventId = getCalendarEventId(context, activityID)
        if (calendarEventId < 0) return false

        val deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDAR_EVENT_URL), calendarEventId)
        val rows = try {
            context.contentResolver.delete(deleteUri, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "delete event failed", e)
            -1
        }

        if (rows < 0) {
            Log.e(TAG, "delete event failed, rows=$rows")
            return false
        }

        removeCalendarEventId(context, activityID)
        removeActivityExistFlag(context, activityID)
        return true
    }

    // ==================== SharedPreferences 操作 ====================

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun isActivityEventExist(context: Context, activityId: String): Boolean {
        return getPrefs(context).getBoolean(KEY_CALENDAR_HAS_ACTIVITY_PREFIX + activityId, false)
    }

    private fun saveActivityExistFlag(context: Context, activityId: String) {
        getPrefs(context).edit()
            .putBoolean(KEY_CALENDAR_HAS_ACTIVITY_PREFIX + activityId, true)
            .apply()
    }

    private fun removeActivityExistFlag(context: Context, activityId: String) {
        getPrefs(context).edit()
            .remove(KEY_CALENDAR_HAS_ACTIVITY_PREFIX + activityId)
            .apply()
    }

    private fun getCalendarEventId(context: Context, activityId: String): Long {
        return getPrefs(context).getLong(KEY_CALENDAR_EVENT_ID_PREFIX + activityId, -1)
    }

    private fun saveCalendarEventId(context: Context, activityId: String, calendarEventId: Long) {
        getPrefs(context).edit()
            .putLong(KEY_CALENDAR_EVENT_ID_PREFIX + activityId, calendarEventId)
            .apply()
    }

    private fun removeCalendarEventId(context: Context, activityId: String) {
        getPrefs(context).edit()
            .remove(KEY_CALENDAR_EVENT_ID_PREFIX + activityId)
            .apply()
    }

    // ==================== 工具方法 ====================

    private fun buildEventNote(originalNote: String?): String {
        val linkWithTip = "$TASK_CENTER_APP_LINK\n$CALENDAR_LINK_TIP"
        return if (originalNote.isNullOrEmpty()) {
            linkWithTip
        } else {
            "$originalNote\n\n$linkWithTip\n"
        }
    }

    /**
     * 将时间戳转换为 RFC5545 格式的时间字符串（UNTIL 字段使用）
     */
    private fun getRFC5545Time(timeMillis: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(timeMillis)
    }

    /**
     * 处理权限请求结果（需要在 Activity 的 onRequestPermissionsResult 中调用）
     */
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE_CALENDAR) return
        val granted = grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        val data = mapOf("isAuth" to if (granted) 1 else 0)
        pendingPermissionCallback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
        pendingPermissionCallback = null
    }

    companion object {
        const val MODULE_NAME = "WSKuiklyCalendarReminderModule"
        private const val TAG = "KRCalendarReminder"

        private const val METHOD_IS_CALENDAR_ENABLED = "isCalendarEnabled"
        private const val METHOD_REQUEST_CALENDAR_PERMISSION = "requestCalendarPermission"
        private const val METHOD_SAVE_EVENT = "saveEvent"
        private const val METHOD_DELETE_EVENT = "deleteEvent"
        private const val METHOD_QUERY_EVENT_STATE = "queryEventState"

        private const val CALENDAR_EVENT_URL = "content://com.android.calendar/events"
        private const val CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders"
        private const val DEFAULT_CALENDAR_ID = 1

        private const val PREFS_NAME = "ws_calendar_reminder"
        private const val KEY_CALENDAR_HAS_ACTIVITY_PREFIX = "calendar_has_activity_"
        private const val KEY_CALENDAR_EVENT_ID_PREFIX = "calendar_event_id_"

        private const val TASK_CENTER_APP_LINK =
            "demo://kuikly?page_name=TaskCenterPage&bundle_name=TaskCenterPage&needlogin=1"
        private const val CALENDAR_LINK_TIP = "点击链接打开应用"

        private const val REQUEST_CODE_CALENDAR = 10086

        private val CALENDAR_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR
        )

        // 权限请求回调（静态持有，因为权限结果通过 Activity 回调）
        private var pendingPermissionCallback: KuiklyRenderCallback? = null

        /**
         * 静态方法：处理权限请求结果
         * 在 Activity 的 onRequestPermissionsResult 中调用
         */
        fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
            if (requestCode != REQUEST_CODE_CALENDAR) return
            val granted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            val data = mapOf("isAuth" to if (granted) 1 else 0)
            pendingPermissionCallback?.invoke(mapOf("code" to 0, "msg" to "", "data" to data))
            pendingPermissionCallback = null
        }
    }
}

/**
 * 日历事件信息数据类
 */
data class CalendarActivityInfo(
    val startTime: Long = 0,
    val endTime: Long = 0,
    val endDate: Long = 0,
    val recurrentInterval: Int = 0,
    val alarmOffset: Int = 0,
    val url: String = "",
    val eventID: String = "",
    val eventTitle: String = "",
    val eventSubTitle: String = "",
    var eventNote: String = ""
) {
    fun isInvalid(): Boolean = startTime <= 0 || endTime <= 0

    companion object {
        fun fromJson(json: JSONObject): CalendarActivityInfo {
            return CalendarActivityInfo(
                startTime = json.optLong("startTime", 0),
                endTime = json.optLong("endTime", 0),
                endDate = json.optLong("endDate", 0),
                recurrentInterval = json.optInt("recurrentInterval", 0),
                alarmOffset = json.optInt("alarmOffset", 0),
                url = json.optString("url", ""),
                eventID = json.optString("eventID", ""),
                eventTitle = json.optString("eventTitle", ""),
                eventSubTitle = json.optString("eventSubTitle", ""),
                eventNote = json.optString("eventNote", "")
            )
        }
    }
}
