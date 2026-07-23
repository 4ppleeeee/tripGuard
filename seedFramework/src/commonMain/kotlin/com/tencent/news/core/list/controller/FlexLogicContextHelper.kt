package com.tencent.news.core.list.controller

import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.view.createBreakCircleRef

internal object FlexLogicContextHelper {

    fun bindPageWidget(targets: List<IListItem>, pageWidget: StructPageWidget2) {
        targets.forEach {
            it.bindingContext {
                // ListItem对象实际是由宿主创建的，在iOS上要破掉这个循环引用，否则影响垃圾回收；
                // 安卓没这个问题
                this.pageWidget = createBreakCircleRef(pageWidget)
            }
        }
    }

}