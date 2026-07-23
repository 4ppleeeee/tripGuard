package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import android.util.Log
import com.tencent.beacon.event.open.BeaconEvent
import com.tencent.beacon.event.open.BeaconReport
import com.tencent.beacon.event.open.EventType
import com.tencent.tab.exp.sdk.export.injector.log.ITabLog
import com.tencent.tab.exp.sdk.export.listener.ITabRefreshListener
import com.tencent.tab.exp.sdk.impl.TabExpDependInjector
import com.tencent.tab.exp.sdk.impl.TabExpInitTask
import com.tencent.tab.exp.sdk.impl.TabExpSDKFactory
import com.tencent.tab.exp.sdk.impl.TabExpSDKSetting
import com.tencent.tab.tabmonitor.export.injector.report.ITabMetricsReport
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.TabExpInitConfig
import com.tencent.kmm.startup.std.tasks.QimeiState
import com.tencent.kmm.startup.std.tasks.TabExpInitResult

private const val TAG = "TabExpInitTask"
private const val PROFILE_KEY_APP_VERSION = "appVersion"

object AndroidTabExpRuntime {
    @Volatile
    private var initializedAppId: String = ""

    private val startListener = ITabRefreshListener {
        Log.i(TAG, "TAB SDK start finished.")
    }

    fun init(
        app: Application,
        config: TabExpInitConfig,
        qimei: String,
        qimei36: String,
        isDebug: Boolean,
    ) {
        runCatching {
            TabExpInitTask.init(app)

            val extraParams = hashMapOf<String, String>()
            if (qimei36.isNotBlank()) {
                extraParams[TabExpSDKSetting.QIMEI_36_KEY] = qimei36
            }

            val setting = TabExpSDKSetting.Builder()
                .appId(config.appId)
                .appKey(config.appKey)
                .guid(qimei)
                .sceneId(config.sceneId)
                .profiles(hashMapOf(PROFILE_KEY_APP_VERSION to config.appVersion))
                .extraParams(extraParams)
                .build()

            Log.i(TAG, "init tab sdk, appId=${config.appId}, sceneId=${config.sceneId}")
            val sdk = TabExpSDKFactory.singleton()
                .create(setting, createTabExpDependInjector(isDebug))
            sdk?.start(startListener)
            initializedAppId = config.appId
        }.onFailure { error ->
            Log.e(TAG, "init TAB SDK failed", error)
        }
    }

    fun getTabExpInt(key: String, defaultValue: Int): Int {
        val appId = initializedAppId
        if (appId.isBlank()) {
            return defaultValue
        }
        return runCatching {
            val info = TabExpSDKFactory.singleton()
                .get(appId, "")
                ?.tabExperiment
                ?.getExpInfoByName(key, true)
                ?: return defaultValue

            val rawValue = info.expParams[key]
                ?: info.expParams["value"]
                ?: info.expParams.values.firstOrNull()
                ?: return defaultValue
            rawValue.toIntOrNull() ?: defaultValue
        }.getOrElse {
            Log.e(TAG, "getTabExpInt failed, key=$key", it)
            defaultValue
        }
    }

    private fun createTabExpDependInjector(isDebug: Boolean): TabExpDependInjector {
        return TabExpDependInjector.Builder()
            .reportImpl { reportInfo ->
                val eventType = if (reportInfo.isRealTime) EventType.REALTIME else EventType.NORMAL
                val eventResult = BeaconReport.getInstance().report(
                    BeaconEvent.builder()
                        .withAppKey(reportInfo.beaconAppKey)
                        .withCode(reportInfo.eventName)
                        .withParams(reportInfo.eventParams)
                        .withType(eventType)
                        .withIsSucceed(reportInfo.isSuccess)
                        .build()
                )
                eventResult?.isSuccess == true
            }
            .metricsReportImpl { metricsReportParams ->
                val metricsAppKey = if (isDebug) {
                    ITabMetricsReport.MONITOR_DEBUG_APP_KEY
                } else {
                    ITabMetricsReport.MONITOR_RELEASE_APP_KEY
                }
                val eventResult = BeaconReport.getInstance().report(
                    BeaconEvent.builder()
                        .withCode(ITabMetricsReport.MONITOR_EVENT_CODE)
                        .withParams(metricsReportParams)
                        .withAppKey(metricsAppKey)
                        .build()
                )
                eventResult?.isSuccess == true
            }
            .logImpl(
                object : ITabLog {
                    override fun v(tag: String?, msg: String?) {
                        Log.v(tag, msg.orEmpty())
                    }

                    override fun d(tag: String?, msg: String?) {
                        Log.d(tag, msg.orEmpty())
                    }

                    override fun i(tag: String?, msg: String?) {
                        Log.i(tag, msg.orEmpty())
                    }

                    override fun w(tag: String?, msg: String?) {
                        Log.w(tag, msg.orEmpty())
                    }

                    override fun e(tag: String?, msg: String?) {
                        Log.e(tag, msg.orEmpty())
                    }
                }
            )
            .build()
    }
}

/**
 * Android TAB AB 实验 SDK 初始化任务
 */
fun initTabExp(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<TabExpInitResult>
) {
    val config = context.configOrNull<TabExpInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    AndroidTabExpRuntime.init(
        app = app,
        config = config,
        qimei = QimeiState.qimei,
        qimei36 = QimeiState.qimei36,
        isDebug = context.isDebug,
    )
    callback(TabExpInitResult(config.appId))
}
