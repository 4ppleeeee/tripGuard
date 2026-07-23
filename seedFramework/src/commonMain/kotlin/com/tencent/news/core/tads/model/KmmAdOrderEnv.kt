@file:Suppress("RemoveEmptyPrimaryConstructor", "RedundantConstructorKeyword")

package com.tencent.news.core.tads.model

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getCurTimePassMillis
import com.tencent.news.core.extension.getCurTimestampMillis
import com.tencent.news.core.extension.safeJsonClone
import com.tencent.news.core.platform.BasePlatformModel
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.tads.constants.AdAndroidWaitSysWindowResult
import com.tencent.news.core.tads.constants.AdFlipOverTurnState
import com.tencent.news.core.tads.constants.AdMediaType
import com.tencent.news.core.tads.constants.AdNativeType
import com.tencent.news.core.tads.constants.AdRePullType
import com.tencent.news.core.tads.constants.AdRefreshType
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.tads.extension.IAdOrderVMEx.notifyDebugInfoChanged
import com.tencent.news.core.tads.model.ai.AiPolicy
import com.tencent.news.core.tads.model.ai.AiPolicyList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class AdOrderExpSource(val code: Int) {
    TOP(0),
    BOTTOM(1),
    CACHE(2),
    RELATED(3),
    RELOAD(4),
}

/**
 * 客户端绑定的一些环境参数，方便逻辑判断；例如广告所在页面的 idStr 等等
 */
@Serializable
class KmmAdOrderEnv constructor() : BasePlatformModel(), IAdOrderDtoDoc, IKmmKeep {

    @Transient
    @kotlin.jvm.Transient
    // 宿主兼容使用：通过 IKmmAdOrder 获取 IKmmAdFeedsItem
    // （由宿主赋值，防止iOS循环引用问题）
    var adItemWeakRef: (() -> IKmmAdFeedsItem?)? = null

    @Transient
    @kotlin.jvm.Transient
    var adOrder: IKmmAdOrder? = null

    var isKmm: Boolean = false

    var pageArticleId: String? = ""             // 当前广告场景，关联的文章id（请求时的 article_id 参数）

    var preCellPicShowType: Int = INVALID_NUM   // 前一个cell的样式（一般用于判断特殊场景的广告样式，例如视频频道）
    var preCellIdStr: String = ""               // 前一个cell的id（主要用于广告位置固定逻辑）

    var videoCmsID: String? = ""                // 文章的cmsId

    var isFromGameBonbon: Boolean = false       // 是否来自bonbon页的游戏挂卡（不受总频控影响）
    var isGameBonbonPendentItem: Boolean = false // 是否来自bonbon页的带过来的挂卡（不受总频控和游戏频控限制）

    var selectFeedbackId: String = ""           // 负反馈的类型

    var index: Int = 1                          // 广告相对位置，从1开始（按当前列表广告插入状态计算）
    var sessionIndex: Int = INVALID_NUM         // 广告相对位置，从1开始（按session内 真实曝光时累加，session变化时重置）
        set(value) {
            field = value
            notifyDebugInfoChanged()
        }
    var uiSeq: Int = INVALID_NUM                // 在ui数据中的绝对位置

    var createTime: Long = getCurTimeMillis()   // 订单创建的时间

    var isOpenSound: Boolean = false                  // 广告的声音是否打开

    var originExposedTime: Long = 0             // 原始曝光时的时间戳
        private set
    var isOriginExposed: Boolean = false        // 是否原始曝光过
        set(value) {
            field = value
            if (value && originExposedTime <= 0L) {
                originExposedTime = getCurTimeMillis()
            }
            notifyDebugInfoChanged()
        }

    var realExposedTime: Long = 0               // 真实曝光时的时间戳
        private set
    var isRealExposed: Boolean = false          // 是否真实曝光过
        set(value) {
            field = value
            if (value && realExposedTime <= 0L) {
                realExposedTime = getCurTimeMillis()
            }
            notifyDebugInfoChanged()
        }

    var requestDebug: String = ""               // 这一刷返回的debug信息（开发环境有值，base64加密的）

    var interval: Int = 0                       // 与上一条广告的绝对位置差值，首条广告为0 包含所有类型：硬广、硬广位原生、专属位原生

    var hardInterval: Int = 0                   // 与前一条硬广类广告的间隔（只包含：硬广、硬广位原生）
    var hardAdMinInterval: Int = 0              // SSP下发的硬广最小间隔（app_params.ad_min_interval）

    var fromRefreshType: AdRefreshType? = null  // 广告是由那种刷新方式拉取的

    @Transient
    var aiState = KmmAdAiState()                // 【端智能】相关本地状态
    var isAiRePulled: Boolean = false           // 【端智能】是否是端智能重拉的单子
    var rePullType: Int = AdRePullType.NORMAL   // 【端智能】该订单的拉取方式
    var triggerSoid: String = ""                // 【端智能】触发替换的原始订单的soid（多次替换也是首刷的soid）

    var isLandingPage = false                   // 是否是落地页

    var isFlexList: Boolean = false             // kmm重构后的flex列表

    var itemClickTime: Long = -1L               // item点击时间（单位毫秒）

    var hasReportVideoPlay: Boolean = false     // 【竖版视频】vv特殊口径，卡片划走才重新报一次
    var lastStartPlayTime: Long = 0             // 【竖版视频】最新一次起播时间戳
    var totalPlayDuration: Long = 0L            // 【竖版视频】累计播放时长（单位毫秒）

    var templateCardPlayStartTime: Long = 0L    // 模板卡动画起播时间（单位毫秒）
    val templateCardShowingSet: MutableSet<String> = linkedSetOf() // 当前正在展示的模板卡片id

    var isDisliked = false      // 是否被负反馈删除过
    var hasRead = false         // 已读态

    var isSubOrder = false      // 是否是子订单

    var isTab2InsertAd = false  // 是否是手动插入到tab2的订单

    var flipOverTurnStatue = AdFlipOverTurnState.FRONT  // 巨幕广告翻转状态  @AdFlipOverTurnState

    var mediaType: Int = AdMediaType.UNKNOWN      // 媒体类型（奥运画廊存在视频和图片混排情况）  @AdMediaType

    var videoPlayPosition: Long = 0L  // 视频播放进度

    var needPreChangeCgi: Boolean = false         // 需要单独报点击cgi请求标识

    var nativeAdType: AdNativeType = AdNativeType.HARD_AD        // 是否是原生广告,不携带 == 非原生， 0 == 原生降级硬广， 1 == 原生广告正常展示

    @KmmInternalApi
    var landingUrl: String = ""       // 落地页地址（换链之后会替换这个url，变成落地页纯h5链接，不带gdt上报）

    var waitSysWindowResult: AdAndroidWaitSysWindowResult =
        AdAndroidWaitSysWindowResult.UNKNOWN // 延迟双开绑定的等待结果

    var adLinkLandingPageActCode: Int = 0  // 广告落地页 ActCode值，用于链路上报中
    var adLinkLandingPageType: Int = 0     // 打开广告落地页类型值，用于链路上报中

    var isXJPageDowngrade: Boolean = false  // 官方页降级执行

    var consultMarqueeQuestionIndex = INVALID_NUM

    var forceSmallImageInDoubleColumn: Boolean = false // 双列模式下强制使用左文右图样式（不修改原始subType）

    var h5OpenType: AdH5OpenType = AdH5OpenType.NORMAL

    var expAction: AdOrderExpSource = AdOrderExpSource.TOP  // 订单曝光来源
    var newsTraceId: String = ""

    var disableOneShotAnimation: Boolean = false

    var disallowAdNum: Int? = null

    private fun notifyDebugInfoChanged() {
        adOrder.notifyDebugInfoChanged()
    }

    override fun kmmDeepClone(): KmmAdOrderEnv {
        val result = super.kmmDeepClone() as? KmmAdOrderEnv
            ?: safeJsonClone()
            ?: KmmAdOrderEnv()
        result.aiState = KmmAdAiState() // ai都是临时数据，建个新的即可，后面有需求再做深拷贝

        return result
    }

    companion object {
        const val PRE_CELL_ID_NONE = "__PRE_CELL_ID_NONE__" // 用于标记广告是第一个，前面没有文章了
    }

}


class KmmAdAiState : IKmmKeep {

    // sdk‘判定’重拉时给的，用在重拉阶段，传给后台（这个字段是sdk从ams_ad_info里f8字段解析出来的）
    var rePullAdContext: String = ""

    var invokePolicy: AiPolicyList = null       // 端智能触发策略
    var triggeredPolicy: AiPolicy? = null       // 被触发的策略（本地绑定的）

    var lastRePullTime: Long = getCurTimestampMillis() // 上次触发的时间点（做频控使用）

    override fun toString(): String {
        return listOfNotNull(
            "策略列表：${invokePolicy}",
            if (triggeredPolicy != null) "命中策略：${triggeredPolicy}" else null,
            if (lastRePullTime > 0) "上次触发已经过${getCurTimePassMillis(lastRePullTime)}ms" else null
        ).joinToString()
    }

    fun updateRePullTime() {
        lastRePullTime = getCurTimestampMillis()
    }

}


enum class AdH5OpenType {
    NORMAL,
    // 延迟双开
    SYS_BLOCK_DOUBLE_OPEN
}
