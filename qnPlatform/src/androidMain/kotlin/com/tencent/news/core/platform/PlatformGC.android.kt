package com.tencent.news.core.platform

/** Android 无 Kotlin/Native 运行时 GC，空实现。 */
actual fun configGC(
    interval: Int,
    targetHeapUtilization: Double,
) {
}
