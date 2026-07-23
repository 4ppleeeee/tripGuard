package com.tencent.news.core.util.frequency

import com.tencent.news.core.extension.differentDays
import com.tencent.news.core.extension.getMonthOfYear
import com.tencent.news.core.platform.api.StorageTable
import com.tencent.news.core.platform.api.appStatus
import com.tencent.news.core.platform.api.appStorage
import com.tencent.news.core.platform.api.getTable
import com.tencent.news.core.platform.getCurTimeMillis
import kotlin.math.abs

/**
 * 频次控制
 */
object FrequencyControl {
    const val SP_NAME = "kmm_frequency_sp"

    interface Strategy {
        fun isLimit(key: String): Boolean   // 是否受频控限制（true:受限，通常是出现频率到达最大值， false：未受限）
        fun record(key: String): Boolean        // 记录一次频控计数
        fun clear(key: String): Boolean = false // 清除频控
        fun getStatusLog(key: String): String = ""         // 用于调试频控现状的日志
    }

    /**
     * 每个自然日指定次数限制
     */
    class CountPerDayStrategy(private val limitCount: Int) : Strategy {

        override fun isLimit(key: String): Boolean {
            val recordTime: Long = getTable().getLong(timeKey(key), 0L)
            val currentTime = getCurTimeMillis()
            val isSameDay = differentDays(recordTime, currentTime) == 0
            val count: Int = getTable().getInt(countKey(key), 0)
            return if (isSameDay) {
                count >= limitCount
            } else {
                limitCount <= 0
            }
        }

        override fun record(key: String): Boolean {
            val recordTime: Long = getTable().getLong(timeKey(key), 0L)
            val currentTime = getCurTimeMillis()
            val isSameDay = differentDays(recordTime, currentTime) == 0
            if (isSameDay) {
                val count: Int = getTable().getInt(countKey(key), 0)
                getTable().putInt(countKey(key), count + 1)
            } else {
                getTable().apply {
                    putInt(countKey(key), 1)
                    putLong(timeKey(key), getCurTimeMillis())
                }
            }
            return true
        }

        private fun countKey(key: String): String = key + "_count"

        private fun timeKey(key: String): String = key + "_time"

        override fun clear(key: String): Boolean {
            getTable().apply {
                remove(timeKey(key))
                remove(countKey(key))
            }
            return true
        }

        private fun getCurTime(key: String): Long = getTable().getLong(timeKey(key), 0L)
        private fun getCurCount(key: String): Int = getTable().getInt(countKey(key), 0)

        override fun getStatusLog(key: String) = "$key 频控${getCurCount(key)}/${limitCount}"

    }

    /**
     * 每个自然日控制限制(集合版） Set<String>
     */
    class CountPerDayStrategySet {

        fun isLimit(key: String, value: String): Boolean {
            val recordTime: Long = getTable().getLong(timeKey(key), 0L)
            val currentTime = getCurTimeMillis()
            val isSameDay = differentDays(recordTime, currentTime) == 0
            val set = getTable().getStringSet(countKey(key), null)
            if (isSameDay) {
                return set?.contains(value) == true
            }
            return false
        }

        fun record(key: String, value: String): Boolean {
            val recordTime: Long = getTable().getLong(timeKey(key), 0L)
            val currentTime = getCurTimeMillis()
            val isSameDay = differentDays(recordTime, currentTime) == 0
            if (isSameDay) {
                val set: MutableSet<String> =
                    getTable().getStringSet(countKey(key), mutableSetOf())?.toMutableSet()
                        ?: mutableSetOf()
                set.add(value)
                getTable().apply {
                    putStringSet(countKey(key), set)
                }
            } else {
                val set: MutableSet<String> = mutableSetOf()
                set.add(value)
                getTable().apply {
                    putStringSet(countKey(key), set)
                    putLong(timeKey(key), getCurTimeMillis())
                }
            }
            return true
        }

        private fun countKey(key: String): String {
            return key + "_count"
        }

        private fun timeKey(key: String): String {
            return key + "_time"
        }

        fun clear(key: String): Boolean {
            getTable().remove(timeKey(key))
            getTable().remove(countKey(key))
            return true
        }
    }

    fun getTable(): StorageTable {
        return appStorage().getTable(SP_NAME)
    }

    /**
     * 固定周期频控
     *
     * - 同一个周期内，累加次数，根据[limitCount]判断是否超过限制
     * - 进入不同周期时，重置次数为1
     * - 周期判断由[periodChecker]决定
     *
     * Note：
     * - 固定周期不需要记录每次的时间，只需要记录周期开始时间即可
     * - 自然天、自然月等都属于固定周期
     * - 最近xxx一般不属于固定周期
     *
     * @param periodChecker 周期判断
     * @param limitCount 周期内限制次数
     */
    class FixedPeriodStrategy(
        private val periodChecker: IIntervalChecker,
        private val limitCount: Int,
    ) : Strategy {

        override fun isLimit(key: String): Boolean {
            val isSameInterval = isSameInterval(key)
            val count: Int = getTable().getInt(countKey(key), 0)
            return if (isSameInterval) {
                count >= limitCount
            } else {
                limitCount <= 0
            }
        }

        override fun record(key: String): Boolean {
            val isSameDay = isSameInterval(key)
            if (isSameDay) {
                val count: Int = getTable().getInt(countKey(key), 0)
                getTable().putInt(countKey(key), count + 1)
            } else {
                getTable().apply {
                    putInt(countKey(key), 1)
                    putLong(timeKey(key), getCurrentTime())
                }
            }
            return true
        }

        private fun isSameInterval(key: String): Boolean {
            val recordTime: Long = getTable().getLong(timeKey(key), 0L)
            val currentTime = getCurrentTime()
            val isSameInterval = periodChecker.invoke(currentTime, recordTime)
            return isSameInterval;
        }

        private fun getCurrentTime(): Long =
            getCurTimeMillis() / 1000

        private fun countKey(key: String): String {
            return key + "_count_interval"
        }

        private fun timeKey(key: String): String {
            return key + "_time_interval"
        }
    }


    interface IIntervalChecker {
        fun invoke(currentTime: Long, recordTime: Long): Boolean
    }

    /**
     * 指定间隔周期
     */
    class IntervalPeriodChecker(private val limitInterval: Long) : IIntervalChecker {
        override fun invoke(currentTime: Long, recordTime: Long): Boolean {
            return currentTime - recordTime < limitInterval
        }
    }

    /**
     * 指定时间间隔频次控制(间隔单位为秒)
     */
    class CountPerIntervalStrategy(private val limitInterval: Long, private val limitCount: Int) :
        Strategy by FixedPeriodStrategy(IntervalPeriodChecker(limitInterval), limitCount) {
    }

    /**
     * 自然月周期
     */
    internal object MonthlyIntervalChecker : IIntervalChecker {
        override fun invoke(currentTime: Long, recordTime: Long): Boolean {
            val currentMonth = getMonthOfYear(currentTime * 1000)
            val recordMonth = getMonthOfYear(recordTime * 1000)
            return currentMonth == recordMonth
        }
    }

    /**
     * 自然月周期频次控制
     */
    class MonthlyPeriodStrategy(private val limitCount: Int) :
        Strategy by FixedPeriodStrategy(MonthlyIntervalChecker, limitCount)


    val ONCE_STRATEGY: Strategy =
        object : Strategy {
            /**
             * 一个key只出一次
             */
            override fun isLimit(key: String): Boolean {
                return getTable().getBoolean(key, false)
            }

            override fun record(key: String): Boolean {
                getTable().putBoolean(key, true)
                return true
            }
        }

    /**
     * 按照版本做频次控制
     */
    class CountPerVersionStrategy(private val limitCount: Int) : Strategy {

        override fun isLimit(key: String): Boolean {
            val currentCount = getTable().getInt(countKey(key), 0)
            return currentCount >= limitCount
        }

        override fun record(key: String): Boolean {
            val currentCount = getTable().getInt(countKey(key), 0)
            getTable().putInt(countKey(key), currentCount + 1)
            return true
        }

        private fun countKey(key: String): String {
            return "${appStatus().getVersion()}_$key"
        }
    }

    /**
     * 按设备做频次控制
     */
    class CountPerDeviceStrategy(private val limitCount: Int) : Strategy {
        override fun isLimit(key: String): Boolean = getCurCount(key) >= limitCount

        override fun record(key: String): Boolean {
            getTable().putInt(key, getCurCount(key) + 1)
            return true
        }

        override fun clear(key: String): Boolean {
            getTable().putInt(key, 0)
            return true
        }

        private fun getCurCount(key: String): Int = getTable().getInt(key, 0)

        override fun getStatusLog(key: String) = "$key 频控${getCurCount(key)}/${limitCount}"
    }

    /**
     * 每次冷启动出[limitCount]次
     */
    class CountPerColdStart(private val limitCount: Int) : Strategy {

        companion object {
            private val LIMIT_COUNT_MAP = HashMap<String, Int>()
        }

        override fun isLimit(key: String): Boolean {
            val currentCount = LIMIT_COUNT_MAP[key] ?: 0
            return currentCount >= limitCount
        }

        override fun record(key: String): Boolean {
            val currentCount = LIMIT_COUNT_MAP[key] ?: 0
            LIMIT_COUNT_MAP[key] = currentCount + 1
            return true
        }
    }

    /**
     * 注意, 这是内存级别的控制, 并没有持续化到本地
     */
    class MillisFrequency(private val limitMillis: Long) : Strategy {

        companion object {
            private val LAST_MILLIS_MAP = HashMap<String, Long>()
        }

        override fun isLimit(key: String): Boolean {
            return abs((getCurrentTime() - getLastTimeMillis(key))) <= limitMillis
        }

        override fun record(key: String): Boolean {
            LAST_MILLIS_MAP[key] = getCurrentTime()
            return true
        }

        fun getLastTimeMillis(key: String): Long {
            return LAST_MILLIS_MAP[key] ?: 0
        }

        private fun getCurrentTime(): Long {
            return getCurTimeMillis()
        }

    }


    /**
     * 持续化到本地时间频控
     */
    class MillisSpFrequency(
        private val tableName: String,
        private val limitMillis: Long,
    ) : Strategy {

        override fun isLimit(key: String): Boolean {
            return abs((getCurrentTime() - getLastTimeMillis(key))) <= limitMillis
        }

        override fun record(key: String): Boolean {
            getSp().putLong(key, getCurrentTime())
            return true
        }

        fun getLastTimeMillis(key: String): Long {
            return getSp().getLong(key, 0L)
        }

        private fun getCurrentTime(): Long {
            return getCurTimeMillis()
        }

        private fun getSp() = appStorage().getTable(SP_NAME)
    }

    /**
     * 活跃日频控
     *
     * [record]: 每次启动调用，记录当前活跃天数
     * [isLimit] 结合 [activeDaysChecker] 判断当前活跃天是否触发频控
     */
    class DauStrategy(private val activeDaysChecker: (Int) -> Boolean) : Strategy {

        override fun isLimit(key: String): Boolean {
            return activeDaysChecker(getTable().getInt(dauCountKey(key), 0))
        }

        override fun record(key: String): Boolean {
            val lastTime = getTable().getLong(dauTimeKey(key), 0L)
            val currentTime = getCurTimeMillis()
            if (isNewDau(currentTime, lastTime)) { // 新日活
                val count = getTable().getInt(dauCountKey(key), 0)
                getTable().apply {
                    putInt(dauCountKey(key), count + 1) // 日活跃+1
                    putLong(dauTimeKey(key), currentTime) // 日活跃时间更新
                }
                return true
            }
            return false
        }

        /**
         * 判断新日活标准：日历日
         */
        private fun isNewDau(currentTime: Long, lastTime: Long) =
            differentDays(lastTime, currentTime) > 0

        private fun dauCountKey(key: String): String {
            return "${key}_dau_count"
        }

        private fun dauTimeKey(key: String): String {
            return "${key}_dau_last_time"
        }

    }

    class MinuteStrategy(private val min: Long) : Strategy {
        override fun isLimit(key: String): Boolean {
            return getCurTimeMillis() - getTable().getLong(key, 0) < min * 60 * 1000
        }

        override fun record(key: String): Boolean {
            getTable().putLong(key, getCurTimeMillis())
            return true
        }

    }

    class SecondStrategy(private val second: Long) : Strategy {
        override fun isLimit(key: String): Boolean {
            return getCurTimeMillis() - getTable().getLong(key, 0) < second * 1000
        }

        override fun record(key: String): Boolean {
            getTable().putLong(key, getCurTimeMillis())
            return true
        }

    }

}

/**
 * 内存中，毫秒级 频次限制
 */
open class MillisFrequency(private val limitMillis: Long) {

    private var lastActionTime = 0L

    fun run(action: () -> Unit) {
        val deltaTime = getCurrentTime() - lastActionTime
        if (lastActionTime <= 0 || deltaTime < 0 || deltaTime >= limitMillis) {
            action.invoke()
            lastActionTime = getCurrentTime()
        }
    }

    protected open fun getCurrentTime() = getCurTimeMillis()

}