package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR

suspend fun <K> mutate(key: K, data: Any? = null, shouldRevalidate: Boolean = true) {
    val swr = get<SWR>()
    swr.mutate(key, data, shouldRevalidate)
}