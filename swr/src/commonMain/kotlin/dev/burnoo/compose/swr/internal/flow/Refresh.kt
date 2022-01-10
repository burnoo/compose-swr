package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.testable.now
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun <T> Flow<T>.refresh(
    refreshInterval: Long,
    getLastUsageTime: () -> Instant,
): Flow<T> {
    return flow {
        collect {
            if (refreshInterval > 0) {
                while (true) {
                    delay(refreshInterval)
                    val refreshDuration = refreshInterval.milliseconds
                    while (refreshDuration > now() - getLastUsageTime()) {
                        val lastRevalidationAgo = now() - getLastUsageTime()
                        delay(refreshDuration - lastRevalidationAgo)
                    }
                    emit(it)
                }
            }
        }
    }
}