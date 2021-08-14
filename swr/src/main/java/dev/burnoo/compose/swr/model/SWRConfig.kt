package dev.burnoo.compose.swr.model

typealias SWRConfigBlock<K, D> = SWRConfigBody<K, D>.() -> Unit

data class SWRConfig<K, D> internal constructor(
    val initialData: D?,
    val refreshInterval: Long,
    val shouldRetryOnError: Boolean,
    val errorRetryInterval: Long,
    val errorRetryCount: Int?,
    val dedupingInterval: Long,
    val loadingTimeout: Long,
    val onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?,
    val revalidateOnMount: Boolean?,
    val isPaused: () -> Boolean,
) {

    internal fun shouldRevalidateOnMount() = revalidateOnMount ?: (initialData == null)
}

class SWRConfigBody<K, D> internal constructor() {

    var initialData: D? = null

    var refreshInterval = 0L
    var shouldRetryOnError = true
    var errorRetryInterval = 5000L
    var errorRetryCount: Int? = null

    var dedupingInterval = 2000L

    var loadingTimeout = 3000L
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)? = null

    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)? = null

    var revalidateOnMount: Boolean? = null
    var isPaused: () -> Boolean = { false }
}

@Suppress("FunctionName")
internal fun <K, D> SWRConfig(block: SWRConfigBlock<K, D>): SWRConfig<K, D> {
    return SWRConfigBody<K, D>().apply(block).run {
        SWRConfig(
            initialData = initialData,
            refreshInterval = refreshInterval,
            shouldRetryOnError = shouldRetryOnError,
            errorRetryInterval = errorRetryInterval,
            errorRetryCount = errorRetryCount,
            dedupingInterval = dedupingInterval,
            loadingTimeout = loadingTimeout,
            onLoadingSlow = onLoadingSlow,
            onSuccess = onSuccess,
            onError = onError,
            revalidateOnMount = revalidateOnMount,
            isPaused = isPaused,
        )
    }
}