@file:Suppress("PropertyName", "VariableNaming", "ConstructorParameterNaming")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.page.model.StructWidgetEx.buildWidgetList
import com.tencent.news.core.tag.model.IKmmTagInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


object TitleBarShowType {
    const val SHOW_TYPE = 3
}

@Serializable
@SerialName(StructWidgetType.COMMON_TITLE_BAR)
open class CommonTitleBarWidget : StructWidget(), IWidgetParent<TitleBarWidgetLayout>, IKmmKeep {

    var leftBtns: MutableList<StructWidget>? = null
    var centerBtns: MutableList<StructWidget>? = null
    var actionBtns: MutableList<StructWidget>? = null

    @Serializable(TitleBarWidgetDataWrapperSerializer::class)
    var data: TitleBarWidgetData? = null

    var action: TitleBarWidgetAction? = null

    var ui: TitleBarWidgetUI = TitleBarWidgetUI()

    override fun getWidgetType() = StructWidgetType.COMMON_TITLE_BAR

    override fun buildLayoutWidgets(layout: TitleBarWidgetLayout?) {
        layout ?: return
        leftBtns = buildWidgetList(layout.left_btns)
        centerBtns = buildWidgetList(layout.center_btns)
        actionBtns = buildWidgetList(layout.action_btns)
    }

    override fun getSubWidgets(): List<StructWidget>? = safeList(leftBtns, centerBtns, actionBtns)

    companion object {
        fun createFixTopStyle(title: String = "") = CommonTitleBarWidget().apply {
            ui.isHideBottomLine = true
            ui.isBarIconDark = true
            ui.alwaysShowCenter = true

            if (title.isNotEmpty()) {
                centerBtns = mutableListOf(TitleBtnWidget.create(title))
            }
        }
    }

}

@Serializable
class TitleBarWidgetData : StructWidgetData(), IKmmKeep {
    var title: String = ""
    var tagInfo: IKmmTagInfo? = null
}

class TitleBarWidgetDataWrapperSerializer : DataWrapperSerializer<TitleBarWidgetData>(
    StructWidgetType.COMMON_TITLE_BAR, TitleBarWidgetData.serializer()
)

@Serializable
data class TitleBarWidgetLayout(
    var action_btns: List<StructWidgetRef>? = null,
    var left_btns: List<StructWidgetRef>? = null,
    var center_btns: List<StructWidgetRef>? = null
) : StructWidgetLayout(), IKmmKeep

@Serializable
class TitleBarWidgetAction : StructWidgetAction(), IKmmKeep {
    var collapseRatio: Double = 0.95
}

@Serializable
class TitleBarWidgetUI : IKmmKeep {
    var isBarIconDark: Boolean = false          // true：TitleBar图标默认用黑色图标（页面header为浅色、图标为黑色）
    var alwaysShowCenter: Boolean = false       // true：无论Header折叠展开，始终展示 center_btns
    var alwaysTransparentBg: Boolean = false    // true：背景始终透明
    var alwaysShowLeftBtns: Boolean = false     // true：无论Header折叠展开，始终展示 left_btns
    var forceLightTitleInTransparentBg: Boolean = false // true：透明背景下标题使用压图浅色
    var isHideBottomLine: Boolean = false       // true：隐藏底部分割线
    var hideBackBtn: Boolean = false            // true：隐藏返回按钮
    var backBtnIconSize: Float = 24f             // 返回按钮图标大小
    var backBtnFontWeight: Int = 400            // 返回按钮粗细
    var fixTitleBarAboveContent: Boolean = false    // true：TitleBar固定在页面上方，不随滑动变化
}
