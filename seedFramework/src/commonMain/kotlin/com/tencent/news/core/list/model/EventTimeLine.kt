package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

/**
 * 事件脉络里面其中一个时间节点
 * Created by hengyangji on 2017/8/7.
 */
@Serializable
class EventTimeLine : BaseExposureKmmModel(), IKmmKeep {
    var time: String? = null
    var timeStamp: String? = null
    var desc: String? = null
    var forceText: Boolean = false // 客户端自用，强制显示text样式
    var articleId: String? = null
    var atype: String? = null
    var linkDesc: String? = null
    var item: QnKmmFeedsItem? = null
    var isFirstItem: Boolean = false // 客户端使用
    var isFoldLastItem: Boolean = false // 折叠状态下最后一个cell
    var isLastItem: Boolean = false
    var rec_icon: String? = null
    var rec_night_icon: String? = null
    val is_top_event: String? = null
    var style: String? = null // 从EventTimeLineModule的style复制而来， 本地字段
    var tabId: String? = "" // 属于那个tab下的脉络
    var isForcusItem = false // 是否是焦点item
    // 客户端字段：是否是"点击未读气泡/状态卡后刚到达的新节点"，用于 time 文本高亮
    var isNewlyArrived = false
    // 客户端字段：外层是否挂了 hangingView（脉络页：tabs 数量 >= 2 时有 ChannelBar）
    // 用于首个 cell 的 top padding 计算：有 hanging=10dp、无 hanging=14dp（均大于默认 7dp）
    var hasOuterHanging = false
    // 客户端字段：隐藏当前节点上方短竖线，但不使用首节点大间距布局
    var hideTopLine = false
    // 客户端字段：StatusCell 是否展示“展示摘要”开关
    var hasAbstractToggle = false
    // 客户端字段：摘要内容是否展开（文字摘要/图片摘要共用）
    var isSummaryExpanded = false

    /** 节点类型原始字段：1=人工总结/精编节点，0=普通脉络节点；保持与 JSON key 一致，兼容 Gson 路径 */
    var eventline_node_type: Int = 0

    /** 是否隐藏标签原始字段：0展示，1不展示；保持与 JSON key 一致，兼容 Gson 路径 */
    var not_show: Int = 0

    /** Tag 文案原始字段（精编等标签）；保持与 JSON key 一致，兼容 Gson 路径 */
    var tag_name: String = ""

    /** 节点标签文案原始字段；保持与 JSON key 一致，兼容 Gson 路径 */
    var node_mark: String = ""

    /** 人工摘要原始字段；保持与 JSON key 一致，兼容 Gson 路径 */
    var manual_summary: ManualSummary? = null

    /** 对外保持 camelCase 访问入口 */
    var eventlineNodeType: Int
        get() = eventline_node_type
        set(value) {
            eventline_node_type = value
        }

    var notShow: Int
        get() = not_show
        set(value) {
            not_show = value
        }

    var tagName: String
        get() = tag_name
        set(value) {
            tag_name = value
        }

    var nodeMark: String
        get() = node_mark
        set(value) {
            node_mark = value
        }

    var manualSummary: ManualSummary?
        get() = manual_summary
        set(value) {
            manual_summary = value
        }

    val isAi: Boolean
        get() = "ai_thing_trace" == style

    val isTop: Boolean
        get() = "1" == is_top_event

    override fun getExposureKey() = "EventTimeLine"
}
