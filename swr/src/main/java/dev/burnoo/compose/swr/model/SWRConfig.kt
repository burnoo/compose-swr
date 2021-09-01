package dev.burnoo.compose.swr.model

import dev.burnoo.compose.swr.domain.flow.SWROnRetry
import kotlinx.coroutines.CoroutineScope

typealias SWRConfigBlock<K, D> = SWRConfigBody<K, D>.() -> Unit

operator fun <K, D> SWRConfigBlock<K, D>.plus(
    configBlock: SWRConfigBlock<K, D>
): SWRConfigBlock<K, D> {
    return {
        this@plus(this)
        configBlock(this)
    }
}

data class SWRConfig<K, D> internal constructor(
    val fetcher: suspend (K) -> D,
    val revalidateIfStale: Boolean,
    val revalidateOnMount: Boolean?,
    val refreshInterval: Long,
    val shouldRetryOnError: Boolean,
    val dedupingInterval: Long,
    val loadingTimeout: Long,
    val errorRetryInterval: Long,
    val errorRetryCount: Int?,
    val fallbackData: D?,
    val onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onErrorRetry: SWROnRetry<K, D>?,
    val onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?,
    val isPaused: () -> Boolean,
    val scope: CoroutineScope? = null
)

@PublishedApi
@Suppress("FunctionName")
internal fun <K, D> SWRConfig(block: SWRConfigBlock<K, D>): SWRConfig<K, D> {
    return SWRConfigBody<K,D>().apply(block).run {
        SWRConfig(
            fetcher = fetcher ?: throw IllegalStateException("Fetcher cannot be null"),
            revalidateIfStale = revalidateIfStale,
            revalidateOnMount = revalidateOnMount,
            refreshInterval = refreshInterval,
            shouldRetryOnError = shouldRetryOnError,
            dedupingInterval = dedupingInterval,
            loadingTimeout = loadingTimeout,
            errorRetryInterval = errorRetryInterval,
            errorRetryCount = errorRetryCount,
            fallbackData = fallbackData,
            onSuccess = onSuccess,
            onError = onError,
            onErrorRetry = onErrorRetry,
            onLoadingSlow = onLoadingSlow,
            isPaused = isPaused,
            scope = scope
        )
    }
}

class SWRConfigBody<K, D> internal constructor() {

    var fetcher: (suspend (K) -> D)? = null
    var revalidateOnMount: Boolean? = null
    var revalidateIfStale = true
    var refreshInterval = 0L
    var shouldRetryOnError = true
    var dedupingInterval = 2000L
    var loadingTimeout = 3000L
    var errorRetryInterval = 5000L
    var errorRetryCount: Int? = null
    var fallbackData: D? = null
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onErrorRetry: SWROnRetry<K, D>? = null
    var isPaused: () -> Boolean = { false }
    var scope: CoroutineScope? = null
}