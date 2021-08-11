package dev.burnoo.compose.swr

import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.Revalidator

fun <K> mutate(key: K) {
    Revalidator(
        cache = get(),
        now = get(),
        key = key as Any,
        scope = get()
    ).revalidate(forced = true)
}