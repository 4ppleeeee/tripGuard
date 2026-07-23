package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(StructWidgetType.SIMPLE_TITLE_BAR)
class SimpleTitleBarWidget : CommonTitleBarWidget(), IKmmKeep {

    override fun getWidgetType() = StructWidgetType.SIMPLE_TITLE_BAR

    companion object {
        /**
         * 创建简单的标题栏，只包含标题和返回按钮
         */
        fun create(
            title: String,
            hideBackBtn: Boolean = false,
            isBarIconDark: Boolean = true, // 这个貌似不好使
        ): SimpleTitleBarWidget {
            return SimpleTitleBarWidget().apply {
                // UI配置
                ui.isHideBottomLine = true
                ui.isBarIconDark = isBarIconDark
                ui.alwaysShowCenter = true
                ui.hideBackBtn = hideBackBtn

                // 左侧按钮：默认显示返回按钮
                leftBtns = mutableListOf()

                // 中间标题
                centerBtns = mutableListOf(TitleBtnWidget.create(title))

                // 右侧按钮：暂时为空
                actionBtns = mutableListOf()

                // 数据配置
                data = TitleBarWidgetData().apply {
                    this.title = title
                }
            }
        }
    }
}