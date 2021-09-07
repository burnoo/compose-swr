package dev.burnoo.compose.swr

import androidx.compose.material.Text
import dev.burnoo.compose.swr.domain.DefaultCache
import dev.burnoo.compose.swr.utils.BaseTest
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomCacheTest : BaseTest() {

    @Test
    fun clearCustomCache() {
        val cache = DefaultCache()
        composeTestRule.setContent {
            SWRConfigProvider<String>(value = {
                provider = { cache }
                scope = testCoroutineScope
            }) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = { stringFetcher.fetch(it) }
                )
                when {
                    error != null -> Text("Failure")
                    data != null -> Text(data)
                    else -> Text("Loading")
                }
            }
        }
        assertTextLoading()

        advanceTimeBy(100L)
        assertTextRevalidated(1)

        cache.clear()
        assertTextLoading()
    }

    @Test
    fun getDataFromCustomCache() {
        val cache = DefaultCache()
        composeTestRule.setContent {
            SWRConfigProvider<String>(value = {
                provider = { cache }
                scope = testCoroutineScope
            }) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = { stringFetcher.fetch(it) }
                )
                when {
                    error != null -> Text("Failure")
                    data != null -> Text(data)
                    else -> Text("Loading")
                }
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
            SWRConfigProvider<String>(value = {
                provider = { cache }
                scope = testCoroutineScope
            }) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = { stringFetcher.fetch(it) }
                )
                when {
                    error != null -> Text("Failure")
                    data != null -> Text(data)
                    else -> Text("Loading")
                }
            }
        }
        assertTextLoading()

        Assert.assertEquals(setOf<Any>(key), cache.keys())
    }
}