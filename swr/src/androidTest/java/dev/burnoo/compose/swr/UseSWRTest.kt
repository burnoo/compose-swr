package dev.burnoo.compose.swr

import androidx.compose.material.Text
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
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
        assertText("Loading")
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
        testCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun mutate() = runBlockingTest {
        setContent()
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
        })
        testCoroutineScope.advanceTimeBy(200L)
        assertTextRevalidated(1)

        testCoroutineScope.advanceTimeBy(700L)
        assertTextRevalidated(1)

        testCoroutineScope.advanceTimeBy(700L)
        assertTextRevalidated(1)

        testNow.advanceTimeBy(4000L) // To skip deduping
        testCoroutineScope.advanceTimeBy(700L)
        assertTextRevalidated(2)
    }

    @Test
    fun retryFailing() {
        val failingFetcher = FailingFetcher()
        setContent(config = {
            errorRetryInterval = 3000
            errorRetryCount = 3
        }, fetcher = failingFetcher::fetch)
        assertText("Loading")

        testCoroutineScope.advanceTimeBy(100)
        assertEquals(1, failingFetcher.failCount)
        assertText("Loading")

        testCoroutineScope.advanceTimeBy(2000)
        assertEquals(1, failingFetcher.failCount)
        assertText("Loading")

        testCoroutineScope.advanceTimeBy(1100)
        assertEquals(2, failingFetcher.failCount)
        assertText("Loading")

        testCoroutineScope.advanceTimeBy(3100)
        assertEquals(3, failingFetcher.failCount)
        assertText("Loading")

        testCoroutineScope.advanceTimeBy(3100)
        assertEquals(4, failingFetcher.failCount)
        assertText("Failure")
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

        assertEquals(0, onLoadingSlow.invocations.size)

        testCoroutineScope.advanceTimeBy(2000L)
        assertEquals(key, onLoadingSlow.invocations[0].key)
        assertEquals(2000L, onLoadingSlow.invocations[0].config.loadingTimeout)
    }

    @Test
    fun onSuccess() {
        val onSuccess = OnSuccess()
        setContent(config = {
            this.onSuccess = onSuccess::invoke
        })

        assertEquals(0, onSuccess.invocations.size)

        testCoroutineScope.advanceUntilIdle()
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

        assertEquals(0, onError.invocations.size)

        testCoroutineScope.advanceUntilIdle()
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

    private fun assertText(text: String) {
        composeTestRule.onNode(isRoot()).onChildAt(0).assertTextEquals(text)
    }
}

