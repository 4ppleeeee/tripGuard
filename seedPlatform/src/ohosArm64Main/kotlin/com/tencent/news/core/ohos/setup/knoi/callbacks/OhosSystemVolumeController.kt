package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.platform.ISystemVolumeController
import com.tencent.news.core.platform.IVolumeListener
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.qnLogcat
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosSystemVolumeController = JSValue

/**
 * 注入鸿蒙端 [ISystemVolumeController] 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于 @kit.AudioKit 的
 * AudioVolumeManager.on('volumeChange', ...) 真实实现桥接到 KMP 层：
 *  - ArkTS 侧仅负责系统音量变化事件的订阅与分发（startListening / stopListening）；
 *  - Kotlin 侧维护多 listener、计算 isUp（比较前后音量差）。
 *
 * ArkTS 侧通过 Kommon.setup 调用 getHarmonyStartupProvider().setupSystemVolumeController(new
 * OhosSystemVolumeControllerCallback()) 注入实现。
 */
fun setupOhosSystemVolumeController(volumeController: IOhosSystemVolumeController) {
    QnPlatformLogic.systemVolumeController =
        OhosSystemVolumeControllerProvider(volumeController.asOhosSystemVolumeController())
}

/**
 * Kotlin 侧的 [ISystemVolumeController] 实现，负责：
 *  1. 维护业务 listener 集合，支持多订阅者；
 *  2. 首个 listener 注册时让 ArkTS 侧真正开始监听音量变化；
 *  3. 最后一个 listener 取消时通知 ArkTS 侧停止监听；
 *  4. 根据 ArkTS 回传的音量值，比较前值以判定 isUp（音量升/降）。
 */
private class OhosSystemVolumeControllerProvider(
    private val native: OhosSystemVolumeController,
) : ISystemVolumeController {

    private companion object {
        const val TAG = "OhosSystemVolumeController"
    }

    private val listeners = mutableSetOf<IVolumeListener>()
    private var isNativeRegistered = false
    private var lastVolume: Int = -1

    override fun registerListener(listener: IVolumeListener) {
        if (!listeners.add(listener)) return
        if (isNativeRegistered) return
        runCatching {
            native.startListening { volume ->
                onNativeVolumeChanged(volume)
            }
            isNativeRegistered = true
        }.onFailure {
            qnLogcat()?.logE(TAG, "startListening failed", it)
            listeners.remove(listener)
        }
    }

    override fun unregisterListener(listener: IVolumeListener) {
        if (!listeners.remove(listener)) return
        if (listeners.isEmpty()) {
            stopNativeListening()
        }
    }

    private fun stopNativeListening() {
        if (!isNativeRegistered) return
        isNativeRegistered = false
        lastVolume = -1
        runCatching { native.stopListening() }
            .onFailure { qnLogcat()?.logE(TAG, "stopListening failed", it) }
    }

    private fun onNativeVolumeChanged(volume: Double) {
        val newVolume = volume.toInt()
        val isUp = if (lastVolume < 0) true else newVolume >= lastVolume
        lastVolume = newVolume
        // 快照避免回调中 unregister 造成 ConcurrentModificationException
        listeners.toList().forEach { listener ->
            runCatching { listener.onVolumeChanged(newVolume, isUp) }
                .onFailure { qnLogcat()?.logE(TAG, "onVolumeChanged callback error", it) }
        }
    }
}

/**
 * ArkTS 侧系统音量能力实现接口。
 *
 * knoi 编译时会自动生成 ArkTS 侧的接口定义，ArkTS 侧 OhosSystemVolumeControllerCallback
 * 实现该接口并通过 getHarmonyStartupProvider().setupSystemVolumeController 注入。
 *
 * 设计要点：
 *  - 仅提供【启停 + 音量回调】，Kotlin 侧负责 multi-listener 分发；
 *  - 回调只传当前音量绝对值（0-15 范围的媒体音量），isUp 由 Kotlin 侧对比前值计算。
 */
@KNCallback
interface OhosSystemVolumeController {

    /**
     * 开始监听系统媒体音量变化。
     * ArkTS 侧基于 audio.getAudioManager().getVolumeManager()
     *     .on('volumeChange', (event) => ...) 实现。
     *
     * @param onVolumeChanged 音量变化回调，参数为当前音量值（Double，整数值）
     */
    fun startListening(onVolumeChanged: (volume: Double) -> Unit)

    /** 停止监听，对应 ArkTS 侧 audioVolumeManager.off('volumeChange') */
    fun stopListening()
}
