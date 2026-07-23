package com.tencent.kmm.demo.library.log

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android 重组分析日志文件 Logger（与 WsVideoLogger 同构）。
 *
 * 设计要点：
 * - 目录：`{WsLogger 主日志目录的父目录}/rcprofiler`
 * - 文件名：`rcprofiler_yyyyMMdd.log`，按天滚动；单文件上限 10MB，超过则切出 `rcprofiler_yyyyMMdd_HHmmss.log`
 * - 线程模型：单线程异步写入
 * - 保留策略：最多保留最近 [MAX_RETAIN_DAYS] 天
 * - 级别过滤：debug 包 d/i/w/e 全量；release 包仅 w/e
 * - 编码：UTF-8 明文
 * - flush：同步等待队列清空后 fsync 到磁盘
 */
object WsRcProfilerLogger {

    private const val TAG_SELF = "WsRcProfilerLogger"
    private const val SUB_DIR = "rcprofiler"
    private const val FILE_PREFIX = "rcprofiler_"
    private const val MAX_FILE_SIZE = 10L * 1024 * 1024 // 10MB
    private const val MAX_RETAIN_DAYS = 5
    private const val QUEUE_CAPACITY = 2000

    private val initialized = AtomicBoolean(false)
    private var isDebug: Boolean = false

    @Volatile
    private var logDir: File? = null

    @Volatile
    private var currentFile: File? = null

    @Volatile
    private var writer: OutputStreamWriter? = null

    @Volatile
    private var currentDateStamp: String = ""

    private val queue = LinkedBlockingQueue<String>(QUEUE_CAPACITY)

    private val executor = Executors.newSingleThreadExecutor(object : ThreadFactory {
        override fun newThread(r: Runnable): Thread = Thread(r, "WsRcProfilerLogger-Writer").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY - 1
        }
    })

    private val dateFmt = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyyMMdd", Locale.US)
    }
    private val timeFmt = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    }
    private val rolloverFmt = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    }

    fun setup(context: Any?, isDebug: Boolean) {
        if (!initialized.compareAndSet(false, true)) return
        this.isDebug = isDebug
        val ctx = context as? Context
        if (ctx == null) {
            Log.e(TAG_SELF, "setup: context is null or not Context, skipping init")
            return
        }
        try {
            val dir = resolveRcProfilerLogDir(ctx)
            if (dir == null) {
                Log.e(TAG_SELF, "setup: cannot resolve parent log dir")
                return
            }
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG_SELF, "setup: mkdirs failed: ${dir.absolutePath}")
                return
            }
            logDir = dir
            Log.i(TAG_SELF, "setup: rcprofilerLogDir=${dir.absolutePath}, isDebug=$isDebug")
            executor.execute {
                cleanExpired()
                ensureWriter()
            }
        } catch (e: Throwable) {
            Log.e(TAG_SELF, "setup: failed", e)
        }
    }

    fun d(tag: String?, message: String?) {
        if (!isDebug) return
        enqueue('D', tag, message, null)
    }

    fun i(tag: String?, message: String?) {
        if (!isDebug) return
        enqueue('I', tag, message, null)
    }

    fun w(tag: String?, message: String?) = enqueue('W', tag, message, null)
    fun e(tag: String?, message: String?) = enqueue('E', tag, message, null)
    fun e(tag: String?, message: String?, throwable: Throwable?) =
        enqueue('E', tag, message, throwable)

    fun getLogFileDir(): String? = logDir?.absolutePath

    fun flush() {
        if (!initialized.get()) return
        try {
            executor.submit {
                drainAndFlush()
            }.get(2, TimeUnit.SECONDS)
        } catch (e: Throwable) {
            Log.w(TAG_SELF, "flush: timeout or failed: ${e.message}")
        }
    }

    // -------------------------------------------------------------------------
    // 内部实现
    // -------------------------------------------------------------------------

    /**
     * 重组日志目录 = 主日志目录的**父目录** 下的 `rcprofiler` 子目录。
     * 若主日志目录未就绪，则回退到 `{files}/logs_parent/rcprofiler`（仅兜底）。
     */
    private fun resolveRcProfilerLogDir(ctx: Context): File? {
        val mainDirPath = runCatching { WsLogger.getLogFileDir() }.getOrNull()
        val parent: File? = when {
            !mainDirPath.isNullOrEmpty() -> File(mainDirPath).parentFile
            else -> ctx.filesDir
        }
        return parent?.let { File(it, SUB_DIR) }
    }

    private fun enqueue(level: Char, tag: String?, message: String?, throwable: Throwable?) {
        if (!initialized.get()) return
        val line = formatLine(level, tag, message, throwable)
        if (!queue.offer(line)) {
            Log.w(TAG_SELF, "queue full, drop log: ${line.take(80)}")
            return
        }
        executor.execute { drain() }
    }

    private fun formatLine(level: Char, tag: String?, message: String?, throwable: Throwable?): String {
        val time = timeFmt.get()!!.format(Date())
        val safeTag = tag ?: "-"
        val safeMsg = message ?: ""
        val tr = throwable?.let { "\n" + it.stackTraceToString() } ?: ""
        return "$time $level/$safeTag $safeMsg$tr\n"
    }

    private fun drain() {
        val w = ensureWriter() ?: return
        try {
            var line = queue.poll() ?: return
            do {
                w.write(line)
                val f = currentFile
                if (f != null && f.length() >= MAX_FILE_SIZE) {
                    rolloverBySize()
                    break
                }
                line = queue.poll() ?: break
                val now = dateFmt.get()!!.format(Date())
                if (now != currentDateStamp) {
                    rolloverByDate(now)
                    break
                }
            } while (true)
            w.flush()
        } catch (e: Throwable) {
            Log.w(TAG_SELF, "drain: write failed", e)
            closeWriter()
        }
    }

    private fun drainAndFlush() {
        drain()
        runCatching { writer?.flush() }
    }

    @Synchronized
    private fun ensureWriter(): OutputStreamWriter? {
        writer?.let { return it }
        val dir = logDir ?: return null
        val now = dateFmt.get()!!.format(Date())
        currentDateStamp = now
        val file = File(dir, "${FILE_PREFIX}$now.log")
        currentFile = file
        return try {
            val os = FileOutputStream(file, /* append = */ true)
            OutputStreamWriter(os, StandardCharsets.UTF_8).also { writer = it }
        } catch (e: Throwable) {
            Log.e(TAG_SELF, "ensureWriter: open file failed: ${file.absolutePath}", e)
            null
        }
    }

    @Synchronized
    private fun closeWriter() {
        runCatching { writer?.flush() }
        runCatching { writer?.close() }
        writer = null
    }

    private fun rolloverBySize() {
        val dir = logDir ?: return
        val stamp = rolloverFmt.get()!!.format(Date())
        closeWriter()
        val f = currentFile
        if (f != null && f.exists()) {
            runCatching { f.renameTo(File(dir, "${FILE_PREFIX}${stamp}.log")) }
        }
        ensureWriter()
    }

    private fun rolloverByDate(newStamp: String) {
        closeWriter()
        currentDateStamp = newStamp
        ensureWriter()
        cleanExpired()
    }

    /** 清理过期文件，只保留最近 [MAX_RETAIN_DAYS] 天。 */
    private fun cleanExpired() {
        val dir = logDir ?: return
        val keepAfter = System.currentTimeMillis() - MAX_RETAIN_DAYS * 24L * 60 * 60 * 1000
        runCatching {
            dir.listFiles()?.forEach { f ->
                if (f.isFile && f.name.startsWith(FILE_PREFIX) && f.lastModified() < keepAfter) {
                    f.delete()
                }
            }
        }
    }
}
