package dev.burnoo.compose.swr

internal class SWR {

    private val cacheMap = mapOf<Any, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <V> getFromCache(key: Any): V? {
        return cacheMap[key] as? V
    }
}