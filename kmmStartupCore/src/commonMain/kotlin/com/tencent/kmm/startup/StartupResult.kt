package com.tencent.kmm.startup

data class StartupResult(
    val totalCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val totalDurationMs: Long,
    val taskDurations: Map<String, Long>,
    val failures: Map<String, Throwable>
)
