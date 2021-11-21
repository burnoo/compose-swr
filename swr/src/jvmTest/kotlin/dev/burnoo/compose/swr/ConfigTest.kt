package dev.burnoo.compose.swr

import androidx.compose.material.Text
import dev.burnoo.compose.swr.config.SWRLocalConfigBlock
import dev.burnoo.compose.swr.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigTest : ComposeBaseTest() {

    @Test
    fun useFetcherFromParameterOverrideConfig() {
        val fetcher = stringFetcher::fetch
        val onSuccess = OnSuccess()
        setSWRContent(fetcher = fetcher, config = {
            this.onSuccess = onSuccess::invoke
            this.fetcher = FailingFetcher()::fetch
        })
        advanceTimeBy(100L)
        Assert.assertEquals(fetcher, onSuccess.invocations.first().config.fetcher)
    }

    @Test
    fun useFetcherFromConfig() {
        composeTestRule.setContent {
            val (data, error) = useSWR<String, String>(
                key = key,
                config = {
                    scope = testCoroutineScope
                    fetcher = stringFetcher::fetch
                })
            DataErrorLoading(data, error)
        }
        advanceTimeBy(100L)
        assertTextRevalidated(1)
    }

    @Test
    fun useFetcherFromLocalConfig() = runBlocking {
        composeTestRule.setContent {
            val config: SWRLocalConfigBlock<String> = {
                fetcher = { stringFetcher.fetch(it) }
                scope = testCoroutineScope
            }
            SWRConfigProvider(config = config) {
                val (data, error) = useSWR<String, String>(key = key)
                DataErrorLoading(data, error)
            }
        }
        assertTextLoading()
        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun useFallbackFromLocalConfig() {
        composeTestRule.setContent {
            SWRConfigProvider<String>(config = {
                fetcher = { stringFetcher.fetch(it) }
                revalidateOnMount = false
                fallback = mapOf(
                    key to "${key}0"
                )
            }) {
                val (data, error) = useSWR<String, String>(key)
                DataErrorLoading(data, error)
            }
        }
        assertTextRevalidated(0)
    }

    @Test
    fun joinConfigsWithNestedConfigProviders() = runBlocking {
        composeTestRule.setContent {
            val parentConfig: SWRLocalConfigBlock<String> = {
                fetcher = { stringFetcher.fetch(it) }
            }
            val childConfig: SWRLocalConfigBlock<String> = {
                scope = testCoroutineScope
            }
            SWRConfigProvider(config = parentConfig) {
                SWRConfigProvider(config = childConfig) {
                    val (data, error) = useSWR<String, String>(key = key)
                    DataErrorLoading(data, error)
                }
            }
        }
        assertTextLoading()
        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun useSWRConfig() = runBlockingTest {
        composeTestRule.setContent {
            val parentConfig: SWRLocalConfigBlock<String> = {
                fetcher = { stringFetcher.fetch(it) }
                scope = testCoroutineScope
                refreshInterval = 123L
            }
            SWRConfigProvider(config = parentConfig) {
                val (_, _, config) = useSWRConfig<String, String>()
                Text(text = config.refreshInterval.toString())
            }
        }
        assertText("123")
    }
}