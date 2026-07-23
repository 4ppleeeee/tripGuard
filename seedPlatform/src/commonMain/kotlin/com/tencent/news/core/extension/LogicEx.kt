package com.tencent.news.core.extension

fun Boolean?.isTrue(): Boolean = this == true

fun Boolean?.isFalse(): Boolean = this == false

fun Boolean?.isTrueOrNull(): Boolean = this != false

fun Boolean?.isFalseOrNull(): Boolean = this != true