package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.model.internal.InternalState
import dev.burnoo.compose.swr.model.internal.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

internal fun <K, D> Flow<Event<D>>.syncWithGlobal(stateFlow: MutableStateFlow<InternalState<K, D>>): Flow<Event<D>> {
    return onEach { stateFlow.value += it }
}
