package com.tencent.kmm.demo.library.log

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Android demo logger facade.
 */
object WsLogger {
    private var isDebug: Boolean = false
    private var logDir: File? = null

    fun setup(context: Any?, isDebug: Boolean) {
        this.isDebug = isDebug
        val ctx = context as? Context ?: run {
            Log.e("WsLogger", "setup: context is null or not Context, skipping init")
            return
        }
        logDir = File(ctx.cacheDir, "logs").also { it.mkdirs() }
        Log.i("WsLogger", "setup: Android Log facade ready, isDebug=$isDebug, pkg=${ctx.packageName}")
    }

    fun d(tag: String?, message: String?) {
        if (!isDebug) return
        Log.d(tag.orDefaultTag(), message.orEmpty())
    }

    fun i(tag: String?, message: String?) {
        Log.i(tag.orDefaultTag(), message.orEmpty())
    }

    fun w(tag: String?, message: String?) {
        Log.w(tag.orDefaultTag(), message.orEmpty())
    }

    fun e(tag: String?, message: String?) {
        Log.e(tag.orDefaultTag(), message.orEmpty())
    }

    fun e(tag: String?, message: String?, throwable: Throwable?) {
        Log.e(tag.orDefaultTag(), message.orEmpty(), throwable)
    }

    fun getLogFileDir(): String? = logDir?.absolutePath

    fun flush() = Unit

    private fun String?.orDefaultTag(): String = this?.takeIf { it.isNotBlank() } ?: "KmmDemo"
}
