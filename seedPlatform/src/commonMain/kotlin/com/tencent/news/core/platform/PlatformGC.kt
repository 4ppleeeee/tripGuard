package com.tencent.news.core.platform

/**
 * Kotlin/Native 运行时 GC 调优入口，仅 iOS / 鸿蒙生效，Android 为空实现。
 * 须在应用启动最早期调用（早于任何业务 setup）。
 *
 * @param interval GC 间隔（秒）
 * @param targetHeapUtilization 目标堆利用率（0.0 ~ 1.0）
 */
expect fun configGC(
    interval: Int,
    targetHeapUtilization: Double,
)
