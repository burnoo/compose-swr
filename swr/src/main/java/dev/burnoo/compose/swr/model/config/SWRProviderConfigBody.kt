package dev.burnoo.compose.swr.model.config

import dev.burnoo.compose.swr.domain.SWRCache
import dev.burnoo.compose.swr.domain.flow.SWROnRetry
import kotlinx.coroutines.CoroutineScope

interface SWRProviderConfigBodyTyped<K, D> {

    var fetcher: (suspend (K) -> D)?
    var revalidateOnMount: Boolean?
    var revalidateIfStale: Boolean
    var refreshInterval: Long
    var shouldRetryOnError: Boolean
    var dedupingInterval: Long
    var loadingTimeout: Long
    var errorRetryInterval: Long
    var errorRetryCount: Int?
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)?
    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)?
    var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)?
    var onErrorRetry: SWROnRetry<K, D>?
    var isPaused: () -> Boolean
    var provider: () -> SWRCache
    var scope: CoroutineScope?
}

typealias SWRConfigProviderBody<K> = SWRProviderConfigBodyTyped<K, Any>

typealias SWRProviderConfigBlock<K> = SWRConfigProviderBody<K>.() -> Unit

operator fun <K> SWRProviderConfigBlock<K>.plus(
    configBlock: SWRProviderConfigBlock<K>
): SWRProviderConfigBlock<K> {
    return {
        this@plus(this)
        configBlock(this)
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, D> SWRProviderConfigBlock<K>.plusConfigBlock(
    configBlock: SWRConfigBlock<K, D>
): SWRConfigBlock<K, D> {
    return {
        this@plusConfigBlock(this as SWRConfigProviderBody<K>)
        configBlock(this)
    }
}