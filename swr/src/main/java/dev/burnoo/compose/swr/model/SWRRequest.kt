package dev.burnoo.compose.swr.model

internal data class SWRRequest<K, D>(
    val key: K,
    val fetcher: suspend (K) -> D,
    val config: SWRConfig<K, D>
)