package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal class SWR(
    private val cache: Cache,
    private val refresher: Refresher,
    private val now: Now,
    private val recomposeCoroutineScope: RecomposeCoroutineScope,
) {
    fun <K, D> init(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        cache.initForKeyIfNeeded(key, fetcher, config)
    }

    fun <K> launch(key: K, refreshInterval: Long, scope: CoroutineScope) {
        Revalidator(cache, now, key as Any, recomposeCoroutineScope.value ?: scope)
            .revalidate()
        refresher.handleRefreshing(key, refreshInterval)
    }

    fun <K, D> getStateFlow(key: K): StateFlow<SWRResult<D>> = cache.getOrCreateStateFlow(key)
}