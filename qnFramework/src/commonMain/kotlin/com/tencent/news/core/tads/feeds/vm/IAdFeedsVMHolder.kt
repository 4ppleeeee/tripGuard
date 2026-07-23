package com.tencent.news.core.tads.feeds.vm

import com.tencent.news.core.extension.IVMDoc
import com.tencent.news.core.tads.tab2.vm.IAdClickInterceptorVM
import com.tencent.news.core.tads.vm.VMArrayHolder
import com.tencent.news.core.tads.vm.VMHolder


// todo【架构说明】新增vm应遵循：(doc/【规范】模块化架构.md)
interface IAdFeedsVMHolder : IVMDoc {

    // 贴片生命周期管理（统一派发可见/不可见事件）
    val overlayLifecycle: IAdOverlayLifecycle

    // 金融banner
    val financialBanner: VMArrayHolder<IAdFinancialBannerVM>

    // 营销频道置顶大卡
    val marketingHeaderCard: VMHolder<IAdMarketingHeaderCardVM>

    // 营销频道方块大卡
    val marketingSquareCard: VMHolder<IAdMarketingSquareCardVM>

    // 普通画廊
    val galleryCard: VMHolder<IAdGalleryCardVM>

    // 奥运画廊
    val galleryOlympicCard: VMHolder<IAdGalleryOlympicCardVM>

    // 组图
    val foldCard: VMHolder<IAdFoldCardVM>

    // mdpa
    val mdpaCard: VMHolder<IAdMDPACardVM>

    // 微广多图
    val multiImageCard: VMHolder<IAdMultiImageCardVM>

    // 扭动互动
    val twistInteract: VMHolder<IAdTwistInteractVM>

    // 时间线挂件
    val timelineInteract: VMHolder<IAdTimelineInteractVM>

    // 时间线主框架
    val timelineMainFrame: VMHolder<IAdTimelineMainFrameVM>

    // 超级蒙层前置蒙层
    val maskViewCard: VMHolder<IAdMaskViewCardVM>

    // 点击拦截器，用于hook点击行为
    val clickInterceptorVM: VMHolder<IAdClickInterceptorVM>    // 点击拦截器
}
