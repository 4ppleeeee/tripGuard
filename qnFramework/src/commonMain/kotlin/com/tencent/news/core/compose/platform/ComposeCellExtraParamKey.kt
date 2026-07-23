package com.tencent.news.core.compose.platform

/**
 * ITEM_CELL 运行时扩展参数 key。
 *
 * 这类参数由宿主写入 [StructCellArgs.extraPageData]，用于表达 Native/Hippy 容器实例级上下文。
 * 结构化扩展内容建议用 JSON 字符串承载，避免后续频繁扩展主构造字段。
 */
object ComposeCellExtraParamKey {

    const val RUNTIME_TOKEN = "composeCellRuntimeToken"
    const val REPORT_PARAMS = "composeCellReportParams"
    const val ELEMENT_ID = "composeCellElementId"
    const val ELEMENT_PARAMS = "composeCellElementParams"
}
