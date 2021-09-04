package dev.burnoo.compose.swr

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.burnoo.compose.swr.domain.*
import dev.burnoo.compose.swr.domain.flow.exponentialBackoff
import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import dev.burnoo.compose.swr.model.config.SWRLocalConfigBlock
import dev.burnoo.compose.swr.model.config.plus
import dev.burnoo.compose.swr.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

private const val key = "k"

@ExperimentalCoroutinesApi
class UseSWRTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCoroutineScope = TestCoroutineScope()
    private val testNow = TestNow()

    private val stringFetcher = StringFetcher()

    @Before
    fun setUp() {
        now = testNow
        restartRandom()
        LocalCache = compositionLocalOf { DefaultCache() }
        LocalConfigBlocks.clear()
    }

    @Test
    fun showLoading() {
        setDefaultContent()
        assertTextLoading()
    }

    @Test
    fun showFallbackData() {
        setDefaultContent(config = {
            fallbackData = "${key}0"
        })
        assertTextRevalidated(0)
    }

    @Test
    fun enabledRevalidateIfStaleWithFallbackData() {
        setDefaultContent(config = {
            fallbackData = "${key}0"
            revalidateIfStale = true
        })
        assertTextRevalidated(0)

        advanceTimeBy(200L)
        assertTextRevalidated(1)
    }

    @Test
    fun revalidateOnMount() {
        setDefaultContent(config = {
            fallbackData = "${key}0"
            revalidateIfStale = false
            revalidateOnMount = true
        })
        assertTextRevalidated(0)

        advanceTimeBy(200L)
        assertTextRevalidated(1)
    }

    @Test
    fun doNotRevalidateOnMount() {
        setDefaultContent(config = {
            revalidateIfStale = true
            revalidateOnMount = false
        })
        assertTextLoading()

        advanceTimeBy(200L)
        assertTextLoading()
    }

    @Test
    fun showSuccess() = runBlockingTest {
        setDefaultContent()
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun showError() = runBlockingTest {
        val failingFetcher = FailingFetcher()
        setDefaultContent(fetcher = failingFetcher::fetch) { shouldRetryOnError = false }
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
    }

    @Test
    fun mutateRevalidate() = runBlockingTest {
        setMutationContent()
        assertTextLoading()

        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)

        clickMutate()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(2)

        clickMutate()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(3)
    }

    @Test
    fun mutateWithOnSuccessCallback() = runBlocking {
        val onSuccess = OnSuccess()
        setMutationContent(config = {
            this.onSuccess = onSuccess::invoke
        })
        assertTextLoading()
        clickMutate()
        testCoroutineScope.advanceUntilIdle()
        assertEquals(2, onSuccess.invocations.size)
    }

    @Test
    fun refresh() = runBlocking {
        val stringFetcher = StringFetcher(delay = 2000L)
        setDefaultContent(stringFetcher::fetch) {
            refreshInterval = 1000L
            dedupingInterval = 0L
        }
        assertTextLoading()

        advanceTimeBy(2000L)
        assertTextRevalidated(1)

        advanceTimeBy(2000L)
        assertTextRevalidated(2)

        advanceTimeBy(2000L)
        assertTextRevalidated(3)
    }

    @Test
    fun refreshWithDeduping() = runBlocking {
        setDefaultContent(config = {
            refreshInterval = 1000L
            dedupingInterval = 2000L
        })
        assertTextLoading()

        advanceTimeBy(200L)
        assertTextRevalidated(1)

        advanceTimeBy(1000L)
        assertTextRevalidated(1)

        advanceTimeBy(1100L)
        assertTextRevalidated(2)
    }

    @Test
    fun mutateRefreshWithDeduping() = runBlocking {
        setMutationContent(config = {
            refreshInterval = 2000L
            dedupingInterval = 1000L
            scope = testCoroutineScope
        })
        assertTextLoading()

        advanceTimeBy(100L)
        assertTextRevalidated(1)

        advanceTimeBy(1500L)
        clickMutate()
        advanceTimeBy(100L)
        assertTextRevalidated(2)

        advanceTimeBy(500L)
        assertTextRevalidated(2)

        advanceTimeBy(1500L)
        assertTextRevalidated(3)

        advanceTimeBy(1999L)
        assertTextRevalidated(3)
    }

    @Test
    fun mutateWithoutRevalidationRefresh() = runBlocking {
        setMutationContent(config = {
            refreshInterval = 2000L
            dedupingInterval = 1000L
            scope = testCoroutineScope
        }, mutationData = "${key}0", shouldRevalidate = false)
        assertTextLoading()
        advanceTimeBy(100L)

        repeat(10) {
            clickMutate()
            advanceTimeBy(999L)
            assertTextRevalidated(0)
        }
    }

    @Test
    fun retryFailingExponential() {
        val retryInterval = 3000L
        val failingFetcher = FailingFetcher()
        val delays = (1..3).map { attempt -> exponentialBackoff(retryInterval, attempt) }
        restartRandom()
        setDefaultContent(fetcher = failingFetcher::fetch) {
            shouldRetryOnError = true
            errorRetryInterval = retryInterval
            errorRetryCount = 3
        }
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(100)
        assertEquals(1, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(delays[0] - 100)
        assertEquals(1, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(200)
        assertEquals(2, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(delays[1] + 100)
        assertEquals(3, failingFetcher.failCount)
        assertTextFailure()

        testCoroutineScope.advanceTimeBy(delays[2] + 100)
        assertEquals(4, failingFetcher.failCount)
        assertTextFailure()
    }

    @Test
    fun onLoadingSlowTriggered() {
        val onLoadingSlow = OnLoadingSlow()
        val slowFetcher = StringFetcher(delay = 10_000L)
        val config: SWRConfigBlock<String, String> = {
            this.onLoadingSlow = onLoadingSlow::invoke
            loadingTimeout = 2000L
        }
        setDefaultContent(slowFetcher::fetch, config)
        assertTextLoading()
        assertEquals(0, onLoadingSlow.invocations.size)

        testCoroutineScope.advanceTimeBy(2000L)
        assertTextLoading()
        assertEquals(key, onLoadingSlow.invocations[0].key)
        assertEquals(2000L, onLoadingSlow.invocations[0].config.loadingTimeout)
    }

    @Test
    fun onLoadingSlowNotTriggered() {
        val onLoadingSlow = OnLoadingSlow()
        val normalFetcher = StringFetcher(delay = 1000L)
        val config: SWRConfigBlock<String, String> = {
            this.onLoadingSlow = onLoadingSlow::invoke
            loadingTimeout = 2000L
        }
        setDefaultContent(normalFetcher::fetch, config)
        assertTextLoading()
        assertEquals(0, onLoadingSlow.invocations.size)

        testCoroutineScope.advanceTimeBy(2000L)
        assertTextRevalidated(1)
        assertEquals(0, onLoadingSlow.invocations.size)
    }

    @Test
    fun onSuccess() {
        val onSuccess = OnSuccess()
        setDefaultContent(config = {
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
    fun onError() {
        val failingFetcher = FailingFetcher()
        val onError = OnError()
        setDefaultContent(fetcher = failingFetcher::fetch) {
            this.onError = onError::invoke
            errorRetryCount = 1
            errorRetryInterval = 2000L
        }
        assertTextLoading()
        assertEquals(0, onError.invocations.size)

        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
        assertEquals(key, onError.invocations[0].key)
        assertEquals(failingFetcher.exception, onError.invocations[0].error)
        assertEquals(onError::invoke, onError.invocations[0].config.onError)
    }

    @Test
    fun isPausedTest() = runBlocking {
        setDefaultContent(config = {
            isPaused = { true }
            dedupingInterval = 0L
            refreshInterval = 500L
        })
        assertTextLoading()

        advanceTimeBy(100000L)
        assertTextLoading()
    }

    @Test
    fun onErrorRetry() = runBlocking {
        val failingFetcher = FailingFetcher()
        setDefaultContent(fetcher = failingFetcher::fetch) {
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
        }
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

    @Test
    fun isValidatingTest() = runBlocking {
        val errorDelay = 3000L
        val failingFetcher = FailingFetcher()
        var recompositionCount = 0
        composeTestRule.setContent {
            val (_, _, isValidating) = useSWR(
                key,
                fetcher = { failingFetcher.fetch(it) },
                config = {
                    onErrorRetry = { _, _, _, _ ->
                        delay(errorDelay)
                        true
                    }
                    scope = testCoroutineScope
                })
            recompositionCount++
            Text(text = isValidating.toString())
        }
        assertText("true")
        advanceTimeBy(100L)
        assertText("false")
        advanceTimeBy(errorDelay)
        assertText("true")
        advanceTimeBy(100L)
        assertText("false")
        assertEquals(4, recompositionCount)
    }

    @Test
    fun fetcherOverriding() {
        val fetcher = stringFetcher::fetch
        val onSuccess = OnSuccess()
        setDefaultContent(config = {
            this.onSuccess = onSuccess::invoke
            this.fetcher = FailingFetcher()::fetch
        }, fetcher = fetcher)
        advanceTimeBy(100L)
        assertEquals(fetcher, onSuccess.invocations.first().config.fetcher)
    }

    @Test
    fun fetcherFromConfigSuccess() {
        composeTestRule.setContent {
            val (data, error) = useSWR<String, String>(
                key = key,
                config = {
                    scope = testCoroutineScope
                    fetcher = stringFetcher::fetch
                })
            when {
                error != null -> Text("Failure")
                data != null -> Text(data)
                else -> Text("Loading")
            }
        }
        advanceTimeBy(100L)
        assertTextRevalidated(1)
    }

    @Test
    fun showSuccessFromLocal() = runBlockingTest {
        composeTestRule.setContent {
            val config: SWRLocalConfigBlock<String> = {
                fetcher = { stringFetcher.fetch(it) }
                scope = testCoroutineScope
            }
            SWRConfigProvider(value = config) {
                val (data, error) = useSWR<String, String>(key = key)
                when {
                    error != null -> Text("Failure")
                    data != null -> Text(data)
                    else -> Text("Loading")
                }
            }
        }
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun nestedConfigProvider() = runBlockingTest {
        composeTestRule.setContent {
            val parentConfig: SWRLocalConfigBlock<String> = {
                fetcher = { stringFetcher.fetch(it) }
            }
            val childConfig: SWRLocalConfigBlock<String> = {
                scope = testCoroutineScope
            }
            SWRConfigProvider(value = parentConfig) {
                SWRConfigProvider(value = childConfig) {
                    val (data, error) = useSWR<String, String>(key = key)
                    when {
                        error != null -> Text("Failure")
                        data != null -> Text(data)
                        else -> Text("Loading")
                    }
                }
            }
        }
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
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
            SWRConfigProvider(value = parentConfig) {
                val (_, _, config) = useSWRConfig<String, String>()
                Text(text = config.refreshInterval.toString())
            }
        }
        assertText("123")
    }

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

        assertEquals("${key}1", cache.get<String, String>(key))
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

        assertEquals(setOf<Any>(key), cache.keys())
    }

    @Test
    fun useFallbackMap() {
        composeTestRule.setContent {
            SWRConfigProvider<String>(value = {
                fetcher = { stringFetcher.fetch(it) }
                revalidateOnMount = false
                fallback = mapOf(
                    key to "${key}0"
                )
            }) {
                val (data, error) = useSWR<String, String>(key)
                when {
                    error != null -> Text("Failure")
                    data != null -> Text(data)
                    else -> Text("Loading")
                }
            }
        }
        assertTextRevalidated(0)
    }

    private fun setDefaultContent(
        fetcher: suspend (String) -> String = { stringFetcher.fetch(it) },
        config: SWRConfigBlock<String, String> = {}
    ) {
        composeTestRule.setContent {
            val (data, error) = useSWR(
                key = key,
                fetcher = fetcher,
                config = config + { scope = testCoroutineScope })
            when {
                error != null -> Text("Failure")
                data != null -> Text(data)
                else -> Text("Loading")
            }
        }
    }

    private fun setMutationContent(
        fetcher: suspend (String) -> String = { stringFetcher.fetch(it) },
        config: SWRConfigBlock<String, String> = {},
        mutationData: String? = null,
        shouldRevalidate: Boolean = true
    ) {
        composeTestRule.setContent {
            val (data, error, _, mutate) = useSWR(
                key = key,
                fetcher = fetcher,
                config = config + { scope = testCoroutineScope })
            when {
                error != null -> Text("Failure")
                data != null -> Text(data)
                else -> Text("Loading")
            }
            Button(onClick = {
                testCoroutineScope.launch { mutate(mutationData, shouldRevalidate) }
            }) {
                Text("Mutate")
            }
        }
    }

    private fun clickMutate() {
        composeTestRule.onNodeWithText("Mutate").performClick()
    }

    private fun assertTextRevalidated(count: Int) {
        assertText("$key$count")
    }

    private fun assertTextLoading() {
        assertText("Loading")
    }

    private fun assertTextFailure() {
        assertText("Failure")
    }

    private fun assertText(text: String) {
        composeTestRule.onNode(isRoot()).onChildAt(0).assertTextEquals(text)
    }

    private fun advanceTimeBy(durationMillis: Long) {
        testNow.advanceTimeBy(durationMillis)
        testCoroutineScope.advanceTimeBy(durationMillis)
    }

    private fun restartRandom() {
        random = Random(0)
    }
}