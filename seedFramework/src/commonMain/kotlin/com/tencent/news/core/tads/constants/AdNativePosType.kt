package com.tencent.news.core.tads.constants

/* 请求上报时采用，标识广告原始数据类型 */
enum class AdNativePosType(val value: Int) {
    NON_NATIVE(0),     // 非原生
    HARD_AD_NATIVE(1), // 硬广原生槽位
    NATIVE_AD(2)       // 原生广告槽位
}

/* 曝光上报时采用，数据处理完成后对应的广告类型 */
enum class AdNativeType(val value: String) {
    HARD_AD(""),            // 普通硬广
    DEGRADED_NATIVE("0"),   // 原生降级硬广
    NATIVE_AD("1")          // 原生广告
}