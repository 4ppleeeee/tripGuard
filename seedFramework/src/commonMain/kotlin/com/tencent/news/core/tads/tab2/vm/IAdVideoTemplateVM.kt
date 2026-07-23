package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.isValidStrColor
import com.tencent.news.core.list.vm.ClickAction
import com.tencent.news.core.list.vm.IClickVM
import com.tencent.news.core.tads.feeds.vm.IAdReportVM
import com.tencent.news.core.tads.model.IAdWeChatStoreInfo
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.tads.tab2.config.AdVideoCardConfig
import com.tencent.news.core.tads.tab2.config.AdVideoTemplateConfig
import com.tencent.news.core.tads.vm.IAdActionBtnVM
import com.tencent.news.core.tads.vm.IAdDebugMsgVM
import com.tencent.news.core.tads.vm.IAdExportVM
import com.tencent.news.core.tads.vm.IAdFakeVoteVM
import com.tencent.news.core.tads.vm.IAdFeedbackBtnVM
import com.tencent.news.core.tads.vm.IAdStoreIconVM
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IAdVideoCardVM {
    val templateConfig: MutableStateFlow<AdVideoTemplateConfig>

    val visibleState: MutableStateFlow<Boolean>     // 控制显隐
    val animateState: MutableStateFlow<Boolean>     // 控制动画启动
    val resetSignal: StateFlow<Int>                 // reset 信号，每次 resetConfig 或重播触发时递增
    val feedbackBtn: IAdFeedbackBtnVM

    val enableDensityScale: Boolean get() = false  // 大字版模板卡密度缩放开关

    var lifecycleFlow: SharedFlow<PageLifecycleEvent?>

    suspend fun registerScope()
    fun resetConfig()

    /** 通知卡片进入新一轮播放，用于驱动 UI 内部按播放轮次复位动画状态。 */
    fun notifyReplay()

    fun onCardShow(config: AdVideoCardConfig)
    fun onCardHide(config: AdVideoCardConfig)
}

/** 竖版视频页面语义事件观察者。 */
interface IAdVerticalVideoPageEventObserver {

    /** 视频开始播放。 */
    fun onVideoStart() {}

    /** 视频暂停播放。 */
    fun onVideoPause() {}

    /** 视频停止播放。 */
    fun onVideoStop() {}

    /** 视频播放完成。 */
    fun onVideoComplete() {}

    /** 展示结束页卡。 */
    fun onVideoShowFinishCover(showAutoPlay: Boolean, debugInDemo: Boolean) {}

    /** 收起结束页卡。 */
    fun onVideoDismissFinishCover() {}

    /** 模板卡动画进入。 */
    fun onAnimEnter() {}

    /** 模板卡动画退出。 */
    fun onAnimExit() {}

    /** 页面可见。 */
    fun onPageAppear() {}

    /** 页面不可见。 */
    fun onPageDisappear() {}

    /** 页面数据重置。 */
    fun onDataReset() {}

    /** 宿主暂停微信小店横滑轮播。 */
    fun onWxStoreCarouselHostTapPause() {}

    /** 结束页卡关闭。 */
    fun onFinishCardClose(fromUserClose: Boolean) {}

    /** 结束页卡已消失。 */
    fun onFinishCardDismissed() {}

    /** 页面当前挂载释放。 */
    fun onVerticalVideoComposePageDispose() {}

    /** 页面销毁。 */
    fun onVerticalVideoComposePageDestroy() {}
}

/*
* [竖版视频] - 模板卡
*/
interface IAdVideoTemplateCardVM : IAdVideoCardVM, IAdVerticalVideoPageEventObserver {

    val rewardTaskVM: IAdVideoRewardTaskVM?
    val fakeVote: IAdFakeVoteVM
    val videoRightToolsVM: IAdVideoRightToolsVM
    var outerClickAction: ClickAction?   // todo jiamin 为了解决Kuikly和native混排时点击失效，由宿主注入，等kuikly修复后下掉

    /**
     * 微信小店新横滑样式的轮播 VM。
     * 未命中该样式时返回 null，UI 仅根据是否存在决定是否挂载轮播业务层。
     */
    val wxStoreCarouselVM: IAdWxStoreCarouselVM?

    /**
     * 内容曝光/活跃态：是否允许子卡片曝光上报、横滑小店图片是否应轮播。
     * 由 VM 内部组合「动画态」与「是否被宿主暂停」得出——普通卡等同于 animateState；
     * 横滑小店在退后台/落地页暂停（carouselPaused=true）时为 false。UI 直接消费，不再自行拼接。
     */
    val reportActive: StateFlow<Boolean>

    /** 用户手动关闭模板大卡。 */
    fun onTemplateCardManualClose() {}

}

/**
 * 微信小店新横滑样式的图片轮播 VM，承接图片展示数据、轮播语义动作和埋点上抛。
 */
interface IAdWxStoreCarouselVM {
    val imageUrls: List<String>
    val imageCount: Int
    val progressVM: IAdWxStoreCarouselProgressVM

    /**
     * 宿主点击信号：宿主点击图片顶部等非图片区域时自增，UI 据此复用图片点击的 toggle 暂停逻辑，
     * 使图片暂停态与宿主侧共用同一份状态。初值为 0，UI 首次收集到 0 时不触发 toggle。
     */
    val hostTapSignal: StateFlow<Long>

    /**
     * 图片轮播是否被宿主侧暂停（退后台 / 跳转落地页）。与 animateState 解耦：
     * 仅用于暂停/恢复图片轮播本身，不影响三段卡入场动画（三段卡只看 animateState）。
     */
    val carouselPaused: StateFlow<Boolean>

    /** 图片加载状态变化时调用，用于记录素材加载结果。 */
    fun onImageLoadStateChanged(index: Int, success: Boolean)

    /** 图片点击时调用，用于记录点击语义并上抛业务埋点。 */
    fun onImageClick(index: Int)

    /** 用户点击暂停/恢复轮播时调用，通知宿主同步暂停或恢复视频播放。 */
    fun onTapPauseChanged(paused: Boolean)

    /** 宿主点击图片顶部等非图片区域时调用，自增 [hostTapSignal] 以驱动 UI toggle 暂停态。 */
    fun onHostTapPause()

    /** 宿主页面退到后台 / 跳转落地页时调用：暂停图片轮播（仅暂停轮播，不影响三段卡动画与重播）。 */
    fun onHostLifecyclePause()

    /** 宿主页面回到前台 / 落地页返回时调用：恢复图片轮播（原地续播，不回第 0 页）。 */
    fun onHostLifecycleResume()
}

/**
 * 微信小店图片轮播进度 VM，承接轮播状态机快照与轮播语义事件。
 */
interface IAdWxStoreCarouselProgressVM {
    val uiState: StateFlow<AdWxStoreCarouselProgressUiState>
    val stopAfterFirstCycle: Boolean

    /** 更新轮播进度状态快照，供 UI 动画和滚动回调同步状态机。 */
    fun updateUiState(uiState: AdWxStoreCarouselProgressUiState)

    /**
     * 图片首轮轮播完成时调用，触发统一 finish-card 展示流程。
     * @return true 表示结束蒙层已展示、轮播应停止；false 表示蒙层未展示（如已展示过），轮播应继续。
     */
    fun onFirstCycleComplete(): Boolean

    /**
     * 用户在最后一张图片继续向后手动滑动时调用，触发统一 finish-card 展示流程。
     * @return true 表示结束蒙层已展示、轮播应停止；false 表示蒙层未展示（如已展示过）。
     */
    fun onManualSwipePastLast(): Boolean

    /**
     * 宿主切走广告（派发 Data.OnReset）时调用，重置结束蒙层一次性展示状态，
     * 使下次重新进入这条广告时可再次展示。
     * 切 tab / 退后台只会触发 OnDisappear，不应走到此方法，避免回前台再次播完时蒙层重弹。
     */
    fun onCarouselReset() {}

    /** 图片曝光时调用，用于记录当前图片曝光。 */
    fun onImageExpose(index: Int)

    /** 轮播切页时调用，用于区分自动/手动滑动并记录滑动行为。 */
    fun onCarouselSlide(fromIndex: Int, toIndex: Int, isAuto: Boolean)
}

/**
 * 微信小店图片轮播进度状态，只包含 UI 状态机可直接消费的基础字段。
 */
data class AdWxStoreCarouselProgressUiState(
    val displayPage: Int = 0,
    val isAutoAdvancing: Boolean = false,
    val snapshotPage: Int = 0,
    val snapshotProgress: Float = 0f,
    val holdProgressDuringScroll: Boolean = false,
    val settleFrameHold: Boolean = false,
    val loopBoundaryHold: Boolean = false,
    val loopBoundaryHoldPage: Int = 0,
    val settledProgressOverride: Float? = null,
    val settledPage: Int = 0,
    val hasWarmupDone: Boolean = false,
    val hasReportedFirstCycle: Boolean = false,
    val isFirstCycleStopped: Boolean = false,
    val shouldResumeAfterManualScroll: Boolean = false,
    val lastPlayingState: Boolean = false,
    val autoTokenSeed: Long = 0L,
    val pendingAutoToken: Long = 0L,
    val pendingAutoTargetPage: Int = 0,
    val manualSettleSignal: Long = 0L,
    val consumedManualSettleSignal: Long = 0L,
    val scrollSessionSeq: Long = 0L,
    val pendingNoTransitionReleaseSeq: Long = 0L,
    val lastReportedExposePage: Int = -1,
)

/**
 * 微信小店专属 finish-card VM，承接结束页卡直接渲染字段和点击语义。
 */
interface IAdWxStoreEndCardVM {
    val imageUrls: List<String>
    val backgroundImageUrl: String?
    val title: String
    val storeName: String
    val discountText: String
    val tags: List<String>
    val storeIconVM: IAdStoreIconVM?
    val fallbackStoreIconFont: IconFont?
    val carouselLabels: List<LiveShopLabels>
    val actionBtn: IAdActionBtnVM

    /** 处理背景区域点击，统一走 finish-card 关闭语义。 */
    fun onBackgroundClick()

    /** 处理店铺头部点击，统一走商品点击语义。 */
    fun onStoreHeaderClick()

    /** 处理商品图片点击，统一走商品点击语义。 */
    fun onProductClick()

    /** 处理商品文案区域点击，统一走商品点击语义。 */
    fun onProductInfoClick()

    /** 处理主按钮点击，统一走行动按钮点击语义。 */
    fun onActionButtonClick()
}

interface IAdBigCardVM : IAdReportVM, IAdCardCloseVM, IAdBigCardStyleVM {
    val cardHighlightColor: String
}

interface IAdConsultBigCardVM : IAdBigCardVM {
    val marqueeCard: IAdConsultMarqueeVM

    val actionBtn: IAdCardActionBtnVM

    val iconUrl: String
    val title: String
    val desc: String
    val label: String
}

interface IAdConsultMarqueeVM : IAdReportVM {
    val typeIcon: String
    val typeName: String

    val questions: List<String>
    val questionLoopTime: Long

    val bgColor: Long
    val textColor: Long

    fun updateMarqueeIndex(index: Int)
}

interface IAdConsultButtonVM : IAdReportVM {
    val question: String
    val actionBtn: IAdCardActionBtnVM
    val actionText: String              // 行动按钮文案
    val actionHighlightColor: String    // 高亮主题色
    val actionHighlightTime: Long       // 高亮开始时间（>0生效）
    val enableHighlight: Boolean get() = actionHighlightTime >= 0 && actionHighlightColor.isValidStrColor()

    fun updateMarqueeIndex(index: Int)
}

interface IAdConsultLocationCardVM : IAdReportVM {
    val locationText: String
}

interface IAdConsultNewBigCardVM : IAdBigCardVM {
    val actionBtn: IAdCardActionBtnVM
    val questions: List<String>
    val questionLoopTime: Long
    val questionAnimStyle: Int // 1=仅文案上下轮播（默认），2=问答气泡整体缩放切换
    val iconUrl: String
    val title: String
    val labels: List<String>
    val hasDescriptionInfo: Boolean
    fun updateMarqueeIndex(index: Int)
}

interface IAdMiniGameSmallCardVM : IAdReportVM {
    val iconUrl: String // 行业图标
    val name: String    // 类别名（例如：小游戏）
    val desc: String    // 游戏描述（例如：人气榜top10）
    val labels: List<TrinityStageMiniGameLabels>  // 轮播标签列表
        get() = emptyList()

    val labelLoopTime: Long  // 轮播时间
        get() = 0L

    val autoCarouselSwitch: Boolean
        get() = false

    val playAnimation: Boolean
        get() = false
}

interface IAdDownloadIndustrySmallCardVM : IAdReportVM {
    val industryIconUrl: String // 行业图标
    val industryName: String    // 应用/游戏
    val industryDesc: String    // 固定行业引导文案
    val metricText: String      // 下载量或低分兜底文案
    val tagText: String         // 行业标签
    val showRating: Boolean     // iOS 评分展示
    val ratingValue: Float      // 0~5
    val ratingText: String      // 一位小数评分

    /** 小卡展开动画延迟，单位毫秒；由模板配置解析后提供给 UI。 */
    val expandDelayMs: Long
}

interface IAdCardActionBtnVM {
    var colorChangeEnabled: Boolean         // 是否颜色切换
    var actionIconFont: IconFont?           // 行动按钮图标
    val actionText: String                  // 行动按钮文案
    val actionHighlightColor: String        // 高亮主题色
    val actionHighlightTime: Long get() = 0 // 高亮开始时间（>0生效）
    val btnBgColorState: StateFlow<Long>
    fun updateHighlightColor(colorLong: Long)
    fun onClick()
}

interface IAdMiniGameMiddleCardVM : IAdReportVM {
    val gameIcon: IAdIconVM
    val gameName: String                // 游戏名
    val gameDesc: String                // 游戏描述

    val gameLabels: List<TrinityStageMiniGameLabels>?
    val labelLoopTime: Long

    val actionText: String              // 行动按钮文案
    val actionHighlightColor: String    // 高亮主题色
    val actionHighlightTime: Long       // 高亮开始时间（>0生效）

    val enableHighlight: Boolean get() = actionHighlightTime >= 0 && actionHighlightColor.isValidStrColor()

}

interface IAdMiniGameBigCardVM : IAdBigCardVM {
    val smallCard: IAdMiniGameSmallCardVM

    val gameIcon: IAdIconVM     // 游戏图标
    val gameName: String        // 游戏名
    val gameDesc: String        // 游戏描述

    val actionHighlightColor: String    // 高亮主题色
    val actionBtn: IAdCardActionBtnVM   // 行动按钮

    val highlightLabel: String          // 高亮标签（主题色的）
    val commonLabels: List<TrinityStageMiniGameLabels>? // 黑框小标签
    val promotionLabels: List<TrinityStageMiniGameLabels>? // 宣传图样式轮播标签
    val labelLoopTime: Long                 // 分类标签轮播间隔

    val threeCardUseBigImage: String        // 宣传大图的url
    val isThreeCardUseBigImage: Boolean     // 是否使用宣传大图
    val promotionImageClickVM: IClickVM?    // 宣传大图区域点击

    val hasLabel: Boolean get() = highlightLabel.isNotEmpty() || commonLabels.isNotNullOrEmpty()
}

interface IAdIconVM {
    val iconUrl: String?                        // 图标url
    val fallBackUrl: String?                    // 兜底图链接（如果图标url失效会降级这个）
}

// TODO: mountain opt 小店的公共能力需要下沉，目前复用得不太合理。
// 小店，直播小卡公共接口
interface IAdProductSmallCardVM : IAdReportVM {
    val icon: IAdIconVM                         // 商品图标
    val productTitle: String?                   // 商品标题
    val wxShopDiscountLabelText: String?        // 微信小店商品折扣标签
    val discountText: String                    // 折扣文案，如 "8.7折"、"9折"
    val liveShopLabels: List<LiveShopLabels>    // 轮播标签
    val labelLoopTime: Long                     // 轮播间隔时长
    val showLabelIcon: Boolean                  // 是否展示标签icon
}

interface IAdLiveShopDataVM {
    val hasProduct: Boolean
    fun updateProduct(callback: ((ResultEx) -> Unit)? = null)
}

interface IAdLiveShopSmallCardVM : IAdProductSmallCardVM {
    suspend fun onCreate() {}
    suspend fun onDestroy() {}
}

interface IAdLiveShopBigCardVM : IAdBigCardVM {
    val smallCard: IAdLiveShopSmallCardVM
    val actionBtn: IAdCardActionBtnVM
}

interface IAdWeChatStoreSmallCardVM : IAdProductSmallCardVM {
    val storeInfo: IAdWeChatStoreInfo?
    val storeIconVM: IAdStoreIconVM?
}

interface IAdWeChatStoreBigCardVM : IAdBigCardVM {
    val smallCard: IAdWeChatStoreSmallCardVM
    val actionBtn: IAdCardActionBtnVM
}

interface IAdEcommerceGeneralSmallCardVM : IAdReportVM {
    val icon: IAdIconVM                // 行业图标
    val productTitle: String           // 后台下发文案（收起态固定展示）
    val countdownSeconds: Int          // 倒计时总秒数（固定 1800 = 30分钟）
    val countdownSuffix: String        // 倒计时后缀文案（"后过期"）
    val countdownFlow: StateFlow<Int>  // 实时倒计时剩余十分之一秒（大卡可共享）
    fun startCountdown()               // 启动倒计时
    fun stopCountdown()                // 停止倒计时任务并保留当前剩余时间
}

interface IAdEcommerceGeneralBigCardVM : IAdBigCardVM {
    val icon: IAdIconVM                 // 广告主头像（40x40）
    val title: String                   // 标题
    val amountPrefix: String            // 金额前缀（现金券="¥"，折扣券=""）
    val amountText: String              // 金额数字（如 "30" 或 "8.5"）
    val amountSuffix: String            // 金额后缀（现金券=""，折扣券="折"）
    val showThresholdText: Boolean      // 是否展示门槛文案
    val thresholdText: String           // 生效门槛（如 "满200元减"）
    val voucherName: String             // 券名称（如 "购物红包"）
    val actionBtn: IAdCardActionBtnVM   // "领券买"按钮
    val smallCard: IAdEcommerceGeneralSmallCardVM  // 关联的小卡（共享倒计时）
}

/** 优惠券倒计时展示 VM，封装小卡、大卡和结束页共用的倒计时状态与生命周期。 */
interface IAdCouponCountdownVM {
    val countdownSeconds: Int           // 倒计时总秒数
    val countdownSuffix: String         // 倒计时后缀文案
    val countdownFlow: StateFlow<Int>   // 实时倒计时剩余十分之一秒

    /** 启动优惠券倒计时。 */
    fun startCountdown()

    /** 停止优惠券倒计时任务并保留当前剩余时间，支持卡片阶段切换后继续接力。 */
    fun stopCountdown()
}

/** 618 小店券一、二段小卡 VM，承接券文案、行业 icon 与倒计时状态。 */
interface IAdShop618CouponSmallCardVM : IAdReportVM {
    val icon: IAdIconVM                 // 行业图标
    val textIn618Activity: String       // 是否展示 6.18 活动 Text
    val titleText: String               // 券主文案
    val subtitleText: String            // 券辅助文案
    val showSubtitleText: Boolean       // 是否展示辅助文案
    val showCountdown: Boolean          // 是否展示倒计时
    val countdownOnlyInExpanded: Boolean // 倒计时是否仅展开态展示
    val actionText: String              // 行动引导文案
    val countdownVM: IAdCouponCountdownVM // 倒计时展示 VM
}

/** 618 小店券券包基础 VM，承接通用券文案、行动按钮与右上角 Lottie 挂件秒段。 */
interface IAdShop618CouponVoucherVM {
    val titleText: String                       // 券主文案
    val subtitleText: String                    // 券辅助文案
    val showSubtitleText: Boolean               // 是否展示券辅助文案
    val couponHotTagSecond: Int                 // 右上角 Lottie 挂件秒段：-1=不展示，1=大额券，2=官方立减，3~10=2~9张券，11=9+张券
    val actionBtn: IAdCardActionBtnVM           // 行动按钮
}

/** 618 微信小店金额券券包 VM。 */
interface IAdShop618WechatCouponVoucherVM : IAdShop618CouponVoucherVM {
    val amountPrefix: String                    // 金额前缀
    val amountText: String                      // 金额文本
    val isLargeAmountText: Boolean              // 金额过大时展示缩小字号
    val amountAssistText: String                // 金额区辅助文案
    val showAmount: Boolean                     // 是否展示金额区
    val showCountdown: Boolean                  // 是否展示倒计时
    val countdownVM: IAdCouponCountdownVM       // 倒计时展示 VM
}

/** 618 直播小店券包 VM。 */
interface IAdShop618LiveCouponVoucherVM : IAdShop618CouponVoucherVM

/** 618 小店券三段大卡/结束页券卡 VM，承接白色券包可直接渲染的数据与点击语义。 */
interface IAdShop618CouponBigCardVM : IAdBigCardVM {
    val icon: IAdIconVM                         // 商品图或行业兜底图
    val textIn618Activity: String               // 是否展示 6.18 活动 Text
    val productTitle: String                    // 商品标题
    val storeName: String                       // 店铺名或广告主名
    val amountPrefix: String                    // 金额前缀，直播无金额时为空
    val amountText: String                      // 金额文本，直播无金额时为空
    val amountAssistText: String                // 金额区辅助文案
    val showAmount: Boolean                     // 是否展示金额区
    val titleText: String                       // 券主文案
    val subtitleText: String                    // 券辅助文案
    val showSubtitleText: Boolean               // 是否展示券辅助文案
    val badgeText: String                       // 右上角角标文案
    val showBadge: Boolean                      // 是否展示角标
    val showCountdown: Boolean                  // 是否展示倒计时
    val couponHotTagSecond: Int                 // 右上角 Lottie 挂件秒段：-1=不展示，1=大额券，2=官方立减，3~10=2~9张券，11=9+张券，单张满减券不展示
    val countdownVM: IAdCouponCountdownVM       // 倒计时展示 VM
    val voucherVM: IAdShop618CouponVoucherVM    // 券包展示 VM
    val actionBtn: IAdCardActionBtnVM           // 行动按钮
    val labels: List<LiveShopLabels>            // 商品信息辅助文案
    val labelLoopTime: Long                     // 商品辅助文案轮播间隔
}


interface IAdVideoTitleCardVM : IAdReportVM {
    val advertiserName: String
    val adTitle: String
    val storeName: String?
        get() = null
    val storeIconVM: IAdStoreIconVM?
        get() = null

    /** 展示名称：storeName 优先，为空则降级到 advertiserName */
    val displayName: String
        get() = "@${storeName?.takeIf { it.isNotEmpty() } ?: advertiserName}"

    /** 是否展示店铺标识（好店/R标） */
    val showStoreIcon: Boolean
        get() = storeIconVM?.iconFont != null
}

typealias AdCompanionSizeCallback = (widthInDp: Float, heightInDp: Float) -> Unit

interface IAdVideoCompanionCardVM : IAdReportVM {
    val iconUrl: String
    val iconAspectRatio: Float
    val tag: String
    val title: String
    val gameScore: Float
    val giftTotalNum: Int
    val descriptionList: List<String>

    val expandText: String
    val isExpanded: StateFlow<Boolean>
    val isGameCompanion: Boolean

    val initHeightDp: Float     // 挂件初始化高度
    val showCloseBtn: Boolean
    val isCardVisible: StateFlow<Boolean>

    // 外部监听器：要注意解注册！
    var onAfterClose: (() -> Unit)?                 // 点击关闭后回调
    var onHookJump: ((IKmmAdFeedsItem) -> Boolean)? // 触发外链落地页跳转时回调（其余跳转类型不会回调）
    var onSizeChanged: AdCompanionSizeCallback?

    val debugMsg: IAdDebugMsgVM

    fun startExpandAnim()
    fun resetExpandAnim()
    fun onClose()
}

interface IAdVideoFlickerButtonCardVM : IAdReportVM {
    val actionHighlightColor: String    // 高亮主题色
    val actionHighlightTime: Long       // 高亮开始时间（>0生效）

    val actionBtn: IAdActionBtnVM
    val industryActionBtn: IAdCardActionBtnVM?
    val flickerVM: IAdFlickerVM?

    val enableHighlight: Boolean get() = actionHighlightTime >= 0 && actionHighlightColor.isValidStrColor()
}

interface IAdFlickerVM {
    val flickerTime: Long               // 扫动动画开始时间（>0生效）
    val flickerLottie: String           // 闪光 lottie
}

interface IAdCardCloseVM {
    fun onClose()           // 关闭模版卡（会执行回退动画）
}

interface IAdDefaultBigCardVM : IAdBigCardVM {
    val icon: IAdIconVM     // 图标
    val name: String        // 名称
    val desc: String        // 描述

    val actionBtn: IAdActionBtnVM
    val actionHighlightColor: String    // 高亮主题色
    val storeFallback: IAdDefaultBigCardStoreFallbackVM?
        get() = null
}

interface IAdDefaultBigCardStoreFallbackVM {
    val icon: IAdIconVM
    val name: String
    val storeIconVM: IAdStoreIconVM?
}

/** 默认大卡 V2：支持浮层图文与标签/数据外显 */
interface IAdDefaultBigCardV2VM : IAdBigCardVM {
    val icon: IAdIconVM
    val name: String
    val desc: String

    val actionBtn: IAdActionBtnVM
    val actionHighlightColor: String

    /** 信息外显项：标签（带背景）与转化信息（纯文本） */
    val infoItems: List<IAdDefaultBigCardV2InfoItem>

    /** 轮播间隔（毫秒），来源于 cardConfig.loopTime */
    val infoLoopTime: Long
}

interface IAdDefaultBigCardV2InfoItem {
    val text: String
    val tags: List<String>
    val hasBg: Boolean
}

/**
 * 短剧三段卡大卡 ViewModel
 */
interface IAdShortDramaBigCardVM : IAdBigCardVM {
    val icon: IAdIconVM                   // 短剧封面
    val name: String                      // 短剧名称
    val desc: String                      // 广告文案
    val infoItems: List<IAdShortDramaBigCardInfoItemVM> // 轮播信息
    val actionBtn: IAdCardActionBtnVM     // 行动按钮
    val labelLoopTime: Long               // 轮播时间
}

interface IAdShortDramaBigCardInfoItemVM {
    val text: String                       // 信息文案
    val tags: List<String>                 // 标签样式文案
}

interface IAdDownloadIndustryBigCardVM : IAdBigCardVM {
    val industryIconUrl: String             // 行业图标
    val industryName: String                // 应用/游戏
    val industryDesc: String                // 固定行业引导文案

    val appIcon: IAdIconVM                  // 应用/游戏图标
    val appName: String                     // 应用/游戏名称
    val appDesc: String                     // 广告描述

    val metricText: String                  // 下载量或低分兜底文案
    val tagText: String                     // 行业标签
    val showRating: Boolean                 // iOS 评分展示
    val ratingValue: Float                  // 0~5
    val ratingText: String                  // 一位小数评分

    val actionBtn: IAdActionBtnVM
    val actionHighlightColor: String

    /**
     * 下载行业大卡行动按钮点击。
     *
     * @param downloadActionHandled 下载动作是否已由行动按钮 VM 处理；为 true 时业务点击只保留必要上报并跳过落地页跳转。
     */
    fun onActionButtonClick(downloadActionHandled: Boolean = false)
}

interface IAdAppChannelInfoVM : IAdReportVM {
    val authorName: String      // 运营商
    val versionName: String     // app版本
    val developerName: String   // 开发者
    val suitableAge: String     // 适用年龄

    val permissionsInfo: String // 应用权限
    val privacyInfo: String     // 隐私政策
    val featureInfo: String     // 功能介绍
    val icpInfo: String         // 备案信息（@7540 ‘备案号’‘备案单位’合并到一个url里展示）

    val line1EllipsizeText: String
    val line1Text: String

    val line2EllipsizeText: String
    val line2Text: String

    fun isDataValid(): Boolean
    fun is3LineStyle(): Boolean
}

interface IAdVideoHotClickAreaVM : IAdReportVM, IAdExportVM {
    val disable: Boolean
    val heightRate: Float
    val marginBottomRate: Float
    val marginLeftRate: Float
    val marginRightRate: Float
}

interface IAdMiniCardPendantVM : IAdReportVM {
    val icon: String
    val desc: String
}

interface IAdBigCardStyleVM {
    val colorStyle: AdBigCardColorStyle
}
