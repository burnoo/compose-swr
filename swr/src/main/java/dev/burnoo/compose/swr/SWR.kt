package dev.burnoo.compose.swr

import androidx.compose.runtime.ProduceStateScope
import dev.burnoo.compose.swr.SWRResult.Error
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
            }.also { result ->
                handleCallbacks(result)
                value = result
            }
        }
    }

    private suspend fun withRetrying(getResult: suspend () -> SWRResult<D>) {
        var retryCount = 0
        while (config.errorRetryCount <= 0 || retryCount <= config.errorRetryCount) {
            val result = getResult()
            when {
                result is Success && config.refreshInterval <= 0L -> break
                result is Success -> delay(config.refreshInterval)
                result is Error && config.shouldRetryOnError ->
                    delay(config.errorRetryInterval)
                else -> break
            }
            retryCount++
        }
    }

    private suspend fun withSlowLoadingTimeout(getResult: suspend () -> SWRResult<D>): SWRResult<D> {
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
        Error(e)
    }

    private fun handleCallbacks(result: SWRResult<D>) {
        when (result) {
            is Success -> config.onSuccess?.invoke(result.data, key, config)
            is Error -> config.onError?.invoke(result.exception, key, config)
            else -> Unit
        }
    }
}