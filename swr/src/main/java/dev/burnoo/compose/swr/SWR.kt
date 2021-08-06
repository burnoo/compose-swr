package dev.burnoo.compose.swr

import androidx.compose.runtime.ProduceStateScope
import dev.burnoo.compose.swr.SWRResult.Loading
import dev.burnoo.compose.swr.SWRResult.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SWR<K, D>(
    private val cache: Cache,
    private val key: K,
    private val fetcher: suspend (K) -> D,
    private val config: SWRConfig<K, D>
) {
    private val scope = CoroutineScope(Dispatchers.Default)

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
            withSlowLoadingTimeout {
                fetchAndCacheResult()
            }.also { value = it }
        }
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

    private suspend fun <T> withSlowLoadingTimeout(getResult: suspend () -> SWRResult<T>): SWRResult<T> {
        val loadingTimeoutJob = scope.launch {
            delay(config.loadingTimeout)
            config.onLoadingSlow?.invoke(key, config)
        }
        return getResult().also { loadingTimeoutJob.cancel() }
    }

    private suspend fun fetchAndCacheResult() = try {
        val data = fetcher(key)
        cache[key] = data
        Success(data)
    } catch (e: Exception) {
        SWRResult.Error(e)
    }
}