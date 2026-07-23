package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import android.util.Log
import com.tencent.beacon.event.open.BeaconReport
import com.tencent.qmsp.oaid2.VendorManager
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext

private const val TAG = "OaidInitTask"

/**
 * Android 设备 OAID 初始化任务。
 *
 * 通过 VendorManager（qmsp-oaid2 SDK）获取设备 OAID。
 * 获取结果写入 [OaidState]，供 AndroidAppStatusProvider.getOAID() 使用。
 */
fun initOaid(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<String>
) {
    try {
        val app = context.nativeContext as? Application
            ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")
        val vendorManager = VendorManager()
        vendorManager.getVendorInfo(app) { _, _, oaid ->
            Log.i(TAG, "initOaid: oaid=${oaid.isNotEmpty()}")
            // 同步注入到 Beacon SDK，让灯塔后台 oaid[A144] 字段能由 SDK 内部填充。
            // BeaconReport 已在更早的 BeaconInitTask 完成 start，此处可直接 setOAID。
            if (oaid.isNotEmpty()) {
                runCatching { BeaconReport.getInstance().setOAID(oaid) }
                    .onFailure { Log.i(TAG, "setOAID 失败", it) }
            }
            callback(oaid.orEmpty())
        }
    } catch (e: Throwable) {
        Log.i(TAG, "initOaid 异常", e)
        callback("")
    }
}
