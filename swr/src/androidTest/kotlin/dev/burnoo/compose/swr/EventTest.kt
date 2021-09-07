package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import dev.burnoo.compose.swr.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventTest : BaseTest() {
    
    @Test
    fun triggerOnLoadingSlowEvent() {
        val onLoadingSlow = OnLoadingSlow()
        val slowFetcher = StringFetcher(delay = 10_000L)
        setSWRContent(slowFetcher::fetch, config = {
            this.onLoadingSlow = onLoadingSlow::invoke
            loadingTimeout = 2000L
        })
        assertTextLoading()
        assertEquals(0, onLoadingSlow.invocations.size)

        testCoroutineScope.advanceTimeBy(2000L)
        assertTextLoading()
        assertEquals(key, onLoadingSlow.invocations[0].key)
        assertEquals(2000L, onLoadingSlow.invocations[0].config.loadingTimeout)
    }

    @Test
    fun triggerOnLoadingSlowNotEvent() {
        val onLoadingSlow = OnLoadingSlow()
        val normalFetcher = StringFetcher(delay = 1000L)
        val config: SWRConfigBlock<String, String> = {
            this.onLoadingSlow = onLoadingSlow::invoke
            loadingTimeout = 2000L
        }
        setSWRContent(normalFetcher::fetch, config)
        assertTextLoading()
        assertEquals(0, onLoadingSlow.invocations.size)

        testCoroutineScope.advanceTimeBy(2000L)
        assertTextRevalidated(1)
        assertEquals(0, onLoadingSlow.invocations.size)
    }

    @Test
    fun triggerOnSuccessEvent() {
        val onSuccess = OnSuccess()
        setSWRContent(config = {
            this.onSuccess = onSuccess::invoke
        })
        assertTextLoading()
        assertEquals(0, onSuccess.invocations.size)

        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
        assertEquals(key, onSuccess.invocations[0].key)
        assertEquals("${key}1", onSuccess.invocations[0].data)
        assertEquals(onSuccess::invoke, onSuccess.invocations[0].config.onSuccess)
    }

    @Test
    fun triggerOnErrorEvent() {
        val failingFetcher = FailingFetcher()
        val onError = OnError()
        setSWRContent(fetcher = failingFetcher::fetch, config = {
            this.onError = onError::invoke
            errorRetryCount = 1
            errorRetryInterval = 2000L
        })
        assertTextLoading()
        assertEquals(0, onError.invocations.size)

        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
        assertEquals(key, onError.invocations[0].key)
        assertEquals(failingFetcher.exception, onError.invocations[0].error)
        assertEquals(onError::invoke, onError.invocations[0].config.onError)
    }
}