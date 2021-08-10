package dev.burnoo.compose.swr

import androidx.compose.material.Text
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
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

    private val refresherCoroutineScope = TestCoroutineScope()
    private val revalidatorCoroutineScope = TestCoroutineScope()
    private val recomposeCoroutineScope = TestCoroutineScope()
    private val testNow = TestNow()

    private val stringFetcher = StringFetcher()

    @Before
    fun setUp() {
        KoinContext.koinApp = testKoinApplication(
            refresherCoroutineScope,
            revalidatorCoroutineScope,
            recomposeCoroutineScope,
            testNow
        )
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
        recomposeCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun mutate() = runBlockingTest {
        setContent()
        recomposeCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)

        mutate("k")
        revalidatorCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(2)

        mutate("k")
        revalidatorCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(3)
    }

    @Test
    fun refreshDeduping() = runBlocking {
        setContent(config = {
            refreshInterval = 1000L
        })
        recomposeCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)

        refresherCoroutineScope.advanceTimeBy(700L)
        assertTextRevalidated(1)

        refresherCoroutineScope.advanceTimeBy(700L)
        revalidatorCoroutineScope.advanceUntilIdle()
        assertTextRevalidated(1)

        testNow.advanceTimeBy(4000L) // To skip deduping
        refresherCoroutineScope.advanceTimeBy(700L)
        revalidatorCoroutineScope.advanceUntilIdle()
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

        recomposeCoroutineScope.advanceTimeBy(100)
        assertEquals(1, failingFetcher.failCount)
        assertText("Loading")

        recomposeCoroutineScope.advanceTimeBy(2000)
        assertEquals(1, failingFetcher.failCount)
        assertText("Loading")

        recomposeCoroutineScope.advanceTimeBy(1100)
        assertEquals(2, failingFetcher.failCount)
        assertText("Loading")

        recomposeCoroutineScope.advanceTimeBy(3100)
        assertEquals(3, failingFetcher.failCount)
        assertText("Loading")

        recomposeCoroutineScope.advanceTimeBy(3100)
        assertEquals(4, failingFetcher.failCount)
        assertText("Failure")
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

