package com.tencent.kmm.demo.home

import kotlin.test.Test
import kotlin.test.assertEquals

class DemoHomePageWidgetTest {

    @Test
    fun homeProvidesExactlyTwoTestEntries() {
        assertEquals(
            listOf(DemoRoutes.MAIN_TAB, DemoRoutes.PLATFORM_CAPABILITIES),
            buildDemoEntries().map { it.pageName },
        )
    }
}
