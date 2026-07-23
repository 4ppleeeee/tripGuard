package com.tencent.news.core.platform.api

import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionSceneTest {
    @Test
    fun defaultLocationSceneKeepsLegacyRecommendSemantics() {
        assertEquals("location.recommend", PermissionScenes.DefaultLocation.id)
        assertEquals(1, PermissionScenes.DefaultLocation.legacyCode)
    }
}
