@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.page.model.StructWidgetEx.buildWidgetList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.BOTTOM_BAR)
open class BottomBarWidget : StructWidget(), IWidgetParent<BottomBarWidgetLayout> {

    // bottomBar上方区域
    var topBar: MutableList<StructWidget>? = null

    // bottomBar内部的按钮组
    var btnList: MutableList<StructWidget>? = null

    var ui: BottomBarWidgetUi? = null

    override fun getWidgetType() = StructWidgetType.BOTTOM_BAR

    override fun buildLayoutWidgets(layout: BottomBarWidgetLayout?) {
        layout ?: return
        btnList = buildWidgetList(layout.btn_list)
    }

    override fun getSubWidgets(): List<StructWidget>? {
        return safeList(btnList)
    }

    companion object {
        fun createBaseStyle(btnList: List<StructWidget>): BottomBarWidget {
            return BottomBarWidget().apply {
                this.ui = BottomBarWidgetUi().apply {
                    bar_style = BarStyle().apply {
                        style_id = "new_struct_bottom_bar_style"
                    }
                }

                this.btnList = btnList.toMutableList()
            }
        }
    }

}

fun BottomBarWidget.btnList(vararg buttons: StructWidget?) = apply {
    btnList = buttons.filterNotNull().toMutableList()
}

fun BottomBarWidget.topBar(vararg buttons: StructWidget?): BottomBarWidget {
    this.topBar = buttons.filterNotNull().toMutableList()
    return this
}

@Serializable
data class BottomBarWidgetLayout(
    var btn_list: List<StructWidgetRef>? = null
) : StructWidgetLayout()

@Serializable
class BottomBarWidgetUi : IKmmKeep {
    var bar_style: BarStyle? = null
    var hide: Boolean = false
}

@Serializable
class BarStyle : IKmmKeep {
    var style_id: String = ""
}