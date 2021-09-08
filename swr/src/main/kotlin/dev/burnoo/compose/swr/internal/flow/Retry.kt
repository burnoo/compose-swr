package dev.burnoo.compose.swr.internal.flow

import dev.burnoo.compose.swr.internal.model.Request
import dev.burnoo.compose.swr.retry.SWROnRetry
import dev.burnoo.compose.swr.retry.onRetryDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

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

internal fun <K, D> Flow<Request<K, D>>.retryOnError(
    getResult: suspend (Request<K, D>) -> Result<D>
): Flow<Result<D>> {
    return retryMap(getResult) { request, result, attempt ->
        val config = request.config
        result.onFailure {
            val onErrorRetry: SWROnRetry<K, D> = config.onErrorRetry ?: onRetryDefault()
            return@retryMap onErrorRetry(it, request.key, config, attempt)
        }
        false
    }
}
