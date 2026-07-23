package com.tencent.news.core.video.api.alpha

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure

/**
 * 透明视频播放器接口（平台无关）
 *
 * 各平台需实现此接口，包装平台特定的透明播放 SDK：
 * - Android: AlphaPlayer (com.tencent.ams.fusion.widget.alphaplayer)
 * - iOS: AVFoundation + CoreVideo Alpha 通道解码
 * - HarmonyOS: 平台图形渲染组件
 */
interface IQnAlphaVideoView {

    /**
     * 初始化播放器并设置播放信息
     *
     * @param playInfo 播放信息，包含视频格式、地址、缩放模式等，后续扩展只需修改 IQnAlphaVideoPlayInfo
     */
    fun setPlayInfo(playInfo: IQnAlphaVideoPlayInfo)

    /**
     * 开始播放
     */
    fun play()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 设置播放状态回调
     */
    fun setListener(listener: IQnAlphaVideoListener)

    /**
     * 释放资源
     *
     * 释放播放器占用的所有资源，包括解码器、渲染资源等。
     * 调用后播放器不可再使用，如需再次播放需重新 init。
     */
    fun release()
}


interface IQnAlphaVideoPlayInfo : IKmmKeep, IKmmPure {
    val url: String
    val formatType: AlphaVideoFormatType
    val scaleType: AlphaVideoScaleType
}