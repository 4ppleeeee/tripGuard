package com.tencent.news.core.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithName


actual fun getPlatformDate(): IKmmPlatformDate = IOSKmmPlatformDate()

class IOSKmmPlatformDate : IKmmPlatformDate {

    override fun createDate(timeInMillis: Long): IKtDate {
        return IOSKtDate(timeInMillis)
    }

    override fun getCurTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    override fun getElapsedRealtime(): Long {
        return (NSProcessInfo.processInfo.systemUptime * 1000).toLong()
    }

    override fun formatTimestamp(timeInSeconds: Long, format: String): String {
        val date = createNSDate(timeInSeconds * 1000)
        return getFormatter(format).stringFromDate(date)
    }

    override fun parseTimeSecondsByFormat(timeStr: String, format: String): Long {
        return getFormatter(format).dateFromString(timeStr)?.timeIntervalSince1970()?.toLong() ?: 0
    }

    private fun getFormatter(format: String): NSDateFormatter {
        return NSDateFormatter().apply {
            dateFormat = format
            // 使用英文locale避免显示"上午"/"下午"等本地化字符
            locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX")
            // 如果格式字符串最后一位是'z'或'Z'，使用UTC时区
            timeZone = if (format.isNotEmpty() && format.last().lowercase() == "z") {
                NSTimeZone.timeZoneWithName("UTC") ?: NSTimeZone.timeZoneWithName("Asia/Shanghai") ?: NSTimeZone.localTimeZone()
            } else {
                NSTimeZone.timeZoneWithName("Asia/Shanghai") ?: NSTimeZone.localTimeZone()
            }
        }
    }
}

class IOSKtDate(timeInMillis: Long) : IKtDate {

    private val date = createNSDate(timeInMillis)
    private val c = NSCalendar.currentCalendar

    override val year: Int
        get() = c.components(NSCalendarUnitYear, date).year.toInt()

    override val dayOfYear: Int
        get() = c.ordinalityOfUnit(NSCalendarUnitDay, NSCalendarUnitYear, date).toInt()

    override val hourOfDay: Int // 【疑问】这里经过单测验证，hour比实际会+1，框架就是这样吗？
        get() = c.ordinalityOfUnit(NSCalendarUnitHour, NSCalendarUnitDay, date).toInt() - 1

    override val month: Int
        get() = c.ordinalityOfUnit(NSCalendarUnitMonth, NSCalendarUnitYear, date).toInt()

    override val dayOfMonth: Int
        get() = c.ordinalityOfUnit(NSCalendarUnitDay, NSCalendarUnitMonth, date).toInt()
    override val weekday: Int
        get() = c.components(NSCalendarUnitWeekday, date).weekday.toInt()

    override val minute: Int
        get() = c.components(NSCalendarUnitMinute, date).minute.toInt()

    override val second: Int
        get() = c.components(NSCalendarUnitSecond, date).second.toInt()
}

private fun createNSDate(timeInMillis: Long): NSDate {
    // 区分dateWithTimeIntervalSinceReferenceDate, 坐标原点是2001年
    return NSDate.dateWithTimeIntervalSince1970(timeInMillis / 1000.0)
}