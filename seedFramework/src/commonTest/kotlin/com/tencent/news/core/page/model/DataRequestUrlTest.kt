package com.tencent.news.core.page.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataRequestUrlTest {

    @Test
    fun `build request url uses explicit default host when request has relative service`() {
        val request = DataRequest().apply {
            service = "/gw/page/channel"
        }

        val url = request.buildRequestUrl(
            channelInfo = null,
            defaultHost = "https://base.example.com/",
        )

        assertEquals("https://base.example.com/gw/page/channel", url)
    }

    @Test
    fun `build request url prefers request host over default host`() {
        val request = DataRequest().apply {
            host = "https://request.example.com/"
            service = "/gw/page/channel"
        }

        val url = request.buildRequestUrl(
            channelInfo = null,
            defaultHost = "https://base.example.com/",
        )

        assertEquals("https://request.example.com/gw/page/channel", url)
    }

    @Test
    fun `build request url keeps absolute service unchanged`() {
        val request = DataRequest().apply {
            service = "https://absolute.example.com/gw/page/channel"
        }

        val url = request.buildRequestUrl(
            channelInfo = null,
            defaultHost = "https://base.example.com/",
        )

        assertEquals("https://absolute.example.com/gw/page/channel", url)
    }

    @Test
    fun `build request url fails when relative service has no host`() {
        val request = DataRequest().apply {
            service = "/gw/page/channel"
        }

        assertFailsWith<IllegalStateException> {
            request.buildRequestUrl(
                channelInfo = null,
                defaultHost = "",
            )
        }
    }
}
