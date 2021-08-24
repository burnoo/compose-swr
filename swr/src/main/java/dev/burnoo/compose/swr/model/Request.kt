package dev.burnoo.compose.swr.model

internal data class Request<K, D>(
    val key: K,
    val fetcher: suspend (K) -> D,
    val config: SWRConfig<K, D>
)