package dev.burnoo.compose.swr

import androidx.compose.runtime.*

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfig<K, D>.() -> Unit = {}
): State<SWRResult<D>> {
    val scope = rememberCoroutineScope()
    val cache = get<Cache>()
    val swrConfig = SWRConfig<K, D>().apply(config)
    cache.initForKeyIfNeeded(key, fetcher, swrConfig)
    LaunchedEffect(key) {
        Revalidator(cache, key, scope).revalidate()
        get<Refresher>().handleRefreshing(key, swrConfig.refreshInterval)
    }
    return cache.getOrCreateStateFlow<K, D>(key).collectAsState()
}