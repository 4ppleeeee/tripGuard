package com.tencent.news.core.live.vm

/**
 * 直播分享卡片 ViewModel 接口
 * 用于处理分享卡片的生命周期和回调
 */
interface ILiveShareCardVM {
    /**
     * 页面关闭时调用
     */
    fun onClose()
    
    /**
     * 页面关闭后的回调
     * iOS 端可以通过 setOnAfterClose 设置此回调来清理资源
     */
    var onAfterClose: (() -> Unit)?
}
