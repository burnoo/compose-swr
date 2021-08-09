package dev.burnoo.compose.swr

suspend fun <K> mutate(key: K) {
    Revalidator(
        cache = get(),
        key = key
    ).revalidate(forced = true)
}