package com.tencent.news.core.tads.feeds.vm

import com.tencent.news.core.list.vm.IClickVM
import com.tencent.news.core.list.vm.IImageBtnVM
import com.tencent.news.core.platform.api.GyroscopeData
import com.tencent.news.core.tads.constants.AdBrokenInfoType
import com.tencent.news.core.tads.constants.AdDisplayCode
import com.tencent.news.core.tads.constants.AdGdtClickActType
import com.tencent.news.core.tads.model.AdDisplayInteractState
import com.tencent.news.core.tads.model.TwistDirection
import com.tencent.news.core.tads.vm.IAdExportVM
import com.tencent.news.core.tads.vm.IAdFeedbackBtnVM
import com.tencent.news.core.tads.vm.IAdvertiserVM
import kotlinx.coroutines.flow.StateFlow


interface IAdFinancialBannerVM {
    val pic: IImageBtnVM
}

interface IAdMarketingHeaderCardVM {
    val defaultIndex: Int // 全局会记录上次选中位置
    val curStatus: IAdMarketingCardStatus?

    val coverList: List<IAdMarketingCoverVM>

    fun updateIndex(index: Int)
}

interface IAdMarketingSquareCardVM {
    val cover: IAdMarketingCoverVM
}


interface IAdMarketingCoverVM : IAdReportVM {
    val uniqueKey: String
    val advertiser: IAdvertiserVM
    val feedbackBtn: IAdFeedbackBtnVM
    val title: String
    val bgUrl: String
    val vid: String
    val paletteColor: String
}

interface IAdMarketingCardStatus {
    val paletteColor: String
}

/**
 * ==========================================================================================
 * 画廊卡片VM
 */
interface IAdGalleryCardVM {
    val title: String
    val feedbackBtn: IAdFeedbackBtnVM
    val coverList: List<IAdGalleryCoverVM>

    fun onUserLoop()    // 用户手动横划卡片

    fun onSlideJump()   // 尾部拖拽跳转
}

interface IAdGalleryOlympicCardVM : IAdGalleryCardVM, IAdReportVM {
    val brokenVideoUrl: String?             // 破窗视频Url
    val medalUrl: String?                   // 勋章URL
    val lbFlowerUrl: String?                // 左下角烟花url
    val trFlowerUrl: String?                // 右上角烟花url

    fun isOlympicFeedOnlyLargePicData(): Boolean // 是否为奥运信息流纯图模式

    fun brokenClick()                       // 破框点击
}

interface IAdVideoVM : IAdExportVM {
    val videoId: String
    val videoUrl: String
    val videoCoverUrl: String
    val videoDuration: Long
    val videoDurationText: String
    val videoWidth: Int
    val videoHeight: Int
    val sdtFrom: String     // 视频带宽计费渠道
}

// 画廊-Cover
interface IAdGalleryCoverVM : IAdReportVM {
    val subType: Int        // 父订单的subType类型，决定cell是否图和视频混排

    val coverUrl: String    // 封面图
    val title: String       // 压图的标题

    val video: IAdVideoVM?  // 如果是视频，会有这个

    override fun onExpose()
    override fun getClickVM(actType: AdGdtClickActType): IClickVM?

    fun tryShowBrokenWidget(
        type: AdBrokenInfoType,
        callback: (canShow: Boolean, brokenImageUrl: String) -> Unit
    )
}

/**
 * ==========================================================================================
 * 组图卡片VM
 */
interface IAdFoldCardVM : IAdReportVM {
    val videoVms: List<IAdFoldCardVideoVM> // 视频子组图集合
    val imgVms: List<IAdFoldCardImgVM>    // 图片子组图集合

    val isVideoComment: Boolean             // 视频底层评论页

    val isStreamCardVideo: Boolean           // 视频组卡
}

/**
 * 子组图VM
 */
interface IAdFoldCardItemVM : IAdReportVM {
    val coverUrl: String        // 封面图
    val title: String           // 标题
    val index: Int              // 角标
}

/**
 * 子组图-视频VM
 */
interface IAdFoldCardVideoVM : IAdFoldCardItemVM {
    val advertiserName: String   // 广告主名称

    val abstract: String        // 描述
    val showPlayBtn: Boolean    // 展示播放按钮

    val video: IAdVideoVM?  // 如果是视频，会有这个

    fun onUserLoop()            // 用户手动横划卡片
    fun onAutoLoop()            // 自动滑动卡片

    fun onCardExposure()        // card滑动曝光
}

/**
 * 子组图-图片VM
 */
interface IAdFoldCardImgVM : IAdFoldCardItemVM {
    val clickUrl: String        // 点击Url
}


/**
 * ==========================================================================================
 * MDPA卡片VM
 */
interface IAdMDPACardVM {
    val title: String                      // 标题
    val itemVM: List<IAdMDPAItemVM>        // 子卡片VM
    val hasPriceInFirstThreeItems: Boolean // 超过三个子卡片有价格
}

/**
 * 子卡片VM
 */
interface IAdMDPAItemVM : IAdReportVM {
    val coverUrl: String        // 封面图 URl
    val title: String           // 标题
    val price: String           // 价格
    val originalPrice: String   // 原价
}

/**
 * ==========================================================================================
 * 微广多图卡片VM
 */
interface IAdMultiImageCardVM : IAdReportVM {
    val backgroundUrl: String               // 背景图片，用作高斯模糊
    val imageListVM: List<String>           // 图片VM
    val isPageVisible: StateFlow<Boolean>   // 页面可见性（控制轮播暂停/继续）

    fun onClick()
}

/**
 * ==========================================================================================
 * 展示互动组件 VM
 */

// 展示互动组件基础 ViewModel 接口
interface IBaseAdDisplayInteractVM : IAdReportVM {
    val displayCode: AdDisplayCode               // 互动类型
    val interactState: StateFlow<AdDisplayInteractState> // 互动状态
    val isVisible: StateFlow<Boolean>            // 组件可见性

    suspend fun registerScope(overlayLifecycle: IAdOverlayLifecycle) // 注册协程作用域，监听贴片生命周期
    fun onGyroscopeData(data: GyroscopeData)     // 陀螺仪数据回调
    fun onInteractTriggered()                    // 互动成功触发
    fun showFullAnimation()                      // 触发全屏引导动画
    fun resetInteract()                          // 重置互动状态
    fun onDestroy()                              // Page 销毁，释放资源
}

// 扭动互动 VM 接口
interface IAdTwistInteractVM : IBaseAdDisplayInteractVM {
    val twistProgress: StateFlow<Float>           // 扭动进度 [0, 1]
    val twistDirection: StateFlow<TwistDirection> // 扭动方向
    val animationOffset: StateFlow<Float>        // 动画偏移量
    val showGuideAnimation: StateFlow<Boolean>   // 是否显示全屏引导
    val hasEverTwisted: StateFlow<Boolean>       // 是否曾经触发过摇动（用于控制循环动画一次性播放）
    val interactTitle: StateFlow<String>         // 引导文案（正向扭动 / 回正提示）
    val shortTitle: String                       // 短标题（时间线底部展示）
    val mobileLottie: String                     // 手机 progress 动画（小尺寸）
    val mobileLoopLottie: String                 // 手机循环摇摆动画（小尺寸）
    val progressLottie: String                   // 进度箭头动画（小尺寸）
    val progressLoopLottie: String               // 进度箭头循环动画（小尺寸）
    val fullMobileLottie: String                 // 手机 progress 动画（全屏）
    val fullMobileLoopLottie: String             // 手机循环摇摆动画（全屏）
    val fullProgressLottie: String               // 进度箭头动画（全屏）
    val fullProgressLoopLottie: String           // 进度箭头循环动画（全屏）
}

// 时间线扭动
interface IAdTimelineInteractVM : IAdTwistInteractVM
