package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.*
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRState
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
    ): Flow<SWRState<D>> {
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
            .retryOnError { fetchStateWithCallbacks(key, config) }
            .syncWithGlobal(globalSharedFlow)
    }

    fun <K, D> getGlobalFlow(key: K): Flow<SWRState<D>> = cache.getMutableSharedFlow(key)

    fun <K, D> getInitialResult(key: K, configBlock: SWRConfigBlock<K, D> = {}): SWRState<D> {
        return SWRState.fromData(key, data = SWRConfig(configBlock).initialData)
    }

    suspend fun <K, D> mutate(key: K, data: D?, shouldRevalidate: Boolean) {
        val stateFlow = cache.getMutableSharedFlow<K, Any>(key)
        if (data != null) {
            stateFlow.emit(SWRState.fromData(key, data))
        }
        if (shouldRevalidate) {
            cache.updateUsageTime(key, now())
            val fetcher = cache.getFetcher<K, Any>(key)
            stateFlow.emit(fetchState(key, fetcher))
        }
    }
}