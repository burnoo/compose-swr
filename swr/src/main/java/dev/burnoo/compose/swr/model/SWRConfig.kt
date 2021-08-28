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
    val initialData: D?,
    val fetcher: suspend (K) -> D,
    val refreshInterval: Long,
    val shouldRetryOnError: Boolean,
    val errorRetryInterval: Long,
    val errorRetryCount: Int?,
    val dedupingInterval: Long,
    val loadingTimeout: Long,
    val onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val revalidateIfStale: Boolean,
    val onErrorRetry: SWROnRetry<K, D>?,
    val isPaused: () -> Boolean,
    val scope: CoroutineScope? = null
)

@Suppress("FunctionName")
internal fun <K, D> SWRConfig(block: SWRConfigBlock<K, D>): SWRConfig<K, D> {
    return SWRConfigBody<K,D>().apply(block).run {
        SWRConfig(
            initialData = initialData,
            fetcher = fetcher ?: throw IllegalStateException("Fetcher cannot be null"),
            refreshInterval = refreshInterval,
            shouldRetryOnError = shouldRetryOnError,
            errorRetryInterval = errorRetryInterval,
            errorRetryCount = errorRetryCount,
            dedupingInterval = dedupingInterval,
            loadingTimeout = loadingTimeout,
            onLoadingSlow = onLoadingSlow,
            onSuccess = onSuccess,
            onError = onError,
            revalidateIfStale = revalidateIfStale,
            onErrorRetry = onErrorRetry,
            isPaused = isPaused,
            scope = scope
        )
    }
}

class SWRConfigBody<K, D> internal constructor() {

    var initialData: D? = null

    var fetcher: (suspend (K) -> D)? = null

    var refreshInterval = 0L
    var shouldRetryOnError = true
    var errorRetryInterval = 5000L
    var errorRetryCount: Int? = null

    var dedupingInterval = 2000L

    var loadingTimeout = 3000L
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)? = null

    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)? = null

    var revalidateIfStale = true

    var onErrorRetry: SWROnRetry<K, D>? = null
    var isPaused: () -> Boolean = { false }

    var scope: CoroutineScope? = null
}