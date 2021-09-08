package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.utils.AndroidBaseTest
import dev.burnoo.compose.swr.utils.FailingFetcher
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class ShowStateTest : AndroidBaseTest() {

    @Test
    fun showLoading() {
        setSWRContent()
        assertTextLoading()
    }

    @Test
    fun showSuccess() = runBlockingTest {
        setSWRContent()
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun showError() = runBlockingTest {
        val failingFetcher = FailingFetcher()
        setSWRContent(fetcher = failingFetcher::fetch, config = { shouldRetryOnError = false })
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
    }

    @Test
    fun showFallbackData() {
        setSWRContent(config = {
            fallbackData = "${key}0"
        })
        assertTextRevalidated(0)
    }
}