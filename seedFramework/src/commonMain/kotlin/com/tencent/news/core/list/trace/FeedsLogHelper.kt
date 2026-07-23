package com.tencent.news.core.list.trace

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.getShiplySwitch

// 信息流日志辅助工具
internal object FeedsLogHelper {

    private val enableWidgetFileLog by lazy { getShiplySwitch("enable_widget_file_log") }

    fun printWidgetLog(logKey: String, newPageWidget: StructPageWidget) {

        NewsChannelLog.fileLog("解析到组件列表 ${newPageWidget.getAllWidgets().size}项：${newPageWidget}")

        if (enableWidgetFileLog) {
            NewsChannelLog.fileLog(logKey, newPageWidget.buildLogStr())
        } else {
            NewsChannelLog.verbose(logKey) { newPageWidget.buildLogStr() }
        }
    }

    private fun StructPageWidget.buildLogStr(): String {
        val log = StringBuilder()
        log.appendLine("====组件数据===start====")
        getAllWidgets().forEach {
            log.appendLine(it.toString())
        }
        log.appendLine("====组件数据====end=====")
        return log.toString()
    }

    fun buildItemLogStr(itemList: List<IKmmFeedsItem>): String {
        val log = StringBuilder()
        log.appendLine("====列表数据 ${itemList.size}项 start====")
        itemList.forEach {
            log.appendLine(it.toString())
        }
        log.appendLine("====列表数据====end=====")
        return log.toString()
    }

}