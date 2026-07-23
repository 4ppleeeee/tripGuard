package com.tencent.news.core.list.controller

import kotlin.test.Test
import kotlin.test.assertFalse

class FlexibleFeedsControllerEmptyResultTest {

    @Test
    fun `empty page with hasMore should not be treated as error by default`() {
        assertFalse(LIST_ERROR_FOR_HAS_MORE_EMPTY_DEFAULT)
    }
}
