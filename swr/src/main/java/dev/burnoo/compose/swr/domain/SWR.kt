package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.dedupe
import dev.burnoo.compose.swr.domain.flow.retryOnError
import dev.burnoo.compose.swr.domain.flow.syncWithGlobal
import dev.burnoo.compose.swr.domain.flow.refresh
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.internal.Event
import dev.burnoo.compose.swr.model.internal.InternalState
import dev.burnoo.compose.swr.model.internal.Request
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

internal class SWR(
    private val cache: Cache
) {
    fun <K, D> initIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        cache.initForKeyIfNeeded(key, fetcher, config)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <K, D> getLocalFlow(
        key: K,
        fetcher: suspend (K) -> D,
        config: SWRConfig<K, D>
    ): Flow<Event<D>> {
        val stateFlow = cache.getStateFlow<K, D>(key)
        val revalidationFlow = flow {
            val shouldRevalidate = config.revalidateOnMount
                ?: (stateFlow.value.data == null || config.revalidateIfStale)
            if (shouldRevalidate) {
                emit(Unit)
            }
        }
        val refreshFlow = flowOf(Unit).refresh(
            refreshInterval = config.refreshInterval,
            getRevalidationTime = { stateFlow.value.revalidationTime }
        )
        return merge(revalidationFlow, refreshFlow)
            .buffer(1)
            .dropWhile { config.isPaused() || stateFlow.value.isValidating }
            .dedupe(
                dedupingInterval = config.dedupingInterval,
                getRevalidationTime = { stateFlow.value.revalidationTime }
            )
            .map { Request(key, fetcher, config) }
            .transform { revalidate(it) }
            .syncWithGlobal(stateFlow)
    }

    fun <K, D> getGlobalFlow(key: K): StateFlow<InternalState<K, D>> =
        cache.getStateFlow(key)

    suspend fun <K, D> mutate(key: K, data: D?, shouldRevalidate: Boolean) {
        val stateFlow = cache.getStateFlow<K, D>(key)
        if (data != null) {
            stateFlow.value += Event.Local(data)
        }
        if (shouldRevalidate) {
            stateFlow.value += Event.StartValidating
            val fetcher = cache.getFetcher<K, D>(key)
            val config = cache.getConfig<K, D>(key)
            val request = Request(key, fetcher, config)
            getResult(request)
                .onSuccess { d -> stateFlow.value += Event.Success(d) }
                .onFailure { e -> stateFlow.value += Event.Error(e) }
        }
    }

    private suspend fun <D, K> FlowCollector<Event<D>>.revalidate(request: Request<K, D>) {
        flowOf(request)
            .retryOnError {
                emit(Event.StartValidating)
                getResult(it)
                    .onSuccess { d -> emit(Event.Success(d)) }
                    .onFailure { e -> emit(Event.Error(e)) }
            }
            .collect()
    }
}