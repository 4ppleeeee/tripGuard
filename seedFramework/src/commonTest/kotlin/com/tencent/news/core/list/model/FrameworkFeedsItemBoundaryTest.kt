package com.tencent.news.core.list.model

import com.tencent.news.core.vm.IFeedsDtoItemStub
import kotlin.test.Test
import kotlin.test.assertSame

class FrameworkFeedsItemBoundaryTest {

    @Test
    fun `kmm feeds item combines framework item and business dto stub`() {
        val item = DefaultVMItem()

        val frameworkItem: IFrameworkFeedsItem = item
        val businessDtoStub: IFeedsDtoItemStub = item
        val kmmItem: IKmmFeedsItem = item

        assertSame(item, frameworkItem)
        assertSame(item, businessDtoStub)
        assertSame(item, kmmItem)
    }
}
