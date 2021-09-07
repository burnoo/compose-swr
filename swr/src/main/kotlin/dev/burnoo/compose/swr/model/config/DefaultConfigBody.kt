package dev.burnoo.compose.swr.model.config

import dev.burnoo.compose.swr.domain.DefaultCache
import dev.burnoo.compose.swr.domain.SWRCache
import dev.burnoo.compose.swr.domain.flow.SWROnRetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class DefaultConfigBody<K, D> internal constructor() :
    SWRLocalConfigBodyTyped<K, D>,
    SWRConfigBody<K, D> {

    override var fetcher: (suspend (K) -> D)? = null
    override var revalidateOnMount: Boolean? = null
    override var revalidateIfStale = true
    override var revalidateFlow: Flow<*> = emptyFlow<Nothing>()
    override var refreshInterval = 0L
    override var shouldRefresh: () -> Boolean = { true }
    override var shouldRetryOnError = true
    override var dedupingInterval = 2000L
    override var loadingTimeout = 3000L
    override var errorRetryInterval = 5000L
    override var errorRetryCount: Int? = null
    override var fallback: Map<K, Any> = emptyMap()
    override var fallbackData: D? = null
    override var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)? = null
    override var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    override var onError: ((error: Throwable, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    override var onErrorRetry: SWROnRetry<K, D>? = null
    override var isPaused: () -> Boolean = { false }
    override var provider: () -> SWRCache = { DefaultCache() }
    override var scope: CoroutineScope? = null
}