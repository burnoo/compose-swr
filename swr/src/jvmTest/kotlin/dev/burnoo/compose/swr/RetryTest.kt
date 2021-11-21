package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.retry.exponentialBackoff
import dev.burnoo.compose.swr.utils.ComposeBaseTest
import dev.burnoo.compose.swr.utils.FailingFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RetryTest : ComposeBaseTest() {

    @Test
    fun retryDefaultExponentialBackoff() {
        val retryInterval = 3000L
        val failingInstantFetcher = FailingFetcher(delay = 0L)
        val delays = (1L..3L).map { attempt -> exponentialBackoff(retryInterval, attempt) }
        restartRandom()
        setSWRContent(fetcher = failingInstantFetcher::fetch, config = {
            shouldRetryOnError = true
            errorRetryInterval = retryInterval
            errorRetryCount = 3
        })
        assertEquals(1, failingInstantFetcher.failCount)
        assertTextFailure()

        advanceTimeBy(delays[0] - 1L)
        assertEquals(1, failingInstantFetcher.failCount)
        assertTextFailure()

        advanceTimeBy(1L)
        assertEquals(2, failingInstantFetcher.failCount)
        assertTextFailure()

        advanceTimeBy(delays[1])
        assertEquals(3, failingInstantFetcher.failCount)
        assertTextFailure()

        advanceTimeBy(delays[2])
        assertEquals(4, failingInstantFetcher.failCount)
        assertTextFailure()
    }

    @Test
    fun retryCustomOnErrorRetry() = runBlocking {
        val failingFetcher = FailingFetcher()
        setSWRContent(fetcher = failingFetcher::fetch, config = {
            errorRetryInterval = 3000L
            errorRetryCount = 3
            onErrorRetry = { _, _, config, attempt ->
                if (config.shouldRetryOnError && config.errorRetryCount.let { it == null || attempt <= it }) {
                    delay(config.errorRetryInterval)
                    true
                } else {
                    false
                }
            }
        })
        assertTextLoading()

        advanceTimeBy(100L)
        assertEquals(1, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(2900)
        assertEquals(1, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(200)
        assertEquals(2, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(3100)
        assertEquals(3, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(3100)
        assertEquals(4, failingFetcher.failCount)
        assertTextFailure()
    }
}