package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.cache.DefaultCache
import dev.burnoo.compose.swr.utils.*
import dev.burnoo.compose.swr.utils.DataErrorLoading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomCacheTest : ComposeBaseTest() {

    @Test
    fun clearCustomCache() {
        val cache = DefaultCache()
        composeTestRule.setContent {
            SWRConfigProvider<String>(config = {
                provider = { cache }
                scope = testScope
            }) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = { stringFetcher.fetch(it) }
                )
                DataErrorLoading(data, error)
            }
        }
        assertTextLoading()

        advanceTimeBy(100L)
        assertTextRevalidated(1)

        cache.clear()
        composeTestRule.waitForIdle()
        assertTextLoading()
    }

    @Test
    fun getDataFromCustomCache() {
        val cache = DefaultCache()
        composeTestRule.setContent {
            SWRConfigProvider<String>(config = {
                provider = { cache }
                scope = testScope
            }) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = { stringFetcher.fetch(it) }
                )
                DataErrorLoading(data, error)
            }
        }
        assertTextLoading()

        advanceTimeBy(100L)
        assertTextRevalidated(1)

        Assert.assertEquals("${key}1", cache.get<String, String>(key))
    }

    @Test
    fun getKeysFromCustomCache() {
        val cache = DefaultCache()
        composeTestRule.setContent {
            SWRConfigProvider<String>(config = {
                provider = { cache }
                scope = testScope
            }) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = { stringFetcher.fetch(it) }
                )
                DataErrorLoading(data, error)
            }
        }
        assertTextLoading()

        Assert.assertEquals(setOf<Any>(key), cache.keys())
    }
}