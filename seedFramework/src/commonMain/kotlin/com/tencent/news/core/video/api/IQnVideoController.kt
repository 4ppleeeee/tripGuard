package com.tencent.news.core.video.api

/**
 * 仅通知播放器做一些操作
 */
interface IQnVideoController {
    fun prepareAndStart(continuePlay: Boolean = false)

    /**
     * 下载视频，准备播放
     */
    fun prepare()

    /**
     * 播放视频
     */
    fun start()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 恢复播放，通常是播放中退后台，然后回到前台时候会开始播放；如果暂停态退后台，再回前台不会播放
     */
    fun resume()

    /**
     * 停止播放
     * @param reset 是否要reset播放数据
     */
    fun stop(reset: Boolean = false)

    /**
     * 释放播放器，调用后播放器不可继续使用
     */
    fun release()

    /**
     * 重置播放器
     */
    fun reset()

    /**
     * 跳转到指定位置
     * @param positionSeconds 指定位置，单位：秒（与 progressFlow 中的 position 单位一致）
     */
    fun seekTo(positionSeconds: Long) {}

    /**
     * 设置播放倍速
     * @param speed 播放倍速，1.0f 为正常速度
     */
    fun setSpeed(speed: Float) {}
}