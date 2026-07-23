package com.tencent.news.core.page.model

import com.tencent.news.core.page.extension.StructPageWidgetEx.businessType

private typealias BizType = StructPageBusinessType

object StructPageBusinessTypeEx {

    fun StructPageWidget?.isVideoTopicPage(): Boolean = businessType == BizType.VIDEO_TOPIC
    fun StructPageWidget?.isTopicPage(): Boolean = businessType == BizType.TOPIC
    fun StructPageWidget?.isIpPage(): Boolean = businessType == BizType.IP
    fun StructPageWidget?.isQaPage(): Boolean = businessType == BizType.QA
    fun StructPageWidget?.isCommentPage(): Boolean = businessType == BizType.COMMENT
}
