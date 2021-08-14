package dev.burnoo.compose.swr.domain.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun <T> Flow<T>.dedupe(
    dedupingInterval: Long,
    getLastMutationTime: () -> Instant,
    getNow: () -> Instant
): Flow<T> {
    return flow {
        collect {
            val ago = getNow() - getLastMutationTime()
            if (ago > Duration.milliseconds(dedupingInterval)) {
                emit(it)
            }
        }
    }
}