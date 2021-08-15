package dev.burnoo.compose.swr.domain.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun <T> Flow<T>.withRefresh(
    refreshInterval: Long,
    getLastMutationTime: () -> Instant,
    getNow: () -> Instant
): Flow<T> {
    return flow {
        collect {
            emit(it)
            if (refreshInterval > 0) {
                while (true) {
                    delay(refreshInterval)
                    val refreshDuration = Duration.milliseconds(refreshInterval)
                    while (refreshDuration > getNow() - getLastMutationTime()) {
                        val lastMutationAgo = getNow() - getLastMutationTime()
                        delay(refreshDuration - lastMutationAgo)
                    }
                    emit(it)
                }
            }
        }
    }
}