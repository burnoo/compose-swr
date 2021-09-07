package dev.burnoo.compose.swr.cache

internal class DefaultCache : SWRCache() {
    override fun <K, V> provideMutableMap() = mutableMapOf<K, V>()
}