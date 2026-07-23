package com.tencent.news.core.push.guide

interface INotificationGuideConfig {
    val title: String
    val content: String
    val confirmText: String
    val cancelText: String
    var hideMask: Boolean   // 是否隐藏宿主guide原本已有的灰色背景
    val onShow: (() -> Unit)?
    val onHide: (() -> Unit)?
    val onConfirm: (() -> Unit)?
    val onCancel: (() -> Unit)?
}