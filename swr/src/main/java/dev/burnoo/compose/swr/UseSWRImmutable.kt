package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import dev.burnoo.compose.swr.model.config.plus

@Composable
inline fun <reified K, reified D> useSWRImmutable(
    key: K,
    noinline fetcher: suspend (K) -> D,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWRImmutable(key, config + { this.fetcher = fetcher })
}

@Composable
inline fun <reified K, reified D> useSWRImmutable(
    key: K,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    val immutableConfig: SWRConfigBlock<K, D> = {
        revalidateIfStale = false
    }
    return useSWR(key, immutableConfig + config)
}