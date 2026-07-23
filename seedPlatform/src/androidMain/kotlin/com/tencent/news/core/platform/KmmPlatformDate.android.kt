package com.tencent.news.core.platform

import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.DAY_OF_YEAR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.YEAR
import java.util.Date
import java.util.Locale


actual fun getPlatformDate(): IKmmPlatformDate = AndroidPlatformDate()

class AndroidPlatformDate : IKmmPlatformDate {

    override fun createDate(timeInMillis: Long): IKtDate = AndroidKtDate(timeInMillis)

    override fun getCurTimeMillis(): Long = System.currentTimeMillis()

    override fun getElapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun formatTimestamp(timeInSeconds: Long, format: String): String {
        val date = Date(timeInSeconds * 1000L)
        return getFormatter(format).format(date)
    }

    override fun parseTimeSecondsByFormat(timeStr: String, format: String): Long {
        if (timeStr.isBlank()) return 0L

        return kotlin.runCatching {
            getFormatter(format).parse(timeStr).time / 1000L
        }.getOrElse {
            // timeStr 传递非法字符串时，可能解析失败
            return 0L
        }
    }

    private fun getFormatter(format: String): SimpleDateFormat =
        SimpleDateFormat(format, Locale.getDefault())

}

class AndroidKtDate(timeInMillis: Long) : IKtDate {

    private val date = Calendar.getInstance(Locale.CHINA).apply {
        this.timeInMillis = timeInMillis
    }

    override val year: Int
        get() = date.get(YEAR)

    override val dayOfYear: Int
        get() = date.get(DAY_OF_YEAR)

    override val hourOfDay: Int
        get() = date.get(HOUR_OF_DAY)

    override val month: Int
        get() = date.get(MONTH) + 1 // 口径约定的月份从1开始

    override val dayOfMonth: Int
        get() = date.get(DAY_OF_MONTH)

    override val weekday: Int
        get() = date.get(DAY_OF_WEEK)

    override val minute: Int
        get() = date.get(MINUTE)

    override val second: Int
        get() = date.get(SECOND)

}