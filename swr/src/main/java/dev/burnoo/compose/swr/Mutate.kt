package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR

suspend fun <K> mutate(key: K) {
    val swr = get<SWR>()
    swr.mutate(key)
}