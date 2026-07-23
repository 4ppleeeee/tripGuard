package com.tencent.news.core.video.api

interface IQnVideoLifeObservable {
    fun subscribe(callback: IQnVideoLife?)
    fun unSubscribe(callback: IQnVideoLife?)
}

/**
 * 播放器状态回调
 */
interface IQnVideoLife {

    fun onVideoPrepared() {}

    /**
     * 开始播放
     */
    fun onVideoStart() {}

    /**
     * 开始渲染
     */
    fun onVideoStartRender() {}

    /**
     * 视频暂停
     */
    fun onVideoPause() {}

    /**
     * 视频停止
     */
    fun onVideoStop(errWhat: Int, errCode: Int, errMsg: String?) {}

    /**
     * 视频完成
     */
    fun onVideoComplete() {}

    /**
     * 视频网络信息返回（对齐 Android: ITvkVideoLifeObserver.onNetVideoInfo）
     *
     * 端上播放器（TVK）在拉取 vinfo CGI 成功后回调，携带鉴权所需的关键字段：
     * - mediaVideoState(payState) / st：判定是否 canPlay
     * - previewDurationSec：是否支持试看及试看时长
     *
     * 由 KMM 鉴权 ViewModel 消费，驱动"该播/需付费/试看"的状态分支。
     */
    fun onNetVideoInfo(info: QnNetVideoInfo) {}

    /**
     * 试看结束（对齐 Android: ITvkVideoLifeObserver.onPermissionTimeout）
     *
     * 端上播放器在试看时长到达时回调，KMM 鉴权 ViewModel 触发 finishTryPlay。
     */
    fun onPermissionTimeout() {}
}