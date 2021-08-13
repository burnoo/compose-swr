package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.*
import dev.burnoo.compose.swr.domain.flow.dedupe
import dev.burnoo.compose.swr.domain.flow.withRefresh
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
    fun <K, D> initIfNeeded(key: K, fetcher: suspend (K) -> D) {
        cache.initForKeyIfNeeded(key, fetcher)
    }

    fun <K, D> getLocalFlow(
        key: K,
        fetcher: suspend (K) -> D,
        configBlock: SWRConfigBlock<K, D>
    ): Flow<SWRResult<D>> {
        val config = SWRConfig(configBlock)
        val globalSharedFlow = cache.getMutableSharedFlow<K, D>(key)
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
            .syncWithGlobal(globalSharedFlow)
    }

    fun <K, D> getGlobalFlow(key: K): Flow<SWRResult<D>> = cache.getMutableSharedFlow(key)

    fun <K, D> getInitialResult(key : K, configBlock: SWRConfigBlock<K, D> = {}): SWRResult<D> {
        return SWRResult.fromData(key, data = SWRConfig(configBlock).initialData)
    }

    suspend fun <K> mutate(key: K) {
        val stateFlow = cache.getMutableSharedFlow<K, Any>(key)
        cache.updateUsageTime(key, now())
        val fetcher = cache.getFetcher<K, Any>(key)
        stateFlow.emit(fetchResult(key, fetcher))
    }
}