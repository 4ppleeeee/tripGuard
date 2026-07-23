package com.tencent.news.core.tads.pop.vm

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.pop.IPopVM
import com.tencent.news.core.tads.model.IAdClickHotArea
import com.tencent.news.core.tads.model.IKmmAdFeedsItem

typealias AdBrandGiftAction = () -> Unit

interface IAdBrandGiftVM : IPopVM {
    val channel: String
    val oid: String
    val adItem: IKmmAdFeedsItem              // 用于检测真实曝光
    val bottomBtnUrl: String                 // 按钮url
    val isHideAdIcon: Boolean                // 是否展示右上角广告标志
    val iconText: String?                    // 右上角广告标志文案
    val clickArea: IAdClickHotArea?          // 点击区域
    val canShrinkAnimation: Boolean          // 是否需要回缩动画，信息流banner广告使用
    val onRepeatPopUp: AdBrandGiftAction        // 已经弹出品牌献礼,重复调用show时回调, 不需要宿主调用
    val onCloseClick: AdBrandGiftAction         // 点击关闭按钮
    val onClickHotArea: AdBrandGiftAction       // 点击点击热区
    val onClickBtn: AdBrandGiftAction           // 点击按钮
    val onClickOutside: AdBrandGiftAction       // 点击非响应区域
    val onShow: AdBrandGiftAction                           // 展示成功
    val onCountdownEnd: AdBrandGiftAction                   // 倒计时结束
    val onShowFailBecauseLowPriority: AdBrandGiftAction     // 由于低优先级未展示
    val onDismissBecauseLowPriority: AdBrandGiftAction      // 展示了，但是被高优先级顶掉
    val cellCenterLocation: (() -> List<Int>)?              // 收缩动画坐标, 目前只有在信息流banner广告位使用
}

interface IAdVideoBrandGiftVM : IAdBrandGiftVM {
    val videoUrl: String
    val lottieUrl: String
    var localFilePath: String?    // 宿主自行赋值，各个宿主保存的路径不同
    val onVideoStart: AdBrandGiftAction     // 视频开始播放
    val onVideoEnd: AdBrandGiftAction       // 视频结束播放
    val onDownloadFail: (code: Int?, msg: String?) -> Unit  // 预加载资源失败
}

interface IAdImageBrandGiftVM : IAdBrandGiftVM {
    val imgUrl: String
    val onDownloadFail: AdBrandGiftAction   // 预加载资源失败
}