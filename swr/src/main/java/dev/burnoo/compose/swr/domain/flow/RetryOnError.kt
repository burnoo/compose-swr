package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

internal fun <K, D> Flow<SWRRequest<K, D>>.retryOnError(
    getStateFlow: Flow<SWRRequest<K, D>>.() -> Flow<SWRState<D>>
): Flow<SWRState<D>> {
    return retryMapFlow(getStateFlow) { request, result, attempt ->
        val config = request.config
        val shouldRetry = result is SWRState.Error && config.shouldRetryOnError &&
                (config.errorRetryCount == 0 || attempt < config.errorRetryCount)
        if (shouldRetry) {
            delay(config.errorRetryInterval)
        }
        shouldRetry
    }
}

internal fun <T, R> Flow<T>.retryMap(
    map: suspend (T) -> R,
    predicate: suspend FlowCollector<R>.(value: T, result: R, attempt: Int) -> Boolean
): Flow<R> {
    var retryCount = 0

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
