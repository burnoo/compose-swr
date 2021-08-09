package dev.burnoo.compose.swr

import kotlinx.coroutines.*

internal class Refresher(
    private val cache: Cache,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val refresherMap = mutableMapOf<Any, Job>()

    fun <K> handleRefreshing(key: K, newRefreshInterval: Long) {
        val revalidator = Revalidator(cache, key)
        val stateFlow = cache.getOrCreateStateFlow<K, Any>(key)
        val config = cache.getConfig<K, Any>(key)

        if (newRefreshInterval <= 0 && config.refreshInterval <= 0) return
        val refreshInterval = when {
            config.refreshInterval <= 0 -> newRefreshInterval
            newRefreshInterval <= 0 -> config.refreshInterval
            else -> minOf(newRefreshInterval, config.refreshInterval)
        }
        cache.updateConfig<K, Any>(key, { this.refreshInterval = refreshInterval })
        if (refresherMap[key as Any] != null) return
        val refreshJob = scope.launch {
            while (true) {
                delay(config.refreshInterval)
                if (stateFlow.subscriptionCount.value > 0) {
                    revalidator.revalidate()
                }
            }
        }
        refresherMap[key] = refreshJob
    }
}