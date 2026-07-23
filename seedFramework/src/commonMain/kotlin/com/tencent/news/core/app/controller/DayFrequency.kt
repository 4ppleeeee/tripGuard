package com.tencent.news.core.app.controller

import com.tencent.news.core.extension.checkClearStorageDataWhenDayChanged
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.list.trace.DayFreqLog
import com.tencent.news.core.platform.api.appStorage

typealias OnIncreaseAction = (newCount: Int) -> Unit

/**
 * 频率控制优先使用这个工具类
 * @see com.tencent.news.core.util.frequency.FrequencyControl
 */
class DayFrequency(
    private val tableName: String,
    private val limitCount: Int = 1, // <=0 为不限制次数
    private val onIncrease: OnIncreaseAction? = null,
) {

    fun increaseCount() = increaseCount(getKey())

    fun isLimited(): Boolean = isLimited(getKey())

    fun increaseCount(key: String?) {
        if (!enableLimit()) {
            onIncrease?.invoke(0)
            return
        }
        checkDataClear()

        val newCount = getCount(key) + 1
        setCount(key, newCount)

        onIncrease?.invoke(newCount)

        DayFreqLog.fileLog("", "$tableName 频控增加：${newCount}/${limitCount}")
    }

    fun isLimited(key: String?): Boolean {
        if (!enableLimit()) {
            return false
        }
        checkDataClear()

        return getCount(key) >= limitCount
    }

    private fun checkDataClear() =
        checkClearStorageDataWhenDayChanged(tableName, checkHourOfDay = 4)

    private fun enableLimit() = limitCount > 0

    private fun getCount(key: String?) = appStorage().getKV(tableName, getKey(key)).safeToInt()

    private fun setCount(key: String?, count: Int) {
        appStorage().setKV(tableName, getKey(key), count.toString())
    }

    private fun getKey(key: String? = null): String = key.takeIfNotEmpty() ?: "show_count"

}