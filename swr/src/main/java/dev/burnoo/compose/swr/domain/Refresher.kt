package dev.burnoo.compose.swr.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class Refresher(
    private val cache: Cache,
    private val now: Now,
    private val scope: CoroutineScope
) {
    private val refresherMap = mutableMapOf<Any, Job>()

    fun handleRefreshing(key: Any, newRefreshInterval: Long) {
        val revalidator = Revalidator(cache, now, key, scope)
        val stateFlow = cache.getOrCreateStateFlow<Any, Any>(key)
        val config = cache.getConfig<Any, Any>(key)

        if (newRefreshInterval <= 0 && config.refreshInterval <= 0) return
        val refreshInterval = when {
            config.refreshInterval <= 0 -> newRefreshInterval
            newRefreshInterval <= 0 -> config.refreshInterval
            else -> minOf(newRefreshInterval, config.refreshInterval)
        }
        cache.updateConfig<Any, Any>(key, { this.refreshInterval = refreshInterval })
        if (refresherMap[key] != null) return
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