package com.tencent.news.core.qncore

import kotlin.test.Test
import kotlin.test.assertEquals

class QnCoreAppAlertTest {

    @Test
    fun qnCoreOwnsPushRemindDialogEntry() {
        val types = mutableListOf<String>()
        QnCoreAppAlert.setPushRemindDialogHandler { type ->
            types += type
        }
        try {
            QnCoreAppAlert.checkShowPushRemindDialog(type = "news_push")

            assertEquals(listOf("news_push"), types)
        } finally {
            QnCoreAppAlert.resetPushRemindDialogHandler()
        }
    }
}
