@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.safeList
import com.tencent.news.core.page.model.StructWidgetEx.buildWidgetList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
abstract class HeaderWidget(
    @Transient
    open var headerList: MutableList<StructWidget>? = null,
) : StructWidget(), IWidgetParent<HeaderWidgetLayout> {

    // header初始化后就折叠起来
    var initCollapse: Boolean? = null

    override fun buildLayoutWidgets(layout: HeaderWidgetLayout?) {
        layout ?: return
        headerList = buildWidgetList(layout.header_list)
    }

    override fun getSubWidgets(): List<StructWidget>? {
        return safeList(headerList)
    }

    open fun widgetHeaderVM(): IWidgetHeaderVM? {
        return null
    }
}

interface IWidgetHeaderVM {}

@Serializable
open class HeaderWidgetData : StructWidgetData() {
    var title: String = ""
    var desc: String = ""
    var bg_image: StructImage? = null
}

@Serializable
data class HeaderWidgetLayout(
    var header_list: List<StructWidgetRef>? = null
) : StructWidgetLayout()

@Serializable
class HeaderWidgetAction : StructWidgetAction() {

}