package dev.burnoo.compose.swr.retry

import dev.burnoo.compose.swr.domain.random
import dev.burnoo.compose.swr.retry.exponentialBackoff
import kotlinx.coroutines.delay
import kotlin.math.floor

internal fun exponentialBackoff(
    errorRetryInterval: Long,
    attempt: Long
) = floor((random.nextDouble() + 0.5) * 1.shl(attempt.toInt())).toLong() * errorRetryInterval

internal fun <K, D> onRetryDefault(): SWROnRetry<K, D> = { _, _, config, attempt ->
    if (config.shouldRetryOnError && config.errorRetryCount.let { it == null || attempt <= it }) {
        delay(exponentialBackoff(config.errorRetryInterval, attempt))
        true
    } else {
        false
    }
}
