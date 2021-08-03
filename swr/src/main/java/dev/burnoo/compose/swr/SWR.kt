package dev.burnoo.compose.swr

internal class SWR {

    private val cacheMap = mutableMapOf<Any, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <V> getFromCache(key: Any): V? {
        return cacheMap[key] as? V
    }

    fun <K : Any, V> saveToCache(key: K, value: V) {
        cacheMap[key] = value as Any
    }
}