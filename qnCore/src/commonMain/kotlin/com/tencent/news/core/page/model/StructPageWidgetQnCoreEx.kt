package com.tencent.news.core.page.model

import com.tencent.news.core.page.model.StructWidgetEx.findSingleWidget

// 优先取 pageStruct 的，空的再用发布按钮上带下来的
fun StructPageWidget.getPublishCommentIdWithInputBtn(): String =
    (data?.comment_id ?: "").ifEmpty {
        findSingleWidget<InputBtnWidget>()?.data?.comment_id ?: ""
    }
