package com.tencent.news.core.video.api.alpha
/**
 * 播放控制接口（Compose 侧）
 *
 * 由 QnAlphaVideoState 通过 delegate by 暴露给业务侧。
 * 每个方法触发对应的 action 闭包，闭包由 NativeInvoker 注入。
 */
interface IQnAlphaVideoController {
    fun play()
    fun pause()
    fun release()
}