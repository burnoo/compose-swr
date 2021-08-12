package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant

@Suppress("UNCHECKED_CAST")
internal class Cache {
    private val stateFlowCache = mutableMapOf<Any, MutableStateFlow<SWRResult<Any>>>()
    private val fetcherCache = mutableMapOf<Any, suspend (Any) -> Any>()
    private val revalidationTimeCache = mutableMapOf<Any, Instant>()

    fun <K, D> initForKey(key: K, fetcher: suspend (K) -> D) {
        fetcherCache.getOrPut(key as Any, { fetcher as suspend (Any) -> Any })
        stateFlowCache.getOrPut(key as Any, {
            MutableStateFlow<SWRResult<D>>(SWRResult.Loading) as MutableStateFlow<SWRResult<Any>>
        })
    }

    fun <K, D> getMutableStateFlow(key: K): MutableStateFlow<SWRResult<D>> {
        return stateFlowCache[key as Any] as MutableStateFlow<SWRResult<D>>
    }

    fun <K, D> getFetcher(key: K): suspend (K) -> D {
        return fetcherCache[key as Any] as suspend (K) -> D
    }

    fun <K> getRevalidationTime(key: K): Instant {
        return revalidationTimeCache.getOrDefault(key as Any, Instant.DISTANT_PAST)
    }

    fun <K> updateUsageTime(key: K, now: Instant) {
        revalidationTimeCache[key as Any] = now
    }
}