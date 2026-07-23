package com.tencent.news.core.page.model

// 没有实际UI内容，单纯用于撑开状态栏高度的占位TitleBar
class StatusBarSpacerTitleBarWidget : CommonTitleBarWidget() {
    init {
        ui.fixTitleBarAboveContent = true // 固定在页面顶部
    }
}