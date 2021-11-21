package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.utils.ComposeBaseTest
import dev.burnoo.compose.swr.utils.StringFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshTest : ComposeBaseTest() {

    @Test
    fun refreshWithoutDeduping() = runBlocking {
        val instantFetcher = StringFetcher(delay = 0L)
        setSWRContent(fetcher = instantFetcher::fetch, config = {
            refreshInterval = 1000L
            dedupingInterval = 0L
        })
        assertTextRevalidated(1)

        advanceTimeBy(1000L)
        assertTextRevalidated(2)

        advanceTimeBy(1000L)
        assertTextRevalidated(3)

        advanceTimeBy(1000L)
        assertTextRevalidated(4)
    }

    @Test
    fun refreshWithDeduping() = runBlocking {
        val instantFetcher = StringFetcher(delay = 0L)
        setSWRContent(fetcher = instantFetcher::fetch, config = {
            refreshInterval = 1000L
            dedupingInterval = 2000L
        })
        assertTextRevalidated(1)

        advanceTimeBy(1000L)
        assertTextRevalidated(1)

        advanceTimeBy(1000L)
        assertTextRevalidated(1)

        advanceTimeBy(999L)
        assertTextRevalidated(1)

        advanceTimeBy(1L)
        assertTextRevalidated(2)
    }

    @Test
    fun doNotRefreshWhileFetching() = runBlocking {
        val stringFetcher = StringFetcher(delay = 2000L)
        setSWRContent(stringFetcher::fetch, config = {
            refreshInterval = 100L
            dedupingInterval = 0L
        })
        assertTextLoading()

        advanceTimeBy(2000L)
        assertTextRevalidated(1)
    }

    @Test
    fun doNotRefreshWhenShouldRefreshIsFalse() {
        setSWRContent(config = {
            shouldRefresh = { false }
            dedupingInterval = 0L
            refreshInterval = 500L
            scope = testCoroutineScope
        })
        assertTextLoading()

        advanceTimeBy(100L)
        assertTextRevalidated(1)

        advanceTimeBy(10_000L)
        assertTextRevalidated(1)
    }
}