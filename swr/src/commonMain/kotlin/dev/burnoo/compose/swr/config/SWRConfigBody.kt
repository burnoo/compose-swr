package dev.burnoo.compose.swr.config

import dev.burnoo.compose.swr.retry.SWROnRetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface SWRConfigBody<K, D> {

    var fetcher: (suspend (K) -> D)?
    var revalidateOnMount: Boolean?
    var revalidateIfStale: Boolean
    var revalidateFlow: Flow<*>
    var refreshInterval: Long
    var shouldRefresh: () -> Boolean
    var shouldRetryOnError: Boolean
    var dedupingInterval: Long
    var loadingTimeout: Long
    var errorRetryInterval: Long
    var errorRetryCount: Int?
    var fallbackData: D?
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?
    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?
    var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?
    var onErrorRetry: SWROnRetry<K, D>?
    var isPaused: () -> Boolean
    var scope: CoroutineScope?
}

typealias SWRConfigBlock<K, D> = SWRConfigBody<K, D>.() -> Unit

operator fun <K, D> SWRConfigBlock<K, D>.plus(
    configBlock: SWRConfigBlock<K, D>
): SWRConfigBlock<K, D> {
    return {
        this@plus(this)
        configBlock(this)
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, D> SWRConfigBlock<K, D>.withLocalConfigBlock(
    configBlock: SWRLocalConfigBlock<K>
): SWRConfigBlock<K, D> {
    return {
        this@withLocalConfigBlock(this)
        configBlock(this as SWRLocalConfigBody<K>)
    }
}