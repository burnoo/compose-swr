package dev.burnoo.compose.swr.retry

import dev.burnoo.compose.swr.config.SWRConfig

typealias SWROnRetry<K, D> = suspend (
    error: Throwable, key: K, config: SWRConfig<K, D>, attempt: Int
) -> Boolean