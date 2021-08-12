package dev.burnoo.compose.swr.domain.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach

internal fun <T> Flow<T>.syncWithGlobal(sharedFlow: MutableSharedFlow<T>): Flow<T> {
    return onEach { sharedFlow.emit(it) }
        .combine(sharedFlow) { _, globalValue -> globalValue }
}
