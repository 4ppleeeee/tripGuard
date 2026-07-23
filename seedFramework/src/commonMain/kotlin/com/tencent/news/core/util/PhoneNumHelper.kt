package com.tencent.news.core.util

fun isPhoneNumValid(phoneNum: String): Boolean {
    val regex = "^1[3-9]\\d{9}$".toRegex()
    return regex.matches(phoneNum)
}

fun encryptedPhoneNum(phoneNum: String): String {
    if (!isPhoneNumValid(phoneNum)) {
        return phoneNum
    }
    return phoneNum.substring(0, 3) + "****" + phoneNum.substring(7, 11)
}