package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.utils.ComposeBaseTest
import dev.burnoo.compose.swr.utils.OnSuccess
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutateTest : ComposeBaseTest() {

    @Test
    fun mutate() = runBlocking {
        setSWRContent()
        assertTextLoading()

        waitForIdle()
        assertTextRevalidated(1)

        clickMutate()
        waitForIdle()
        assertTextRevalidated(2)

        clickMutate()
        waitForIdle()
        assertTextRevalidated(3)
    }

    @Test
    fun mutateWithOnSuccessCallback() = runBlocking {
        val onSuccess = OnSuccess()
        setSWRContent(config = {
            this.onSuccess = onSuccess::invoke
            dedupingInterval = 0L
        })
        assertTextLoading()
        clickMutate()
        waitForIdle()
        assertEquals(1, onSuccess.invocations.size)
    }

    @Test
    fun mutateAndRefreshWithDeduping() = runBlocking {
        setSWRContent(config = {
            refreshInterval = 2000L
            dedupingInterval = 1000L
            scope = testScope
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
    fun mutateAndRefreshWithoutRevalidation() = runBlocking {
        setSWRContent(config = {
            refreshInterval = 2000L
            dedupingInterval = 1000L
            scope = testScope
        }, mutationData = "${key}0", shouldRevalidate = false)
        assertTextLoading()
        advanceTimeBy(100L)

        repeat(10) {
            clickMutate()
            advanceTimeBy(999L)
            assertTextRevalidated(0)
        }
    }
}