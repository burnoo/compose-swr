package dev.burnoo.compose.swr

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.burnoo.compose.swr.utils.*
import dev.burnoo.compose.swr.utils.DataErrorLoading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevalidationTest : ComposeBaseTest() {

    @Test
    fun revalidateOnMount() {
        setSWRContent(config = {
            fallbackData = "${key}0"
            revalidateIfStale = false
            revalidateOnMount = true
        })
        assertTextRevalidated(0)

        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun doNotRevalidateOnMount() {
        setSWRContent(config = {
            revalidateIfStale = true
            revalidateOnMount = false
        })
        assertTextLoading()

        advanceTimeBy(10_000L)
        assertTextLoading()
    }

    @Test
    fun revalidateIfStaleWithFallbackData() {
        setSWRContent(config = {
            fallbackData = "${key}0"
            revalidateIfStale = true
        })
        assertTextRevalidated(0)

        waitForIdle()
        assertTextRevalidated(1)
    }

    @Test
    fun useRevalidateFlow() = runBlocking {
        val revalidateFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        setSWRContent(config = {
            this.revalidateFlow = revalidateFlow
            scope = testScope
        })
        assertTextLoading()

        waitForIdle()
        assertTextRevalidated(1)

        advanceTimeBy(10_000L)
        assertTextRevalidated(1)

        revalidateFlow.emit(Unit)
        waitForIdle()
        assertTextRevalidated(2)
    }

    @Test
    fun doNotRevalidateWhenPaused() = runBlocking {
        setSWRContent(config = {
            isPaused = { true }
            dedupingInterval = 0L
            refreshInterval = 500L
        })
        assertTextLoading()

        advanceTimeBy(100000L)
        assertTextLoading()
    }

    @Test
    fun validateOnceWithSWRImmutable() {
        val delayedFlow = flow {
            delay(1000)
            emit(true)
        }
        composeTestRule.setContent {
            val delayedState by delayedFlow.collectAsState(
                initial = false,
                context = testScope.coroutineContext
            )
            val (data, error) = useSWRImmutable(
                key = key,
                fetcher = { stringFetcher.fetch(it) },
                config = {
                    scope = if (!delayedState) testScope else null
                })
            DataErrorLoading(data, error)
        }
        advanceTimeBy(100L)
        assertTextRevalidated(1)
        advanceTimeBy(9000L)
        assertTextRevalidated(1)
    }
}