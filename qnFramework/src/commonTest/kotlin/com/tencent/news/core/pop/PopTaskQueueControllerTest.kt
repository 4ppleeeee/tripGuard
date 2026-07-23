package com.tencent.news.core.pop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PopTaskQueueControllerTest {

    @Test
    fun sameKeyEntryReplacesOlderPendingEntry() {
        val handledEntries = mutableListOf<TestEntry>()
        val queue = createQueue(
            nowProvider = { 0L },
            onReady = {
                handledEntries += it
                PopTaskQueueDrainDecision.CONTINUE
            }
        )
        val first = TestEntry(key = "super_mask", priority = 1, order = 0, readyTime = 0L)
        val latest = TestEntry(key = "super_mask", priority = 2, order = 1, readyTime = 0L)

        assertTrue(queue.enqueue(first).enqueued)
        val replaceResult = queue.enqueue(latest)

        assertTrue(replaceResult.enqueued)
        assertEquals(first, replaceResult.replaced)
        assertEquals(1, queue.pendingCount())

        queue.drainReady()

        assertEquals(listOf(latest), handledEntries)
        assertEquals(0, queue.pendingCount())
    }

    @Test
    fun readyEntryIsDequeuedBeforeFutureHighPriorityEntry() {
        var now = 0L
        var scheduledDelay: Long? = null
        val handledEntries = mutableListOf<TestEntry>()
        val queue = createQueue(
            nowProvider = { now },
            scheduler = { delayMs, _ -> scheduledDelay = delayMs },
            onReady = {
                handledEntries += it
                PopTaskQueueDrainDecision.CONTINUE
            }
        )
        val futureHigh = TestEntry(key = "future_high", priority = 100, order = 0, readyTime = 100L)
        val readyLow = TestEntry(key = "ready_low", priority = 1, order = 1, readyTime = 0L)
        val readyMid = TestEntry(key = "ready_mid", priority = 10, order = 2, readyTime = 0L)

        queue.enqueue(futureHigh)
        queue.enqueue(readyLow)
        queue.enqueue(readyMid)

        queue.drainReady()

        assertEquals(listOf(readyMid, readyLow), handledEntries)
        assertEquals(1, queue.pendingCount())
        assertEquals(100L, scheduledDelay)

        now = 100L
        queue.drainReady()

        assertEquals(listOf(readyMid, readyLow, futureHigh), handledEntries)
        assertEquals(0, queue.pendingCount())
    }

    @Test
    fun disposedQueueRejectsNewEntriesAndReturnsPendingEntries() {
        val queue = createQueue(
            nowProvider = { 0L },
            onReady = { PopTaskQueueDrainDecision.CONTINUE }
        )
        val pending = TestEntry(key = "pending", priority = 1, order = 0, readyTime = null)
        val afterDispose = TestEntry(key = "after_dispose", priority = 1, order = 1, readyTime = 0L)

        assertTrue(queue.enqueue(pending).enqueued)
        val disposedEntries = queue.dispose()
        val enqueueAfterDispose = queue.enqueue(afterDispose)

        assertEquals(listOf(pending), disposedEntries)
        assertTrue(queue.isDisposed())
        assertFalse(enqueueAfterDispose.enqueued)
        assertEquals(0, queue.pendingCount())
    }

    private fun createQueue(
        nowProvider: () -> Long,
        scheduler: (delayMs: Long, action: () -> Unit) -> Unit = { _, _ -> },
        onReady: (TestEntry) -> PopTaskQueueDrainDecision
    ): PopTaskQueueController<TestEntry> {
        return PopTaskQueueController(
            context = PopTaskQueueContext(
                name = "test_queue",
                nowProvider = nowProvider,
                scheduler = scheduler
            ),
            config = PopTaskQueueConfig(
                keyProvider = { it.key },
                readyTimeProvider = { entry, _ -> entry.readyTime },
                readyComparator = compareByDescending<TestEntry> { it.priority }
                    .thenBy { it.order },
                onReady = onReady
            )
        )
    }

    private data class TestEntry(
        val key: String,
        val priority: Int,
        val order: Int,
        val readyTime: Long?
    )
}
