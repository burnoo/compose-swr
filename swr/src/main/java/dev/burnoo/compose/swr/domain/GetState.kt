package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal suspend fun <K, D> getState(request: SWRRequest<K, D>) : SWRState<D> {
    val (key, fetcher, config) = request
    return withOnLoadingSlow(
        timeoutMillis = config.loadingTimeout,
        onLoadingSlow = { config.onLoadingSlow?.invoke(key, config) },
        function = { fetchAndWrapState(key, fetcher) }
    ) .also { handleCallbacks(it, key, config) }
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

internal suspend fun <K, D> fetchAndWrapState(key : K, fetcher: suspend (K) -> D) = try {
    SWRState.fromData(key, fetcher(key))
} catch (e: Exception) {
    SWRState.fromError(key, e)
}

internal fun <K, D> handleCallbacks(
    state: SWRState<D>,
    key: K,
    config: SWRConfig<K, D>
) {
    when (state) {
        is SWRState.Success -> config.onSuccess?.invoke(state.data, key, config)
        is SWRState.Error -> config.onError?.invoke(state.exception, key, config)
        is SWRState.Loading.Retry -> config.onError?.invoke(state.exception, key, config)
        else -> Unit
    }
}
