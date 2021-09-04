package dev.burnoo.compose.swr.model.config

import dev.burnoo.compose.swr.domain.SWRCache
import dev.burnoo.compose.swr.domain.flow.SWROnRetry
import kotlinx.coroutines.CoroutineScope

interface SWRLocalConfigBodyTyped<K, D> {

    var fetcher: (suspend (K) -> D)?
    var revalidateOnMount: Boolean?
    var revalidateIfStale: Boolean
    var refreshInterval: Long
    var shouldRetryOnError: Boolean
    var dedupingInterval: Long
    var loadingTimeout: Long
    var errorRetryInterval: Long
    var errorRetryCount: Int?
    var fallback: Map<K, Any>
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?
    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?
    var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?
    var onErrorRetry: SWROnRetry<K, D>?
    var isPaused: () -> Boolean
    var provider: () -> SWRCache
    var scope: CoroutineScope?
}

internal typealias SWRLocalConfigBlockTyped<K, D> = SWRLocalConfigBodyTyped<K, D>.() -> Unit

typealias SWRLocalConfigBody<K> = SWRLocalConfigBodyTyped<K, Any>

typealias SWRLocalConfigBlock<K> = SWRLocalConfigBody<K>.() -> Unit

operator fun <K> SWRLocalConfigBlock<K>.plus(
    configBlock: SWRLocalConfigBlock<K>
): SWRLocalConfigBlock<K> {
    return {
        this@plus(this)
        configBlock(this)
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, D> SWRLocalConfigBlock<K>.withConfigBlock(
    configBlock: SWRConfigBlock<K, D>
): SWRConfigBlock<K, D> {
    return {
        this@withConfigBlock(this as SWRLocalConfigBody<K>)
        configBlock(this)
    }
}