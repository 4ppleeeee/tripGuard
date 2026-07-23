package com.tencent.kmm.startup.std.tasks

import android.app.Application
import android.util.Log
import android.view.View
import com.tencent.news.core.platform.api.getShiplyConfig
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.lottie.config.InjectionConfig
import com.tencent.news.lottie.interfaces.DayNightMode
import com.tencent.news.lottie.interfaces.IInjectConfig
import com.tencent.news.lottie.interfaces.ILottieLogger
import com.tencent.news.lottie.interfaces.ILottieNetworkFetcher
import com.tencent.news.lottie.interfaces.ILottieReporter
import com.tencent.news.lottie.interfaces.INetworkMonitor
import com.tencent.news.lottie.task.LottieResourceTaskManager
import com.tencent.news.lottie.utils.md5
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Android Lottie 初始化任务
 *
 * 从 androidApp 迁移，负责初始化 Lottie 动画库的配置
 */
fun initLottie(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<Unit>
) {
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    val logger = LottieLogger()
    val fetcher = LottieNetworkFetcher()
    val filePath = File(app.filesDir, "lottie")
    val reporter = object : ILottieReporter {
        override fun report(key: String, param: Map<String, String>) = Unit
    }
    val monitor = object : INetworkMonitor {
        override fun contains(key: String): Boolean = false
        override fun register(key: String, onAvailable: () -> Unit) = Unit
        override fun unregister(key: String) = Unit
    }

    val packageInterface: IInjectConfig = object : IInjectConfig {
        override val isDebugMode: Boolean = context.isDebug

        override fun application(): android.content.Context = app

        override fun dayNightMode(view: View): DayNightMode = DayNightMode.SYSTEM_DEFAULT

        override fun densityScale(): Float = 1F

        override fun fetcher(): ILottieNetworkFetcher = fetcher

        override fun forceSoftwareRenderMode(): Boolean = false

        override fun isNetAvailable(): Boolean = true

        override fun logger(): ILottieLogger = logger

        override fun lottieFilePath(): File = filePath

        override fun maxCacheFileCount(): Int = 30

        override fun maxRetryCount(): Int = 1

        override fun networkMonitor(): INetworkMonitor = monitor

        override fun remoteSwitch(key: String, default: Boolean): Boolean = getShiplySwitch(key, default)

        override fun remoteValue(key: String, default: String): String = getShiplyConfig(key, default)

        override fun reporter(): ILottieReporter = reporter

        override fun showDebugOverlay(): Boolean = false

        override fun showMoreLottieLogInDebugMode(): Boolean = true
    }

    InjectionConfig.config = packageInterface
    LottieResourceTaskManager.initManager()

    callback(Unit)
}

/**
 * Lottie 网络请求
 * 使用 ktor 替代 okhttp
 */
internal class LottieNetworkFetcher : ILottieNetworkFetcher {
    override suspend fun fetch(url: String): String {
        var downloadedFilePath = ""
        kotlin.runCatching {
            val client = HttpClient()
            val response = client.get(url)
            val dir = InjectionConfig.config.lottieFilePath()
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val downloadedFile = File(dir, url.md5())
            response.bodyAsChannel().toInputStream().use { source ->
                FileOutputStream(downloadedFile).use { fileOut ->
                    source.copyTo(fileOut)
                }
            }
            InjectionConfig.config.logger()
                .w("downloadFile", "File downloaded and saved as ${downloadedFile.absolutePath}")
            downloadedFilePath = downloadedFile.absolutePath
            client.close()
        }.getOrElse {
            InjectionConfig.config.logger().e("LottieNetworkFetcher", it.message ?: "")
        }

        return downloadedFilePath
    }

    override fun onError(msg: String) {
        InjectionConfig.config.logger().e("NetFetcher", msg)
    }
}

/**
 * Lottie 日志实现
 */
internal class LottieLogger : ILottieLogger {
    override fun debug(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable) {
        Log.e(tag, msg, tr)
    }

    override fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }
}
