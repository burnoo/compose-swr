package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.model.InternalState
import dev.burnoo.compose.swr.model.SWREvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

internal fun <K, D> Flow<SWREvent<D>>.syncWithGlobal(stateFlow: MutableStateFlow<InternalState<K, D>>): Flow<SWREvent<D>> {
    return onEach { stateFlow.value += it }
}
