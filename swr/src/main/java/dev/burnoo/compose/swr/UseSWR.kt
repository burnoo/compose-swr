package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    configBlock: SWRConfig<D>.() -> Unit = {}
): State<SWRResult<D>> {
    val swr = SWR(
        cache = get(),
        key = key,
        fetcher = fetcher,
        config = SWRConfig<D>().apply(configBlock)
    )
    return produceState(swr.getInitialResult(), key, swr::producer)
}
