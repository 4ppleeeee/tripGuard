package com.tencent.news.core.page.model

open class FixHeightImageBgWidget(
    val dayUrl: String,
    val nightUrl: String,
    val fixHeight: Int, // 高度单位dp
) : StructWidget() {
    override fun getWidgetType() = StructWidgetType.VM_WRAPPER
}