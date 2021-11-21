package dev.burnoo.compose.swr.cache

import dev.burnoo.compose.swr.internal.model.InternalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Suppress("UNCHECKED_CAST")
abstract class SWRCache {
    private val stateFlowCache by lazy { provideMutableMap<Any, MutableStateFlow<InternalState<Any, Any>>>() }

    protected abstract fun <K, V> provideMutableMap(): MutableMap<K, V>

    fun <K, D> get(key: K): D? {
        val stateFlow = stateFlowCache[key as Any] as? StateFlow<InternalState<K, D>> ?: return null
        return stateFlow.value.data
    }

    fun keys(): Set<Any> {
        return stateFlowCache.keys
    }

    fun clear() {
        stateFlowCache.forEach { (key, stateFlow) ->
            stateFlow.value = InternalState.initial(key)
        }
    }

    internal fun <K, D> initForKeyIfNeeded(key: K) {
        stateFlowCache.getOrPut(key as Any, {
            MutableStateFlow(InternalState.initial(key))
        })
    }

    internal fun <K, D> getStateFlow(key: K): MutableStateFlow<InternalState<K, D>> {
        return stateFlowCache[key as Any] as MutableStateFlow<InternalState<K, D>>
    }
}