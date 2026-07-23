package com.tencent.news.core.video.api


interface IQnVideoView {
    fun getDataHandler(): IQnVideoDataHandler               // 数据逻辑
    fun getUiController(): IQnVideoUiController             // Ui逻辑
    fun getVolumeController(): IQnVideoVolumeController     // 声音逻辑
    fun getVideoController(): IQnVideoController            // 操作播放器逻辑
    fun getLifeObservable(): IQnVideoLifeObservable         // 生命周期提供者
    fun getProgressManager(): IQnVideoProgressManager       // 进度逻辑
    fun getDeviceMuteManager(): IQnDeviceMuteManager        // 设备静音监听
}





