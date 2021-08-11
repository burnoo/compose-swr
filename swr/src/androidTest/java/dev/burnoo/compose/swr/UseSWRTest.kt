package dev.burnoo.compose.swr

import androidx.compose.material.Text
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import dev.burnoo.compose.swr.di.KoinContext
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRResult
import dev.burnoo.compose.swr.utils.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    fun showSuccess() = runBlockingTest {
        setContent()
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun showError() = runBlockingTest {
        val failingFetcher = FailingFetcher()
        setContent(fetcher = failingFetcher::fetch)
        assertTextLoading()
        testCoroutineScope.advanceUntilIdle()
        assertTextFailure()
    }

    @Test
    fun mutate() = runBlockingTest {
        setContent()
        assertTextLoading()

        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)

        mutate("k")
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(2)

        mutate("k")
        testCoroutineScope.advanceUntilIdle()
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

        advanceTimeBy(1000L)
        assertTextRevalidated(2)
    }

    @Test
    fun retryFailing() {
        val failingFetcher = FailingFetcher()
        setContent(config = {
            errorRetryInterval = 3000
            errorRetryCount = 3
        }, fetcher = failingFetcher::fetch)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(100)
        assertEquals(1, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(2000)
        assertEquals(1, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(1100)
        assertEquals(2, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(3100)
        assertEquals(3, failingFetcher.failCount)
        assertTextLoading()

        testCoroutineScope.advanceTimeBy(3100)
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
        assertEquals(2, onError.invocations.size)
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
                is SWRResult.Loading -> Text("Loading")
                is SWRResult.Success -> Text(result.data)
                is SWRResult.Error -> Text("Failure")
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

