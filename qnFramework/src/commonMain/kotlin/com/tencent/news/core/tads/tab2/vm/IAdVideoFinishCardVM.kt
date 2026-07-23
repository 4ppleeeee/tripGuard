package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.tads.constants.AdVideoFinishCardStyle
import com.tencent.news.core.tads.vm.IAdActionBtnVM
import com.tencent.news.core.tads.vm.IAdStoreIconVM
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 结束卡一次性 UI 事件。
 */
enum class AdVideoFinishCardUiEvent {
    Close,
    Next,
}

/**
 * 结束卡播放器控制事件，用于宿主通知 UI 层控制播放器状态。
 */
enum class AdVideoFinishCardPlayerControlEvent {
    Resume,
    Pause,
}

/**
 * 结束卡播放器区域所需的只读视频数据。
 */
interface IAdVideoFinishCardVideoVM {
    val videoId: String
    val videoUrl: String
    val coverUrl: String
    val isPortraitVideo: Boolean
    val videoAspectRatio: Float
}

/**
 * 结束卡信息区 VM，承接头像、名称、标题等文案区域的数据与点击语义。
 */
interface IAdVideoFinishCardInfoVM {
    val displayName: String
    val titleText: String
    val avatarUrl: String
    val storeIconVM: IAdStoreIconVM?
    val discountText: String

    /**
     * 处理信息区点击，例如名称、标题、头像等区域。
     */
    fun onClick()
}

/**
 * 结束卡按钮区 VM，承接主按钮/次按钮的数据与点击语义。
 */
interface IAdVideoFinishCardActionVM {
    val primaryActionBtn: IAdActionBtnVM

    /**
     * 主行动按钮高亮色，供 UI 与下载进度态统一使用。
     */
    val primaryActionColor: Long
    val showSecondaryAction: Boolean
    val secondaryActionText: String

    /**
     * 处理主按钮点击。
     */
    fun onPrimaryActionClick()

    /**
     * 处理次按钮点击。
     */
    fun onSecondaryActionClick()
}

/**
 * 结束卡自动连播区 VM，承接倒计时与自动连播提示状态。
 */
interface IAdVideoFinishCardAutoPlayVM {
    val showAutoPlay: StateFlow<Boolean>
    val countDownTime: StateFlow<Int>
}

/**
 * 竖版视频 finish-card VM。
 *
 * 面向 UI 暴露：
 * - 布局级配置与生命周期
 * - 按子组件聚合后的子 VM
 * - 宿主集成所需的绑定能力
 */
interface IAdVideoFinishCardVM : IAdVerticalVideoPageEventObserver {

    val enableDensityScale: Boolean
    val style: AdVideoFinishCardStyle
    val backgroundUrl: String
    val playerVM: IAdVideoFinishCardVideoVM
    val infoVM: IAdVideoFinishCardInfoVM
    val actionVM: IAdVideoFinishCardActionVM
    val autoPlayVM: IAdVideoFinishCardAutoPlayVM
    val appChannelInfoVM: IAdAppChannelInfoVM?

    /**
     * 电商通用专属结束卡复用的券面 VM。
     * 仅在命中电商通用结束卡样式时返回非空，其他样式保持为空并沿用原布局。
     */
    val ecommerceGeneralBigCardVM: IAdEcommerceGeneralBigCardVM?

    /**
     * 618 小店券专属结束卡复用的券面 VM。
     * 仅在命中 618 小店券结束卡样式时返回非空，其他样式保持为空并沿用原布局。
     */
    val shop618CouponBigCardVM: IAdShop618CouponBigCardVM?

    /**
     * 微信小店专属结束卡 VM。
     * 仅在命中微信小店新横滑结束卡样式时返回非空，其他样式保持为空并沿用原布局。
     */
    val wxStoreEndCardVM: IAdWxStoreEndCardVM?
    val displayName: String
        get() = infoVM.displayName
    val titleText: String
        get() = infoVM.titleText
    val avatarUrl: String
        get() = infoVM.avatarUrl
    val storeIconVM: IAdStoreIconVM?
        get() = infoVM.storeIconVM
    val discountText: String
        get() = infoVM.discountText
    val primaryActionBtn: IAdActionBtnVM
        get() = actionVM.primaryActionBtn
    val onlyPrimaryActionClickable: Boolean
    val showSecondaryAction: Boolean
        get() = actionVM.showSecondaryAction
    val secondaryActionText: String
        get() = actionVM.secondaryActionText

    val visibleState: StateFlow<Boolean>
    val showAutoPlay: StateFlow<Boolean>
        get() = autoPlayVM.showAutoPlay
    val countDownTime: StateFlow<Int>
        get() = autoPlayVM.countDownTime
    val uiEvent: SharedFlow<AdVideoFinishCardUiEvent>

    /** 播放器控制事件流，宿主通过 [onPlayerResume] / [onPlayerPause] 下发指令。 */
    val playerControlEvent: SharedFlow<AdVideoFinishCardPlayerControlEvent>

    /**
     * 结束卡播放器当前播放态（true=播放中，false=暂停/未开始）
     */
    val isPlayerPlaying: StateFlow<Boolean>

    /**
     * 页面展示时调用，使用 VM 内部配置的默认倒计时。
     */
    fun onAppear(showAutoPlay: Boolean)

    /**
     * 页面展示时调用，使用外部指定的倒计时时间。
     */
    fun onAppear(showAutoPlay: Boolean, countDownTime: Int)

    /**
     * 页面由模板内部触发展示时调用，自动连播状态同步向宿主查询。
     */
    fun onAppearWithHostAutoNext()

    /**
     * 页面隐藏或销毁时调用，释放与页面生命周期相关的资源。
     */
    fun onDisappear()

    /**
     * 通知播放器恢复播放，通常在页面 OnAppear 且 finishCard 正在展示时调用。
     */
    fun onPlayerResume()

    /**
     * 通知播放器暂停播放，通常在页面 OnDisappear 且 finishCard 正在展示时调用。
     */
    fun onPlayerPause()

    /**
     * 处理主按钮点击后的 finish-card 语义逻辑，如上报。
     */
    fun onPrimaryActionClick() {
        actionVM.onPrimaryActionClick()
    }

    /**
     * 处理文字区域点击。
     */
    fun onTextClick() {
        infoVM.onClick()
    }

    /**
     * 处理卡片背景区域点击。
     */
    fun onBackgroundClick()

    /**
     * 处理播放器区域点击。
     */
    fun onPlayerClick()

    /**
     * 处理关闭按钮点击。
     */
    fun onCloseClick()

    /**
     * 更新结束页卡播放器当前播放进度，供关闭结束页卡时透传给宿主续播。
     */
    fun updatePlayerProgress(positionMs: Long)

    /**
     * 处理宿主主动关闭结束卡，不带用户点击上报。
     *
     * 具体是否需要补齐宿主 close 语义由 VM 根据结束卡样式决定。
     */
    fun dismissByHost()

    /**
     * 处理次按钮点击，例如 style5 的“看下一条”。
     */
    fun onSecondaryActionClick() {
        actionVM.onSecondaryActionClick()
    }

    /**
     * 宿主注册关闭回调。
     *
     * @param action 参数依次为结束页卡播放器当前播放进度、是否由用户主动关闭触发；传 null 表示取消监听。
     */
    fun bindCloseAction(action: ((Long, Boolean) -> Unit)?)

    /**
     * 宿主注册“下一条”回调；传 null 表示取消监听。
     */
    fun bindNextAction(action: (() -> Unit)?)

    /**
     * 宿主注册自动连播回调；传 null 表示取消监听。
     * 参数为本次倒计时结束后实际生效的秒数。
     */
    fun bindAutoNextAction(action: ((Int) -> Unit)?)

    /**
     * 宿主注册当前是否支持自动连播的同步 provider；传 null 表示取消监听。
     */
    fun bindHostAutoNextProvider(action: (() -> Boolean)?)

    /**
     * 查询宿主当前是否支持自动连播。
     */
    fun queryHostAutoNext(): Boolean?

    /**
     * 宿主注册播放器封面 provider；通常返回由 `coverImage` 落盘得到的本地 URL。
     */
    fun bindPlayerCoverUrlProvider(action: (() -> String?)?)

    /**
     * 宿主注册通用封面 provider，作为播放器封面缺失时的业务兜底。
     */
    fun bindHostCoverUrlProvider(action: (() -> String?)?)

    /**
     * 清理宿主注入的所有回调和 provider，通常在页面销毁或重建绑定前调用。
     */
    fun clearHostBindings()
}
