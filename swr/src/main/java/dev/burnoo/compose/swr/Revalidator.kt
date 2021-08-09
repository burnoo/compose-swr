package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.SWRResult.Error
import dev.burnoo.compose.swr.SWRResult.Success
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal class Revalidator<K>(
    private val cache: Cache,
    private val key: K,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val stateFlow = cache.getOrCreateStateFlow<K, Any>(key)
    private val fetchUsage = cache.getFetchUsage<K, Any>(key)
    private val config = cache.getConfig<K, Any>(key)

    suspend fun revalidate(forced: Boolean = false) {
        if (forced || shouldRevalidate()) {
            stateFlow.value = withErrorRetrying {
                cache.updateUsageTime(key)
                withSlowLoadingTimeout {
                    fetchResult { fetchUsage.fetcher(key) }
                }.also(::handleCallbacks)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun shouldRevalidate(): Boolean {
        val lastUsageAgo = Clock.System.now() - fetchUsage.usageTimeInstant
        val dedupingDuration = Duration.milliseconds(config.dedupingInterval)
        return lastUsageAgo > dedupingDuration
    }

    private suspend fun withErrorRetrying(getResult: suspend () -> SWRResult<Any>): SWRResult<Any> {
        suspend fun rec(errorRetryCount: Int = 0): SWRResult<Any> {
            val result = getResult()
            return when {
                result is Error && config.shouldRetryOnError && errorRetryCount < config.errorRetryCount -> {
                    delay(config.errorRetryInterval)
                    rec(errorRetryCount + 1)
                }
                else -> result
            }
        }
        return rec(errorRetryCount = 0)
    }

    private suspend fun withSlowLoadingTimeout(getResult: suspend () -> SWRResult<Any>): SWRResult<Any> {
        val loadingTimeoutJob = scope.launch {
            delay(config.loadingTimeout)
            config.onLoadingSlow?.invoke(key, config)
        }
        return getResult().also { loadingTimeoutJob.cancel() }
    }

    private fun handleCallbacks(result: SWRResult<Any>) {
        when (result) {
            is Success -> config.onSuccess?.invoke(result.data, key, config)
            is Error -> config.onError?.invoke(result.exception, key, config)
            else -> Unit
        }
    }

    private suspend fun <D> fetchResult(fetch: suspend () -> D) = try {
        Success(fetch())
    } catch (e: Exception) {
        Error(e)
    }
}