package com.tencent.news.core.page.model

// 【通用】渐变背景组件（从上到下线性渐变）
open class GradientBgWidget(
    val dayStartColor: String,   // 日间模式 - 渐变起始色值（如 "#FF3377FF"）
    val dayEndColor: String,     // 日间模式 - 渐变结束色值
    val nightStartColor: String = dayStartColor,    // 夜间模式 - 渐变起始色值
    val nightEndColor: String = dayEndColor,        // 夜间模式 - 渐变结束色值
    val fixHeight: Int,          // 高度单位dp
) : StructWidget() {
    override fun getWidgetType() = StructWidgetType.VM_WRAPPER
}
