package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

@Suppress("UNCHECKED_CAST")
internal class Cache {
    private val sharedFlowCache = mutableMapOf<Any, MutableSharedFlow<SWRState<Any>>>()
    private val fetcherCache = mutableMapOf<Any, suspend (Any) -> Any>()
    private val revalidationTimeCache = mutableMapOf<Any, Instant>()
    private val configCache = mutableMapOf<Any, SWRConfig<Any, Any>>()
    private val isValidatingCache = mutableMapOf<Any, MutableStateFlow<Boolean>>()

    fun <K, D> initForKeyIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        fetcherCache.getOrPut(key as Any, { fetcher as suspend (Any) -> Any })
        sharedFlowCache.getOrPut(key as Any, {
            MutableSharedFlow<SWRState<D>>() as MutableSharedFlow<SWRState<Any>>
        })
        configCache.getOrPut(key as Any, { config as SWRConfig<Any, Any> })
    }

    fun <K, D> getMutableSharedFlow(key: K): MutableSharedFlow<SWRState<D>> {
        return sharedFlowCache[key as Any] as MutableSharedFlow<SWRState<D>>
    }

    fun <K, D> getFetcher(key: K): suspend (K) -> D {
        return fetcherCache[key as Any] as suspend (K) -> D
    }

    fun <K, D> getConfig(key: K): SWRConfig<K, D> {
        return configCache[key as Any] as SWRConfig<K, D>
    }

    fun <K> getRevalidationTime(key: K): Instant {
        return revalidationTimeCache.getOrDefault(key as Any, Instant.DISTANT_PAST)
    }

    fun <K> isValidating(key: K) : Boolean {
        return isValidatingFlow(key).value
    }

    fun <K> isValidatingFlow(key: K) : StateFlow<Boolean> {
        return isValidatingCache.getOrPut(key as Any, { MutableStateFlow(false) })
    }

    fun <K> updateRevalidationTime(key: K, now: Instant) {
        revalidationTimeCache[key as Any] = now
    }

    suspend fun <K> updateIsValidating(key: K, isValidating: Boolean) {
        val stateFlow = isValidatingCache[key as Any]
        if (stateFlow != null) {
            stateFlow.emit(isValidating)
        } else {
            isValidatingCache[key] = MutableStateFlow(isValidating)
        }
    }
}