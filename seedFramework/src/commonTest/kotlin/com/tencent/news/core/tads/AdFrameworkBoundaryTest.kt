package com.tencent.news.core.tads

import com.tencent.news.core.constants.INVALID_NUM
import com.tencent.news.core.tads.service.FrameworkAdServiceBridge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdFrameworkBoundaryTest {

    @Test
    fun `invalid number sentinel lives outside ad package`() {
        assertEquals(-1, INVALID_NUM)
    }

    @Test
    fun `ad service bridge is owned by ad package`() {
        assertTrue(FrameworkAdServiceBridge.impl.isCloseAd(INVALID_NUM, ""))
    }
}
