package com.tencent.kmm.demo

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetadataParserTest {

    @Test
    fun parseRetriesXhsLoginRedirectWithMobileUserAgent() {
        val noteUrl = "https://www.xiaohongshu.com/discovery/item/6a56469d00000000110167c8"
        val loginUrl = "https://www.xiaohongshu.com/login?redirectPath=${
            URLEncoder.encode(noteUrl, StandardCharsets.UTF_8.name())
        }"
        val requests = mutableListOf<Pair<String, String?>>()

        val metadata = MetadataParser.parse(noteUrl) { url ->
            FakeConnection(url) { connection ->
                requests += url.toString() to connection.requestHeaders["User-Agent"]
                if (requests.size == 1) {
                    FakeResponse(status = 302, location = loginUrl)
                } else {
                    FakeResponse(
                        status = 200,
                        body = """
                            <html>
                              <body>
                                <script>
                                  window.__INITIAL_STATE__={
                                    "noteData": {
                                      "data": {
                                        "noteData": {
                                          "noteId": "6a56469d00000000110167c8",
                                          "title": "上海周末 citywalk",
                                          "desc": "外滩、武康路和咖啡店路线。",
                                          "imageList": [
                                            {
                                              "urlDefault": "https://sns-webpic-qc.xhscdn.com/mobile-cover"
                                            }
                                          ]
                                        }
                                      }
                                    }
                                  }
                                </script>
                              </body>
                            </html>
                        """.trimIndent(),
                    )
                }
            }
        }

        assertEquals("上海周末 citywalk", metadata.title)
        assertEquals("外滩、武康路和咖啡店路线。", metadata.description)
        assertEquals("https://sns-webpic-qc.xhscdn.com/mobile-cover", metadata.imageUrl)
        assertEquals(noteUrl, requests[1].first)
        assertTrue(requests[1].second.orEmpty().contains("Mobile Safari"))
    }

    @Test
    fun parseMarksWafChallengePageAsPartialWarning() {
        val noteUrl = "https://www.mafengwo.cn/i/24886269.html?sys_ver="

        val metadata = MetadataParser.parse(noteUrl) { url ->
            FakeConnection(url) {
                FakeResponse(
                    status = 202,
                    body = """
                        <!DOCTYPE html><html>
                        <head>
                          <script src="/C2WF946J0/probe.js?v=vc1jasc"></script>
                        </head>
                        <body></body>
                        </html>
                    """.trimIndent(),
                    headers = mapOf("Set-Cookie" to "x-waf-captcha-referer=; Path=/; Max-Age=60;"),
                )
            }
        }

        assertEquals(noteUrl, metadata.resolvedUrl)
        assertEquals("平台返回 WAF/验证码探针页，当前 HTTP 解析器拿不到正文。", metadata.warning)
    }

    private data class FakeResponse(
        val status: Int,
        val location: String? = null,
        val body: String = "",
        val headers: Map<String, String> = emptyMap(),
    )

    private class FakeConnection(
        url: URL,
        private val responseFactory: (FakeConnection) -> FakeResponse,
    ) : HttpURLConnection(url) {
        val requestHeaders = linkedMapOf<String, String>()
        private val response by lazy { responseFactory(this) }

        override fun setRequestProperty(key: String, value: String) {
            requestHeaders[key] = value
        }

        override fun getResponseCode(): Int = response.status

        override fun getHeaderField(name: String?): String? =
            if (name.equals("Location", ignoreCase = true)) {
                response.location
            } else {
                response.headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
            }

        override fun getInputStream(): InputStream = response.body.byteInputStream()

        override fun disconnect() = Unit

        override fun usingProxy(): Boolean = false

        override fun connect() = Unit
    }
}
