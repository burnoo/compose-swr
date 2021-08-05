package dev.burnoo.compose.swr

import androidx.compose.runtime.ProduceStateScope
import dev.burnoo.compose.swr.SWRResult.Loading
import dev.burnoo.compose.swr.SWRResult.Success
import kotlinx.coroutines.delay

internal class SWR<K, D>(
    private val cache: Cache,
    private val key: K,
    private val fetcher: suspend (K) -> D,
    private val config: SWRConfig<D>
) {
    fun getInitialResult(): SWRResult<D> {
        val cachedData: D? = cache[key]
        val initialData = config.initialData
        return when {
            cachedData != null -> Success(cachedData)
            initialData != null -> Success(initialData)
            else -> Loading
        }
    }

    suspend fun producer(scope: ProduceStateScope<SWRResult<D>>) = scope.run {
        withRetrying {
            fetchAndCacheResult().also { value = it }
        }
    }

    private suspend fun fetchAndCacheResult() = try {
        val data = fetcher(key)
        cache[key] = data
        Success(data)
    } catch (e: Exception) {
        SWRResult.Error(e)
    }

    private suspend fun <T> withRetrying(getResult: suspend () -> SWRResult<T?>) {
        while (true) {
            val result = getResult()
            when {
                result is Success && config.refreshInterval <= 0L -> break
                result is Success -> delay(config.refreshInterval)
                result is SWRResult.Error && config.shouldRetryOnError ->
                    delay(config.errorRetryInterval)
                else -> break
            }
        }
    }
}