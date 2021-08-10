package dev.burnoo.compose.swr

import org.koin.core.qualifier.qualifier

fun <K> mutate(key: K) {
    Revalidator(
        cache = get(),
        now = get(),
        key = key,
        scope = get(qualifier(ScopeQualifiers.Revalidator))
    ).revalidate(forced = true)
}