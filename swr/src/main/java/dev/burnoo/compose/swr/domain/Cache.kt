package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.internal.InternalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

@Suppress("UNCHECKED_CAST")
internal class Cache {
    private val stateFlowCache = mutableMapOf<Any, StateFlow<InternalState<Any, Any>>>()
    private val fetcherCache = mutableMapOf<Any, suspend (Any) -> Any>()
    private val configCache = mutableMapOf<Any, SWRConfig<Any, Any>>()

    fun <K, D> initForKeyIfNeeded(key: K, fetcher: suspend (K) -> D, config: SWRConfig<K, D>) {
        fetcherCache.getOrPut(key as Any, { fetcher as suspend (Any) -> Any })
        configCache.getOrPut(key as Any, { config as SWRConfig<Any, Any> })
        stateFlowCache.getOrPut(key as Any, {
            MutableStateFlow(
                value = InternalState(
                    key = key,
                    data = null,
                    error = null,
                    isValidating = false,
                    revalidationTime = Instant.DISTANT_PAST,
                    mutationTime = Instant.DISTANT_PAST
                )
            )
        })
    }

    fun <K, D> getStateFlow(key: K): MutableStateFlow<InternalState<K, D>> {
        return stateFlowCache[key as Any] as MutableStateFlow<InternalState<K, D>>
    }

    fun <K, D> getFetcher(key: K): suspend (K) -> D {
        return fetcherCache[key as Any] as suspend (K) -> D
    }

    fun <K, D> getConfig(key: K): SWRConfig<K, D> {
        return configCache[key as Any] as SWRConfig<K, D>
    }
}