package com.tencent.news.core.compose.debug.profiler

import com.tencent.kuikly.compose.profiler.ComposableRecomposedEvent
import com.tencent.kuikly.compose.profiler.RecompositionEvent
import com.tencent.kuikly.compose.profiler.RecompositionFrameEndEvent
import com.tencent.kuikly.compose.profiler.RecompositionFrameStartEvent
import com.tencent.kuikly.compose.profiler.RecompositionOutputStrategy
import com.tencent.kuikly.compose.profiler.RecompositionReport
import com.tencent.news.core.platform.qnRcProfilerFileLog

/**
 * 文件日志输出策略。
 *
 * 将重组追踪数据通过 [qnRcProfilerFileLog] 输出到独立的 `rcprofiler/` 子目录，
 * 与主日志、视频日志位于同一父目录，可通过「分享日志」功能一并导出查看。
 *
 * TAG 统一为 `RCProfiler`，搜索此关键字即可过滤所有重组分析日志。
 *
 * @param logFrameEvents 是否输出每帧实时事件日志。
 *   true（默认）= 每帧实时输出，日志量大但信息完整；
 *   false = 仅在 getReport 时输出汇总报告，适合长时间采集。
 */
class FileLogOutputStrategy(
    private val logFrameEvents: Boolean = true
) : RecompositionOutputStrategy {

    companion object {
        const val TAG = "RCProfiler"
    }

    override fun onFrameComplete(events: List<RecompositionEvent>) {
        if (!logFrameEvents) return
        if (events.isEmpty()) return

        val hasRecomposition = events.any { it is ComposableRecomposedEvent }
        if (!hasRecomposition) return

        val log = qnRcProfilerFileLog() ?: return

        var indent = 0
        for (event in events) {
            when (event) {
                is RecompositionFrameStartEvent -> {
                    log.logD(TAG, "Frame #${event.frameId} START (ts=${event.timestampMs}ms)")
                    indent++
                }
                is RecompositionFrameEndEvent -> {
                    indent = (indent - 1).coerceAtLeast(0)
                    log.logD(TAG, "Frame #${event.frameId} END (duration=${event.durationMs}ms, recomposed=${event.recomposedCount})")
                }
                is ComposableRecomposedEvent -> {
                    if (event.composableName == "<anonymous>") continue
                    val locationInfo = if (event.sourceLocation != null) " @${event.sourceLocation}" else ""
                    val scopeInfo = if (event.scopeKey != null) " [scope=${event.scopeKey}]" else " [scope=none]"
                    val parentInfo = " [parent=${event.parentName ?: "<unknown>"}]"
                    val paramInfo = buildParamChangeString(event)
                    val statesInfo = if (event.triggerStates.isNotEmpty()) {
                        " triggers=[${event.triggerStates.joinToString(", ")}]"
                    } else ""
                    val indentStr = "  ".repeat(indent)
                    log.logD(TAG, "${indentStr}RECOMPOSED: ${event.composableName}$locationInfo (${event.durationMs}ms)$scopeInfo$parentInfo$paramInfo$statesInfo")
                }
                else -> { /* ScrollContextEvent / TouchContextEvent 等暂不处理 */ }
            }
        }
    }

    override fun onReportReady(report: RecompositionReport) {
        val log = qnRcProfilerFileLog() ?: return

        log.logI(TAG, "=== Recomposition Report ===")
        log.logI(TAG, "Session: ${report.sessionId}")
        log.logI(TAG, "Duration: ${report.durationMs}ms | Frames: ${report.totalFrames} | Recompositions: ${report.totalRecompositions}")

        if (report.hotspots.isNotEmpty()) {
            log.logI(TAG, "--- HOTSPOTS ---")
            for (hotspot in report.hotspots) {
                val loc = if (hotspot.sourceLocation != null) " @${hotspot.sourceLocation}" else ""
                log.logI(TAG, "  ${hotspot.name}$loc: ${hotspot.recompositionCount}x (avg=${formatFloat(hotspot.avgDurationMs)}ms, max=${hotspot.maxDurationMs}ms)")
            }
        }

        if (report.composables.isNotEmpty()) {
            log.logI(TAG, "--- Composables ---")
            for (stats in report.composables) {
                val marker = if (stats.isHotspot) " [HOTSPOT]" else ""
                val paramInfo = if (stats.paramChangeFrequency.isNotEmpty()) {
                    val freqs = stats.paramChangeFrequency.entries
                        .sortedByDescending { it.value }
                        .joinToString(", ") { "#${it.key}:${it.value}x" }
                    " params changed: [$freqs]"
                } else {
                    " no params change"
                }
                val stateInfo = if (stats.triggerStates.isNotEmpty()) {
                    " state changes: [${stats.triggerStates.joinToString(", ")}]"
                } else {
                    " no state change"
                }
                val loc = if (stats.sourceLocation != null) " @${stats.sourceLocation}" else ""
                log.logI(TAG, "  ${stats.name}$loc: ${stats.recompositionCount}x (avg=${formatFloat(stats.avgDurationMs)}ms)$marker$paramInfo$stateInfo")

                // Scope 分布行
                if (stats.scopeDistribution.isNotEmpty() || stats.noScopeRecompositions > 0) {
                    val scopeInfo = if (stats.scopeDistribution.isNotEmpty()) {
                        val sorted = stats.scopeDistribution.entries.sortedByDescending { it.value }
                        val displayed = sorted.take(5).joinToString(", ") { "${it.key}: ${it.value}x" }
                        val more = if (sorted.size > 5) ", ...+${sorted.size - 5} more" else ""
                        "{$displayed$more}"
                    } else {
                        "{}"
                    }
                    log.logI(TAG, "    → scopes: $scopeInfo, no-scope: ${stats.noScopeRecompositions}")
                }
            }
        }

        log.logI(TAG, "=== End of Recomposition Report ===")
    }

    override fun onReset() {
        qnRcProfilerFileLog()?.logI(TAG, "Profiler reset")
    }

    private fun buildParamChangeString(event: ComposableRecomposedEvent): String {
        val changes = event.paramChanges ?: return " params=[no params change]"
        if (!changes.hasChanges) return " params=[no changes] (0/${changes.totalParams})"
        val positions = changes.changedParams.joinToString(", ") { idx -> "#$idx" }
        return " params changed: [$positions] (${changes.changedParams.size}/${changes.totalParams})"
    }

    private fun formatFloat(value: Double): String {
        val intPart = value.toLong()
        val fracPart = ((value - intPart) * 10).toLong().let { kotlin.math.abs(it) }
        return "$intPart.$fracPart"
    }
}
