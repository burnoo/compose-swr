package dev.burnoo.compose.swr.internal

import dev.burnoo.compose.swr.cache.SWRCache
import dev.burnoo.compose.swr.internal.flow.dedupe
import dev.burnoo.compose.swr.internal.flow.refresh
import dev.burnoo.compose.swr.internal.flow.retryOnError
import dev.burnoo.compose.swr.internal.flow.syncWithGlobal
import dev.burnoo.compose.swr.config.SWRConfig
import dev.burnoo.compose.swr.internal.model.Event
import dev.burnoo.compose.swr.internal.model.InternalState
import dev.burnoo.compose.swr.internal.model.Request
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

internal class SWR<K, D>(
    private val key: K,
    private val config: SWRConfig<K, D>,
    private val cache: SWRCache
) {

    init {
        cache.initForKeyIfNeeded<K, D>(key)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLocalFlow(): Flow<Event<D>> {
        val stateFlow = cache.getStateFlow<K, D>(key)
        val initialFlow = flow {
            val shouldRevalidate = config.revalidateOnMount
                ?: (stateFlow.value.data == null || config.revalidateIfStale)
            if (shouldRevalidate) {
                emit(Unit)
            }
        }
        val refreshFlow = flowOf(Unit)
            .refresh(
                refreshInterval = config.refreshInterval,
                getLastUsageTime = { stateFlow.value.revalidationTime }
            )
            .dropWhile { !config.shouldRefresh() }
        return merge(initialFlow, refreshFlow, config.revalidateFlow)
            .buffer(1)
            .dropWhile { config.isPaused() || stateFlow.value.isValidating }
            .dedupe(
                dedupingInterval = config.dedupingInterval,
                getLastUsageTime = { stateFlow.value.revalidationTime }
            )
            .map { Request(key, config) }
            .transform { revalidate(it) }
            .syncWithGlobal(stateFlow)
    }

    fun getGlobalFlow(): StateFlow<InternalState<K, D>> =
        cache.getStateFlow(key)

    suspend fun mutate(data: D?, shouldRevalidate: Boolean) {
        val stateFlow = cache.getStateFlow<K, D>(key)
        if (data != null) {
            stateFlow.value += Event.Local(data)
        }
        if (shouldRevalidate) {
            stateFlow.value += Event.StartValidating
            val request = Request(key, config)
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