package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.math.floor

internal fun exponentialBackoff(
    errorRetryInterval: Long,
    attempt: Int,
    nextDouble: () -> Double,
) = floor((nextDouble() + 0.5) * 1.shl(attempt)).toLong() * errorRetryInterval

internal fun <K, D> Flow<SWRRequest<K, D>>.retryOnError(
    nextDouble: () -> Double,
    getStateFlow: Flow<SWRRequest<K, D>>.() -> Flow<SWRState<D>>
): Flow<SWRState<D>> {
    return retryMapFlow(getStateFlow) { request, state, attempt ->
        val config = request.config
        if (state is SWRState.Error &&
            config.shouldRetryOnError &&
            config.errorRetryCount.let { it == null || attempt <= it }
        ) {
            emit(SWRState.fromRetry(request.key, attempt, state.exception))
            delay(exponentialBackoff(config.errorRetryInterval, attempt, nextDouble))
            true
        } else {
            false
        }
    }
}

internal fun <T, R> Flow<T>.retryMap(
    map: suspend (T) -> R,
    predicate: suspend FlowCollector<R>.(value: T, result: R, attempt: Int) -> Boolean
): Flow<R> {
    var retryCount = 1

    suspend fun FlowCollector<R>.collector(
        value: T,
        result: R
    ) {
        if (predicate(value, result, retryCount)) {
            retryCount++
            collector(value, map(value))
        } else {
            emit(result)
        }
    }

    return flow {
        collect { request ->
            collector(request, map(request))
        }
    }
}

internal fun <T, R> Flow<T>.retryMapFlow(
    map: Flow<T>.() -> Flow<R>,
    predicate: suspend FlowCollector<R>.(value: T, result: R, attempt: Int) -> Boolean
): Flow<R> {
    return retryMap(
        map = { request -> flowOf(request).map().first() },
        predicate = predicate
    )
}
