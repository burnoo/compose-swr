package dev.burnoo.compose.swr

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant

internal data class FetchUsage<K, D>(
    val fetcher: suspend (K) -> D,
    val usageTimeInstant: Instant
)

@Suppress("UNCHECKED_CAST")
internal class Cache(val now: Now) {
    private val stateFlowCache = mutableMapOf<Any, MutableStateFlow<SWRResult<Any>>>()
    private val fetcherCache = mutableMapOf<Any, suspend (Any) -> Any>()
    private val usageTimeInstantCache = mutableMapOf<Any, Instant>()
    private val configCache = mutableMapOf<Any, SWRConfig<Any, Any>>()

    fun <K, D> initForKeyIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        fetcherCache.getOrPut(key as Any, { fetcher as suspend (Any) -> Any })
        configCache.getOrPut(key as Any, { config as SWRConfig<Any, Any> })
    }

    fun <K, D> getOrCreateStateFlow(key: K): MutableStateFlow<SWRResult<D>> {
        val cachedStateFlow = stateFlowCache[key as Any] as? MutableStateFlow<SWRResult<D>>
        if (cachedStateFlow == null) {
            val initialResult = getInitialResult<D, K>(key)
            val newStateFlow = MutableStateFlow(initialResult)
            stateFlowCache[key] = newStateFlow as MutableStateFlow<SWRResult<Any>>
            return newStateFlow
        }
        return cachedStateFlow
    }

    private fun <D, K> getInitialResult(key: K): SWRResult<D> {
        val config = configCache[key as Any] as SWRConfig<K, D>
        return config.initialData?.let<D, SWRResult.Success<D>> {
            SWRResult.Success(it)
        } ?: SWRResult.Loading
    }

    fun <K, D> getFetchUsage(key: K): FetchUsage<K, D> {
        return FetchUsage(
            fetcher = fetcherCache[key as Any] as suspend (K) -> D,
            usageTimeInstant = usageTimeInstantCache.getOrDefault(key as Any, Instant.DISTANT_PAST)
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, D> getConfig(key: K): SWRConfig<K, D> {
        return configCache[key as Any] as SWRConfig<K, D>
    }

    fun <K, D> updateConfig(key: K, config: SWRConfig<K, D>.() -> Unit) {
        configCache[key as Any] = getConfig<K, D>(key).apply(config) as SWRConfig<Any, Any>
    }

    fun <K> updateUsageTime(key: K) {
        usageTimeInstantCache[key as Any] = now()
    }
}