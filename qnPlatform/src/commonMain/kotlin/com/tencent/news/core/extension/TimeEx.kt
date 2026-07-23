package com.tencent.news.core.extension


// 将 99s 转换成 "1:39"
fun formatTimeInSeconds(timeInSecond: Long): String {
    val seconds = timeInSecond % 60
    val minutes = (timeInSecond / 60) % 60
    val hours = timeInSecond / 3600

    return if (hours > 0) {
        "${hours}:${padTimeNum(minutes)}:${padTimeNum(seconds)}"
    } else {
        "${padTimeNum(minutes)}:${padTimeNum(seconds)}"
    }
}

fun padTimeNum(num: Number): String {
    return num.toString().padStart(2, '0')
}