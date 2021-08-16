package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.domain.flow.dedupe
import dev.burnoo.compose.swr.domain.flow.retryOnError
import dev.burnoo.compose.swr.domain.flow.syncWithGlobal
import dev.burnoo.compose.swr.domain.flow.withRefresh
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
    fun <K, D> initIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        cache.initForKeyIfNeeded(key, fetcher, config)
    }

    fun <K, D> getLocalFlow(
        key: K,
        fetcher: suspend (K) -> D,
        config: SWRConfig<K, D>
    ): Flow<SWRState<D>> {
        val globalSharedFlow = cache.getMutableSharedFlow<K, D>(key)
        return flowOf(SWRRequest(key, fetcher, config))
            .withRefresh(
                refreshInterval = config.refreshInterval,
                getRevalidationTime = { cache.getRevalidationTime(key) },
                getNow = { now() }
            )
            .buffer(1)
            .run { if (!config.shouldRevalidateOnMount()) drop(1) else this }
            .dropWhile { config.isPaused() || cache.isValidating(key) }
            .dedupe(
                dedupingInterval = config.dedupingInterval,
                getRevalidationTime = { cache.getRevalidationTime(key) },
                getNow = { now() }
            )
            .onEach { cache.updateRevalidationTime(key, now()) }
            .retryOnError(nextDouble = { random.nextDouble() }, getState = { request ->
                cache.updateIsValidating(key, true)
                getState(request).also { cache.updateIsValidating(key, false) }
            })
            .syncWithGlobal(globalSharedFlow)
    }

    fun <K, D> getGlobalFlow(key: K): Flow<SWRState<D>> = cache.getMutableSharedFlow(key)

    fun <K, D> getInitialState(key: K, configBlock: SWRConfigBlock<K, D> = {}): SWRState<D> {
        return SWRState.fromData(key, data = SWRConfig(configBlock).initialData)
    }

    suspend fun <K, D> mutate(key: K, data: D?, shouldRevalidate: Boolean) {
        val stateFlow = cache.getMutableSharedFlow<K, Any>(key)
        cache.updateRevalidationTime(key, now())
        if (data != null) {
            stateFlow.emit(SWRState.fromData(key, data))
        }
        if (shouldRevalidate) {
            val fetcher = cache.getFetcher<K, Any>(key)
            val config = cache.getConfig<K, Any>(key)
            val request = SWRRequest(key, fetcher, config)
            cache.updateIsValidating(key, true) // It should cancel local I think
            stateFlow.emit(value = getState(request))
            cache.updateIsValidating(key, false)
        }
    }

    fun <K> isValidating(key: K): StateFlow<Boolean> {
        return cache.isValidatingFlow(key)
    }
}