package com.tencent.news.qncore.platform.api

import kotlin.test.Test
import kotlin.test.assertEquals

class QnCoreLocationScenesTest {
    @Test
    fun locationScenesKeepLegacyCodes() {
        assertEquals(1, QnCoreLocationScenes.Recommend.legacyCode)
        assertEquals(2, QnCoreLocationScenes.PublishComment.legacyCode)
        assertEquals(3, QnCoreLocationScenes.PublishWeibo.legacyCode)
        assertEquals(4, QnCoreLocationScenes.ChangeProfileLocal.legacyCode)
        assertEquals(5, QnCoreLocationScenes.PostWeather.legacyCode)
        assertEquals(6, QnCoreLocationScenes.AigcPostDetail.legacyCode)
        assertEquals(7, QnCoreLocationScenes.AigcAgentDetail.legacyCode)
    }
}
