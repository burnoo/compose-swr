package dev.burnoo.compose.swr.model.internal

import dev.burnoo.compose.swr.model.config.SWRConfig

internal data class Request<K, D>(
    val key: K,
    val config: SWRConfig<K, D>
)