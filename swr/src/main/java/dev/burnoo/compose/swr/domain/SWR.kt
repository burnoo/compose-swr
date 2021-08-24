package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.dedupe
import dev.burnoo.compose.swr.domain.flow.retryOnError
import dev.burnoo.compose.swr.domain.flow.syncWithGlobal
import dev.burnoo.compose.swr.domain.flow.withRefresh
import dev.burnoo.compose.swr.model.InternalState
import dev.burnoo.compose.swr.model.Request
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWREvent
import kotlinx.coroutines.flow.*

internal class SWR(
    private val cache: Cache
) {
    fun <K, D> initIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        cache.initForKeyIfNeeded(key, fetcher, config)
    }

    fun <K, D> getLocalFlow(
        key: K,
        fetcher: suspend (K) -> D,
        config: SWRConfig<K, D>
    ): Flow<SWREvent<D>> {
        val stateFlow = cache.getStateFlow<K, D>(key)
        return flowOf(Request(key, fetcher, config))
            .withRefresh(
                refreshInterval = config.refreshInterval,
                getRevalidationTime = { stateFlow.value.revalidationTime }
            )
            .buffer(1)
            .run { if (!config.shouldRevalidateOnMount()) drop(1) else this }
            .dropWhile { config.isPaused() || stateFlow.value.isValidating }
            .dedupe(
                dedupingInterval = config.dedupingInterval,
                getRevalidationTime = { stateFlow.value.revalidationTime }
            )
            .transform { revalidate(it) }
            .syncWithGlobal(stateFlow)
    }

    fun <K, D> getGlobalFlow(key: K): StateFlow<InternalState<K, D>> =
        cache.getStateFlow(key)

    suspend fun <K, D> mutate(key: K, data: D?, shouldRevalidate: Boolean) {
        val stateFlow = cache.getStateFlow<K, Any>(key)
        if (data != null) {
            stateFlow.value += SWREvent.Local(data)
        }
        if (shouldRevalidate) {
            stateFlow.value += SWREvent.StartValidating
            val fetcher = cache.getFetcher<K, Any>(key)
            val config = cache.getConfig<K, Any>(key)
            val request = Request(key, fetcher, config)
            getResult(request)
                .onSuccess { d -> stateFlow.value += SWREvent.Success(d) }
                .onFailure { e -> stateFlow.value += SWREvent.Error(e) }
        }
    }

    private suspend fun <D, K> FlowCollector<SWREvent<D>>.revalidate(request: Request<K, D>) {
        flowOf(request)
            .retryOnError {
                emit(SWREvent.StartValidating)
                getResult(it)
                    .onSuccess { data -> emit(SWREvent.Success(data)) }
                    .onFailure { e -> emit(SWREvent.Error(e)) }
            }
            .collect()
    }
}