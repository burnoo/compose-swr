package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.utils.ComposeBaseTest
import dev.burnoo.compose.swr.utils.FailingFetcher
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
class ShowStateTest : ComposeBaseTest() {

    @Test
    fun showLoading() {
        setSWRContent()
        assertTextLoading()
    }

    @Test
    fun showSuccess() = runBlocking {
        setSWRContent()
        assertTextLoading()
        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun showError() = runBlocking {
        val failingFetcher = FailingFetcher()
        setSWRContent(fetcher = failingFetcher::fetch, config = { shouldRetryOnError = false })
        assertTextLoading()
        waitForIdle()
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