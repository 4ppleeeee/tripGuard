package com.tencent.news.core.platform

import com.tencent.news.core.ohos.setup.ohosDateService

actual fun getPlatformDate(): IKmmPlatformDate {
    return OhosPlatformDate
}

internal object OhosPlatformDate : IKmmPlatformDate {

    override fun createDate(timeInMillis: Long): IKtDate {
        return OhosKtDate(timeInMillis)
    }

    override fun getCurTimeMillis(): Long {
        return ohosDateService.getCurTimeMillis()
    }

    override fun formatTimestamp(timeInSeconds: Long, format: String): String {
        return ohosDateService.formatTimestamp(timeInSeconds, format)
    }

    override fun parseTimeSecondsByFormat(timeStr: String, format: String): Long {
        if (timeStr.isBlank()) return 0L

        return kotlin.runCatching {
            ohosDateService.parseTimeSecondsByFormat(timeStr, format)
        }.getOrElse {
            // timeStr 传递非法字符串时，可能解析失败
            return@getOrElse 0L
        }
    }

}

internal class OhosKtDate(timeInMillis: Long) : IKtDate {

    override val year: Int = ohosDateService.getYear(timeInMillis)

    override val dayOfYear: Int = ohosDateService.getDayOfYear(timeInMillis)

    override val month: Int = ohosDateService.getMonth(timeInMillis)

    override val dayOfMonth: Int = ohosDateService.getDayOfMonth(timeInMillis)

    override val hourOfDay: Int = ohosDateService.getHourOfDay(timeInMillis)

    override val weekday: Int = ohosDateService.getDayOfWeek(timeInMillis)

    override val minute: Int = ohosDateService.getMinute(timeInMillis)

    override val second: Int = ohosDateService.getSecond(timeInMillis)
}

