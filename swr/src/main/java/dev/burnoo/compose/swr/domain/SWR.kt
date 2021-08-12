package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.*
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

internal class SWR(
    private val cache: Cache,
    private val now: Now
) {
    fun <K, D> getFlow(
        key: K,
        fetcher: suspend (K) -> D,
        configBlock: SWRConfigBlock<K, D>
    ): Flow<SWRResult<D>> {
        val config = SWRConfig(configBlock)
        cache.initForKey(key, fetcher)
        val globalMutableStateFlow = cache.getMutableStateFlow<K, D>(key)
        return flowOf(SWRRequest(key, fetcher, config))
            .withRefresh(config.refreshInterval)
            .buffer(1)
            .dedupe(
                dedupingInterval = config.dedupingInterval,
                getLastUsageTime = { cache.getRevalidationTime(key) },
                getNow = { now() }
            )
            .onEach { cache.updateUsageTime(key, now()) }
            .retryOnError { fetchResultWithCallbacks(key, config) }
            .syncWithGlobalState(globalMutableStateFlow)
    }

    fun <K, D> getInitialResult(configBlock: SWRConfigBlock<K, D> = {}): SWRResult<D> {
        return SWRConfig(configBlock).initialData?.let { SWRResult.Success(it) }
            ?: SWRResult.Loading
    }

    suspend fun <K> mutate(key: K) {
        val stateFlow = cache.getMutableStateFlow<K, Any>(key)
        val fetcher = cache.getFetcher<K, Any>(key)
        stateFlow.value = fetchResult { fetcher(key) }
    }
}