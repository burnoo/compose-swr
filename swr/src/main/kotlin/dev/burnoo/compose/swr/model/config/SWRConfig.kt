package dev.burnoo.compose.swr.model.config

import dev.burnoo.compose.swr.domain.SWRCache
import dev.burnoo.compose.swr.domain.flow.SWROnRetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

data class SWRConfig<K, D> internal constructor(
    val fetcher: (suspend (K) -> D)?,
    val revalidateIfStale: Boolean,
    val revalidateOnMount: Boolean?,
    val revalidateFlow: Flow<*>,
    val refreshInterval: Long,
    val shouldRefresh: () -> Boolean,
    val shouldRetryOnError: Boolean,
    val dedupingInterval: Long,
    val loadingTimeout: Long,
    val errorRetryInterval: Long,
    val errorRetryCount: Int?,
    val fallback: Map<K, Any>,
    val fallbackData: D?,
    val onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onErrorRetry: SWROnRetry<K, D>?,
    val onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?,
    val isPaused: () -> Boolean,
    val provider: () -> SWRCache,
    val scope: CoroutineScope?
) {

    fun requireFetcher() = fetcher ?: throw IllegalStateException("Fetcher cannot be null")

    @Suppress("UNCHECKED_CAST")
    internal fun getFallback(key: K): D? = fallbackData ?: (fallback[key] as? D?)
}

@PublishedApi
@Suppress("FunctionName")
internal fun <K, D> SWRConfig(block: DefaultConfigBody<K, D>.() -> Unit): SWRConfig<K, D> {
    return DefaultConfigBody<K, D>().apply(block).run {
        SWRConfig(
            fetcher = fetcher,
            revalidateIfStale = revalidateIfStale,
            revalidateOnMount = revalidateOnMount,
            revalidateFlow = revalidateFlow,
            refreshInterval = refreshInterval,
            shouldRefresh = shouldRefresh,
            shouldRetryOnError = shouldRetryOnError,
            dedupingInterval = dedupingInterval,
            loadingTimeout = loadingTimeout,
            errorRetryInterval = errorRetryInterval,
            errorRetryCount = errorRetryCount,
            fallbackData = fallbackData,
            fallback = fallback,
            onSuccess = onSuccess,
            onError = onError,
            onErrorRetry = onErrorRetry,
            onLoadingSlow = onLoadingSlow,
            isPaused = isPaused,
            provider = provider,
            scope = scope
        )
    }
}