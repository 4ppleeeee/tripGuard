@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.tads.feeds

import com.tencent.news.core.extension.isNotNullOrBlank
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.tads.constants.AdLoid
import com.tencent.news.core.tads.constants.AdRefreshType
import com.tencent.news.core.tads.constants.AdType
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.tads.model.AdScene
import com.tencent.news.core.tads.model.IAdInsertGame
import com.tencent.news.core.tads.model.IKmmAdFeedsItem


open class AdFeedsRequest constructor(
    val refreshType: AdRefreshType = AdRefreshType.RESET,
    val brushNum: Int = 0,
    val adType: Int = AdType.TIMELINE,
    val env: AdFeedsEnv = AdFeedsEnv(),
) {
    var skipAdList: Boolean = false // 本次不请求广告数据（仅用于中插接口 requestInsertContent）
    var responseAdListJson: Boolean = false     // 是否返回广告原始json数据

    // 以下为 kmm 内部绑定的数据
    var adScene: AdScene = AdScene(AdLoid.NONE, "")

    var onBeforeRequest: (() -> Unit)? = null
}

// 这些字段最后都会装到 SlotEnvData 中

data class AdFeedsEnv(
    var isRecoveryMode: Boolean = false,

    var stayTimeOnPage: Long = 0,               // 当前页面停留时长，单位：ms（目前主要视频落地页用）
    var videoAutoPlay: Boolean = false,         // 视频是否是自动播放
    var isLandingPage: Boolean = false,         // 是否是视频落地页

    var isLocalChannel: Int = INVALID_NUM,      // 是否是地方站频道

    var feedsLaunchType: Int = INVALID_NUM,     // oneshot使用，信息流第一刷启动方式（2-冷启，1-热启）
    var firstView: Int = INVALID_NUM,           // 是否是今天首次请求：（1-是首次，0-不是）

    var articleFirstCategory: Int = INVALID_NUM,    // 文章一级分类（图文底层页 getSimpleNews下发）
    var articleSecondCategory: Int = INVALID_NUM,   // 文章二级分类（图文底层页 getSimpleNews下发）

    var sourceEntranceId: String? = "",         // 竖版场景请求广告时携带来源（tab2、沉浸式)

    var searchWord: String = "",                // 搜索词（投放定向使用）
) {
    var resetByTime: Boolean = false            // 是否30分钟自动reset

    // 仅外部拉起后的落地页广告请求允许透传 pre_trace_id；页面切换后新上下文默认 false
    var carryPreTraceId: Boolean = false

    var longVideoEnv: AdLongVideoEnv? = null    // 长视频相关

    var channelShowType: Int = INVALID_NUM      // 频道双列-固定样式（比如用于区分发现频道和要闻频道）
    var listLayoutType: Int = INVALID_NUM       // 频道布局类型 是双列还是单列

    var detailMidAdCount: Int? = 1              // loid=98，文章广告请求广告条数
    var skipHotBriefModule: Boolean = false     // 要闻首刷命中“跳过置顶+热点精选”时透传给广告侧

    // 【视频挂卡】当前视频在列表中的位置
    var companionCur: Int = INVALID_NUM
    // 【视频挂卡】列表中已有挂卡广告的视频位置集合，如 "1,3,5"
    var companionSeq: String = ""

}


data class AdLongVideoEnv(
    val title: String = "",
    val actors: String = "",
    val categories: String = "",
    val isVip: String = "",
    val vipStartDate: String = "",
    val vipEndDate: String = "",
    val vipLevel: String = "",
)

/**
 * 广告的网络请求参数（传给接入层用的）
 */

data class AdCgiParams(
    val request: AdFeedsRequest,

    val rtAd: String = "1",
    val adReqData: String,
    val feedbackCur: String = "",
    val feedbackNewsId: String = "",
) {
    fun isValid(): Boolean = adReqData.isNotNullOrBlank()

    fun toRequestParams(): Map<String, String> {
        return mapOf(
            "rtAd" to rtAd,
            "adReqData" to adReqData,
            "feedbackCur" to feedbackCur,
            "feedbackNewsId" to feedbackNewsId
        )
    }
}


interface IAdList {
    fun isOrderEmpty(): Boolean
    fun isCloseAllAd(): Boolean
}


data class AdListParseResult constructor(
    val adList: IAdList? = null,
    val adListJson: String? = null, // 业务场景可以按需保留原始json数据（AdFeedsRequest.responseAdListJson=true）
    val insertGame: IAdInsertGame? = null,
    val newAdItemList: List<IKmmAdFeedsItem>? = null,
    val error: Throwable? = null,
    val msg: String = "",
) {
    override fun toString(): String {
        if (error != null) {
            return "解析失败，error=${error}, msg=${msg}, adList=${adList}"
        }
        return "解析成功, $adList"
    }
}


data class AdInsertResult(
    val location: Int,
    val adItem: IKmmAdFeedsItem,                    // 广告item
    val feedsItemWithAd: IKmmFeedsItem? = null      // 绑了广告的原生内容
)
