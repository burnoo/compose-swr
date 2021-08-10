package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfig<K, D>.() -> Unit = {}
): State<SWRResult<D>> {
    val now = get<Now>()
    val cache = get<Cache>()
    val swrConfig = SWRConfig(block = config)
    cache.initForKeyIfNeeded(key, fetcher, swrConfig)
    LaunchedEffect(key) {
        val scope = get<RecomposeCoroutineScope>().scope ?: this
        Revalidator(cache, now, key, scope).revalidate()
        get<Refresher>().handleRefreshing(key, swrConfig.refreshInterval)
    }
    return cache.getOrCreateStateFlow<K, D>(key).collectAsState()
}