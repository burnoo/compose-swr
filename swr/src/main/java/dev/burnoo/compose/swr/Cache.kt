package dev.burnoo.compose.swr

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant

internal class Cache {
    private val cacheMap = mutableMapOf<Any, Any>()

    @Suppress("UNCHECKED_CAST")
    operator fun <K, D> get(key: K): D? {
        return cacheMap[key as Any] as? D
    }

    operator fun <K, D> set(key: K, data: D) {
        cacheMap[key as Any] = data as Any
    }
}

internal data class FetchUsage<K, D>(
    val fetcher: suspend (K) -> D,
    val usageTimeInstant: Instant
)

internal class ReactiveCache {
    private val stateFlowCache = mutableMapOf<Any, MutableStateFlow<SWRResult<Any>>>()
    private val fetcherCache = mutableMapOf<Any, suspend (Any) -> Any>()
    private val usageTimeInstantCache = mutableMapOf<Any, Instant>()
    private val configCache = mutableMapOf<Any, SWRConfig<Any, Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <K, D> getOrCreateStateFlow(key: K): MutableStateFlow<SWRResult<D>> {
        val cachedStateFlow = stateFlowCache[key as Any] as? MutableStateFlow<SWRResult<D>>
        if (cachedStateFlow == null) {
            val newStateFlow = MutableStateFlow<SWRResult<D>>(SWRResult.Loading)
            stateFlowCache[key] = newStateFlow as MutableStateFlow<SWRResult<Any>>
            return newStateFlow
        }
        return  cachedStateFlow
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, D> initIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        fetcherCache.getOrPut(key as Any, { fetcher as suspend (Any) -> Any })
        configCache.getOrPut(key as Any, { config as SWRConfig<Any, Any> })
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, D> getFetchUsage(key: K) : FetchUsage<K, D> {
        return FetchUsage(
            fetcher = fetcherCache[key as Any] as suspend (K) -> D,
            usageTimeInstant = usageTimeInstantCache.getOrDefault(key as Any, Instant.DISTANT_PAST)
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, D> getConfig(key: K) : SWRConfig<K, D> {
        return configCache[key as Any] as SWRConfig<K, D>
    }
}