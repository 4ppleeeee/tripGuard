package com.tencent.news.core.extension

import com.tencent.news.core.platform.api.IStorage
import com.tencent.news.core.platform.api.appStorage
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.createDate
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.platform.getPlatformDate
import com.tencent.news.core.platform.qnFileLog
import kotlin.math.abs

/**
 * 是否使用differentDays2方法的开关（默认开启）
 */
private val useNewDifferentDaysSwitch: Boolean by lazy {
    getShiplySwitch("use_new_diff_days", defaultValue = true)
}

// 存储当前数据时间戳的key（按自然日记录数据使用）
private const val DATA_TIMESTAMP_KEY = "data_timestamp_key"

/**
 * 自然日变化时，清空 tableName 存储的数据；
 * 【用法】：一般在数据的 读、写 时机之前，都调用一下这个check
 *
 * @return true: 自然日变化，发生数据清理
 */
fun checkClearStorageDataWhenDayChanged(tableName: String, checkHourOfDay: Int = 0): Boolean {
    val appStorage = appStorage()

    val lastDataTimestamp = appStorage.getDataTimestamp(tableName)
    val curDataTimestamp = getCurYearWithDayStamp()

    if (lastDataTimestamp <= 0) {
        appStorage.setDataTimestamp(tableName, curDataTimestamp)
        return false
    }
    val isChanged = checkDataWhenDayChanged(
        lastDataTimestamp,
        checkHourOfDay,
        "数据表：${tableName}"
    )
    if (isChanged) {
        appStorage.clearKV(tableName)
        appStorage.setDataTimestamp(tableName, curDataTimestamp)
    }
    return isChanged
}

/**
 * 判断是否自然日变化
 * @return true: 自然日变化
 */
fun checkDataWhenDayChanged(
    lastTimestamp: Int,
    checkHourOfDay: Int = 0,
    msg: String = "",
): Boolean {
    val curDataTimestamp = getCurYearWithDayStamp()
    if (checkHourOfDay > 0) {
        val hourOfDay = createDate().hourOfDay
        if (hourOfDay < checkHourOfDay) {
            return false // 当天x点以后才清理（很多投放case会凌晨验收，不能过了自然日就清理）
        }
    }
    if (curDataTimestamp != lastTimestamp) {
        qnFileLog()?.logW(
            "clearDataTimestamp",
            "${msg}，自然日变更：${lastTimestamp}->${curDataTimestamp}，清空时间戳"
        )
        return true
    }
    return false
}

/**
 * 获取当前时间戳
 * 时间戳格式：年份*1000+当年的第几天，例如：2023123
 */
fun getCurYearWithDayStamp(): Int {
    val curDate = createDate()
    return curDate.year * 1000 + curDate.dayOfYear
}

fun getCurTimestampMillis(): Long {
    return getCurTimeMillis()
}

fun getCurTimePassMillis(lastTime: Long): Long {
    return getCurTimestampMillis() - lastTime
}

/**
 * 获取tableName的数据时间戳
 */
private fun IStorage.getDataTimestamp(tableName: String): Int {
    return getKV(tableName, DATA_TIMESTAMP_KEY).toIntOrNull() ?: 0
}

/**
 * 设置tableName的数据时间戳
 */
private fun IStorage.setDataTimestamp(tableName: String, timestamp: Int) {
    setKV(tableName, DATA_TIMESTAMP_KEY, timestamp.toString())
}

/**
 * 限制在一天内，判断指定时间是否在限定的区间内
 * 给定一个时间HH:mm，以及时间范围beginH beginM — endH endM
 */
fun isTimeInHourMinScope(beginH: Int, beginM: Int, endH: Int, endM: Int): Boolean {
    val date = createDate()
    val curHour: Int = date.hourOfDay
    val curMinute: Int = date.minute
    // 0. 如果区间是一个小时内的
    if (beginH == endH) {
        // 如果beginH <= H <= endH && beginM <= m < endM，那么视为在区间内
        return isTimeInOneDayHourScope(
            curHour,
            beginH,
            endH + 1
        ) && isTimeInOneHourMinuteScope(
            curMinute, beginM, endM
        )
    }
    // 1. 如果beginH < H < endH，那么视为在区间内
    if (isTimeInOneDayHourScope(curHour, beginH + 1, endH)) {
        return true
    }
    // 2. 否则如果H=beginH，那么若m在beginM-60之间，视为在区间内
    if (isTimeInOneDayHourScope(
            curHour,
            beginH,
            beginH + 1
        ) && isTimeInOneHourMinuteScope(curMinute, beginM, 60)
    ) {
        return true
    }
    // 3. 否则如果H=endH，那么若m在0-endM之间，视为在区间内
    return isTimeInOneDayHourScope(curHour, endH, endH + 1) && isTimeInOneHourMinuteScope(
        curMinute,
        0,
        endM
    )
}

private fun isTimeInOneDayHourScope(currHour: Int, beginHour: Int, endHour: Int): Boolean {
    val minHour = 0
    val maxHour = 24
    if (beginHour < minHour || beginHour > maxHour || endHour < minHour || endHour > maxHour) {
        return false
    }
    return currHour in beginHour until endHour
}

private fun isTimeInOneHourMinuteScope(currMinute: Int, beginMinute: Int, endMinute: Int): Boolean {
    val minHour = 0
    val maxHour = 60
    if (beginMinute < minHour || beginMinute > maxHour || endMinute < minHour || endMinute > maxHour) {
        return false
    }
    return currMinute in beginMinute until endMinute
}

/**
 * time2和time1相隔的天数  旧版本跨年计算天数会有问题
 *
 * @param time1 单位ms
 * @param time2 单位ms
 * @return
 */
fun differentDays(time1: Long, time2: Long): Int {
    // 判断开关状态，如果打开则使用修复后的differentDays2方法
    if (useNewDifferentDaysSwitch) {
        return differentDays2(time1, time2)
    }

    val date1 = createDate(time1)
    val date2 = createDate(time2)
    val day1 = date1.dayOfYear
    val day2 = date2.dayOfYear
    val year1 = date1.year
    val year2 = date2.year
    if (year1 < year2) {
        var timeDistance = 0;
        for (i in year1 until year2) {
            if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) { // 闰年
                timeDistance += 366;
            } else { // 平年
                timeDistance += 365;
            }
        }
        return timeDistance + (day2 - day1);
    } else if (year2 > year1) {
        var timeDistance = 0;
        for (i in year2 until year1) {
            if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) { // 闰年
                timeDistance += 366;
            } else { // 平年
                timeDistance += 365;
            }
        }
        return timeDistance + (day1 - day2);
    } else {
        return abs(day2 - day1);
    }
}

/**
 * time2和time1相隔的天数（修复版本，正确处理跨年计算）
 *
 * @param time1 单位ms
 * @param time2 单位ms
 * @return
 */
fun differentDays2(time1: Long, time2: Long): Int {
    val date1 = createDate(time1)
    val date2 = createDate(time2)
    val day1 = date1.dayOfYear
    val day2 = date2.dayOfYear
    val year1 = date1.year
    val year2 = date2.year

    if (year1 == year2) {
        return abs(day2 - day1)
    }

    // 确保time1是较早的时间，time2是较晚的时间
    val (earlierDate, laterDate) = if (time1 <= time2) {
        Pair(date1, date2)
    } else {
        Pair(date2, date1)
    }

    val earlierDay = earlierDate.dayOfYear
    val laterDay = laterDate.dayOfYear
    val earlierYear = earlierDate.year
    val laterYear = laterDate.year

    var timeDistance = 0
    for (i in earlierYear until laterYear) {
        if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) { // 闰年
            timeDistance += 366
        } else { // 平年
            timeDistance += 365
        }
    }

    return timeDistance + (laterDay - earlierDay)
}

fun getMonthOfYear(thisTime: Long): Int = getPlatformDate().createDate(thisTime).month