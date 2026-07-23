package com.tencent.news.core.platform

import kotlin.native.runtime.NativeRuntimeApi
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Kotlin/Native 堆大小上限（1.5GB）。
 *
 * 取值依据：与新闻 iOS（QnCore/PlatformGC.ios.kt）线上方案对齐，业界已验证过的保守阈值。
 * iOS 后台 jetsam 存活域在 2GB 左右，留 512MB 给原生/系统缓存。
 *
 * TODO bigbywang: 后续可改造为按设备内存档位动态计算（sysctlbyname("hw.memsize") 分档）
 *  或通过 Shiply 远程下发；当前先保持硬编码，线上观测崩溃/内存指标后再决策。
 */
private const val MAX_HEAP_BYTES_DEFAULT: Long = (1024L + 512L) * 1024L * 1024L

/**
 * iOS 端 GC 调优：设置常规 GC 间隔、目标堆利用率，并限制最大堆为 1.5GB。
 *
 * TODO bigbywang: regularGCInterval 设为固定秒级定时器后，进入后台仍会持续触发 GC 调度。
 *  目前 iosApp 侧尚未将 UIApplication 前后台通知桥接到 AppStateManager，无法直接注册
 *  IAppLifeCycleListener 做"后台设 INFINITE / 前台恢复"的联动（注册了也不会被触发）。
 *  待后续独立需求补齐该桥接后，在此处按 AppStateManager.isForeground() 动态切换 GC 间隔。
 *  当前暴露出的风险：后台 GC 调度开销（与新闻端线上保持一致，暂不构成止血阻塞项）。
 */
@OptIn(NativeRuntimeApi::class)
actual fun configGC(
    interval: Int,
    targetHeapUtilization: Double,
) {
    kotlin.native.runtime.GC.regularGCInterval = interval.toDuration(DurationUnit.SECONDS)
    kotlin.native.runtime.GC.targetHeapUtilization = targetHeapUtilization
    kotlin.native.runtime.GC.maxHeapBytes = MAX_HEAP_BYTES_DEFAULT
}
