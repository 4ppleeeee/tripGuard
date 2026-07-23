@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 页码组件容器：承载多个 [IndexWidget]，本身不进入 feeds 列表（不实现 [IFeedsItemWidget]），
 * 仅作为结构化页面中的"页码控制组件"存在。
 *
 * 典型场景：合集分段加载（CollectionTagDataRepo 据 videoCount + pageSize 构建若干 IndexWidget），
 * 用户点击页码触发 [com.tencent.news.core.list.constants.ListRefreshAction.PAGE_INDEX]，
 * 由 FlexibleFeedsController 通过 [com.tencent.news.core.list.constants.ListRefreshActionData.indexKey]
 * 反查 IndexWidget 并发起 page_reset 请求。
 */
@Serializable
@SerialName(StructWidgetType.INDEXS)
class IndexsWidget : StructWidget(), IWidgetParent<IndexsWidgetLayout> {

    var data: IndexsWidgetData? = null

    var indexes: MutableList<IndexWidget> = mutableListOf()

    override fun getWidgetType() = StructWidgetType.INDEXS

    override fun buildLayoutWidgets(layout: IndexsWidgetLayout?) {
        // 静态 widget 列表，无需根据 layout 重建
    }

    override fun getSubWidgets(): List<StructWidget> = indexes
}

@Serializable
@SerialName(StructWidgetType.INDEX)
class IndexWidget : StructWidget() {

    var data: IndexWidgetData? = null

    var action: IndexWidgetAction? = null

    override fun getWidgetType() = StructWidgetType.INDEX
}

@Serializable
class IndexsWidgetData : StructWidgetData()

@Serializable
class IndexsWidgetLayout : StructWidgetLayout()

@Serializable
class IndexWidgetData : StructWidgetData() {
    /**
     * 1-based 页索引。
     *
     * 业务方（如 [com.tencent.news.core.collect.page.CollectionTagDataRepo]）按段构建 IndexWidget 时填入，
     * 用于业务侧把 widget_id ↔ 第几段 关联起来。UI 侧目前不直接消费该字段，
     * 段标题文字（如 "1-10/11-20"）和高亮态由 UI 自行根据合集总数与当前可见首条的 articlePos 计算。
     */
    var pageIndex: Int = 0
}

@Serializable
class IndexWidgetAction : StructWidgetAction() {
    /** 点击页码时发起的"重置该页"请求；由业务方填充 service / reqdata。 */
    var page_reset: DataRequestAction? = null
}
