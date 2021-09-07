package dev.burnoo.compose.swr.utils

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.burnoo.compose.swr.domain.*
import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import dev.burnoo.compose.swr.model.config.plus
import dev.burnoo.compose.swr.useSWR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import kotlin.random.Random

const val key = "k"

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    protected val testCoroutineScope = TestCoroutineScope()
    private val testNow = TestNow()

    protected val stringFetcher = StringFetcher()

    @Before
    open fun setUp() {
        now = testNow
        restartRandom()
        LocalCache = compositionLocalOf { DefaultCache() }
        LocalConfigBlocks.clear()
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
            when {
                error != null -> Text(textFailure)
                data != null -> Text(data)
                else -> Text(textLoading)
            }
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

    protected fun advanceTimeBy(durationMillis: Long) {
        testNow.advanceTimeBy(durationMillis)
        testCoroutineScope.advanceTimeBy(durationMillis)
    }

    protected fun restartRandom() {
        random = Random(0)
    }
}