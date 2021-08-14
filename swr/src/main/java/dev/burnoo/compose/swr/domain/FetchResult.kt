package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRState

internal suspend fun <K, D> fetchState(key : K, fetcher: suspend (K) -> D) = try {
    SWRState.fromData(key, fetcher(key))
} catch (e: Exception) {
    SWRState.fromError(key, e)
}