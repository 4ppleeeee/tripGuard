package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext

/**
 * Harmony MMKV initialization task.
 *
 * 鸿蒙端 Storage 注入与 Android/iOS 保持一致，统一走 commonMain 的 `setupKmkvStorage()`
 * （基于 mmkvKotlin 封装 → mmkv_c_* C 接口）。
 *
 * 但与 Android/iOS 不同的是：鸿蒙端 kuikly 工作线程可能在 StartupKit.launchAsync 执行
 * 这个异步任务之前就访问 QnPlatformLogic.appStorage，因此真正的 `setupKmkvStorage()`
 * 调用已经提前到 HarmonyStartupProvider.onAppStartup() 的同步阶段完成，
 * 本任务仅作为启动任务图中的占位，保持与 Android/iOS 的任务依赖关系一致。
 *
 * MMKV 根目录的 initialize 由 ArkTS 侧（见 Kommon.setup()）在 onAppStartup() 之前完成，
 * 对应 Android 的 `MMKV.initialize(app)` 与 iOS 的 `MMKV.initialize(rootDir: nil)`。
 */
internal fun initKmkv(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<Unit>
) {
    // Storage 已在 onAppStartup() 中通过 setupKmkvStorage() 同步完成注入
    callback(Unit)
}
