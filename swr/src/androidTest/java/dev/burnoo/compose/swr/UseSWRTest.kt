package dev.burnoo.compose.swr

import androidx.compose.material.Text
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import dev.burnoo.compose.swr.di.KoinContext
import dev.burnoo.compose.swr.domain.flow.exponentialBackoff
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.utils.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
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
        KoinContext.koinApp = testKoinApplication(testCoroutineScope, testNow)
    }

    @Test
    fun showLoading() {
        setContent()
        assertTextLoading()
    }

    @Test
    fun initialData() {
        setContent(config = {
            initialData = "${key}0"
        })
        assertTextRevalidated(0)
    }

    @Test
    fun defaultRevalidateOnMount() {
        setContent(config = {
            initialData = "${key}0"
        })
        assertTextRevalidated(0)

        advanceTimeBy(10_000L)
        assertTextRevalidated(0)
    }

    @Test
    fun enabledRevalidateOnMount() {
        setContent(config = {
            initialData = "${key}0"
            revalidateOnMount = true
        })
        assertTextRevalidated(0)

        advanceTimeBy(200L)
        assertTextRevalidated(1)
    }

    @Test
    fun showSuccess() = runBlockingTest {
        setContent()
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun showError() = runBlockingTest {
        val failingFetcher = FailingFetcher()
        setContent(fetcher = failingFetcher::fetch, config = { shouldRetryOnError = false })
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
    }

    @Test
    fun mutateTest() = runBlockingTest {
        setContent()
        assertTextLoading()

        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)

        testCoroutineScope.launch { mutate("k") }
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(2)

        testCoroutineScope.launch { mutate("k") }
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(3)
    }

    @Test
    fun refresh() = runBlocking {
        val stringFetcher = StringFetcher(delay = 2000L)
        setContent(config = {
            refreshInterval = 1000L
            dedupingInterval = 0L
        }, stringFetcher::fetch)
        assertTextLoading()

        advanceTimeBy(2000L)
        assertTextRevalidated(1)

        advanceTimeBy(2000L)
        assertTextRevalidated(2)

        advanceTimeBy(2000L)
        assertTextRevalidated(3)
    }

    @Test
    fun refreshDeduping() = runBlocking {
        setContent(config = {
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
    fun mutateRefreshDeduping() = runBlocking {
        setContent(config = {
            refreshInterval = 2000L
            dedupingInterval = 1000L
        })
        assertTextLoading()

        advanceTimeBy(100L)
        assertTextRevalidated(1)

        advanceTimeBy(1500L)
        mutate(key)
        assertTextRevalidated(2)

        advanceTimeBy(500L)
        assertTextRevalidated(2)

        advanceTimeBy(1999L)
        assertTextRevalidated(2)

        advanceTimeBy(1L)
        assertTextRevalidated(3)
    }

    @Test
    fun mutateWithoutRevalidationRefresh() = runBlocking {
        setContent(config = {
            refreshInterval = 2000L
            dedupingInterval = 1000L
        })
        assertTextLoading()
        advanceTimeBy(100L)

        repeat(10) {
            mutate(key, data = "${key}0", shouldRevalidate = false)
            advanceTimeBy(999L)
            assertTextRevalidated(0)
        }
    }

    @Test
    fun retryFailingExponential() {
        val retryInterval = 3000L
        val random = Random(0)
        val delays = (1..3).map { attempt ->
            exponentialBackoff(retryInterval, attempt) { random.nextDouble() }
        }
        val failingFetcher = FailingFetcher()
        setContent(config = {
            shouldRetryOnError = true
            errorRetryInterval = retryInterval
            errorRetryCount = 3
        }, fetcher = failingFetcher::fetch)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(100)
        assertEquals(1, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(delays[0] - 100)
        assertEquals(1, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(200)
        assertEquals(2, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(delays[1] + 100)
        assertEquals(3, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(delays[2] + 100)
        assertEquals(4, failingFetcher.failCount)
        assertTextFailure()
    }

    @Test
    fun onLoadingSlow() {
        val onLoadingSlow = OnLoadingSlow()
        val slowFetcher = StringFetcher(delay = 10_000L)
        val config: SWRConfig<String, String>.() -> Unit = {
            this.onLoadingSlow = onLoadingSlow::invoke
            loadingTimeout = 2000L
        }
        setContent(config, slowFetcher::fetch)
        assertTextLoading()
        assertEquals(0, onLoadingSlow.invocations.size)

        testCoroutineScope.advanceTimeBy(2000L)
        assertTextLoading()
        assertEquals(key, onLoadingSlow.invocations[0].key)
        assertEquals(2000L, onLoadingSlow.invocations[0].config.loadingTimeout)
    }

    @Test
    fun onSuccess() {
        val onSuccess = OnSuccess()
        setContent(config = {
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
        setContent(config = {
            this.onError = onError::invoke
            errorRetryCount = 1
            errorRetryInterval = 2000L
        }, fetcher = failingFetcher::fetch)
        assertTextLoading()
        assertEquals(0, onError.invocations.size)

        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
        assertEquals(key, onError.invocations[0].key)
        assertEquals(failingFetcher.exception, onError.invocations[0].error)
        assertEquals(onError::invoke, onError.invocations[0].config.onError)
    }

    private fun setContent(
        config: SWRConfig<String, String>.() -> Unit = {},
        fetcher: suspend (String) -> String = { stringFetcher.fetch(it) }
    ) {
        composeTestRule.setContent {
            val resultState = useSWR(key = key, fetcher = fetcher, config = config)
            when (val result = resultState.value) {
                is SWRState.Loading -> Text("Loading")
                is SWRState.Success -> Text(result.data)
                is SWRState.Error -> Text("Failure")
            }
        }
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
}

