package com.tencent.kmm.demo

import kotlin.test.Test
import kotlin.test.assertEquals

class XhsShareParserTest {

    @Test
    fun extractNoteIdSupportsExploreUrls() {
        val noteId = XhsShareParser.extractNoteId(
            "https://www.xiaohongshu.com/explore/66abc?xsec_token=token"
        )

        assertEquals("66abc", noteId)
    }

    @Test
    fun loginRedirectExtractsOriginalNoteUrl() {
        val redirect = "https://www.xiaohongshu.com/login?redirectPath=https%3A%2F%2Fwww.xiaohongshu.com%2Fdiscovery%2Fitem%2F66abc%3Fxsec_token%3Dtoken"

        assertEquals(true, XhsShareParser.isXhsLoginRedirect(redirect))
        assertEquals(
            "https://www.xiaohongshu.com/discovery/item/66abc?xsec_token=token",
            XhsShareParser.normalizeResolvedUrl(redirect),
        )
    }

    @Test
    fun selectDescriptionKeepsParsedXhsDescriptionBeforeFallback() {
        val resolvedUrl = "https://www.xiaohongshu.com/discovery/item/66abc"

        assertEquals(
            "外滩、武康路和咖啡店路线。",
            XhsShareParser.selectDescription(
                platform = SourcePlatform.XIAOHONGSHU,
                parsedDescription = "外滩、武康路和咖啡店路线。",
                rawText = "上海周末 citywalk http://xhslink.cn/o/test",
                resolvedUrl = resolvedUrl,
            ),
        )
    }
}
