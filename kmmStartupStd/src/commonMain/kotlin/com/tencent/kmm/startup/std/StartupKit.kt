package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupEngine
import com.tencent.kmm.startup.StartupListener
import com.tencent.kmm.startup.StartupResult
import com.tencent.kmm.startup.std.trace.LoggingStartupListener
import com.tencent.kmm.startup.std.tasks.BeaconInitTask
import com.tencent.kmm.startup.std.tasks.BuglyInitTask
import com.tencent.kmm.startup.std.tasks.KmkvInitTask
import com.tencent.kmm.startup.std.tasks.KuiklyAdapterInitTask
import com.tencent.kmm.startup.std.tasks.LoggerInitTask
import com.tencent.kmm.startup.std.tasks.LottieInitTask
import com.tencent.kmm.startup.std.tasks.MidasInitTask
import com.tencent.kmm.startup.std.tasks.QQLoginInitTask
import com.tencent.kmm.startup.std.tasks.OaidInitTask
import com.tencent.kmm.startup.std.tasks.QimeiInitTask
import com.tencent.kmm.startup.std.tasks.ReshubInitTask
import com.tencent.kmm.startup.std.tasks.TabExpInitTask
import com.tencent.kmm.startup.std.tasks.ToggleInitTask
import com.tencent.kmm.startup.std.tasks.TuringInitTask
import com.tencent.kmm.startup.std.tasks.UploadSdkInitTask
import com.tencent.kmm.startup.std.tasks.WXLoginInitTask
import com.tencent.kmm.startup.std.tasks.WeComShareInitTask
import com.tencent.kmm.startup.std.tasks.WeiboShareInitTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StartupKit {

    fun launch(
        context: StartupContext,
        platformTaskProvider: PlatformTaskProvider,
        listeners: List<StartupListener> = emptyList(),
    ): StartupResult {
        val engine = StartupEngine()
            .addTask(LoggerInitTask(platformTaskProvider.loggerInitTask))
            .addTask(KmkvInitTask(platformTaskProvider.kmkvInitTask))
            .addTask(QQLoginInitTask(platformTaskProvider.qqLoginInitTask))
            .addTask(WXLoginInitTask(platformTaskProvider.wxLoginInitTask))
            .addTask(WeiboShareInitTask(platformTaskProvider.weiboShareInitTask))
            .addTask(WeComShareInitTask(platformTaskProvider.weComShareInitTask))
            .addTask(KuiklyAdapterInitTask(platformTaskProvider.kuiklyAdapterInitTask))
            .addTask(QimeiInitTask(platformTaskProvider.qimeiInitTask))
            .addTask(OaidInitTask(platformTaskProvider.oaidInitTask))
            .addTask(TuringInitTask(platformTaskProvider.turingInitTask))
            .addTask(TabExpInitTask(platformTaskProvider.tabExpInitTask))
            .addTask(ToggleInitTask(platformTaskProvider.toggleInitTask))
            .addTask(UploadSdkInitTask(platformTaskProvider.uploadSdkInitTask))
            .addTask(BuglyInitTask(platformTaskProvider.buglyInitTask))
            .addTask(BeaconInitTask(platformTaskProvider.beaconInitTask))
            .addTask(MidasInitTask(platformTaskProvider.midasInitTask))
            .addTask(ReshubInitTask(platformTaskProvider.reshubInitTask))
            .addTask(LottieInitTask(platformTaskProvider.lottieInitTask))
            .apply {
                if (context.isDebug) {
                    addListener(LoggingStartupListener())
                }
                listeners.forEach { addListener(it) }
            }

        return engine.launch(context)
    }

    /**
     * 异步启动所有任务，不阻塞当前线程。
     */
    fun launchAsync(
        context: StartupContext,
        platformTaskProvider: PlatformTaskProvider,
        listeners: List<StartupListener> = emptyList(),
    ) {
        val engine = StartupEngine()
            .addTask(LoggerInitTask(platformTaskProvider.loggerInitTask))
            .addTask(KmkvInitTask(platformTaskProvider.kmkvInitTask))
            .addTask(QQLoginInitTask(platformTaskProvider.qqLoginInitTask))
            .addTask(WXLoginInitTask(platformTaskProvider.wxLoginInitTask))
            .addTask(WeiboShareInitTask(platformTaskProvider.weiboShareInitTask))
            .addTask(WeComShareInitTask(platformTaskProvider.weComShareInitTask))
            .addTask(KuiklyAdapterInitTask(platformTaskProvider.kuiklyAdapterInitTask))
            .addTask(QimeiInitTask(platformTaskProvider.qimeiInitTask))
            .addTask(OaidInitTask(platformTaskProvider.oaidInitTask))
            .addTask(TuringInitTask(platformTaskProvider.turingInitTask))
            .addTask(TabExpInitTask(platformTaskProvider.tabExpInitTask))
            .addTask(ToggleInitTask(platformTaskProvider.toggleInitTask))
            .addTask(UploadSdkInitTask(platformTaskProvider.uploadSdkInitTask))
            .addTask(BuglyInitTask(platformTaskProvider.buglyInitTask))
            .addTask(BeaconInitTask(platformTaskProvider.beaconInitTask))
            .addTask(MidasInitTask(platformTaskProvider.midasInitTask))
            .addTask(ReshubInitTask(platformTaskProvider.reshubInitTask))
            .addTask(LottieInitTask(platformTaskProvider.lottieInitTask))
            .apply {
                if (context.isDebug) {
                    addListener(LoggingStartupListener())
                }
                listeners.forEach { addListener(it) }
            }

        CoroutineScope(Dispatchers.Default).launch {
            engine.launchSuspend(context)
        }
    }
}
