package dev.burnoo.compose.swr.internal

import dev.burnoo.compose.swr.utils.BaseTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WithOnLoadingSlowTest : BaseTest() {

    @Test
    fun `trigger onLoadingSlow`() = runBlocking {
        var onLoadingSlowTriggered = false
        testScope.launch {
            withOnLoadingSlow(
                timeoutMillis = 1000L,
                onLoadingSlow = { onLoadingSlowTriggered = true },
                function = { delay(2000L) }
            )
        }
        testScope.advanceUntilIdle()
        assertTrue(onLoadingSlowTriggered)
    }

    @Test
    fun `do not trigger onLoadingSlow`() = runBlocking {
        var onLoadingSlowTriggered = false
        testScope.launch {
            withOnLoadingSlow(
                timeoutMillis = 1000L,
                onLoadingSlow = { onLoadingSlowTriggered = true },
                function = { delay(500L) }
            )
        }
        testScope.advanceUntilIdle()
        assertFalse(onLoadingSlowTriggered)
    }
}