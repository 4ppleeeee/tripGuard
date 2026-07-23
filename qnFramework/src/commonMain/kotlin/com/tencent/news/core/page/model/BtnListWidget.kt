package com.tencent.news.core.page.model

// 一排按钮（支持 横着 或 竖着）
class BtnListWidget(
    val btnList: List<StructWidget>? = null,
    val ui: BtnListWidgetUI = BtnListWidgetUI(),
    val align: Align = Align.BOTTOM_END,
) : StructWidget(), IWidgetParent<BtnListLayout> {

    override fun getWidgetType() = StructWidgetType.BTN_LIST

    override fun buildLayoutWidgets(layout: BtnListLayout?) {
    }

    override fun getSubWidgets(): List<StructWidget>? = btnList

    enum class Align {
        BOTTOM_CENTER, BOTTOM_START, BOTTOM_END
    }
}

class BtnListLayout : StructWidgetLayout()

data class BtnListWidgetUI(
    val isVertical: Boolean = true,
    val space: Int = 15, // 按钮间距，单位dp
)