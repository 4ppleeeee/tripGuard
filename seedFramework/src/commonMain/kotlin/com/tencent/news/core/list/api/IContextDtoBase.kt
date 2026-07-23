package com.tencent.news.core.list.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.list.model.Divider
import com.tencent.news.core.list.model.Dividers
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import kotlinx.serialization.Transient


// 信息流框架用到的基础绑定信息
interface IContextDtoBase {
    var articleUUID: String

    // 从0开始，客户端记录。记录文章是第几刷下发下来的，reset时归0，后续上拉或下拉都会+1。默认值-1，非二级频道目前未绑定该值
    var articlePage: Int

    // 在当前这一刷中的位置，从1开始；客户端计算时机：置顶、排重、广告插入之后，模块展开之前（模块内的所有文章算一个位置）
    // todo【注意】产品要求这个位置中，广告【不计算】位置
    var articleRealPos: Int

    // 在整个数据列表中的位置，从1开始；客户端计算时机：置顶、排重、广告插入之后，模块展开之前（模块内的所有文章算一个位置）
    // todo【注意】产品要求这个位置中，广告【需要计算】位置
    var articleListPos: Int

    // 文章在当前模块中的位置，从1开始；
    var articleModulePos: Int

    var newsChannel: String         // 上报用的二级频道

    var ignorePos: Boolean          // 标识不参与Pos位置计算
    var ignoreExpose: Boolean       // 不上报曝光

    // 当前所在页面信息
    var pageArticleId: String
    var pageArticleType: String
    var realArticleType: String     // 数据解析时候，绑定的原始articleType，后面再修改articleType的话，这个也不变

    val jumpStartTime: Long
    var jumpStartScheme: String     // 拉起当前页面时的scheme

    var isFlexList: Boolean                 // kmm重构后的flex列表（debug信息里展示使用）

    @KmmInternalApi
    var tmpIsRefreshNewData: Boolean        // 新一刷拉的数据（内部使用的临时标记，外部不要调用）

    var pageBusinessType: String?           // 【专题】结构化页面业务类型
    var isSectionHeader: Boolean            // 【专题】1. 目录导航的定位，是以Header为锚点的
    var isSectionFooter: Boolean            // 【专题】2. 页面有皮肤时，会给Header、Footer卡片加圆角
    var sectionName: String                 // 【专题】模块分区标识（目录导航索引使用）

    // 标识需要从列表中抠出来，置顶到品字形顶部的文章；目前有该标识的文章，会在普通列表里被隐藏
    var moveToHeader: Boolean

    var nativeAd: IKmmAdFeedsItem?          // 【竖版视频】竞价原生广告
    var relateVideos: List<String>?

    var topDivider: Divider?
    var bottomDivider: Divider?
}

open class DefaultContextDto : IContextDtoBase {
    override var articleUUID: String = ""
    override var articlePage: Int = -1
    override var articleRealPos: Int = 0
    override var articleListPos: Int = 0
    override var articleModulePos: Int = 0
    override var newsChannel: String = ""
    override var ignorePos: Boolean = false
    override var ignoreExpose: Boolean = false
    override var pageArticleId: String = ""
    override var pageArticleType: String = ""
    override var realArticleType: String = ""
    override val jumpStartTime: Long = 0L
    override var jumpStartScheme: String = ""
    override var isFlexList: Boolean = false

    @KmmInternalApi
    override var tmpIsRefreshNewData: Boolean = true
    override var pageBusinessType: String? = null
    override var isSectionHeader: Boolean = false
    override var isSectionFooter: Boolean = false
    override var sectionName: String = ""
    override var moveToHeader: Boolean = false
    override var nativeAd: IKmmAdFeedsItem? = null
    override var relateVideos: List<String>? = null

    @Transient
    @kotlin.jvm.Transient
    override var topDivider: Divider? = Dividers.NONE

    @Transient
    @kotlin.jvm.Transient
    override var bottomDivider: Divider? = Dividers.DEF

}

interface IContextDtoHolder {
    var ctxDto: IContextDtoBase
}

interface IContextDtoBinding {
    fun getContextDtoBindingTargets(): List<IContextDtoHolder>?
}
