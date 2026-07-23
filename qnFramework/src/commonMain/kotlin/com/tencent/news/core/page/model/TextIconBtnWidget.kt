package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.dt.constants.DtElementId
import kotlinx.coroutines.flow.StateFlow

/**
 * 带文字的图标按钮 Widget
 * 支持响应头部折叠状态
 */
class TextIconBtnWidget(
    val viewModel: ITextIconBtnVM
) : StructBtnWidget<StructBtnWidgetData>() {

    override val data: StructBtnWidgetData? = null
    override val asWidgetVM: ITextIconBtnVM get() = viewModel

    override fun getWidgetType() = StructWidgetType.VM_WRAPPER
}

interface ITextIconBtnVM : StructWidgetViewModel {
    val displayText: String // 显示的文本
    val iconFont: IconFont // 图标
    val size: StructSize // 图标大小

    val showBackground: Boolean // 是否显示底托背景
    val backgroundCornerRadius: Float // 底托圆角大小（dp）
    val respondToHeaderCollapse: Boolean // 是否响应头部折叠状态（true=折叠时只显示图标，false=始终显示文字+图标）

    val hasRedDot: StateFlow<Boolean> // 是否显示红点
    val contentDescription: String? // 无障碍描述
    val dtElementId: DtElementId? get() = null // 点击埋点元素 ID
    val dtElementParams: Map<String, Any>? get() = null // 埋点元素参数
    val scheme: String // 跳转链接

    fun onClick() // 点击事件
    fun hideRedDot() // 隐藏红点

    // 根据折叠状态计算是否应该显示文字
    fun shouldShowText(isHeaderCollapsed: Boolean): Boolean {
        return if (respondToHeaderCollapse) {
            !isHeaderCollapsed
        } else {
            true
        }
    }

    // 根据折叠状态计算是否应该显示底托
    fun shouldShowBackground(isHeaderCollapsed: Boolean): Boolean {
        return if (respondToHeaderCollapse) {
            showBackground && !isHeaderCollapsed
        } else {
            showBackground
        }
    }
}
