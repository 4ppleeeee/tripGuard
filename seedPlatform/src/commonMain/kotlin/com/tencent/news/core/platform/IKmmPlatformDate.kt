package com.tencent.news.core.platform


expect fun getPlatformDate(): IKmmPlatformDate

interface IKmmPlatformDate {

    // 用于获取 年/月/日 时间
    fun createDate(timeInMillis: Long = getCurTimeMillis()): IKtDate

    // 获取当前的unix时间戳（单位：毫秒）
    fun getCurTimeMillis(): Long

    // 获取单调递增时间戳（毫秒），不受用户修改系统时间影响
    fun getElapsedRealtime(): Long

    // 格式化时间格式（单位：秒），例如："yyyy/MM/dd HH:mm"
    fun formatTimestamp(timeInSeconds: Long, format: String): String

    // 格式化时间格式，例如："2025-03-13 16:03:45" to 1741853025 （单位：秒）
    fun parseTimeSecondsByFormat(timeStr: String, format: String): Long

}

interface IKtDate {
    val year: Int
    val dayOfYear: Int  // 口径：1-365
    val month: Int      // 口径：1-12（各端基础库口径不一样，有的从0开始；这个接口抹平了）
    val dayOfMonth: Int // 口径：1-31
    val hourOfDay: Int

    /**
     * 周几，返回值范围 1-7,从周日开始计数。即:
     * 1 - 周日, 2 - 周一, 3 - 周二, 4 - 周三, 5 - 周四, 6 - 周五, 7 - 周六
     */
    val weekday: Int

    /**
     * 分钟
     */
    val minute: Int

    val second: Int
}

fun getCurTimeMillis(): Long {
    return getPlatformDate().getCurTimeMillis()
}

fun getElapsedRealtime(): Long {
    return getPlatformDate().getElapsedRealtime()
}

fun getCurTimeSecond(): Long {
    return getPlatformDate().getCurTimeMillis() / 1000
}

fun createDate(timeInMillis: Long = getCurTimeMillis()): IKtDate {
    return getPlatformDate().createDate(timeInMillis)
}

fun formatTimestamp(
    timeInSeconds: Long,
    format: String = getDefaultTimeFormat(timeInSeconds),
): String {
    return getPlatformDate().formatTimestamp(timeInSeconds, format)
}

// 【日期默认format规则】：
// - 不同年份："yyyy/MM/dd HH:mm"
// - 同一年内："MM/dd HH:mm"
// - 同一天内："HH:mm"
fun getDefaultTimeFormat(timeInSeconds: Long): String {
    val now = createDate()
    val target = createDate(timeInSeconds * 1000)

    return when {
        now.year != target.year -> "yyyy/MM/dd HH:mm"
        now.dayOfYear != target.dayOfYear -> "MM/dd HH:mm"
        else -> "HH:mm"
    }
}