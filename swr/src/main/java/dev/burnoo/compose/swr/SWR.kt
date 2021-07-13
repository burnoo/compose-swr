package dev.burnoo.compose.swr

import androidx.lifecycle.ViewModel

internal class SWR : ViewModel() {

    private val cacheMap = mapOf<Any, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <V> getFromCache(key: Any): V? {
        return cacheMap[key] as? V
    }
}