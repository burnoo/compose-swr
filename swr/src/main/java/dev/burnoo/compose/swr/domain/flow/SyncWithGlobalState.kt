package dev.burnoo.compose.swr.domain.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

internal fun <T> Flow<T>.syncWithGlobalState(stateFlow: MutableStateFlow<T>): Flow<T> {
    return flow {
        collect {
            emit(it)
            stateFlow.value = it
        }
        stateFlow.collect {
            emit(it)
        }
    }
}
