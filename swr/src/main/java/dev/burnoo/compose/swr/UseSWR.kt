package dev.burnoo.compose.swr

import androidx.compose.runtime.*

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfig<K, D>.() -> Unit = {}
): State<SWRResult<D>> {
    val swr = SWR(
        cache = get(),
        key = key,
        fetcher = fetcher,
        config = SWRConfig<K, D>().apply(config)
    )
    return produceState(swr.getInitialResult(), key, swr::producer)
}

@Composable
fun <K, D> reactiveUseSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfig<K, D>.() -> Unit = {}
): State<SWRResult<D>> {
    val reactiveCache = get<ReactiveCache>()
    reactiveCache.initIfNeeded(key, fetcher, SWRConfig<K, D>().apply(config))
    LaunchedEffect(key) {
        Revalidator(reactiveCache, key).revalidate()
    }
    return reactiveCache.getOrCreateStateFlow<K, D>(key).collectAsState()
}

suspend fun <K> mutate(key: K) {
    Revalidator(
        reactiveCache = get(),
        key = key
    ).revalidate()
}