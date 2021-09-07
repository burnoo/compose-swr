package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.utils.BaseTest
import dev.burnoo.compose.swr.utils.OnSuccess
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MutateTest : BaseTest() {

    @Test
    fun mutate() = runBlockingTest {
        setSWRContent()
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
        setSWRContent(config = {
            this.onSuccess = onSuccess::invoke
        })
        assertTextLoading()
        clickMutate()
        testCoroutineScope.advanceUntilIdle()
        assertEquals(2, onSuccess.invocations.size)
    }

    @Test
    fun mutateAndRefreshWithDeduping() = runBlocking {
        setSWRContent(config = {
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
    fun mutateAndRefreshWithoutRevalidation() = runBlocking {
        setSWRContent(config = {
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
}