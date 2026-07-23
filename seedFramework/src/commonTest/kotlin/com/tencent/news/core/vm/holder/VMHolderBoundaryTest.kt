package com.tencent.news.core.vm.holder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class VMHolderBoundaryTest {

    @Test
    fun `vm holder is framework level lazy holder`() {
        var createCount = 0
        val holder = VMHolder {
            createCount++
            Any()
        }

        val first = holder.createOrGet()
        val second = holder.createOrGet()

        assertSame(first, second)
        assertEquals(1, createCount)
    }
}
