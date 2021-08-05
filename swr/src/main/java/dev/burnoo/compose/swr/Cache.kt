package dev.burnoo.compose.swr

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