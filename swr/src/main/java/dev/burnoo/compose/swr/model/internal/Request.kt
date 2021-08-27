package dev.burnoo.compose.swr.model.internal

import dev.burnoo.compose.swr.model.SWRConfig

internal data class Request<K, D>(
    val key: K,
    val fetcher: suspend (K) -> D,
    val config: SWRConfig<K, D>
)