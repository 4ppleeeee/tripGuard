package com.tencent.news.core.platform

/**
 * 系统音量控制器
 *
 * 对齐 Android: [com.tencent.news.video.VideoAudioManager]
 * - 读/写 AudioManager.STREAM_MUSIC 的当前音量
 * - 通过 ContentObserver（或 VOLUME_CHANGED_ACTION 广播）监听外部按键/系统调节
 *
 * 说明：由平台侧在 setup 阶段注入 [QnPlatformLogic.systemVolumeController]。
 */
interface ISystemVolumeController {
    fun registerListener(listener: IVolumeListener)
    fun unregisterListener(listener: IVolumeListener)

    /**
     * 获取当前系统媒体音量的归一化比例（[0f, 1f]）
     *
     * 对齐 Android: `VideoAudioManager.getVolumeRate()`
     */
    fun getVolumeRate(): Float = 0f

    /**
     * 直接设置系统媒体音量到指定比例（[0f, 1f]）
     *
     * 对齐 Android: `VideoAudioManager.setVolumeRate(float)`
     */
    fun setVolumeRate(rate: Float) {}
}

fun appVolumeController() = QnPlatformLogic.systemVolumeController

fun interface IVolumeListener {
    fun onVolumeChanged(volume: Int, isUp: Boolean)
}
