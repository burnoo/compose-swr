package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.domain.fetchResult
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal fun <K, D> Flow<SWRRequest<K, D>>.fetchResultWithCallbacks(
    key: K,
    config: SWRConfig<K, D>
): Flow<SWRResult<D>> {
    return map { (key, fetcher, _) -> fetchResult(key, fetcher) }
        .handleCallbacks(key, config)
        .withTimeout(config.loadingTimeout) { config.onLoadingSlow?.invoke(key, config) }
}

private fun <K, D> Flow<SWRResult<D>>.handleCallbacks(
    key: K,
    config: SWRConfig<K, D>
): Flow<SWRResult<D>> {
    return this.onEach { result ->
        when (result) {
            is SWRResult.Success -> config.onSuccess?.invoke(result.data, key, config)
            is SWRResult.Error -> config.onError?.invoke(result.exception, key, config)
            else -> Unit
        }
    }
}

private fun <T> Flow<T>.withTimeout(timeoutMillis: Long, onTimeout: () -> Unit): Flow<T> {
    return flow {
        val job = CoroutineScope(currentCoroutineContext()).launch {
            delay(timeoutMillis)
            onTimeout()
        }
        collect { emit(it) }
        job.cancel()
    }
}