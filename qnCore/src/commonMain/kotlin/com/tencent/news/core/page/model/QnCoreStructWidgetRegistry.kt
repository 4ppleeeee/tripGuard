package com.tencent.news.core.page.model

import kotlinx.serialization.modules.subclass

object QnCoreStructWidgetRegistry {

    private var registered = false

    fun registerBusinessWidgets() {
        if (registered) {
            return
        }
        registered = true
        StructWidgetSerializerRegistry.register {
            subclass(AudioRadioVerticalPagerWidget::class)
            subclass(IpBottomBarWidget::class)
            subclass(ColumnHeaderWidget::class)
            subclass(PublishBtnWidget::class)
            subclass(CommentBtnWidget::class)
            subclass(ColumnPayBtnWidget::class)
            subclass(ColumnGiftBtnWidget::class)
            subclass(IpShareBtnWidget::class)
            subclass(AskBtnWidget::class)
            subclass(AudioBtnWidget::class)
            subclass(FavoriteBtnWidget::class)
            subclass(FocusBtnWidget::class)
            subclass(EmojiBtnWidget::class)
            subclass(InputBtnWidget::class)
            subclass(HotSpotBtnWidget::class)
        }
    }
}
