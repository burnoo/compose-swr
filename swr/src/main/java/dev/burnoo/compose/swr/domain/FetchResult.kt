package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRResult

internal suspend fun <K, D> fetchResult(key : K, fetcher: suspend (K) -> D) = try {
    SWRResult.fromData(key, fetcher(key))
} catch (e: Exception) {
    SWRResult.fromError(key, e)
}