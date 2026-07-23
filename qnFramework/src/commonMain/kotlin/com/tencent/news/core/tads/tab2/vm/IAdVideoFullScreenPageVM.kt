package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.tads.vm.IAdFeedbackBtnVM
import com.tencent.news.core.tads.vm.IAdvertiserVM

/**
 * Compose 全屏广告页播放驱动信号。
 *
 * 宿主 overlay 生命周期 → [IAdFullScreenPlaybackVM.requestPlay] / [IAdFullScreenPlaybackVM.requestPause]
 * → SharedFlow → Compose 侧消费，驱动 QnVideoState.start/pause。
 */
enum class AdFullScreenPlaybackSignal { Play, Pause }

/**
 * 全屏广告播放进度（业务侧解耦，不依赖底层视频框架数据结构）。
 *
 * 单位与底层 [com.tencent.news.core.video.api.IQnVideoProgressListener] 对齐，均为「毫秒」。
 *
 * @property positionMs 当前播放位置（毫秒）
 * @property durationMs 视频总时长（毫秒），未知时为 0
 */
data class AdVideoPlayProgress(
    val positionMs: Long,
    val durationMs: Long,
)

/** 全屏广告播放进度回调 */
fun interface OnAdVideoPlayProgressListener {
    fun onPlayProgress(progress: AdVideoPlayProgress)
}

/**
 * 全屏广告页面 VM：聚合视频数据、播放通道、子 VM 与生命周期。
 */
interface IAdVideoFullScreenPageVM : IAdFullScreenVideoData, IAdFullScreenPlaybackVM {

    val adTitle: String
    val actionBtnVM: IAdFullScreenActionButtonVM
    val advertiser: IAdvertiserVM
    val feedbackBtn: IAdFeedbackBtnVM
    val controllerVM: IAdFullScreenControllerVM

    fun resetConfig()

    fun onBackClick()
    /** 宿主注册返回回调；null 取消监听 */
    fun setOnBackListener(block: (() -> Unit)?)

    /** 视频播放完成且开启连播时触发：请求宿主切下一个视频 */
    fun onPlayNext()
    /** 宿主注册连播回调；null 取消监听 */
    fun setOnPlayNextListener(block: (() -> Unit)?)

    /** 宿主注册播放进度回调；传 null 取消监听 */
    fun setOnPlayProgressListener(listener: OnAdVideoPlayProgressListener?)

    /**
     * 由 Compose 侧在收到底层进度更新时调用，用于将进度分发给已注册的
     * [OnAdVideoPlayProgressListener]。业务方一般不直接调用。
     */
    fun notifyPlayProgress(progress: AdVideoPlayProgress)

    fun onPageResume()
    fun onPagePause()
    fun onPageEnter()
    fun onPageExit()

}
