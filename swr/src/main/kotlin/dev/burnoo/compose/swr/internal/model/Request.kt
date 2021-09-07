package dev.burnoo.compose.swr.internal.model

import dev.burnoo.compose.swr.config.SWRConfig

internal data class Request<K, D>(
    val key: K,
    val config: SWRConfig<K, D>
)