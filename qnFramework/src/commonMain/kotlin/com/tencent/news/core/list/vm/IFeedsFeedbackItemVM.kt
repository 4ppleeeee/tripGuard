package com.tencent.news.core.list.vm

interface IFeedsFeedbackItemVM {
    val feedbackId: String      // 选项ID
    val feedbackText: String    // 选项文案

    fun onClick()               // 点击回调
}
