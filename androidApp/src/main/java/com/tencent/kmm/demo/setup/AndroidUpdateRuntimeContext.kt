package com.tencent.kmm.demo.setup

import android.content.Context
import android.os.Build
import com.tencent.kmm.demo.BuildConfig
import com.tencent.kmm.demo.core.update.platform.IUpdateRuntimeContext
import com.tencent.kmm.demo.core.update.platform.IUpdateRuntimeContextHolder
import com.tencent.kmm.startup.std.tasks.QimeiState

/**
 * androidApp 端的 [IUpdateRuntimeContext] 实现：把 BuildConfig / 渠道 / 账号 / Qimei 等
 * 信息注入到 wsUpdate 模块。
 *
 * 调用时机：[com.tencent.kmm.demo.KRApplication.onCreate] 后端，要求账号 / Qimei 已就绪。
 */
internal class AndroidUpdateRuntimeContext(
    private val context: Context,
    private val appStartTimeMs: Long,
) : IUpdateRuntimeContext {

    override fun isGooglePlayChannel(): Boolean = false

    override fun isAlphaBuild(): Boolean =
        BuildConfig.BUILD_TYPE.equals("alpha", ignoreCase = true)

    override fun isCpu32(): Boolean = runCatching {
        Build.SUPPORTED_64_BIT_ABIS.isEmpty()
    }.getOrDefault(false)

    override fun isInForbiddenScene(): Boolean {
        // 简化实现：当前未对接 ActivityLifeCycleMonitor，先返回 false
        // 后续如需精确判断（登录页/卸载反馈页/后台），可在 androidApp 层维护 Activity 栈再返回
        return false
    }

    override fun packageFirstInstallTime(): Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
    }.getOrDefault(0L)

    override fun packageLastUpdateTime(): Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
    }.getOrDefault(0L)

    override fun appStartTime(): Long = appStartTimeMs

    override fun getQua(): String = "V2_AND_WEISHI_${BuildConfig.VERSION_NAME}"

    override fun getQimei36(): String = QimeiState.qimei36

    override fun getCurrentUserId(): String = ""

    override fun getVersionCode(): Int = BuildConfig.VERSION_CODE

    override fun getGrayUpdateAppId(): String =
        if (isAlphaBuild()) "microvision-android-alpha" else "c91a33d0e8"

    companion object {
        /**
         * 注入入口（KRApplication.onCreate 中调用）。
         */
        fun inject(context: Context, appStartTimeMs: Long) {
            IUpdateRuntimeContextHolder.inject(
                AndroidUpdateRuntimeContext(context, appStartTimeMs)
            )
        }
    }
}
