package dev.burnoo.compose.swr.domain

import dev.burnoo.compose.swr.model.SWRResult

internal suspend fun <D> fetchResult(fetch: suspend () -> D) = try {
    SWRResult.Success(fetch())
} catch (e: Exception) {
    SWRResult.Error(e)
}