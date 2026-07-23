package com.tencent.news.core.ohos.setup

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.CLOCK_BOOTTIME
import platform.posix.clock_gettime
import platform.posix.gettimeofday
import platform.posix.localtime_r
import platform.posix.mktime
import platform.posix.strftime
import platform.posix.strptime
import platform.posix.time_tVar
import platform.posix.timespec
import platform.posix.timeval
import platform.posix.tm

internal val ohosDateService = OhosDateService

object OhosDateService {

    fun getYear(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_year + 1900
    }

    fun getDayOfYear(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_yday + 1
    }

    fun getMonth(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_mon + 1
    }

    fun getDayOfMonth(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_mday
    }

    fun getHourOfDay(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_hour
    }

    fun getDayOfWeek(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 1
        // tm_wday: 0-6, 0表示周日，转换为1-7
        return tm.tm_wday + 1
    }

    fun getMinute(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_min
    }

    fun getSecond(timeInMillis: Long): Int = memScoped {
        val tm = convertToTm(timeInMillis) ?: return 0
        return tm.tm_sec
    }

    fun getCurTimeMillis(): Long = memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        return@memScoped tv.tv_sec * 1000 + tv.tv_usec / 1000
    }

    fun getElapsedRealtime(): Long = memScoped {
        val ts = alloc<timespec>()
        clock_gettime(CLOCK_BOOTTIME, ts.ptr)
        return@memScoped ts.tv_sec * 1000 + ts.tv_nsec / 1_000_000
    }

    fun formatTimestamp(timeInSeconds: Long, format: String): String = memScoped {
        val timeVal = alloc<time_tVar>()
        timeVal.value = timeInSeconds

        val tmStruct = alloc<tm>()
        val result = localtime_r(timeVal.ptr, tmStruct.ptr)
        if (result == null) {
            return@memScoped ""
        }

        val cFormat = iso8601FormatToCFormat(format)

        val buffer = allocArray<ByteVar>(256)
        strftime(buffer, 256u, cFormat, tmStruct.ptr)
        return@memScoped buffer.toKString()
    }

    fun parseTimeSecondsByFormat(timeStr: String, format: String): Long = memScoped {
        val cFormat = iso8601FormatToCFormat(format)

        val tmStruct = alloc<tm>()
        val result = strptime(timeStr, cFormat, tmStruct.ptr)

        if (result == null) {
            return@memScoped 0L
        }

        return@memScoped mktime(tmStruct.ptr)
    }

    /**
     * 将时间戳（毫秒）转换为 tm 结构体
     * @param timeInMillis 时间戳（毫秒）
     * @return tm 结构体，如果转换失败返回 null
     * @note 使用 localtime_r 保证线程安全
     */
    private fun NativePlacement.convertToTm(timeInMillis: Long): tm? {
        val timeInSeconds = timeInMillis / 1000
        val timeVal = alloc<time_tVar>()
        timeVal.value = timeInSeconds

        val tmStruct = alloc<tm>()
        val result = localtime_r(timeVal.ptr, tmStruct.ptr)
        if (result == null) {
            return null
        }

        return tmStruct
    }

    // 匹配单字符 m（分钟），但不匹配 %m（月份）或 mm（双字符分钟）
    // 使用负向前瞻和负向后顾确保 m 前后不是 m 或 %
    private val minRegex = Regex("(?<!%)(?<!m)m(?!m)")
    private fun iso8601FormatToCFormat(isoFormat: String): String {
        val cFormat = isoFormat
            .replace("YYYY", "%Y", true)
            .replace("YY", "%Y", true)
            .replace("MM", "%m", false)
            // 兼容M月D日这种单字节格式
            .replaceIfNewNotExist("M", "%m", false)
            .replace("DD", "%d", true)
            .replaceIfNewNotExist("D", "%d", true)
            .replace("HH", "%H", true)
            .replaceIfNewNotExist("H", "%H", true)
            .replace("mm", "%M", false)
            // 注意不能把Month的%m中的m换成%M，
            .replaceIfNewNotExist(minRegex, "%M")
            .replace("SS", "%S", true)
            .replaceIfNewNotExist("S", "%S", true)
            // 去除'Z'/'T'字面量的单引号
            .replace("'", "")

        return cFormat
    }

    private fun String.replaceIfNewNotExist(
        oldValue: String,
        newValue: String,
        ignoreCase: Boolean = false
    ): String {

        if (!contains(newValue)) {
            return replace(oldValue, newValue, ignoreCase)
        }

        return this
    }

    private fun String.replaceIfNewNotExist(oldValue: Regex, newValue: String): String {

        if (!contains(newValue)) {
            return replace(oldValue, newValue)
        }

        return this
    }
}