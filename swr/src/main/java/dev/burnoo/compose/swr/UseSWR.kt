package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.domain.getConfigBlockComposition
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.model.plus
import kotlinx.coroutines.flow.launchIn

@Composable
inline fun <reified K, reified D> useSWR(
    key: K,
    noinline fetcher: suspend (K) -> D,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWR(key, config + { this.fetcher = fetcher })
}

@Composable
inline fun <reified K, reified D> useSWR(
    key: K,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    val localConfigBlock = getConfigBlockComposition<K, D>().current
    val configBlock = localConfigBlock + config
    val swrConfig = SWRConfig(configBlock)
    return useSWRInternal(key, swrConfig)
}

@PublishedApi
@Composable
internal fun <K, D> useSWRInternal(
    key: K,
    config: SWRConfig<K, D>
): SWRState<D> {
    val cache = LocalCache.current
    val swr = SWR(key, config, cache)
    LaunchedEffect(key) {
        swr.getLocalFlow().launchIn(config.scope ?: this)
    }
    return SWRState(swr.getGlobalFlow(), config, cache)
}

