package com.tencent.news.core.page.model

import kotlinx.serialization.Serializable


@Serializable
class DefaultStructWidget : StructWidget() {

    var widget_type: String = ""

    init {
//        throw RuntimeException("未注册的组件 type=${widget_type}")
    }

    override fun getWidgetType(): String {
        return widget_type
    }

}