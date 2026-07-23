package com.tencent.news.core.list.controller

import com.tencent.news.core.list.api.IListPagingRecorder
import com.tencent.news.core.list.api.IListRefreshData
import com.tencent.news.core.platform.getCurTimeMillis

internal class ListPagingRecorder : IListPagingRecorder {

    override var listTransParam = ""
        private set
    var timestamp = 0L
        private set

    fun clear() {
        listTransParam = ""
        timestamp = 0
    }

    fun record(listData: IListRefreshData) {
        val transParam = listData.getListTransParam()
        // 这个判空保护很重要：如果接入层拉取上游数据错误，可能导致分页字段返空，此时不能刷新，否则会导致后续分页异常
        if (!transParam.isNullOrBlank() && transParam != "{}") {
            listTransParam = transParam
        }

        var refreshTime = listData.getRefreshTimestamp()
        if (refreshTime <= 0) {
            refreshTime = getCurTimeMillis() / 1000
        }
        timestamp = refreshTime
    }

}