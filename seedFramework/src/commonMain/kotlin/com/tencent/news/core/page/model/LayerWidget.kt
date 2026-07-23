@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import com.tencent.news.core.extension.safeList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.LAYERS)
open class LayersWidget(
    var fullScreen: MutableList<StructWidget>? = null,
    private val showInSubPage: Boolean = false, // 是否在嵌套的子tab里显示
    /**
     * 永远展示在子tab内嵌容器里的浮层（不受 showInSubPage 影响）。
     * 用于父页面层（showInSubPage=false）但需要跟随子tab定位的浮层（如吸顶悬浮条）。
     * 默认 null，对所有现有调用方零影响。
     */
    var subPageOnly: MutableList<StructWidget>? = null,
) : StructWidget(), IWidgetParent<LayersWidgetLayout> {

    override fun getWidgetType() = StructWidgetType.LAYERS

    override fun buildLayoutWidgets(layout: LayersWidgetLayout?) {
        layout ?: return
    }

    override fun getSubWidgets(): List<StructWidget>? {
        return safeList(fullScreen) + safeList(subPageOnly)
    }

    // 可展示在整个页面上的浮层挂件（不会随着pager左右滑动）
    // （由于有些子tab的widget生成不太规范，作为默认tab时可能会加到父页面行，因此这里要准确过滤下 showInSubPage）
    fun getMainPageWidgets(): List<StructWidget>? {
        return if (!showInSubPage) {
            fullScreen
        } else {
            null
        }
    }

    // 可展示在子tab里的浮层挂件（会随着pager左右滑动）
    fun getSubPageWidgets(): List<StructWidget>? {
        val main = if (showInSubPage) fullScreen.orEmpty() else emptyList()
        val onlySub = subPageOnly.orEmpty()
        if (main.isEmpty() && onlySub.isEmpty()) return null
        return main + onlySub
    }

    companion object {
        fun buildFullScreen(vararg widgets: StructWidget) = LayersWidget(
            fullScreen = widgets.toMutableList()
        )

        fun buildOneBtn(
            btnWidget: StructWidget,
            align: BtnListWidget.Align = BtnListWidget.Align.BOTTOM_END,
            showInSubPage: Boolean = false
        ) = buildBtnList(
            btnList = listOf(btnWidget),
            isVertical = false,
            align = align,
            showInSubPage = showInSubPage
        )

        fun buildBtnList(
            btnList: List<StructWidget>,
            isVertical: Boolean = true,
            align: BtnListWidget.Align = BtnListWidget.Align.BOTTOM_END,
            showInSubPage: Boolean = false
        ) = LayersWidget(
            fullScreen = mutableListOf(
                BtnListWidget(
                    btnList = btnList,
                    ui = BtnListWidgetUI(
                        isVertical = isVertical
                    ),
                    align = align
                )
            ),
            showInSubPage = showInSubPage
        )

    }

}

@Serializable
class LayersWidgetLayout : StructWidgetLayout()
