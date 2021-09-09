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
    predicate: suspend FlowCollector<R>.(item: T, result: R, attempt: Long) -> Boolean
): Flow<R> {
    tailrec suspend fun FlowCollector<R>.retryCollect(item: T, attempt: Long) {
        val result = map(item)
        if (predicate(item, result, attempt)) {
            retryCollect(item, attempt = attempt + 1)
        } else {
            emit(result)
        }
    }

    return flow {
        collect { item ->
            retryCollect(item, attempt = 1L)
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
