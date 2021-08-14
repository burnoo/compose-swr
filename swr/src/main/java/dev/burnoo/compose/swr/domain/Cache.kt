package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant

@Suppress("UNCHECKED_CAST")
internal class Cache {
    private val sharedFlowCache = mutableMapOf<Any, MutableSharedFlow<SWRState<Any>>>()
    private val fetcherCache = mutableMapOf<Any, suspend (Any) -> Any>()
    private val mutationTimeCache = mutableMapOf<Any, Instant>()

    fun <K, D> initForKeyIfNeeded(key: K, fetcher: suspend (K) -> D) {
        fetcherCache.getOrPut(key as Any, { fetcher as suspend (Any) -> Any })
        sharedFlowCache.getOrPut(key as Any, {
            MutableSharedFlow<SWRState<D>>() as MutableSharedFlow<SWRState<Any>>
        })
    }

    fun <K, D> getMutableSharedFlow(key: K): MutableSharedFlow<SWRState<D>> {
        return sharedFlowCache[key as Any] as MutableSharedFlow<SWRState<D>>
    }

    fun <K, D> getFetcher(key: K): suspend (K) -> D {
        return fetcherCache[key as Any] as suspend (K) -> D
    }

    fun <K> getMutationTime(key: K): Instant {
        return mutationTimeCache.getOrDefault(key as Any, Instant.DISTANT_PAST)
    }

    fun <K> updateMutationTime(key: K, now: Instant) {
        mutationTimeCache[key as Any] = now
    }
}