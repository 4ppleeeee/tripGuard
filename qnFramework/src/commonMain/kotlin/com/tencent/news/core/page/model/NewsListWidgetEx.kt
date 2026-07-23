package com.tencent.news.core.page.model

import com.tencent.news.core.list.model.IListItem

object NewsListWidgetEx {

    // 横滑时间轴组件
    fun NewsListWidget?.isTimeSliderComponent(): Boolean =
        this?.data?.section?.component == SectionComponentType.TIME_SLIDER

    fun NewsListWidget.appendNewDataForTimeSliderComponent(newFeedsItemList: List<IListItem>) {
        // 后台重新下发了全量moduleItem，整体直接覆盖
        this.data?.newslist = newFeedsItemList.toMutableList()
    }

}