package dev.burnoo.compose.swr.model

import kotlinx.datetime.Instant

internal data class FetchUsage<K, D>(
    val fetcher: suspend (K) -> D,
    val usageTimeInstant: Instant
)