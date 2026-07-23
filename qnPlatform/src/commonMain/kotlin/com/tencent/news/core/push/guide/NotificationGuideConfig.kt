package com.tencent.news.core.push.guide

class NotificationGuideConfig(
    override val title: String = "",
    override val content: String = ""
) : INotificationGuideConfig {
    override var confirmText: String = "开启通知"
    override var cancelText: String = "先不了"
    override var hideMask: Boolean = false
    override var onShow: (() -> Unit)? = null
    override var onHide: (() -> Unit)? = null
    override var onConfirm: (() -> Unit)? = null
    override var onCancel: (() -> Unit)? = null
}