package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.model.Event
import dev.burnoo.compose.swr.internal.model.InternalState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

internal fun <K, D> Flow<Event<D>>.updateGlobal(
    stateFlow: MutableStateFlow<InternalState<K, D>>
): Flow<Event<D>> {
    return onEach { stateFlow.value += it }
}
