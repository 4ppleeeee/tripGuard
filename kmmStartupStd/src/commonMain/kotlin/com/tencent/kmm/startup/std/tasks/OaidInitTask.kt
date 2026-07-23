package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.std.tasks.BeaconInitTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

/**
 * 设备 OAID 初始化任务。
 *
 * 依赖 QIMEI 初始化完成后执行（QIMEI SDK 需先完成隐私策略配置）；
 * 依赖 Beacon 初始化完成后执行（Android 端需在 BeaconReport.start() 之后调
 * BeaconReport.setOAID(oaid)，让灯塔后台能从 SDK 内部填 oaid[A144]）。
 * 初始化结果写入 [OaidState]，供 SetupAndroidAppStatus.getOAID() 使用。
 */
class OaidInitTask(private val initOaid: PlatformTask<String>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(
        QimeiInitTask.TASK_ID,
        BeaconInitTask.TASK_ID,
    )

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initOaid(context) { oaid ->
            if (oaid.isNotEmpty()) {
                OaidState.update(oaid)
            }
        }
    }

    companion object {
        const val TASK_ID = "oaid"
    }
}
