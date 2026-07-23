package com.tencent.kmm.demo.page

import com.tencent.news.core.list.api.IStructDataLocalRepo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SeedMainTabPageWidgetTest {

    @Test
    fun createsPinStyleStructPageWidget() {
        val widget = createSeedMainTabPageWidget()

        assertNotNull(widget.titleBar)
        assertNotNull(widget.header)
        assertEquals("seed-main", widget.pageConfig.defaultChannelInfo.channelKey)
        assertTrue(widget.pageConfig.dataRepo is IStructDataLocalRepo)
        assertFalse(widget.pageConfig.fixTitleBarAboveContent)
        assertFalse(widget.pageConfig.fixChannelBarBelowTitleBar)
        assertFalse(widget.titleBar?.ui?.alwaysShowCenter == true)

        val channels = widget.pager?.channels.orEmpty()
        assertEquals(listOf("overview", "android", "ios"), channels.map { it.data?.channel_info?.channelKey })
        assertTrue(channels.all { it.empty?.widget_id?.startsWith("seed_pin_content_") == true })
    }
}
