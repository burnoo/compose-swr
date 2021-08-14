package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.*
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.flow.*
import kotlin.random.Random

internal class SWR(
    private val cache: Cache,
    private val now: Now,
    private val random: Random
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
            .run { if (!config.getRevalidateOnMount()) drop(1) else this }
            .dedupe(
                dedupingInterval = config.dedupingInterval,
                getLastUsageTime = { cache.getRevalidationTime(key) },
                getNow = { now() }
            )
            .onEach { cache.updateUsageTime(key, now()) }
            .retryOnError(nextDouble = { random.nextDouble() }) { fetchStateWithCallbacks(key, config) }
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