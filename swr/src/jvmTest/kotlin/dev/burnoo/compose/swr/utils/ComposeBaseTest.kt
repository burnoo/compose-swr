package dev.burnoo.compose.swr.utils

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.burnoo.compose.swr.cache.DefaultCache
import dev.burnoo.compose.swr.config.SWRConfigBlock
import dev.burnoo.compose.swr.config.plus
import dev.burnoo.compose.swr.internal.LocalCache
import dev.burnoo.compose.swr.internal.LocalConfigBlocks
import dev.burnoo.compose.swr.useSWR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule

const val key = "k"

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ComposeBaseTest : BaseTest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    protected val stringFetcher = StringFetcher()

    @Before
    override fun setUp() {
        super.setUp()
        LocalCache = compositionLocalOf { DefaultCache() }
        LocalConfigBlocks.clear()
    }

    override fun advanceTimeBy(durationMillis: Long) {
        super.advanceTimeBy(durationMillis)
        composeTestRule.mainClock.advanceTimeBy(durationMillis)
        composeTestRule.waitForIdle()
    }

    protected fun waitForIdle() {
        testCoroutineScope.advanceUntilIdle()
        composeTestRule.waitForIdle()
    }

    protected fun setSWRContent(
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
            DataErrorLoading(data, error)
            Button(onClick = {
                testCoroutineScope.launch { mutate(mutationData, shouldRevalidate) }
            }) {
                Text("Mutate")
            }
        }
    }

    protected fun assertTextRevalidated(count: Int) {
        assertText("$key$count")
    }

    protected fun assertTextLoading() {
        assertText(textLoading)
    }

    protected fun assertTextFailure() {
        assertText(textFailure)
    }

    protected fun assertText(text: String) {
        composeTestRule.onNode(isRoot()).onChildAt(0).assertTextEquals(text)
    }

    protected fun clickMutate() {
        composeTestRule.onNodeWithText("Mutate").performClick()
    }
}

