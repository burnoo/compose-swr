package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.testable.now
import dev.burnoo.compose.swr.utils.BaseTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshTest : BaseTest() {

    @Test
    fun `emit flow element every n milliseconds`() {
        var counter = 0
        var lastUsageTime = Instant.Companion.DISTANT_PAST
        flowOf(Unit)
            .refresh(refreshInterval = 500L, getLastUsageTime = { lastUsageTime })
            .onEach {
                lastUsageTime = now()
                counter++
            }
            .launchIn(testCoroutineScope)
        assertEquals(0, counter)
        advanceTimeBy(500L)
        assertEquals(1, counter)
        advanceTimeBy(500L)
        assertEquals(2, counter)
        advanceTimeBy(500L)
        assertEquals(3, counter)
    }

    @Test
    fun `wait if last time usage changed`() {
        var counter = 0
        var lastUsageTime = Instant.Companion.DISTANT_PAST
        flowOf(Unit)
            .refresh(refreshInterval = 500L, getLastUsageTime = { lastUsageTime })
            .onEach {
                lastUsageTime = now()
                counter++
            }
            .launchIn(testCoroutineScope)
        assertEquals(0, counter)
        advanceTimeBy(500L)
        assertEquals(1, counter)
        advanceTimeBy(100L)
        lastUsageTime = now()
        advanceTimeBy(400L)
        assertEquals(1, counter)
        advanceTimeBy(100L)
        assertEquals(2, counter)
    }
}