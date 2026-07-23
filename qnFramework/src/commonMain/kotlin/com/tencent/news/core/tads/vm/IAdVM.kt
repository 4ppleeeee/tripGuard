package com.tencent.news.core.tads.vm

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.api.IExportModelData
import com.tencent.news.core.list.vm.ClickAction
import com.tencent.news.core.list.vm.IFeedbackBtnVM
import com.tencent.news.core.tads.comment.vm.IAdStoreProductInfoVM
import com.tencent.news.core.tads.download.IDownloadBtnVM
import com.tencent.news.core.tads.model.IKmmAdOrder
import kotlinx.coroutines.flow.StateFlow

@Deprecated("不用这个了")
interface IAdVM : IKmmKeep {
    fun buildData(adOrder: IKmmAdOrder)
}

interface IAdExportVM : IExportModelData

// tab2 随机点赞数
interface IAdFakeVoteVM {
    val voteNum: Long
}

// 广告DSP名称
interface IAdDspVM : IAdExportVM {
    val dspName: String?
}

// 【debug】红字调试信息
interface IAdDebugMsgVM : IAdExportVM {
    val debugInfo: String
    val debugInfoState: StateFlow<String>
    fun notifyDebugInfoChanged()
}

// 广告标
interface IAdIconVM {
    val iconText: String
    val hideAdIcon: Boolean
}

// 主标题
interface IAdMainTitleVM : IAdExportVM {
    val title: String
    val hasReadState: StateFlow<Boolean>
    fun notifyAdHasRead()
}

// 广告主
interface IAdvertiserVM : IAdExportVM {
    val name: String
    val iconUrl: String
    val isLive: Boolean
    val canShow: Boolean get() = name.isNotEmpty() || iconUrl.isNotEmpty()
    fun onClick()
}

// 广告底栏
interface IAdBottomBarVM {
    val displayAdvertiserVM: IAdvertiserVM
    val storeIconVM: IAdStoreIconVM?
    val storeProductInfoVM: IAdStoreProductInfoVM
    val adIconVM: IAdIconVM
    val actionBtnVM: IAdActionBtnVM
    val feedbackBtnVM: IAdFeedbackBtnVM
}

// 封面图
interface IAdImageCoverVM : IAdExportVM {
    val coverUrl: String
}

// 多图
interface IAdMultiImageVM : IAdExportVM {
    val imageUrls: List<String>
}

// 底部标签（品牌臻选logo + 广告标 + 广告主 等信息）
interface IAdLabelVM : IAdExportVM {
    val brandLabel: String          // 品牌臻选（带特殊边框样式的，文案可以下发）
    val textLabels: List<String>    // 普通灰字标签
}

// 行动按钮
interface IAdActionBtnVM : IAdExportVM {
    val actionText: String
    var actionIconFont: IconFont?
    val btnStyle: Int
    val hideText: Boolean

    // 以下flow状态提供给compose ui使用：
    val actionTextState: StateFlow<String>      // 按钮文案
    val downloadPercentState: StateFlow<Float>  // 下载进度（取值范围 [0.0, 1.0]）
    val btnBgColorState: StateFlow<Long>        // 背景色（受动画、下载状态影响）

    fun onCreate()      // compose生命周期，用于下载监听注册
    fun onDestroy()     // compose生命周期，用于下载监听注册

    fun onRefresh()     // 刷新
    fun onClick()       // 点击事件处理

    fun updateHighlightColor(colorLong: Long)   // 更新高亮主题色（不设置默认蓝底白字）
    fun showDownloadingStyle(): Boolean         // 是否支持下载样式
    fun createDownloadAction(): ClickAction?    // 点击触发下载相关操作（启动、暂停、安装 等）

}

// 目前还未使用，后续需要依靠此接口重构IAdActionBtnVM将下载逻辑摘出来
interface IAdDownloadBtnVM : IDownloadBtnVM {
    val btnBgColorState: StateFlow<Long>        // 背景色（受动画、下载状态影响）
    fun updateHighlightColor(colorLong: Long)   // 更新高亮主题色（不设置默认蓝底白字）
}

// 负反馈按钮
interface IAdFeedbackBtnVM : IFeedbackBtnVM, IAdExportVM {
    val iconText: String
    val hideAdIcon: Boolean
    val feedbackItemList: List<IAdFeedbackItemVM>

    fun onClickComplain()  // 点击举报按钮
}

interface IAdFeedbackItemVM : IAdExportVM {
    val feedbackId: String
    val feedbackType: String
    val feedbackText: String

    fun onClick()
}

// 微信小店标识（好店/R标）
interface IAdStoreIconVM : IKmmKeep {
    val iconFont: IconFont?                 // iconfont 枚举值
    val iconAspectRatio: Float              // 图标宽高比（好店=43/18，R标=1）
    val isHaoDian: Boolean                  // true=好店标，false=R标；宿主可据此在深色背景场景为R标选用夜间色值
    val colorHexDay: String                 // 日间模式色值（默认30%透明度），格式 "#AARRGGBB"，如 "#FFE0B584"
    val colorHexNight: String               // 夜间模式色值（默认30%透明度），格式 "#AARRGGBB"，如 "#FFBA966E"
    val colorHexDay55: String               // 日间模式色值（55%透明度，R标与文本结合使用）
        get() = colorHexDay
    val colorHexNight55: String             // 夜间模式色值（50%透明度，R标与文本结合使用）
        get() = colorHexNight
}
