package com.tencent.news.core.tads.pop.vm

import com.tencent.news.core.pop.IPopVM
import com.tencent.news.core.tads.model.IKmmAdFeedsItem

/**
 * 高光挂件 VM 接口
 * - 高光挂件(HighlightPendant)是上层概念，奥运挂件(OlympicPendant)是其特例
 * - View 层只做纯 UI 渲染，所有业务差异通过 VM 的 onXxx 方法分发
 * - bottomMargin 由 View 层根据常量 dp 值自行做像素转换和导航栏避让
 */
interface IAdHighlightPendantVM : IPopVM {
    val videoUrl: String
    val iconUrl: String
    val foldIcon: String
    val totalDuration: Long
    val scene: Int
    val clickAreaVM: IAdHighlightPendantClickAreaVM
    val isVideoPlaying: Boolean
    val adItem: IKmmAdFeedsItem     // 只可用于曝光检测

    fun onShow()
    fun onDismiss()
    fun onVideoExposure(viewType: Int)
    fun onVideoStart()
    fun onVideoPause()
    fun onVideoUpdate(pos: Long)
    fun onVideoStop()
    fun onVideoComplete()
    fun onVideoError(errorCode: Int)
    fun onClick(areaType: HighlightPendantClickArea)
    fun onTimeOver()
}

enum class HighlightPendantClickArea {
    MAIN_ANIMATION,
    CLOSE_BTN,
    BIG_PENDANT,
    SMALL_PENDANT
}
