package com.tencent.news.core.util.dateformat

import com.tencent.news.core.platform.getPlatformDate
import kotlin.math.abs

object DateUtil {
    fun differentDays(thisTime: Long, thatTime: Long): Int {
        val thisDay = getPlatformDate().createDate(thisTime).dayOfYear
        val thatDay = getPlatformDate().createDate(thatTime).dayOfYear
        return abs(thatDay - thisDay)
    }

    fun getYearMonth(thisTime: Long): Int {
        return getPlatformDate().createDate(thisTime).month
    }

    fun differentSeconds(thisTime: Long, thatTime: Long): Long {
        return abs(thatTime - thisTime) / 1000
    }
}