package com.tencent.news.core.extension

import com.tencent.news.core.platform.api.appI18n
import kotlin.math.round


// 四舍五入，保留1位小数
fun roundToOneDecimal(value: Float): Float = round(value * 10) / 10

// 四舍五入，保留2位小数
fun roundToTwoDecimal(value: Double): Double = round(value * 100) / 100

inline fun Float.getNonZero(defaultValue: Float = Float.MAX_VALUE): Float {
    return this.takeIf { it != 0F } ?: defaultValue
}

private val NUM_SUFFIX_RATE = arrayOf("", "万", "亿")

// 将 10000 转为 ‘万’
fun tenTh2Wan(num: Long): String = tenTh2Wan(num.toString())

fun tenTh2Wan(numStr: String): String {
    val numLen = numStr.length // 位数
    if (numLen < 5) {
        return numStr
    }

    var hideNum = 0
    var rateIndex = 0

    if (numLen >= 5) {
        hideNum = 3
        rateIndex++
    }
    if (numLen >= 9) {
        hideNum = 7
        rateIndex++
    }

    val showOutStr = StringBuilder(numStr.substring(0, numLen - hideNum))

    val r: Char = showOutStr[showOutStr.length - 1]
    if (r != '0') {
        showOutStr.insert(showOutStr.length - 1, ".") // 写入小数点
    } else {
        showOutStr.deleteAt(showOutStr.length - 1) // 删除小数部分
    }

    val numSuffixRate = arrayOf("", "万", "亿")
    return showOutStr.append(numSuffixRate[rateIndex]).toString()
}

fun localizedDescForCount(count: Long): String = localizedDescForCount(
    count = count,
    languageTag = appI18n().currentLanguageTag()
)

fun localizedDescForCount(count: Long, languageTag: String): String {
    return when {
        languageTag.startsWith("zh-Hant") -> chineseDescForCount(
            count,
            tenThousandSuffix = "萬",
            hundredMillionSuffix = "億"
        )
        languageTag.startsWith("zh") -> chineseDescForCount(
            count,
            tenThousandSuffix = "万",
            hundredMillionSuffix = "亿"
        )
        else -> englishDescForCount(count)
    }
}

private fun chineseDescForCount(
    count: Long,
    tenThousandSuffix: String,
    hundredMillionSuffix: String
): String {
    if (count < 0) return "-${chineseDescForCount(-count, tenThousandSuffix, hundredMillionSuffix)}"
    if (count < 10_000) return count.toString()
    if (count < 100_000_000) return formatCompact(count / 10_000.0, tenThousandSuffix)
    return formatCompact(count / 100_000_000.0, hundredMillionSuffix)
}

private fun englishDescForCount(count: Long): String {
    if (count < 0) return "-${englishDescForCount(-count)}"
    if (count < 1_000) return count.toString()
    if (count < 1_000_000) return formatCompact(count / 1_000.0, "K")
    if (count < 1_000_000_000) return formatCompact(count / 1_000_000.0, "M")
    return formatCompact(count / 1_000_000_000.0, "B")
}

private fun formatCompact(value: Double, suffix: String): String {
    val rounded = round(value * 10) / 10
    val integer = rounded.toLong()
    val numberText = if (rounded - integer < 0.05) {
        integer.toString()
    } else {
        val scaled = (rounded * 10).toLong()
        "${scaled / 10}.${scaled % 10}"
    }
    return numberText + suffix
}
