package com.tencent.news.core.qncore

object QnCoreAppAlert {

    private var pushRemindDialogHandler: (type: String) -> Unit = {}

    fun checkShowPushRemindDialog(type: String) {
        pushRemindDialogHandler(type)
    }

    fun setPushRemindDialogHandler(handler: (type: String) -> Unit) {
        pushRemindDialogHandler = handler
    }

    fun resetPushRemindDialogHandler() {
        pushRemindDialogHandler = {}
    }
}
