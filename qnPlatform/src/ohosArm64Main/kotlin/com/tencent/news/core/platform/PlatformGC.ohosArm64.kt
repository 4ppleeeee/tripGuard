package com.tencent.news.core.platform

import kotlin.native.runtime.NativeRuntimeApi

/**
 * 鸿蒙端 GC 调优：延迟主线程挂起 + 清理空页面，缓解列表滑动时 GC 暂停主线程导致的白屏。
 * interval / targetHeapUtilization 在鸿蒙运行时暂无对应 API，保留签名对齐，供后续扩展。
 */
@OptIn(NativeRuntimeApi::class)
actual fun configGC(
    interval: Int,
    targetHeapUtilization: Double,
) {
    // GC 回收时不立即 STW，等主线程空闲再执行
    kotlin.native.runtime.GC.setDelayMainThreadSuspend(true)
    // 持久化上面的配置，避免被运行时重置
    kotlin.native.runtime.GC.setDelayMainThreadSuspendConfig(1)
    // 清理空页面，降低内存占用，防止系统因内存压力杀进程
    kotlin.native.runtime.GC.setCleanEmptyPageEnable(true)
}
