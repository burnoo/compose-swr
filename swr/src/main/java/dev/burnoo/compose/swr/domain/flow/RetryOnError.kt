package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.domain.random
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.Request
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.math.floor

typealias SWROnRetry<K, D> = suspend (
    error: Throwable, key: K, config: SWRConfig<K, D>, attempt: Int
) -> Boolean

internal fun <K, D> onRetryDefault(): SWROnRetry<K, D> = { _, _, config, attempt ->
    if (config.shouldRetryOnError && config.errorRetryCount.let { it == null || attempt <= it }) {
        delay(exponentialBackoff(config.errorRetryInterval, attempt))
        true
    } else {
        false
    }
}

internal fun exponentialBackoff(
    errorRetryInterval: Long,
    attempt: Int
) = floor((random.nextDouble() + 0.5) * 1.shl(attempt)).toLong() * errorRetryInterval

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