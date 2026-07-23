package com.tencent.news.core.app.constants

// 【注意】key 和 strKey 值不能随便改，会作为请求参数使用
enum class ScreenType(val key: Int, val strKey: String, val desc: String) {
    UNKNOWN(0, "unknown", "未知设备"),
    PHONE(1, "phone", "普通手机"),
    PAD(2, "pad", "平板"),
    FLIP(3, "flip", "翻盖手机"),
    FOLD(4, "fold", "折叠手机")
}