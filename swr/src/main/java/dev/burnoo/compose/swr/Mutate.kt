package dev.burnoo.compose.swr

fun <K> mutate(key: K) {
    Revalidator(
        cache = get(),
        now = get(),
        key = key,
        scope = get()
    ).revalidate(forced = true)
}