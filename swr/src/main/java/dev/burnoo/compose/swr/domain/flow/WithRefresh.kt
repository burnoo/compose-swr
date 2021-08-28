package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.domain.now
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
    getRevalidationTime: () -> Instant,
): Flow<T> {
    return flow {
        collect {
            if (refreshInterval > 0) {
                while (true) {
                    delay(refreshInterval)
                    val refreshDuration = Duration.milliseconds(refreshInterval)
                    while (refreshDuration > now() - getRevalidationTime()) {
                        val lastRevalidationAgo = now() - getRevalidationTime()
                        delay(refreshDuration - lastRevalidationAgo)
                    }
                    emit(it)
                }
            }
        }
    }
}