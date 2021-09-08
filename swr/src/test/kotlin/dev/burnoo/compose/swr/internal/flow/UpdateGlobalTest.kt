package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.model.Event
import dev.burnoo.compose.swr.internal.model.InternalState
import dev.burnoo.compose.swr.utils.BaseTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import org.junit.Assert.assertEquals
import org.junit.Test

private const val string = "key"

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateGlobalTest : BaseTest() {

    @Test
    fun `update global StateFlow`() {
        var expectedState = InternalState.initial<String, String>(string)
        val globalStateFLow = MutableStateFlow(InternalState.initial<String, String>(string))
        flow {
            delay(500L)
            emit(Event.StartValidating)
            delay(500L)
            emit(Event.Success(string))
        }
            .updateGlobal(globalStateFLow)
            .launchIn(testCoroutineScope)
        assertEquals(expectedState, globalStateFLow.value)

        advanceTimeBy(500L)
        expectedState += Event.StartValidating
        assertEquals(expectedState, globalStateFLow.value)

        advanceTimeBy(500L)
        expectedState += Event.Success(string)
        assertEquals(expectedState, globalStateFLow.value)
    }
}