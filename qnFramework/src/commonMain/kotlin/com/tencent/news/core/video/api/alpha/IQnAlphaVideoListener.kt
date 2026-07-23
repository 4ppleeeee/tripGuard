package com.tencent.news.core.video.api.alpha

/**
 * 透明视频播放回调接口（平台无关）
 *
 * 各平台实现需确保回调在主线程执行。
 * 桥接层（Bridge）负责将此回调转换为 Compose 侧的 Flow 事件。
 */
interface IQnAlphaVideoListener {

    /**
     * 视频数据加载完成，准备就绪
     */
    fun onPrepared()

    /**
     * 开始播放（首帧已渲染）
     */
    fun onStart()

    /**
     * 暂停播放
     */
    fun onPause()

    /**
     * 停止播放
     */
    fun onStop()

    /**
     * 播放完成
     */
    fun onComplete()

    /**
     * 播放出错
     *
     * @param errorCode 错误码（平台无关，各平台自行映射）
     * @param errorMessage 错误描述信息
     */
    fun onError(errorCode: Int = 0, errorMessage: String? = null)

    /**
     * 底层渲染资源已释放
     *
     * 当底层渲染上下文（GL/Metal/图形管线）被销毁时触发。
     * 收到此回调后，播放器需重新初始化才能恢复播放。
     */
    fun onRenderReleased()

    /**
     * 渲染组件就绪回调
     *
     * 平台差异：
     * - Android: GL 组件初始化完成时回调
     * - iOS: Metal/渲染管线就绪时回调
     * - HarmonyOS: XComponent onLoad 完成时回调
     */
    fun onReady() {}
}