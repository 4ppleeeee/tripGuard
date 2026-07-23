package com.tencent.news.core.tads.articles

import com.tencent.news.core.list.api.IDislikeListener
import com.tencent.news.core.list.vm.IBtnVM
import com.tencent.news.core.list.vm.IImageVM
import com.tencent.news.core.page.model.StructBg
import com.tencent.news.core.page.model.StructText
import com.tencent.news.core.tads.constants.AdGdtClickActType
import com.tencent.news.core.tads.vm.IAdActionBtnVM
import com.tencent.news.core.tads.vm.IAdFeedbackBtnVM
import com.tencent.news.core.tads.vm.IAdIconVM
import com.tencent.news.core.tads.vm.IAdImageCoverVM
import com.tencent.news.core.tads.vm.IAdMainTitleVM
import com.tencent.news.core.tads.vm.IAdStoreIconVM
import com.tencent.news.core.tads.vm.IAdvertiserVM
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * 文中视频广告播放控制事件。
 * 宿主依据 cell 在原生滚动容器内的可见性（scrollViewWillDisplay/EndDisplay）及前后台状态下发，
 * UI 层收集后驱动 QnVideo 起播 / 暂停。
 */
enum class AdMidArticleVideoControlEvent {
    Play,
    Pause,
}

data class AdMidArticleTitleStyle(
    val fontSize: Float = 18f,
    val lineHeight: Float = 22f
)

interface IAdArticleMidVM {
    val coverImage: IAdImageCoverVM
    val mainTitleVM: IAdMainTitleVM
    val advertiserVM: IAdvertiserVM
    val feedbackBtn: IAdFeedbackBtnVM?
    val actionBtnVM: IAdActionBtnVM
    val downloadBtn: IBtnVM
    val dislikeListener: IDislikeListener?  // 处理负反馈，用于宿主移除js标签

    /** 标题字号和行高，随宿主文章字号上下文变化。 */
    val titleStyle: StateFlow<AdMidArticleTitleStyle>

    /** 标题最大展示行数。 */
    val titleMaxLines: Int
        get() = 1

    /** 是否隐藏行动按钮。 */
    val hideActionBtn: Boolean
        get() = false

    // ---------- 文中视频广告（横版大图 / 竖版大图视频化）----------

    /** 是否视频类型：Shiply 总开关命中 + 素材为有效视频。true 时大图区域用视频替换封面图 */
    val isVideoType: Boolean

    /** 视频 vid（需换链播放） */
    val videoId: String

    /** 视频 url（可直接播放） */
    val videoUrl: String

    /** 视频封面图 url（未起播 / 暂停 / 停止时展示，复用大图封面） */
    val videoCoverUrl: String

    /** 播放控制事件流，UI 层收集后驱动 QnVideo 起播 / 暂停 */
    val videoControlEvent: SharedFlow<AdMidArticleVideoControlEvent>

    /** 处理广告点击，actType 表示具体点击区域。 */
    fun onClick(actType: AdGdtClickActType)

    /** 设置负反馈回调，用于宿主移除对应的 JS 标签。 */
    fun setDislikeListener(dislikeListener: IDislikeListener?)

    /** 宿主文章字号上下文变化时调用，用于刷新广告标题的 UI 样式。 */
    fun onArticleFontContextChanged(
        articleOriginalFontSize: Float,
        isSmallFontArticle: Boolean,
        isArticleFontStandardizationApplied: Boolean
    )

    /** 宿主在 cell 可见（scrollViewWillDisplay / 进前台且可见）时调用，触发起播并隐藏封面 */
    fun notifyVideoPlay()

    /** 宿主在 cell 不可见（scrollViewEndDisplay / 退后台）时调用，触发暂停并显示封面 */
    fun notifyVideoPause()
}

/**
 * 文中横版小图广告VM
 */
interface IAdArticleMidSmallVM : IAdArticleMidVM {
    val image: IImageVM
    val title: StructText
    val advertiser: StructText
    val bg: StructBg
    // 广告标志
    val adIcon: IAdIconVM
}


/**
 * 文中横版大图广告
 */
interface IAdArticleMidLargeVM : IAdArticleMidVM {
    val isBgGray: Boolean
}


/**
 * 文中竖版小图广告
 */
interface IAdArticleMidSmallVerVM : IAdArticleMidVM {
    // 广告标志
    val adIcon: IAdIconVM
}

/**
 * 文中竖版大图广告
 */
interface IAdArticleMidLargeVerVM : IAdArticleMidVM {
    // 广告标志
    val adIcon: IAdIconVM
    // 店铺数据优先展示后的广告主名称
    val displayAdvertiserName: String
        get() = advertiserVM.name
    // 店铺数据优先展示后的广告主头像
    val displayAdvertiserIconUrl: String
        get() = advertiserVM.iconUrl
    // 微信小店标识（好店/R标）
    val storeIconVM: IAdStoreIconVM?
        get() = null
}
