package com.tencent.news.core.tads.constants

enum class AdDisplayCode(val code: String) {
    NONE(""),                         // 无互动
    SLIDE("SlideInteractive"),        // 滑动互动
    TIMELINE("TimelineWidget"),       // 时间线挂件（扭动）
    TWIST("ShakeInteractive");        // 扭动互动 @7270 版本引入

    companion object {
        // 根据 code 字符串查找对应的枚举值，未找到则返回 NONE
        fun fromCode(code: String?): AdDisplayCode {
            if (code.isNullOrEmpty()) return NONE
            return values().find { it.code == code } ?: NONE
        }
    }
}
