package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.internal.Request
import dev.burnoo.compose.swr.model.SWRConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal suspend fun <K, D> getResult(request: Request<K, D>): Result<D> {
    val (key, fetcher, config) = request
    return withOnLoadingSlow(
        timeoutMillis = config.loadingTimeout,
        onLoadingSlow = { config.onLoadingSlow?.invoke(key, config) },
        function = { fetchAndWrapState(key, fetcher) }
    ).also { handleCallbacks(it, key, config) }
}

internal suspend fun <T> withOnLoadingSlow(
    timeoutMillis: Long,
    onLoadingSlow: () -> Unit,
    function: suspend () -> T
): T {
    val job = CoroutineScope(currentCoroutineContext()).launch {
        delay(timeoutMillis)
        onLoadingSlow()
    }
    return function().also { job.cancel() }
}

internal suspend fun <K, D> fetchAndWrapState(key: K, fetcher: suspend (K) -> D) = try {
    Result.success(fetcher(key))
} catch (e: Exception) {
    Result.failure(e)
}

internal fun <K, D> handleCallbacks(
    result: Result<D>,
    key: K,
    config: SWRConfig<K, D>
) {
    result
        .onSuccess { data -> config.onSuccess?.invoke(data, key, config) }
        .onFailure { e -> config.onError?.invoke(e, key, config) }
}

