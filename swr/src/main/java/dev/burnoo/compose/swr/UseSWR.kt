package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState

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
