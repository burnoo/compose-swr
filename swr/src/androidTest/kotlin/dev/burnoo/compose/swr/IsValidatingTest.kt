package dev.burnoo.compose.swr

import androidx.compose.material.Text
import dev.burnoo.compose.swr.utils.BaseTest
import dev.burnoo.compose.swr.utils.FailingFetcher
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class IsValidatingTest : BaseTest() {

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
        Assert.assertEquals(4, recompositionCount)
    }
}