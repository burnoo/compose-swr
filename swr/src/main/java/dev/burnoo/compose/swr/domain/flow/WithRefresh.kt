package dev.burnoo.compose.swr.domain.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

internal fun <T> Flow<T>.withRefresh(refreshInterval: Long): Flow<T> {
    return flow {
        collect {
            emit(it)
            if (refreshInterval > 0) {
                while (true) {
                    delay(refreshInterval)
                    emit(it)
                }
            }
        }
    }
}