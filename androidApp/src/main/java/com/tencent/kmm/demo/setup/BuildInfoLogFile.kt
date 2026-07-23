package com.tencent.kmm.demo.setup

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.tencent.kmm.demo.BuildConfig
import com.tencent.kmm.demo.KRApplication
import com.tencent.kmm.demo.library.log.WsLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 分享 / 上传日志打包时实时生成的构建与设备信息文件。
 *
 * zip 内部文件名固定为 `build`，由 [buildOrganizedLogZip] 放到根目录。
 */
internal object BuildInfoLogFile {
    private const val TAG = "BuildInfoLogFile"
    private const val FILE_NAME = "build"

    fun write(context: Context = KRApplication.application): File? {
        val target = resolveFile(context)
        return runCatching {
            val parent = target.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            target.writeText(buildContent(context), Charsets.UTF_8)
            Log.i(TAG, "BuildInfo日志已写入: ${target.absolutePath}, size=${target.length()}")
            target
        }.onFailure {
            Log.e(TAG, "BuildInfo日志写入失败: ${target.absolutePath}", it)
        }.getOrNull()
    }

    fun copyToStaging(buildInfoFile: File?, stagingDir: File) {
        if (buildInfoFile == null || !buildInfoFile.exists() || !buildInfoFile.isFile || buildInfoFile.length() <= 0) {
            return
        }
        if (!stagingDir.exists() && !stagingDir.mkdirs()) {
            Log.w(TAG, "staging目录创建失败: ${stagingDir.absolutePath}")
            return
        }
        runCatching {
            buildInfoFile.copyTo(File(stagingDir, FILE_NAME), overwrite = true)
        }.onFailure {
            Log.w(TAG, "复制BuildInfo日志失败: ${buildInfoFile.absolutePath}", it)
        }
    }

    private fun resolveFile(context: Context): File {
        val logDirPath = WsLogger.getLogFileDir()
        if (!logDirPath.isNullOrEmpty()) {
            val logDir = File(logDirPath)
            val parentDir = logDir.parentFile ?: logDir
            return File(parentDir, FILE_NAME)
        }
        return File(File(context.filesDir, "logs_parent"), FILE_NAME)
    }

    private fun buildContent(context: Context): String {
        val now = System.currentTimeMillis()
        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(Date(now))
        val displayMetrics = context.resources.displayMetrics
        return buildString {
            appendLine("generatedAt=$formattedTime")
            appendLine("generatedAtMillis=$now")

            appendLine()
            appendLine("[app]")
            appendLine("packageName=${context.packageName}")
            appendLine("versionName=${BuildConfig.VERSION_NAME}")
            appendLine("versionCode=${BuildConfig.VERSION_CODE}")
            appendLine("buildType=${BuildConfig.BUILD_TYPE}")
            appendLine("debug=${BuildConfig.DEBUG}")

            appendLine()
            appendLine("[ci]")
            appendLine("ciBuildId=${BuildInfo.buildId}")
            appendLine("ciBuildNum=${BuildInfo.buildNum}")
            appendLine("ciPipelineName=${BuildInfo.pipelineName}")
            appendLine("ciBranch=${BuildInfo.branch}")
            appendLine("ciCommit=${BuildInfo.commit}")
            appendLine("ciBuildTime=${BuildInfo.buildTime}")

            appendLine()
            appendLine("[device]")
            appendLine("manufacturer=${Build.MANUFACTURER.orEmpty()}")
            appendLine("brand=${Build.BRAND.orEmpty()}")
            appendLine("model=${Build.MODEL.orEmpty()}")
            appendLine("device=${Build.DEVICE.orEmpty()}")
            appendLine("product=${Build.PRODUCT.orEmpty()}")
            appendLine("hardware=${Build.HARDWARE.orEmpty()}")
            appendLine("board=${Build.BOARD.orEmpty()}")
            appendLine("androidRelease=${Build.VERSION.RELEASE.orEmpty()}")
            appendLine("sdkInt=${Build.VERSION.SDK_INT}")
            appendLine("fingerprint=${Build.FINGERPRINT.orEmpty()}")
            appendLine("supportedAbis=${Build.SUPPORTED_ABIS.joinToString(",")}")

            appendLine()
            appendLine("[screen]")
            appendLine("widthPixels=${displayMetrics.widthPixels}")
            appendLine("heightPixels=${displayMetrics.heightPixels}")
            appendLine("density=${displayMetrics.density}")
            appendLine("densityDpi=${displayMetrics.densityDpi}")

            appendLine()
            appendLine("[runtime]")
            appendLine("locale=${Locale.getDefault()}")
            appendLine("timeZone=${TimeZone.getDefault().id}")
            appendLine("availableProcessors=${Runtime.getRuntime().availableProcessors()}")
            appendLine("totalStorageMb=${storageMb(total = true)}")
            appendLine("freeStorageMb=${storageMb(total = false)}")
            appendBatteryInfo(context)
        }
    }

    private fun StringBuilder.appendBatteryInfo(context: Context) {
        val batteryInfo = runCatching {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percent = if (level >= 0 && scale > 0) (level * 100) / scale else -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            percent to charging
        }.getOrDefault(-1 to false)
        appendLine("batteryPercent=${if (batteryInfo.first >= 0) batteryInfo.first.toString() else ""}")
        appendLine("batteryCharging=${batteryInfo.second}")
    }

    private fun storageMb(total: Boolean): String {
        return runCatching {
            val stat = StatFs(Environment.getDataDirectory().absolutePath)
            val bytes = if (total) stat.totalBytes else stat.availableBytes
            (bytes / (1024L * 1024L)).toString()
        }.getOrDefault("")
    }
}
