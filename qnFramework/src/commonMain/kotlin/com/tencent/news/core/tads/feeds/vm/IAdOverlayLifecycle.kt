package com.tencent.news.core.tads.feeds.vm

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 广告贴片（Overlay）生命周期接口
 *
 */
interface IAdOverlayLifecycle {

    val appeared: StateFlow<Boolean>             // 贴片可见性状态
    val scrollOverMid: SharedFlow<Unit>          // 卡片滚到屏幕中线
    val videoProgress: SharedFlow<Long>          // 视频播放进度更新
    val videoPlaying: StateFlow<Boolean>         // 视频播放状态（true=播放中，false=暂停/停止）
    val videoCompleteOverlay: StateFlow<Boolean>  // 完播浮层显示/隐藏（true=显示，false=隐藏）
    val visiblePercent: StateFlow<Float>          // 卡片在屏幕中的可见面积百分比 [0, 100]

    // 可见性
    fun onAppear()                               // 浮层可见（onListShow、onAttach 等）
    fun onDisappear()                            // 浮层不可见（onListHide、onDetach 等）

    // 滚动 & 视频进度
    fun onScrollOverMid()                        // 宿主投递：卡片滚到屏幕中线
    fun onVideoStart()                           // 宿主投递：视频开始/恢复播放
    fun onVideoPause()                           // 宿主投递：视频暂停播放
    fun onVideoStop()                            // 宿主投递：视频停止播放
    fun onVideoProgress(position: Long)          // 宿主投递：视频播放进度更新

    // 完播浮层
    fun onVideoCompleteOverlayChanged(showing: Boolean) // 宿主投递：完播浮层显示状态变化（true=显示，false=隐藏）

    // 可见面积
    fun onVisiblePercentChanged(percent: Float)   // 宿主投递：卡片可见面积百分比变化 [0, 100]
}