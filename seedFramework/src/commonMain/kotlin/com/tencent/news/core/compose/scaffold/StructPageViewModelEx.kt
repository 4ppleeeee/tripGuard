package com.tencent.news.core.compose.scaffold

import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.StructWidget
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug

fun StructWidget?.findStructPageVM(): IStructPageViewModel? {
    val result = this?.findStructPageWidget2()?.asWidgetVM
    if (result == null && isDebug()) {
        val msg = "【警告】findStructPageVM 为空，请检查逻辑"
        debugToast(msg)
        NewsChannelLog.error("Widget", msg, Throwable()) // 打印下堆栈，方便查找调用方
    }
    return result
}

tailrec fun IStructPageViewModel?.findRootPageVM(): IStructPageViewModel? {
    this ?: return null
    val parentRootWidget = pageRootWidget.parentRootWidget ?: return this
    val parentPageVM = parentRootWidget.findStructPageVM() ?: return this
    return parentPageVM.findRootPageVM()
}
