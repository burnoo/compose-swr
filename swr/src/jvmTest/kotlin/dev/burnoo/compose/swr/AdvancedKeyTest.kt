package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.utils.AndroidBaseTest
import dev.burnoo.compose.swr.utils.DataErrorLoading
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdvancedKeyTest : AndroidBaseTest() {

    @Test
    fun getKeySuspend() {
        composeTestRule.setContent {
            SWRConfigProvider<String>(value = { scope = testCoroutineScope }) {
                val (fetchedKey) = useSWR("key1", { _ ->
                    delay(1000L)
                    key
                })
                val (data, error) = useSWR(key = fetchedKey, fetcher = stringFetcher::fetch)
                DataErrorLoading(data, error)
            }
        }
        assertTextLoading()

        advanceTimeBy(1000L)
        assertTextLoading()

        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun getKeyThrowable() {
        composeTestRule.setContent {
            SWRConfigProvider<String>(value = {
                scope = testCoroutineScope
            }) {
                val (fetchedKey) = useSWR("key1", { _ ->
                    delay(1000L)
                    key
                })
                val (data, error) = useSWR(
                    getKey = { fetchedKey!! },
                    fetcher = stringFetcher::fetch
                )
                DataErrorLoading(data, error = error)
            }
        }
        assertTextLoading()

        advanceTimeBy(1000L)
        assertTextLoading()

        waitForIdle()
        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun getKeyThrowableLocalFetcher() {
        composeTestRule.setContent {
            SWRConfigProvider<String>(value = {
                fetcher = stringFetcher::fetch
                scope = testCoroutineScope
            }) {
                val (fetchedKey) = useSWR("key1", { _ ->
                    delay(1000L)
                    key
                })
                val (data, error) = useSWR<String, String>(getKey = { fetchedKey!! })
                DataErrorLoading(data, error = error)
            }
        }
        assertTextLoading()

        advanceTimeBy(1000L)
        assertTextLoading()

        waitForIdle()
        assertTextRevalidated(1)
    }
}