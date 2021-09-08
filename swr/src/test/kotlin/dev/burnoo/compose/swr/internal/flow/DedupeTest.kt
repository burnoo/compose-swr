package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.testable.now
import dev.burnoo.compose.swr.utils.BaseTest
import dev.burnoo.compose.swr.utils.TestNow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DedupeTest : BaseTest() {

    @Test
    fun `filter too frequent elements`() {
        var counter = 0
        var lastUsageTime = Instant.Companion.DISTANT_PAST
        flow {
            while (true) {
                delay(500L)
                emit(Unit)
            }
        }
            .dedupe(dedupingInterval = 1000L, getLastUsageTime = { lastUsageTime })
            .onEach {
                lastUsageTime = now()
                counter++
            }
            .launchIn(testCoroutineScope)

        assertEquals(0, counter)
        advanceTimeBy(500L)
        assertEquals(1, counter)
        advanceTimeBy(500L)
        assertEquals(1, counter)
        advanceTimeBy(500L)
        assertEquals(1, counter)
        advanceTimeBy(500L)
        assertEquals(2, counter)
        advanceTimeBy(500L)
        assertEquals(2, counter)
    }

    @Test
    fun `filter all elements if lastUsageTime is updated`() {
        var counter = 0
        var lastUsageTime = Instant.Companion.DISTANT_PAST
        flow {
            while (true) {
                delay(500L)
                emit(Unit)
            }
        }
            .dedupe(dedupingInterval = 1000L, getLastUsageTime = { lastUsageTime })
            .onEach {
                lastUsageTime = now()
                counter++
            }
            .launchIn(testCoroutineScope)

        assertEquals(0, counter)
        advanceTimeBy(250L)
        lastUsageTime = now()
        advanceTimeBy(250L)
        assertEquals(0, counter)
        advanceTimeBy(250L)
        lastUsageTime = now()
        advanceTimeBy(250L)
        assertEquals(0, counter)
        advanceTimeBy(250L)
        lastUsageTime = now()
        advanceTimeBy(250L)
        assertEquals(0, counter)
        advanceTimeBy(250L)
        lastUsageTime = now()
        advanceTimeBy(250L)
        assertEquals(0, counter)
        advanceTimeBy(250L)
        lastUsageTime = now()
        advanceTimeBy(250L)
        assertEquals(0, counter)
    }
}