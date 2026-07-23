package com.tencent.news.core.compose.debug.profiler

import com.tencent.kuikly.compose.profiler.RecompositionProfiler
import com.tencent.news.core.platform.api.appStorage
import com.tencent.news.core.platform.qnRcProfilerFileLog

/**
 * 重组性能分析工具管理器
 *
 * 用于管理 RecompositionProfiler 的启动、停止和配置
 * 开关状态持久化到本地存储
 *
 * 日志查看方式：
 * - 通过「分享日志」功能导出客户端日志文件
 * - 搜索关键字 **RCProfiler** 即可过滤全部重组分析日志
 */
object RecompositionProfilerManager {

    private const val TABLE_NAME = "recomposition_profiler"
    private const val KEY_ENABLED = "enabled"

    /** 自定义文件日志输出策略实例，避免重复注册 */
    private var fileLogStrategy: FileLogOutputStrategy? = null

    fun isEnabled(): Boolean {
        return appStorage().getKV(TABLE_NAME, KEY_ENABLED, "false") == "true"
    }

    fun setEnabled(enabled: Boolean) {
        appStorage().setKV(TABLE_NAME, KEY_ENABLED, enabled.toString())
        if (enabled) {
            startProfiler()
        } else {
            stopProfiler()
        }
    }

    fun startProfiler() {
        RecompositionProfiler.configure {
            sampleRate = 1.0f
            hotspotThreshold = 10
            enableLog = true
            enableFile = false       // 框架内置的 FileOutputStrategy 依赖 FileModule，改用自定义策略
            enableOverlay = true
            overlayTopCount = 50
        }
        RecompositionProfiler.excludeByPrefix("<get-")
        RecompositionProfiler.start()

        // 注册自定义的文件日志输出策略（写入 `rcprofiler/` 独立子目录）
        if (fileLogStrategy == null) {
            val strategy = FileLogOutputStrategy(logFrameEvents = true)
            fileLogStrategy = strategy
            RecompositionProfiler.addOutputStrategy(strategy)
            qnRcProfilerFileLog()?.logI("RCProfiler", "FileLogOutputStrategy 已注册，重组日志将写入 rcprofiler/ 子目录")
        }
    }

    fun stopProfiler() {
        // 移除自定义输出策略
        fileLogStrategy?.let {
            RecompositionProfiler.removeOutputStrategy(it)
            fileLogStrategy = null
        }
        RecompositionProfiler.stop()
    }

    fun resetProfiler() {
        RecompositionProfiler.reset()
    }

    fun getReport() = RecompositionProfiler.getReport()

    fun restoreStateOnStartup() {
        if (isEnabled()) {
            startProfiler()
        }
    }
}
