package com.tencent.news.core.list.controller

import com.tencent.news.core.page.model.NewsListWidget
import com.tencent.news.core.page.model.StructWidgetEx.findSingleWidget

internal object FlexCtrlWidgetHelper {

    // 触发底部分页
    fun FlexCtrl.findAutoLoadListWidget(): NewsListWidget? =
        rootWidget.getMainContentListWidgets().firstOrNull {
            it.canAutoLoadMore()
        }

    // 触发顶部分页
    fun FlexCtrl.findTopAutoLoadListWidget(): NewsListWidget? =
        rootWidget.getMainContentListWidgets().firstOrNull {
            it.canAutoTopMore()
        }

    // 触发顶部刷新
    fun FlexCtrl.findTopRefreshListWidget(): NewsListWidget? =
        rootWidget.getMainContentListWidgets().firstOrNull {
            it.canTopRefresh()
        }

    // 页面最后一个列表widget（如果有无限刷的case，load_more应该都在他身上）
    fun FlexCtrl.findLastNewsListWidget(): NewsListWidget? =
        rootWidget.getMainContentListWidgets().lastOrNull()

    // 点击footer展开
    fun FlexCtrl.findClickLoadListWidget(sectionName: String?): NewsListWidget? =
        rootWidget.findSingleWidget<NewsListWidget> {
            (it as? NewsListWidget)?.data?.section?.name == sectionName
        }

}
