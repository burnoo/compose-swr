package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.testable.now
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
class DedupeTest {

    private val testNow = TestNow()
    private val testCoroutineScope = TestCoroutineScope()

    @Before
    fun setUp() {
        now = testNow
    }

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
                lastUsageTime = testNow()
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

    private fun advanceTimeBy(durationMillis: Long) {
        testNow.advanceTimeBy(durationMillis)
        testCoroutineScope.advanceTimeBy(durationMillis)
    }
}