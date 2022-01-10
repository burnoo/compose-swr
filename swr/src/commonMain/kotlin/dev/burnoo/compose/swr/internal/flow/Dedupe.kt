package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.testable.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun <T> Flow<T>.dedupe(
    dedupingInterval: Long,
    getLastUsageTime: () -> Instant
): Flow<T> {
    return flow {
        collect {
            val ago = now() - getLastUsageTime()
            if (ago > dedupingInterval.milliseconds) {
                emit(it)
            }
        }
    }
}